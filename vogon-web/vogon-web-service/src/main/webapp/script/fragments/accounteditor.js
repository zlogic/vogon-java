app.controller("AccountsEditorController", function ($scope, AccountsService, CurrencyService, NavigationService) {
	$scope.accounts = AccountsService;
	$scope.currencies = CurrencyService;
	$scope.addAccount = function () {
		var account = {includeInTotal: true, showInList: true};
		AccountsService.accounts.unshift(account);
	};
	$scope.deleteAccount = function (account) {
		AccountsService.accounts = AccountsService.accounts.filter(function (comp) {
			return comp !== account;
		});
	};
	$scope.cancelEditing = function () {
		AccountsService.update();
		NavigationService.navigateBack();
	};
	$scope.submitEditing = function () {
		NavigationService.navigateBack();
		AccountsService.submitAccounts(AccountsService.accounts);
	};
});