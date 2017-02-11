app.service("AccountsService", function ($rootScope, HTTPService, AuthorizationService, CurrencyService) {
	var that = this;
	this.accounts = [];
	this.totalsForCurrency = {};
	this.updateTransactions = function () {
		throw messages.UPDATE_TRANSACTIONS_NOT_INITIALIZED;
	};
	var setAccounts = function (data) {
		that.accounts = data;
		that.accounts.sort(function (a, b) {
			return a.id - b.id;
		});
		that.updateTotalsForCurrencies();
	};
	this.updateTotalsForCurrencies = function () {
		var totals = {};
		//Compute totals for currencies
		that.accounts.forEach(
				function (account) {
					if (totals[account.currency] === undefined) {
						var currency = CurrencyService.findCurrency(account.currency);
						currency = currency !== undefined ? currency.currencyCode : undefined;
						totals[account.currency] = {total: 0, name: currency};
					}
					totals[account.currency].total += account.balance;
				});
		that.totalsForCurrency = totals;
	};
	var doUpdate = updateHelper(function () {
		if (AuthorizationService.authorized) {
			return HTTPService.get("service/accounts", undefined, HTTPService.buildRequestParams(false))
					.then(function (data) {
						setAccounts(data.data);
					});
		} else {
			that.accounts = [];
		}
	});
	this.update = function () {
		return doUpdate.update();
	};
	this.submitAccounts = function (accounts) {
		return HTTPService.post("service/accounts", accounts)
				.then(function (data) {
					setAccounts(data.data);
					that.updateTransactions();
				}, that.update);
	};
	this.getAccount = function (id) {
		return that.accounts.filter(function (obj) {
			return obj.id === id;
		})[0];
	};
	$rootScope.$watch(function () {
		return AuthorizationService.authorized;
	}, function () {
		$rootScope.$applyAsync(that.update);
	});
	$rootScope.$watch(function () {
		return CurrencyService.currencies;
	}, function () {
		$rootScope.$applyAsync(that.updateTotalsForCurrencies);
	});
	HTTPService.updateAccounts = this.update;
});
