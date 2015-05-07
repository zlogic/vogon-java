app.controller("TransactionsController", function ($scope, $modal, $interval, TransactionsService, AuthorizationService, AccountsService, UserService, TagsService) {
	$scope.transactionsService = TransactionsService;
	$scope.authorizationService = AuthorizationService;
	$scope.accountsService = AccountsService;
	$scope.tagsService = TagsService;
	$scope.editor = undefined;
	$scope.editingTransaction = undefined;
	$scope.userService = UserService;
	$scope.filterTimer = undefined;
	$scope.filterDirty = false;
	var closeEditor = function () {
		$scope.editingTransaction = undefined;
		if ($scope.editor !== undefined) {
			var deleteFunction = function () {
				$scope.editor = undefined;
			};
			$scope.editor.then(deleteFunction, deleteFunction);
		}
	};
	$scope.addTransaction = function () {
		var transaction = {components: [], date: TransactionsService.getDate(), tags: [], type: TransactionsService.defaultTransactionType.value};
		$scope.transactionsService.transactions.unshift(transaction);
		$scope.startEditing(transaction);
	};
	$scope.startEditing = function (transaction) {
		closeEditor();
		$scope.editingTransaction = transaction;
		$scope.editor = $modal.open({
			templateUrl: "fragments/transactioneditor.fragment",
			controller: "TransactionEditorController",
			size: "lg",
			resolve: {
				transaction: function () {
					return $scope.editingTransaction;
				}
			}
		}).result.then(closeEditor, closeEditor);
	};
	$scope.duplicateTransaction = function (transaction) {
		var newTransaction = angular.copy(transaction);
		newTransaction.id = undefined;
		newTransaction.version = undefined;
		newTransaction.date = TransactionsService.getDate();
		newTransaction.amount = undefined;
		newTransaction.components.forEach(function (component) {
			component.id = undefined;
			component.version = undefined;
		});
		$scope.transactionsService.transactions.unshift(newTransaction);
		$scope.startEditing(newTransaction);
	};
	$scope.openFilterDateCalendar = function ($event) {
		$event.preventDefault();
		$event.stopPropagation();
		$scope.filterDateCalendarOpened = true;
	};
	$scope.applyFilter = function () {
		$scope.filterDirty = true;
		if ($scope.filterTimer === undefined) {
			$scope.filterTimer = $interval(function () {
				$scope.filterDirty = false;
				TransactionsService.update().then(function () {
					$scope.filterTimer = undefined;
					if ($scope.filterDirty)
						$scope.applyFilter();
				});
			}, 1000, 1);
		}
	};
	$scope.$watch(function () {
		return AuthorizationService.authorized;
	}, TransactionsService.update);
	TransactionsService.update();
});
