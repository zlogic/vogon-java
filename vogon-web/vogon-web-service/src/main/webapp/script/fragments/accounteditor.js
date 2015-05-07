app.controller("AccountsEditorController", function ($scope, $modalInstance, AccountsService, CurrencyService) {
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
		$modalInstance.dismiss();
	};
	$scope.submitEditing = function () {
		$modalInstance.close(AccountsService.accounts);
	};
});