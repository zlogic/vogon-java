var app = angular.module("vogon", ["ngCookies", "ui.bootstrap", "nvd3", "infinite-scroll", "ngTagsInput"]);

app.run(function ($templateRequest) {
	$templateRequest("fragments/accounteditor.fragment");
	$templateRequest("fragments/transactioneditor.fragment");
	$templateRequest("fragments/usersettings.fragment");
	$templateRequest("fragments/analytics.fragment");
	$templateRequest("fragments/adminsettings.fragment");
	$templateRequest("fragments/intro.fragment");
});

app.controller("NotificationController", function ($scope, HTTPService, AlertService) {
	$scope.httpService = HTTPService;
	$scope.alertService = AlertService;
	$scope.closeAlert = AlertService.closeAlert;
});

app.controller("LoginController", function ($scope, $http, $modal, AuthorizationService, HTTPService) {
	$scope.authorizationService = AuthorizationService;
	$scope.httpService = HTTPService;
	$scope.loginLocked = "authorizationService.authorized || httpService.isLoading";
	$scope.loginError = undefined;
	$scope.registrationError = undefined;
	$scope.introDialog = undefined;
	var displayLoginError = function (data) {
		$scope.loginError = data.data.error_description;
	};
	var displayRegistrationError = function (data) {
		$scope.registrationError = data.data.exception;
	};
	var reset = function () {
		$scope.loginError = undefined;
		$scope.registrationError = undefined;
		closeIntroDialog();
	};
	var closeIntroDialog = function () {
		if ($scope.introDialog !== undefined) {
			var deleteFunction = function () {
				$scope.introDialog = undefined;
			};
			$scope.introDialog.then(deleteFunction, deleteFunction);
		}
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
		closeIntroDialog();
		$scope.introDialog = $modal.open({
			templateUrl: "fragments/intro.fragment"
		}).result.then(closeIntroDialog, closeIntroDialog);
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

app.controller("UserController", function ($scope, $modal, AuthorizationService, UserService, HTTPService) {
	$scope.authorizationService = AuthorizationService;
	$scope.userService = UserService;
	$scope.httpService = HTTPService;
	$scope.logoutLocked = "!authorizationService.authorized";
	$scope.loginDialog = undefined;
	$scope.userSettingsDialog = undefined;
	$scope.analyticsDialog = undefined;
	$scope.adminSettingsDialog = undefined;
	$scope.logout = function () {
		AuthorizationService.logout();
	};
	var closeUserSettingsDialog = function () {
		if ($scope.userSettingsDialog !== undefined) {
			var deleteFunction = function () {
				$scope.userSettingsDialog = undefined;
			};
			$scope.userSettingsDialog.then(deleteFunction, deleteFunction);
		}
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
		closeUserSettingsDialog();
		$scope.userSettingsDialog = $modal.open({
			templateUrl: "fragments/usersettings.fragment",
			controller: "UserSettingsController"
		}).result.then(closeUserSettingsDialog, closeUserSettingsDialog);
	};
	$scope.showAnalyticsDialog = function () {
		closeAnalyticsDialog();
		$scope.analyticsDialog = $modal.open({
			templateUrl: "fragments/analytics.fragment",
			controller: "AnalyticsController",
			size: "lg"
		}).result.then(closeAnalyticsDialog, closeAnalyticsDialog);
	};
	$scope.showAdminSettingsDialog = function () {
		closeAdminSettingsDialog();
		$scope.adminSettingsDialog = $modal.open({
			templateUrl: "fragments/adminsettings.fragment",
			controller: "AdminSettingsController",
			size: "lg"
		}).result.then(closeAdminSettingsDialog, closeAdminSettingsDialog);
	};
	$scope.isAdmin = function () {
		return UserService.userData !== undefined && UserService.userData.authorities.some(function (authority) {
			return authority === "ROLE_VOGON_ADMIN";
		});
	};
});
