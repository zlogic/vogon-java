var app = angular.module("vogon", ["ngCookies", "ui.bootstrap"]);

app.service("AlertService", function ($timeout) {
	var that = this;
	this.alerts = [];
	this.closeAlert = function (alertIndex) {
		that.alerts.splice(alertIndex, 1);
	};
	this.enabled = function () {
		return true;
	};
	this.addAlert = function (message) {
		if (!that.enabled())
			return;
		var alert = {msg: message, type: "danger"};
		that.alerts.push(alert);
		$timeout(function () {
			var alertIndex = that.alerts.indexOf(alert);
			if (alertIndex !== -1)
				that.closeAlert(alertIndex);
		}, 30000);
	};
});

app.service("HTTPService", function ($http, $q, AlertService) {
	var that = this;
	this.pendingRequests = 0;
	this.isLoading = false;
	this.authorizationHeaders = {};
	var mergeHeaders = function (extraHeaders) {
		var headers = {};
		if (extraHeaders !== undefined)
			merge(headers, extraHeaders);
		merge(headers, that.authorizationHeaders);
		return headers;
	};
	var merge = function (a, b) {
		for (var prop in b)
			a[prop] = b[prop];
	};
	var startRequest = function () {
		that.pendingRequests++;
		that.isLoading = true;
	};
	var endRequest = function () {
		that.pendingRequests--;
		that.isLoading = that.pendingRequests > 0;
	};
	var retryRequest = function (config) {
		config.headers = mergeHeaders(config.headers);
		return $http(config);
	};
	var errorHandler = function (data) {
		endRequest();
		var deferred = $q.defer();
		if (data.status === 401) {
			var fixAuthCall = that.fixAuthorization();
			if (fixAuthCall !== undefined) {
				fixAuthCall
						.then(function () {
							retryRequest(data.config).then(deferred.resolve, deferred.reject), deferred.reject;
						});
				return deferred.promise;
			}
			//TODO: after fixing, update accounts and transactions
		} else {
			AlertService.addAlert("HTTP error: " + data.status + "(" + angular.toJson(data.data) + ")");
			//TODO: update accounts and transactions
		}
		deferred.reject(data);
		return deferred.promise;
	};
	var successHandler = function (data) {
		endRequest();
		return data;
	};
	this.get = function (url, extraHeaders) {
		startRequest();
		var headers = mergeHeaders(extraHeaders);
		return $http.get(url, {headers: headers}).then(successHandler, errorHandler);
	};
	this.post = function (url, data, extraHeaders) {
		startRequest();
		var headers = mergeHeaders(extraHeaders);
		return $http.post(url, data, {headers: headers}).then(successHandler, errorHandler);
	};
	this.fixAuthorization = function () {
		throw "fixAuthorization not properly initialized";
	};
	this.setAccessToken = function (access_token) {
		if (access_token !== undefined)
			that.authorizationHeaders = {Authorization: "Bearer " + access_token};
		else
			that.authorizationHeaders = {};
	};
});

app.service("AuthorizationService", function ($q, AlertService, HTTPService) {
	var that = this;
	var clientId = "vogonweb";
	var postHeaders = {"Content-Type": "application/x-www-form-urlencoded"};
	this.authorized = false;
	this.access_token = undefined;
	this.username = undefined;
	this.password = undefined;
	var encodeForm = function (data) {
		var buffer = [];
		for (var name in data)
			buffer.push([encodeURIComponent(name), encodeURIComponent(data[name])].join("="));
		return buffer.join("&");
	};
	var setToken = function (access_token, username, password) {
		if (access_token !== undefined) {
			that.access_token = access_token;
			localStorage.setItem("access_token", access_token);
			HTTPService.setAccessToken(access_token);
			that.authorized = true;
			if (username !== undefined)
				that.username = username;
			if (password !== undefined)
				that.password = password;
		}
	};
	this.performAuthorization = function (username, password) {
		var params = {username: username, password: password, client_id: clientId, grant_type: "password"};
		return HTTPService.post("oauth/token", encodeForm(params), postHeaders)
				.then(function (data) {
					data = data.data;
					setToken(data.access_token, username, password);
					return data;
				}, function (data) {
					that.resetAuthorization("Unable to authenticate");
					var deferred = $q.defer();
					deferred.reject(data);
					return deferred.promise;
				});
	};
	this.fixAuthorization = function () {
		if (that.username !== undefined && that.password !== undefined) {
			var username = that.username;
			var password = that.password;
			that.resetAuthorization();
			return that.performAuthorization(username, password);
		} else {
			var message;
			if (that.authorized)
				if (that.username !== undefined && that.password !== undefined)
					message = "Username/password not accepted";
				else if (that.access_token !== undefined)
					message = "Access token rejected";
			that.resetAuthorization(that.authorized ? "Can't fix authorization" : undefined);
		}
	};
	this.resetAuthorization = function (message) {
		that.username = undefined;
		that.password = undefined;
		that.access_token = undefined;
		HTTPService.setAccessToken();
		that.authorized = false;
		localStorage.removeItem("access_token");
		if (message !== undefined)
			AlertService.addAlert(message);
	};
	HTTPService.fixAuthorization = this.fixAuthorization;
	AlertService.enabled = function () {
		return that.authorized;
	};

	this.access_token = localStorage["access_token"];
	if (this.access_token !== undefined) {
		HTTPService.setAccessToken(this.access_token);
		HTTPService.get("service/user")
				.then(function (data) {
					that.username = data.data.username;
					that.authorized = true;
				}, function () {
					that.authorized = false;
				});
	} else
		that.authorized = false;
});

app.controller("NotificationController", function ($scope, HTTPService, AlertService) {
	$scope.httpService = HTTPService;
	$scope.alertService = AlertService;
	$scope.closeAlert = AlertService.closeAlert;
});

app.controller("LoginController", function ($scope, AuthorizationService, HTTPService) {
	$scope.authorizationService = AuthorizationService;
	$scope.httpService = HTTPService;
	$scope.loginLocked = "authorizationService.authorized || httpService.isLoading";
	$scope.failed = false;
	$scope.login = function () {
		AuthorizationService.performAuthorization(AuthorizationService.username, AuthorizationService.password)
				.catch(function () {
					$scope.failed = true;
				});
	};
});

app.controller("AuthController", function ($scope, $modal, AuthorizationService, HTTPService) {
	$scope.authorizationService = AuthorizationService;
	$scope.httpService = HTTPService;
	$scope.logoutLocked = "!authorizationService.authorized";
	$scope.loginDialog = undefined;
	$scope.logout = function () {
		$scope.authorizationService.resetAuthorization();
	};
	$scope.toggleLoginDialog = function () {
		if (!AuthorizationService.authorized && AuthorizationService.access_token === undefined && $scope.loginDialog === undefined) {
			$scope.loginDialog = $modal.open({
				templateUrl: "loginDialog",
				controller: "LoginController",
				backdrop: "static"
			});
		} else if (AuthorizationService.authorized && $scope.loginDialog !== undefined) {
			$scope.loginDialog.dismiss();
			delete $scope.loginDialog;
		}
	};
	$scope.$watch(function () {
		return AuthorizationService.authorized;
	}, $scope.toggleLoginDialog);
	$scope.$watch(function () {
		return AuthorizationService.access_token;
	}, $scope.toggleLoginDialog);
	$scope.toggleLoginDialog();
});

app.service("AccountsService", function ($rootScope, HTTPService, AuthorizationService) {
	var that = this;
	this.accounts = [];
	this.updateTransactions = function () {
		throw "updateTransactions not properly initialized";
	};
	this.update = function () {
		if (AuthorizationService.authorized) {
			HTTPService.get("service/accounts")
					.then(function (data) {
						that.accounts = data.data;
					}, that.update);
		} else {
			that.accounts = [];
		}
	};
	this.submitAccounts = function (accounts) {
		return HTTPService.post("service/accounts", accounts)
				.then(function (data) {
					that.accounts = data.data;
					that.updateTransactions();
				}, that.update);
	};
	this.getAccount = function (id) {
		return that.accounts.filter(function (obj) {
			return obj.id === id;
		})[0];
	};
	$rootScope.$watch(function () {
		return AuthorizationService.authorized;
	}, that.update());
});


app.service("CurrencyService", function ($rootScope, HTTPService, AuthorizationService) {
	var that = this;
	this.currencies = [];
	this.update = function () {
		if (AuthorizationService.authorized) {
			HTTPService.get("service/currencies")
					.then(function (data) {
						that.currencies = data.data;
					}, that.update);
		} else {
			that.currencies = [];
		}
	};
	$rootScope.$watch(function () {
		return AuthorizationService.authorized;
	}, that.update());
});

app.controller("AccountsEditorController", function ($scope, $modalInstance, AccountsService, CurrencyService) {
	$scope.accounts = AccountsService;
	$scope.currencies = CurrencyService;
	$scope.addAccount = function () {
		var account = {includeInTotal: true, showInList: true};
		AccountsService.accounts.unshift(account);
	};
	$scope.deleteAccount = function (account) {
		AccountsService.accounts = AccountsService.accounts.filter(function (comp) {
			return comp !== account;
		});
	};
	$scope.cancelEditing = function () {
		$modalInstance.dismiss();
	};
	$scope.submitEditing = function () {
		$modalInstance.close(AccountsService.accounts);
	};
});

app.controller("AccountsController", function ($scope, $modal, AuthorizationService, AccountsService) {
	$scope.authorizationService = AuthorizationService;
	$scope.accountService = AccountsService;
	$scope.editor = undefined;
	$scope.editAccounts = function () {
		closeEditor();
		$scope.editor = $modal.open({
			templateUrl: "editAccountsDialog",
			controller: "AccountsEditorController",
			size: "lg"
		}).result.then(function (accounts) {
			AccountsService.submitAccounts(accounts);
		}, function () {
			AccountsService.update();
		});
	};
	var closeEditor = function () {
		if ($scope.editor !== undefined) {
			var deleteFunction = function () {
				$scope.editor = undefined;
			};
			$scope.editor.then(deleteFunction, deleteFunction);
		}
	};
});

app.controller("TransactionEditorController", function ($scope, $modalInstance, AccountsService, TransactionsService, transaction) {
	$scope.transaction = transaction;
	$scope.accountService = AccountsService;
	$scope.transactionTypes = TransactionsService.transactionTypes;
	$scope.calendarOpened = false;
	$scope.tags = transaction.tags.join(",");
	$scope.openCalendar = function ($event) {
		$event.preventDefault();
		$event.stopPropagation();
		$scope.calendarOpened = true;
	};
	$scope.addTransactionComponent = function () {
		$scope.transaction.components.push({});
	};
	$scope.deleteTransactionComponent = function (component) {
		transaction.components = transaction.components.filter(function (comp) {
			return comp !== component;
		});
	};
	$scope.submitEditing = function () {
		TransactionsService.submitTransaction($scope.transaction);
		$modalInstance.close();
	};
	$scope.cancelEditing = function () {
		TransactionsService.updateTransaction(transaction.id).then(AccountsService.update);
		$modalInstance.dismiss();
	};
	$scope.deleteTransaction = function () {
		TransactionsService.deleteTransaction($scope.transaction);
		$modalInstance.close();
	};
	var tagsToJson = function (tags) {
		if (tags.constructor === String)
			return tags.split(",");
		else
			return tags;
	};
	$scope.syncTags = function () {
		$scope.transaction.tags = tagsToJson($scope.tags);
	};
});


app.service("TransactionsService", function (HTTPService, AuthorizationService, AccountsService) {
	var that = this;
	this.transactions = [];
	this.transactionTypes = [{name: "Expense/income", value: "EXPENSEINCOME"}, {name: "Transfer", value: "TRANSFER"}];
	this.defaultTransactionType = this.transactionTypes[0];
	this.currentPage = 1;
	this.totalPages = 0;
	var dateToJson = function (date) {
		if (date instanceof Date)
			return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate())).toJSON().split("T")[0];
		else
			return date;
	};
	var updateTransactions = function () {
		var nextPage = that.currentPage;
		return HTTPService.get("service/transactions/page_" + (nextPage - 1))
				.then(function (data) {
					that.transactions = data.data;
					that.currentPage = nextPage;
					AccountsService.update();
				}, that.update);
	};
	var updateTransactionsCount = function () {
		return HTTPService.get("service/transactions/pages")
				.then(function (data) {
					that.totalPages = data.data;
				}, that.update);
	};
	this.update = function () {
		if (AuthorizationService.authorized) {
			updateTransactions();
			updateTransactionsCount();
		} else {
			that.transactions = [];
			that.currentPage = 1;
			that.totalPages = 0;
		}
	};
	var updateTransactionLocal = function (data) {
		var found = false;
		that.transactions.forEach(
				function (transaction, i) {
					if (transaction.id === data.id) {
						that.transactions[i] = data;
						found = true;
					}
				});
		return found;
	};
	this.updateTransaction = function (id) {
		if (id === undefined)
			return updateTransactions();
		return HTTPService.get("service/transactions/" + id)
				.then(function (data) {
					if (updateTransactionLocal(data.data))
						AccountsService.update();
					else
						updateTransactions();
				}, that.update);
	};
	this.submitTransaction = function (transaction) {
		transaction.date = dateToJson(transaction.date);
		return HTTPService.post("service/transactions/submit", transaction)
				.then(function (data) {
					if (updateTransactionLocal(data.data))
						AccountsService.update();
					else
						updateTransactions();
				}, that.update);
	};
	this.deleteTransaction = function (transaction) {
		if (transaction === undefined || transaction.id === undefined)
			return updateTransactions();
		return HTTPService.get("service/transactions/delete/" + transaction.id)
				.then(function () {
					updateTransactions();
				}, that.update);
	};
	this.getDate = function () {
		return dateToJson(new Date());
	};
	AccountsService.updateTransactions = this.update;
});

app.controller("TransactionsController", function ($scope, $modal, TransactionsService, AuthorizationService, AccountsService) {
	$scope.transactionsService = TransactionsService;
	$scope.authorizationService = AuthorizationService;
	$scope.accountsService = AccountsService;
	$scope.editor = undefined;
	$scope.editingTransaction = undefined;
	var closeEditor = function () {
		$scope.editingTransaction = undefined;
		if ($scope.editor !== undefined) {
			var deleteFunction = function () {
				$scope.editor = undefined;
			};
			$scope.editor.then(deleteFunction, deleteFunction);
		}
	};
	$scope.addTransaction = function () {
		var transaction = {components: [], date: TransactionsService.getDate(), tags: [], type: TransactionsService.defaultTransactionType.value};
		$scope.transactionsService.transactions.unshift(transaction);
		$scope.startEditing(transaction);
	};
	$scope.startEditing = function (transaction) {
		closeEditor();
		$scope.editingTransaction = transaction;
		$scope.editor = $modal.open({
			templateUrl: "editTransactionDialog",
			controller: "TransactionEditorController",
			size: "lg",
			resolve: {
				transaction: function () {
					return $scope.editingTransaction;
				}
			}
		}).result.then(closeEditor, closeEditor);
	};
	$scope.duplicateTransaction = function (transaction) {
		var newTransaction = angular.copy(transaction);
		newTransaction.id = undefined;
		newTransaction.version = undefined;
		newTransaction.date = TransactionsService.getDate();
		newTransaction.components.forEach(function (component) {
			component.id = undefined;
			component.version = undefined;
		});
		$scope.transactionsService.transactions.unshift(newTransaction);
		$scope.startEditing(newTransaction);
	};
	$scope.$watch(function () {
		return AuthorizationService.authorized;
	}, TransactionsService.update);
	TransactionsService.update();
	$scope.pageChanged = TransactionsService.update;
});
