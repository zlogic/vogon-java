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

app.service("HTTPService", function ($http, AlertService) {
	var that = this;
	var pendingRequests = 0;
	var isLoading = false;
	var authorizationHeaders = {};
	var mergeHeaders = function (extraHeaders) {
		var headers = {};
		merge(headers, that.authorizationHeaders);
		if (extraHeaders !== undefined)
			merge(headers, extraHeaders);
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
		that.isLoading = pendingRequests > 0;
	};
	var errorHandler = function (data) {
		endRequest();
		AlertService.addAlert("HTTP error: " + data.status + "(" + angular.toJson(data.data) + ")");
		if (data.status === 401) {
			that.fixAuthorization();
		} else {
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

app.service("AuthorizationService", function ($cookies, $interval, AlertService, HTTPService) {
	var that = this;
	var authorized = false;
	var access_token = undefined;
	var refresh_token = undefined;
	var username = undefined;
	var password = undefined;
	var clientId = "vogonweb";
	var postHeaders = {"Content-Type": "application/x-www-form-urlencoded"};
	var encodeForm = function (data) {
		var buffer = [];
		for (var name in data)
			buffer.push([encodeURIComponent(name), encodeURIComponent(data[name])].join("="));
		return buffer.join("&");
	};
	var setToken = function (access_token, refresh_token, username, password) {
		if (access_token !== undefined && refresh_token !== undefined) {
			that.access_token = access_token;
			that.refresh_token = refresh_token;
			$cookies.refresh_token = refresh_token;
			HTTPService.setAccessToken(access_token);
			that.authorized = true;
			if (username !== undefined) {
				that.username = username;
				$cookies.username = username;
			}
			if (password !== undefined)
				that.password = password;
		}
	};
	var refreshToken = function (refresh_token, username) {
		if (refresh_token === undefined || username === undefined)
			return;
		var params = {client_id: clientId, grant_type: "refresh_token", refresh_token: refresh_token};
		HTTPService.post("oauth/token", encodeForm(params), postHeaders)
				.then(function (data) {
					data = data.data;
					setToken(data.access_token, data.refresh_token, username);
				}, function () {
					that.refresh_token = undefined;
					if (that.username !== undefined && that.password !== undefined)
						that.performAuthorization(that.username, that.password);
					else
						that.resetAuthorization("Unable to refresh token");
				});
	};
	this.performAuthorization = function (username, password) {
		var params = {username: username, password: password, client_id: clientId, grant_type: "password"};
		HTTPService.post("oauth/token", encodeForm(params), postHeaders)
				.then(function (data) {
					data = data.data;
					setToken(data.access_token, data.refresh_token, username, password);
				}, function () {
					if (that.refresh_token !== undefined) {
						that.refresh_token = undefined;
						that.performAuthorization(username, password);
					} else {
						that.resetAuthorization("Unable to authenticate");
					}
				});
	};
	this.fixAuthorization = function () {
		if (that.refresh_token !== undefined && that.username !== undefined) {
			refreshToken(that.refresh_token, that.username);
		} else {
			that.resetAuthorization("Unable to refresh token");
		}
	};
	this.resetAuthorization = function (message) {
		that.username = undefined;
		that.password = undefined;
		that.access_token = undefined;
		that.refresh_token = undefined;
		HTTPService.setAccessToken();
		that.authorized = false;
		delete $cookies.username;
		delete $cookies.refresh_token;
		if (message !== undefined)
			AlertService.addAlert(message);
	};
	HTTPService.fixAuthorization = function () {
		that.fixAuthorization();
	};
	refreshToken($cookies.refresh_token, $cookies.username);
	$interval(function () {
		refreshToken(that.refresh_token, that.username);
	}, 60 * 1000);
});

app.controller("NotificationController", function ($scope, HTTPService, AlertService) {
	$scope.httpService = HTTPService;
	$scope.alertService = AlertService;
	$scope.closeAlert = AlertService.closeAlert;
});

app.controller("AuthController", function ($scope, AuthorizationService) {
	$scope.authorization = AuthorizationService;
	$scope.username = "";
	$scope.password = "";
	$scope.login = function () {
		AuthorizationService.performAuthorization($scope.username, $scope.password);
		$scope.username = "";
		$scope.password = "";
	};
	$scope.logout = function () {
		AuthorizationService.resetAuthorization();
	};
});

app.service("AccountsService", function ($rootScope, HTTPService, AuthorizationService) {
	var that = this;
	var accounts = [];
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
		AccountsService.update();
		return found;
	};
	var updateTransaction = function (id) {
		if (id === undefined)
			return updateTransactions();
		HTTPService.get("service/transactions/" + id)
				.then(function (data) {
					if (!updateTransactionLocal(data.data))
						updateTransactions();
				}, update);
	};
	var submitTransaction = function (transaction) {
		transaction.date = dateToJson(transaction.date);
		transaction.tags = tagsToJson(transaction.tags);
		HTTPService.post("service/transactions/submit", transaction)
				.then(function (data) {
					if (!updateTransactionLocal(data.data))
						updateTransactions();
				}, update);
	};
	var deleteTransaction = function (transaction) {
		if (transaction === undefined || transaction.id === undefined)
			return updateTransactions();
		HTTPService.get("service/transactions/delete/" + transaction.id)
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
		updateTransaction(transaction.id);
		AccountsService.update();
	};
	$scope.$watch(function () {
		return AuthorizationService.authorized;
	}, update);
	update();
	$scope.pageChanged = update;
});
