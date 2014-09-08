var app = angular.module('vogon', ['ngCookies', 'ui.bootstrap']);

app.service('AuthorizationService', function($http, $cookies, $interval) {
	var that = this;
	var authorized = false;
	var access_token = undefined;
	var refresh_token = undefined;
	var headers = {};
	var username = undefined;
	var password = undefined;
	var clientId = "vogonweb";
	var setToken = function(access_token, refresh_token, username, password) {
		if (access_token !== undefined && refresh_token !== undefined) {
			that.access_token = access_token;
			that.refresh_token = refresh_token;
			$cookies.refresh_token = refresh_token;
			that.headers = {Authorization: "Bearer " + access_token};
			that.authorized = true;
			if (username !== undefined) {
				that.username = username;
				$cookies.username = username;
			}
			if (password !== undefined)
				that.password = password;
		}
	};
	var refreshToken = function(refresh_token, username) {
		if (refresh_token === undefined || username === undefined)
			return;
		var params = {client_id: clientId, grant_type: "refresh_token", refresh_token: refresh_token};
		$http.get("oauth/token", {params: params}).success(function(data) {
			setToken(data.access_token, data.refresh_token, username);
		}).error(function() {
			that.refresh_token = undefined;
			if (that.username !== undefined && that.password !== undefined)
				that.performAuthorization(that.username, that.password);
			else
				that.resetAuthorization();
		});
	};
	this.performAuthorization = function(username, password) {
		var params = {username: username, password: password, client_id: clientId, grant_type: "password"};
		$http.get("oauth/token", {params: params}).success(function(data) {
			setToken(data.access_token, data.refresh_token, username, password);
		}).error(function() {
			if (that.refresh_token !== undefined) {
				that.refresh_token = undefined;
				that.performAuthorization(username, password);
			} else {
				that.resetAuthorization();
			}
		});
	};
	this.fixAuthorization = function() {
		if (that.refresh_token !== undefined && that.username !== undefined) {
			refreshToken(that.refresh_token, that.username);
		} else {
			that.resetAuthorization();
		}
	};
	this.resetAuthorization = function() {
		//TODO: show login failed error
		that.username = undefined;
		that.password = undefined;
		that.access_token = undefined;
		that.refresh_token = undefined;
		that.headers = {};
		that.authorized = false;
		delete $cookies.username;
		delete $cookies.refresh_token;
	};
	refreshToken($cookies.refresh_token, $cookies.username);
	$interval(function() {
		refreshToken(that.refresh_token, that.username);
	}, 60 * 1000);
});

app.controller('AuthController', function($scope, AuthorizationService) {
	$scope.authorization = AuthorizationService;
	$scope.username = "";
	$scope.password = "";
	$scope.login = function() {
		AuthorizationService.performAuthorization($scope.username, $scope.password);
		$scope.username = "";
		$scope.password = "";
	};
	$scope.logout = function() {
		AuthorizationService.resetAuthorization();
	};
});

app.service('AccountsService', function($http, $rootScope, AuthorizationService) {
	var that = this;
	var accounts = [];
	var processError = function(data, status) {
		if (status === 401) {
			AuthorizationService.fixAuthorization();
			//TODO: show error
		} else {
			//TODO: retry?
		}
	};
	this.update = function() {
		if (AuthorizationService.authorized) {
			$http.get("service/accounts", {headers: AuthorizationService.headers})
					.success(function(data) {
						that.accounts = data;
						//TODO: show loading popup just like transactions do
					}).error(processError);
		} else {
			that.accounts = [];
		}
	};
	this.getAccount = function(id) {
		return that.accounts.filter(function(obj) {
			return obj.id === id;
		})[0];
	};
	$rootScope.$watch(function() {
		return AuthorizationService.authorized;
	}, function() {
		that.update();
	});
});

app.controller('AccountsController', function($scope, AuthorizationService, AccountsService) {
	$scope.authorization = AuthorizationService;
	$scope.accountService = AccountsService;
});

app.controller('TransactionsController', function($scope, $http, AuthorizationService, AccountsService) {
	$scope.authorization = AuthorizationService;
	$scope.accounts = AccountsService;
	$scope.transactions = [];
	$scope.currentPage = 1;
	$scope.totalPages = 0;
	$scope.loading = 0;
	$scope.isLoading = false;
	$scope.transactionTypes = [{name: "Expense/income", value: "EXPENSEINCOME"}, {name: "Transfer", value: "TRANSFER"}]
	var updateIsLoading = function() {
		$scope.isLoading = $scope.loading > 0;
	};
	var processError = function(data, status) {
		$scope.loading--;
		updateIsLoading();
		if (status === 401) {
			AuthorizationService.fixAuthorization();
			//TODO: show error
		} else {
			updateTransactions();
			AccountsService.update();
		}
	};
	var updateTransactions = function() {
		$scope.loading++;
		updateIsLoading();
		var nextPage = $scope.currentPage;
		$http.get("service/transactions/page_" + (nextPage - 1), {headers: AuthorizationService.headers})
				.success(function(data) {
					$scope.transactions = data;
					$scope.loading--;
					updateIsLoading();
					$scope.currentPage = nextPage;
					AccountsService.update();
				}).error(processError);
	};
	var updateTransactionsCount = function() {
		$scope.loading++;
		updateIsLoading();
		$http.get("service/transactions/pages", {headers: AuthorizationService.headers})
				.success(function(data) {
					$scope.totalPages = data;
					$scope.loading--;
					updateIsLoading();
				}).error(processError);
	};
	var update = function() {
		if (AuthorizationService.authorized) {
			updateTransactions();
			updateTransactionsCount();
		} else {
			$scope.transactions = [];
		}
	};
	var updateTransactionLocal = function(data) {
		var found = false;
		$scope.transactions.forEach(
				function(transaction, i) {
					if (transaction.id === data.id) {
						$scope.transactions[i] = data;
						found = true;
						AccountsService.update();
					}
				});
		return found;
	};
	var updateTransaction = function(id) {
		if (id === undefined)
			return updateTransactions();
		$scope.loading++;
		updateIsLoading();
		$http.get("service/transactions/" + id, {headers: AuthorizationService.headers})
				.success(function(data) {
					$scope.loading--;
					updateIsLoading();
					if (!updateTransactionLocal(data))
						updateTransactions();
				}).error(processError);
	};
	var submitTransaction = function(transaction) {
		$scope.loading++;
		updateIsLoading();
		$http.post("service/transactions/submit", transaction, {headers: AuthorizationService.headers})
				.success(function(data) {
					$scope.loading--;
					updateIsLoading();
					if (!updateTransactionLocal(data))
						updateTransactions();
				}).error(
				function(data, status) {
					processError(data, status);
					update();
				});
	};
	var deleteTransaction = function(transaction) {
		if (transaction === undefined || transaction.id === undefined)
			return updateTransactions();
		$scope.loading++;
		updateIsLoading();
		$http.get("service/transactions/delete/" + transaction.id, {headers: AuthorizationService.headers})
				.success(function() {
					$scope.loading--;
					updateIsLoading();
					updateTransactions();
				}).error(
				function(data, status) {
					processError(data, status);
					update();
				});
	};
	$scope.addTransactionComponent = function(transaction) {
		transaction.components.push({});
	};
	$scope.deleteTransactionComponent = function(transaction, component) {
		transaction.components = transaction.components.filter(function(comp) {
			return comp !== component;
		});
	};
	$scope.addTransaction = function() {
		$scope.transactions.unshift({isEditing: true, components: [], date: new Date().toJSON().split("T")[0], tags: [], type: $scope.transactionTypes[0].value});
	};
	$scope.startEditing = function(transaction) {
		transaction.isEditing = true;
	};
	$scope.duplicateTransaction = function(transaction) {
		transaction.isEditing = false;
		var newTransaction = angular.copy(transaction);
		newTransaction.isEditing = true;
		newTransaction.id = undefined;
		newTransaction.components.forEach(function(component) {
			component.id = undefined;
		});
		$scope.transactions.unshift(newTransaction);
	};
	$scope.submitEditing = function(transaction) {
		transaction.isEditing = undefined;
		submitTransaction(transaction);
	};
	$scope.deleteTransaction = function(transaction) {
		transaction.isEditing = undefined;
		deleteTransaction(transaction);
	};
	$scope.cancelEditing = function(transaction) {
		updateTransaction(transaction.id);
		AccountsService.update();
	};
	$scope.$watch('authorization.authorized', function() {
		update();
	});
	update();
	$scope.pageChanged = function() {
		update();
	};
});
