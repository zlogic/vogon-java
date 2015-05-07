app.controller("AccountsController", function ($scope, $modal, AuthorizationService, AccountsService, UserService) {
	$scope.authorizationService = AuthorizationService;
	$scope.accountService = AccountsService;
	$scope.editor = undefined;
	$scope.userService = UserService;
	$scope.editAccounts = function () {
		closeEditor();
		$scope.editor = $modal.open({
			templateUrl: "fragments/accounteditor.fragment",
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
