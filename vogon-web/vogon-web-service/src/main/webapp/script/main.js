var app = angular.module('vogon', ['ui.bootstrap']);

app.service('AuthorizationService', function() {
	var authorized = false;
	var username;
});

app.controller('AuthController', function($scope, AuthorizationService) {
	$scope.authorization = AuthorizationService;
	$scope.login = function(username) {
		$scope.authorization.username = username;
		$scope.authorization.authorized = true;
	};
	$scope.logout = function() {
		$scope.authorization.username = null;
		$scope.authorization.authorized = false;
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
	var updateTransactions = function() {
		$scope.loading++;
		updateIsLoading();
		var nextPage = $scope.currentPage;
		$http.get("service/transactions/page_" + (nextPage-1)).success(function(data) {
			$scope.transactions = data;
			$scope.loading--;
			updateIsLoading();
			$scope.currentPage = nextPage;
		});
	};
	var updateTransactionsCount = function() {
		$scope.loading++;
		updateIsLoading();
		$http.get("service/transactions/pages").success(function(data) {
			$scope.totalPages = data;
			$scope.loading--;
			updateIsLoading();
		});
	};
	var update = function() {
		updateTransactions();
		updateTransactionsCount();
	};
	update();
	$scope.pageChanged = function() {
		update();
	};
});
