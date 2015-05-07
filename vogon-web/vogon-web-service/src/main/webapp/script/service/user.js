app.service("UserService", function ($rootScope, AuthorizationService, HTTPService, TagsService) {
	var that = this;
	this.userData = undefined;
	this.update = function () {
		if (AuthorizationService.authorized) {
			HTTPService.get("service/user", undefined, HTTPService.buildRequestParams(false))
					.then(function (data) {
						that.userData = data.data;
					}, function () {
						that.userData = undefined;
					});
			TagsService.update();
		}
	};
	this.submit = function (user) {
		return HTTPService.post("service/user", user)
				.then(function (data) {
					that.userData = data.data;
				}, that.update);
	};
	this.performCleanup = function () {
		return HTTPService.get("service/cleanup")
				.then(function () {
					HTTPService.updateAllData();
				});
	};
	this.performRecalculateBalance = function () {
		return HTTPService.get("service/recalculateBalance")
				.then(function () {
					HTTPService.updateAccounts();
				});
	};
	$rootScope.$watch(function () {
		return AuthorizationService.authorized;
	}, this.update);
	this.update();
	HTTPService.updateUser = this.update;
});