var app = angular.module("vogon", ["ngCookies", "ui.bootstrap", "nvd3", "infinite-scroll", "ngTagsInput"]);

app.controller("NotificationController", function ($scope, HTTPService, AlertService) {
	$scope.httpService = HTTPService;
	$scope.alertService = AlertService;
	$scope.closeAlert = AlertService.closeAlert;
});

app.controller("LoginController", function ($scope, $http, AuthorizationService, HTTPService, NavigationService) {
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
		NavigationService.reset();
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
	$scope.showIntroDialog = function () {
		NavigationService.navigateTo("intro");
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

app.controller("UserController", function ($scope, AuthorizationService, UserService, HTTPService, NavigationService) {
	$scope.authorizationService = AuthorizationService;
	$scope.userService = UserService;
	$scope.httpService = HTTPService;
	$scope.logoutLocked = "!authorizationService.authorized";
	$scope.logout = function () {
		AuthorizationService.logout();
	};
	var closeAnalyticsDialog = function () {
		if ($scope.analyticsDialog !== undefined) {
			var deleteFunction = function () {
				$scope.analyticsDialog = undefined;
			};
			$scope.analyticsDialog.then(deleteFunction, deleteFunction);
		}
	};
	var closeAdminSettingsDialog = function () {
		if ($scope.adminSettingsDialog !== undefined) {
			var deleteFunction = function () {
				$scope.adminSettingsDialog = undefined;
			};
			$scope.adminSettingsDialog.then(deleteFunction, deleteFunction);
		}
	};
	$scope.showUserSettingsDialog = function () {
		NavigationService.navigateTo("usersettings");
	};
	$scope.showAnalyticsDialog = function () {
		NavigationService.navigateTo("analytics");
	};
	$scope.showAdminSettingsDialog = function () {
		NavigationService.navigateTo("adminsettings");
	};
	$scope.isAdmin = function () {
		return UserService.userData !== undefined && UserService.userData.authorities.some(function (authority) {
			return authority === "ROLE_VOGON_ADMIN";
		});
	};
});

app.service("NavigationService", function (AuthorizationService) {
	var that = this;
	var allowedNonAdminPages = ["login", "intro"];
	var defaultBreadcrumbs = ["login", "main"];
	var filterBreadcrumbs = function () {
		var filteredBreadcrumbs = [];
		that.breadcrumbs.forEach(function (page) {
			if (AuthorizationService.authorized || allowedNonAdminPages.indexOf(page) !== -1)
				filteredBreadcrumbs.push(page);
		});
		return filteredBreadcrumbs;
	};
	this.currentPage = function () {
		var breadcrumbs = filterBreadcrumbs();
		return breadcrumbs[breadcrumbs.length - 1];
	};
	this.navigateTo = function (page) {
		if (that.breadcrumbs.last === page)
			return;
		that.breadcrumbs.push(page);
	};
	this.reset = function () {
		that.breadcrumbs = angular.copy(defaultBreadcrumbs);
	};
	this.navigateBack = function () {
		if (that.breadcrumbs.length > 0)
			that.breadcrumbs.pop();
	};
	this.reset();
});

app.controller("NavigationController", function ($scope, NavigationService) {
	$scope.navigationService = NavigationService;
});
