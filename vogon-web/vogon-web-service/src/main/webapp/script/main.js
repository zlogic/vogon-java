var app = angular.module("vogon", ["ngCookies", "ngRoute", "ui.bootstrap", "nvd3", "infinite-scroll", "ngTagsInput"]);

app.run(function ($templateRequest) {
	$templateRequest("fragments/accounts.fragment");
	$templateRequest("fragments/transactioneditor.fragment");
	$templateRequest("fragments/usersettings.fragment");
	$templateRequest("fragments/analytics.fragment");
	$templateRequest("fragments/adminsettings.fragment");
	$templateRequest("fragments/intro.fragment");
});

app.config(function ($routeProvider, $locationProvider) {
	$routeProvider
			.when('/transactions', {
				templateUrl: 'fragments/transactions.fragment',
				controller: 'TransactionsController'
			})
			.when('/accounts', {
				templateUrl: 'fragments/accounts.fragment',
				controller: 'AccountsController'
			})
			.when('/analytics', {
				templateUrl: 'fragments/analytics.fragment',
				controller: 'AnalyticsController'
			})
			.when('/usersettings', {
				templateUrl: 'fragments/usersettings.fragment',
				controller: 'UserSettingsController'
			})
			.when('/adminsettings', {
				templateUrl: 'fragments/adminsettings.fragment',
				controller: 'AdminSettingsController'
			})
			.otherwise({redirectTo: '/transactions'});
	$locationProvider.html5Mode(false);
});

app.controller("NotificationController", function ($scope, HTTPService, AlertService) {
	$scope.httpService = HTTPService;
	$scope.alertService = AlertService;
	$scope.closeAlert = AlertService.closeAlert;
});

app.controller("LoginController", function ($scope, $http, AuthorizationService, HTTPService) {
	$scope.authorizationService = AuthorizationService;
	$scope.httpService = HTTPService;
	$scope.loginLocked = "authorizationService.authorized || httpService.isLoading";
	$scope.loginError = undefined;
	$scope.registrationError = undefined;
	var displayLoginError = function (data) {
		$scope.loginError = data.data.error_description;
	};
	var displayRegistrationError = function (data) {
		$scope.registrationError = data.data.exception;
	};
	var reset = function () {
		$scope.loginError = undefined;
		$scope.registrationError = undefined;
	};
	$scope.login = function () {
		reset();
		AuthorizationService.performAuthorization(AuthorizationService.username, AuthorizationService.password)
				.catch(displayLoginError);
	};
	$scope.register = function () {
		reset();
		var user = {username: AuthorizationService.username, password: AuthorizationService.password};
		return $http.post("register", user)
				.then($scope.login, displayRegistrationError);
	};
	$scope.doSelectedAction = function () {
		if ($scope.selectedTab === "login")
			$scope.login();
		else if ($scope.selectedTab === "register")
			$scope.register();
	};
	$scope.$watch(function () {
		return AuthorizationService.authorized;
	}, reset);
});

app.controller("UserController", function ($scope, $location, AuthorizationService, UserService, HTTPService) {
	$scope.authorizationService = AuthorizationService;
	$scope.userService = UserService;
	$scope.httpService = HTTPService;
	$scope.logoutLocked = "!authorizationService.authorized";
	$scope.logout = function () {
		AuthorizationService.logout();
	};
	$scope.isAdmin = function () {
		return UserService.userData !== undefined && UserService.userData.authorities.some(function (authority) {
			return authority === "ROLE_VOGON_ADMIN";
		});
	};
	$scope.isActivePath = function (path) {
		return path === $location.path().split("/")[1];
	};
});

app.controller("ContentController", function ($scope, AuthorizationService) {
	$scope.authorizationService = AuthorizationService;
});
