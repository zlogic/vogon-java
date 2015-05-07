app.controller("TransactionEditorController", function ($scope, $modalInstance, AccountsService, TransactionsService, TagsService, transaction) {
	$scope.transaction = transaction;
	$scope.accountService = AccountsService;
	$scope.transactionTypes = TransactionsService.transactionTypes;
	$scope.calendarOpened = false;
	$scope.tagsService = TagsService;
	$scope.tags = transaction.tags.join(messages.TAGS_SEPARATOR);
	var tagsFromTransaction = function (transaction) {
		$scope.tags = [];
		transaction.tags.forEach(
				function (tag) {
					$scope.tags.push({text: tag});
				});
	};
	tagsFromTransaction(transaction);
	$scope.openCalendar = function ($event) {
		$event.preventDefault();
		$event.stopPropagation();
		$scope.calendarOpened = true;
	};
	$scope.addTransactionComponent = function () {
		$scope.transaction.components.push({});
	};
	$scope.deleteTransactionComponent = function (component) {
		transaction.components = transaction.components.filter(function (comp) {
			return comp !== component;
		});
	};
	$scope.submitEditing = function () {
		TagsService.mergeTags($scope.transaction.tags);
		TransactionsService.submitTransaction($scope.transaction);
		$modalInstance.close();
	};
	$scope.cancelEditing = function () {
		TransactionsService.updateTransaction(transaction.id).then(AccountsService.update);
		$modalInstance.dismiss();
	};
	$scope.deleteTransaction = function () {
		TransactionsService.deleteTransaction($scope.transaction);
		$modalInstance.close();
	};
	$scope.syncTags = function () {
		$scope.transaction.tags = tagsToJson($scope.tags);
	};
	$scope.isAccountVisible = function (account) {
		return account.showInList || $scope.transaction.components.some(function (component) {
			return component.accountId === account.id;
		});
	};
});