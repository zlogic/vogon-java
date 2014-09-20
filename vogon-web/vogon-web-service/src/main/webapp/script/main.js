var app = angular.module("vogon", ["ngCookies", "ui.bootstrap"]);

app.service("AlertService", function ($timeout) {
	var that = this;
	this.alerts = [];
	this.closeAlert = function (alertIndex) {
		that.alerts.splice(alertIndex, 1);
	};
	this.addAlert = function (message) {
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
		if (data.status === 401) {
			var fixAuthCall = that.fixAuthorization();
			if (fixAuthCall !== undefined) {
				var deferred = $q.defer();
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
		return data;
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

app.service("AuthorizationService", function (AlertService, HTTPService) {
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
				}, function () {
					that.resetAuthorization("Unable to authenticate");
				});
	};
	this.fixAuthorization = function () {
		if (that.username !== undefined && that.password !== undefined) {
			var username = that.username;
			var password = that.password;
			that.resetAuthorization();
			return that.performAuthorization(username, password);
		} else {
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
	}
});

app.controller("NotificationController", function ($scope, HTTPService, AlertService) {
	$scope.httpService = HTTPService;
	$scope.alertService = AlertService;
	$scope.closeAlert = AlertService.closeAlert;
});

app.controller("AuthController", function ($scope, AuthorizationService, HTTPService) {
	$scope.authorization = AuthorizationService;
	$scope.httpService = HTTPService;
	$scope.username = "";
	$scope.password = "";
	$scope.loginLocked = "authorization.authorized || httpService.isLoading";
	$scope.logoutLocked = "!authorization.authorized";
	$scope.login = function () {
		AuthorizationService.performAuthorization($scope.username, $scope.password);
	};
	$scope.logout = function () {
		AuthorizationService.resetAuthorization();
		$scope.username = "";
		$scope.password = "";
	};
});

app.service("AccountsService", function ($rootScope, HTTPService, AuthorizationService) {
	var that = this;
	this.accounts = [];
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
	this.getAccount = function (id) {
		return that.accounts.filter(function (obj) {
			return obj.id === id;
		})[0];
	};
	$rootScope.$watch(function () {
		return AuthorizationService.authorized;
	}, that.update());
});

app.controller('AccountsController', function ($scope, AuthorizationService, AccountsService) {
	$scope.authorization = AuthorizationService;
	$scope.accountService = AccountsService;
});

app.controller('TransactionsController', function ($scope, HTTPService, AuthorizationService, AccountsService) {
	$scope.authorization = AuthorizationService;
	$scope.accounts = AccountsService;
	$scope.transactions = [];
	$scope.currentPage = 1;
	$scope.totalPages = 0;
	$scope.transactionTypes = [{name: "Expense/income", value: "EXPENSEINCOME"}, {name: "Transfer", value: "TRANSFER"}];
	var updateTransactions = function () {
		var nextPage = $scope.currentPage;
		HTTPService.get("service/transactions/page_" + (nextPage - 1))
				.then(function (data) {
					$scope.transactions = data.data;
					$scope.currentPage = nextPage;
					AccountsService.update();
				}, update);
	};
	var updateTransactionsCount = function () {
		HTTPService.get("service/transactions/pages")
				.then(function (data) {
					$scope.totalPages = data.data;
				}, update);
	};
	var update = function () {
		if (AuthorizationService.authorized) {
			updateTransactions();
			updateTransactionsCount();
		} else {
			$scope.transactions = [];
			$scope.currentPage = 1;
			$scope.totalPages = 0;
		}
	};
	var updateTransactionLocal = function (data) {
		var found = false;
		$scope.transactions.forEach(
				function (transaction, i) {
					if (transaction.id === data.id) {
						$scope.transactions[i] = data;
						found = true;
					}
				});
		return found;
	};
	var updateTransaction = function (id) {
		if (id === undefined)
			return updateTransactions();
		return HTTPService.get("service/transactions/" + id)
				.then(function (data) {
					if (!updateTransactionLocal(data.data))
						updateTransactions();
				}, update);
	};
	var submitTransaction = function (transaction) {
		transaction.date = dateToJson(transaction.date);
		transaction.tags = tagsToJson(transaction.tags);
		return HTTPService.post("service/transactions/submit", transaction)
				.then(function (data) {
					if (!updateTransactionLocal(data.data))
						updateTransactions();
				}, update);
	};
	var deleteTransaction = function (transaction) {
		if (transaction === undefined || transaction.id === undefined)
			return updateTransactions();
		return HTTPService.get("service/transactions/delete/" + transaction.id)
				.then(function () {
					updateTransactions();
				}, update);
	};
	var dateToJson = function (date) {
		if (date instanceof Date)
			return date.toJSON().split("T")[0];
		else
			return date;
	};
	var tagsToJson = function (tags) {
		if (tags.constructor === String)
			return tags.split(",");
		else
			return tags;
	};
	$scope.addTransactionComponent = function (transaction) {
		transaction.components.push({});
	};
	$scope.deleteTransactionComponent = function (transaction, component) {
		transaction.components = transaction.components.filter(function (comp) {
			return comp !== component;
		});
	};
	$scope.addTransaction = function () {
		$scope.transactions.unshift({isEditing: true, components: [], date: dateToJson(new Date()), tags: [], type: $scope.transactionTypes[0].value});
	};
	$scope.startEditing = function (transaction) {
		transaction.isEditing = true;
	};
	$scope.duplicateTransaction = function (transaction) {
		transaction.isEditing = false;
		var newTransaction = angular.copy(transaction);
		newTransaction.isEditing = true;
		newTransaction.id = undefined;
		newTransaction.version = undefined;
		newTransaction.date = dateToJson(new Date());
		newTransaction.components.forEach(function (component) {
			component.id = undefined;
			component.version = undefined;
		});
		$scope.transactions.unshift(newTransaction);
	};
	$scope.submitEditing = function (transaction) {
		transaction.isEditing = undefined;
		submitTransaction(transaction);
	};
	$scope.deleteTransaction = function (transaction) {
		transaction.isEditing = undefined;
		deleteTransaction(transaction);
	};
	$scope.cancelEditing = function (transaction) {
		updateTransaction(transaction.id).then(AccountsService.update);
	};
	$scope.$watch(function () {
		return AuthorizationService.authorized;
	}, update);
	update();
	$scope.pageChanged = update;
});
