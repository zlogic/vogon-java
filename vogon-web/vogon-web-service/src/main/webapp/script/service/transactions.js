app.service("TransactionsService", function ($q, HTTPService, AuthorizationService, AccountsService) {
	var that = this;
	this.transactions = [];
	this.transactionTypes = [{name: messages.EXPENSEINCOME, value: "EXPENSEINCOME"}, {name: messages.TRANSFER, value: "TRANSFER"}];
	this.defaultTransactionType = this.transactionTypes[0];
	this.currentPage = 0;
	this.nextPageRequest = undefined;
	this.loadingNextPage = false;
	this.lastPage = false;
	this.sortColumn = "date";
	this.sortAsc = false;
	this.filterDescription = undefined;
	this.filterDate = undefined;
	this.filterTags = undefined;
	var reset = function () {
		that.currentPage = 0;
		that.transactions = [];
		that.lastPage = false;
		that.loadingNextPage = that.nextPageRequest !== undefined;
	};
	this.nextPage = function () {
		if (that.lastPage) {
			that.loadingNextPage = false;
		} else if (AuthorizationService.authorized) {
			if (that.nextPageRequest === undefined) {
				that.loadingNextPage = true;
				var params = {
					page: that.currentPage,
					sortColumn: that.sortColumn.toUpperCase(),
					sortDirection: that.sortAsc ? "ASC" : "DESC"
				};
				if (that.filterDate !== undefined && that.filterDate !== null && that.filterDate !== "")
					params.filterDate = dateToJson(that.filterDate);
				if (that.filterDescription !== undefined && that.filterDescription !== "")
					params.filterDescription = that.filterDescription;
				if (that.filterTags !== undefined) {
					var tags = tagsToJson(that.filterTags);
					if (tags.length > 0)
						params.filterTags = tags;
				}
				return that.nextPageRequest = HTTPService.get("service/transactions/?" + encodeForm(params), undefined, HTTPService.buildRequestParams(false))
						.then(function (data) {
							that.nextPageRequest = undefined;
							that.loadingNextPage = false;
							if (data.data.length !== 0)
								that.transactions = that.transactions.concat(data.data);
							else
								that.lastPage = true;
							that.currentPage++;
						}, function () {
							that.nextPageRequest = undefined;
							reset();
							that.lastPage = true;
						});
			} else {
				return that.nextPageRequest;
			}
		} else {
			reset();
		}
		var deferred = $q.defer();
		deferred.reject();
		return deferred.promise;
	};
	this.update = function () {
		reset();
		AccountsService.update();
		return that.nextPage();
	};
	var updateTransactionLocal = function (data) {
		var found = false;
		that.transactions.forEach(
				function (transaction, i) {
					if (transaction.id === data.id) {
						that.transactions[i] = data;
						found = true;
					}
				});
		return found;
	};
	this.updateTransaction = function (id) {
		if (id === undefined)
			return that.update();
		return HTTPService.get("service/transactions/transaction/" + id)
				.then(function (data) {
					if (updateTransactionLocal(data.data))
						AccountsService.update();
					else
						that.update();
				}, that.update);
	};
	this.submitTransaction = function (transaction) {
		transaction.date = dateToJson(transaction.date);
		return HTTPService.post("service/transactions", transaction)
				.then(function (data) {
					if (updateTransactionLocal(data.data))
						AccountsService.update();
					else
						that.update();
				}, that.update);
	};
	this.deleteTransaction = function (transaction) {
		if (transaction === undefined || transaction.id === undefined)
			return that.update();
		return HTTPService.delete("service/transactions/transaction/" + transaction.id)
				.then(that.update, that.update);
	};
	this.getDate = function () {
		return dateToJson(new Date());
	};
	this.isExpenseIncomeTransaction = function (transaction) {
		return transaction.type === this.transactionTypes[0].value;
	};
	this.isTransferTransaction = function (transaction) {
		return transaction.type === this.transactionTypes[1].value;
	};
	this.getAccounts = function (transaction, predicate) {
		var accounts = [];
		transaction.components.forEach(
				function (component) {
					var account = AccountsService.getAccount(component.accountId);
					if (account !== undefined && predicate(component) && !accounts.some(function (checkAccount) {
						return checkAccount.id === account.id;
					}))
						accounts.unshift(account);
				});
		return accounts;
	};
	this.fromAccountsPredicate = function (component) {
		return component.amount < 0;
	};
	this.toAccountsPredicate = function (component) {
		return component.amount > 0;
	};
	this.allAccountsPredicate = function () {
		return true;
	};
	var getTotalsByCurrency = function (transaction) {
		var totals = {};
		transaction.components.forEach(
				function (component) {
					var account = AccountsService.getAccount(component.accountId);
					if (account === undefined)
						return;
					if (totals[account.currency] === undefined)
						totals[account.currency] = {positiveAmount: 0, negativeAmount: 0};
					if (component.amount > 0 || that.isExpenseIncomeTransaction(transaction))
						totals[account.currency].positiveAmount += component.amount;
					else if (component.amount < 0)
						totals[account.currency].negativeAmount -= component.amount;
				});
		return totals;
	};
	this.isAmountOk = function (transaction) {
		if (this.isExpenseIncomeTransaction(transaction))
			return true;
		if (this.isTransferTransaction(transaction)) {
			var totals = getTotalsByCurrency(transaction);
			var totalsCount = 0;
			for (var currency in totals) {
				if (totalsCount > 0)
					return true;
				totalsCount++;
			}
			for (var currency in totals) {
				var total = totals[currency];
				if (total.positiveAmount !== total.negativeAmount)
					return false;
			}
			return true;
		} else
			return false;
	};
	this.totalsByCurrency = function (transaction) {
		var totals = {};
		var totalsData = getTotalsByCurrency(transaction);
		for (var currency in totalsData) {
			var total = totalsData[currency];
			if (this.isExpenseIncomeTransaction(transaction))
				totals[currency] = total.positiveAmount;
			else if (this.isTransferTransaction(transaction))
				totals[currency] = total.positiveAmount > total.negativeAmount ? total.positiveAmount : total.negativeAmount;
		}
		return totals;
	};
	this.applySort = function (column) {
		if (that.sortColumn === column) {
			that.sortAsc = !that.sortAsc;
		} else {
			that.sortAsc = column === "description";
			that.sortColumn = column;
		}
		that.update();
	};
	AccountsService.updateTransactions = this.update;
	HTTPService.updateTransactions = this.update;
});