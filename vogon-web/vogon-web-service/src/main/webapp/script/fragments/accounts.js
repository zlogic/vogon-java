app.controller("AccountsController", function ($scope, AuthorizationService, AccountsService, UserService, NavigationService) {
	$scope.authorizationService = AuthorizationService;
	$scope.accountService = AccountsService;
	$scope.editor = undefined;
	$scope.userService = UserService;
	$scope.editAccounts = function () {
		NavigationService.navigateTo("accounteditor");
	};
});
