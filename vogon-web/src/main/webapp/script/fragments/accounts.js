app.controller("AccountsController", function ($scope, AuthorizationService, AccountsService, CurrencyService, UserService) {
	$scope.authorizationService = AuthorizationService;
	$scope.accountService = AccountsService;
	$scope.currencies = CurrencyService;
	$scope.editor = undefined;
	$scope.userService = UserService;
	$scope.editingAccounts = false;
	$scope.editAccounts = function () {
		$scope.editingAccounts = true;
	};
	$scope.addAccount = function () {
		var account = {includeInTotal: true, showInList: true, currency: UserService.userData.defaultCurrency};
		AccountsService.accounts.unshift(account);
	};
	$scope.deleteAccount = function (account) {
		AccountsService.accounts = AccountsService.accounts.filter(function (comp) {
			return comp !== account;
		});
	};
	$scope.cancelEditing = function () {
		AccountsService.update();
		$scope.editingAccounts = false;
	};
	$scope.submitEditing = function () {
		AccountsService.submitAccounts(AccountsService.accounts);
		$scope.editingAccounts = false;
	};
});
