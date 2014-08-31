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
			setToken(data.access_token, data.refresh_token);
		}).error(function() {
			that.refresh_token = undefined;
			if(that.username!==undefined && that.password !==undefined)
				that.performAuthorization(that.username,that.password);
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
	}
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

app.controller('TransactionsController', function($scope, $http, AuthorizationService) {
	$scope.authorization = AuthorizationService;
	$scope.transactions = [];
	$scope.currentPage = 1;
	$scope.totalPages = 0;
	$scope.loading = 0;
	$scope.isLoading = false;
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
	$scope.$watch('authorization.authorized', function() {
		update();
	});
	update();
	$scope.pageChanged = function() {
		update();
	};
});
