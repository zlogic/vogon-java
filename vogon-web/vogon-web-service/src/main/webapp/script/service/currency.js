app.service("CurrencyService", function ($rootScope, HTTPService, AuthorizationService) {
	var that = this;
	this.currencies = [];
	this.update = function () {
		if (AuthorizationService.authorized) {
			HTTPService.get("service/currencies")
					.then(function (data) {
						that.currencies = data.data;
					});
		} else {
			that.currencies = [];
		}
	};
	this.findCurrency = function (symbol) {
		var result = that.currencies.filter(
				function (currency) {
					return currency.symbol === symbol;
				});
		if (result.length > 0)
			return result[0].displayName;
	};
	$rootScope.$watch(function () {
		return AuthorizationService.authorized;
	}, function () {
		$rootScope.$applyAsync(that.update);
	});
});