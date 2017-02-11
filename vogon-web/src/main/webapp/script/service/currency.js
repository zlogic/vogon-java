app.service("CurrencyService", function ($rootScope, HTTPService, AuthorizationService) {
	var that = this;
	this.currencies = [];
	var doUpdate = updateHelper(function () {
		if (AuthorizationService.authorized) {
			return HTTPService.get("service/currencies")
					.then(function (data) {
						that.currencies = data.data;
					});
		} else {
			that.currencies = [];
		}
	});
	this.update = function () {
		return doUpdate.update();
	};
	this.findCurrency = function (currencyCode) {
		var result = that.currencies.filter(
				function (currency) {
					return currency.currencyCode === currencyCode;
				});
		if (result.length > 0)
			return result[0];
	};
	$rootScope.$watch(function () {
		return AuthorizationService.authorized;
	}, function () {
		$rootScope.$applyAsync(that.update);
	});
});
