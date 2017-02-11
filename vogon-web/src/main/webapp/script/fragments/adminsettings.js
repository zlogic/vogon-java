app.controller("AdminSettingsController", function ($scope, AuthorizationService, HTTPService, UserService) {
	var that = this;
	$scope.configuration = {};
	var updateConfigurationVariables = function (data) {
		$scope.configuration = {};
		data.forEach(function (configurationVariable) {
			$scope.configuration[configurationVariable.name] = configurationVariable.value;
		});
	};
	var convertConfigurationForPost = function () {
		var configurationPost = [];
		for (var name in $scope.configuration)
			configurationPost.push({name: name, value: $scope.configuration[name]});
		return configurationPost;
	};
	this.update = function () {
		if (AuthorizationService.authorized && UserService.isAdmin())
			return HTTPService.get("service/configuration").then(function (data) {
				updateConfigurationVariables(data.data);
			});
	};
	$scope.submitEditing = function () {
		if (AuthorizationService.authorized && UserService.isAdmin())
			HTTPService.post("service/configuration", convertConfigurationForPost(that.adminSettingsConfiguration))
					.then(that.update, that.update);
	};
	$scope.cancelEditing = function () {
		that.update();
	};
	$scope.$watch(function () {
		return UserService.userData;
	}, function () {
		$scope.$applyAsync(that.update());
	});
});