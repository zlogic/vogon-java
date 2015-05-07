app.service("AccountsService", function ($rootScope, HTTPService, AuthorizationService, CurrencyService) {
	var that = this;
	this.accounts = [];
	this.totalsForCurrency = {};
	this.updateTransactions = function () {
		throw messages.UPDATE_TRANSACTIONS_NOT_INITIALIZED;
	};
	var setAccounts = function (data) {
		that.accounts = data;
		that.updateTotalsForCurrencies();
	};
	this.updateTotalsForCurrencies = function () {
		var totals = {};
		//Compute totals for currencies
		that.accounts.forEach(
				function (account) {
					if (totals[account.currency] === undefined)
						totals[account.currency] = {total: 0, name: CurrencyService.findCurrency(account.currency)};
					totals[account.currency].total += account.balance;
				});
		that.totalsForCurrency = totals;
	};
	this.update = function () {
		if (AuthorizationService.authorized) {
			HTTPService.get("service/accounts", undefined, HTTPService.buildRequestParams(false))
					.then(function (data) {
						setAccounts(data.data);
					});
		} else {
			that.accounts = [];
		}
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
	}, that.update);
	$rootScope.$watch(function () {
		return CurrencyService.currencies;
	}, that.updateTotalsForCurrencies);
	HTTPService.updateAccounts = this.update;
});