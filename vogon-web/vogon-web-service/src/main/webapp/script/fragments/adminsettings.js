app.controller("AdminSettingsController", function ($scope, HTTPService, NavigationService) {
	$scope.configuration = {};
	var updateConfigurationVariables = function (data) {
		$scope.configuration = {};
		data.forEach(function (configurationVariable) {
			$scope.configuration[configurationVariable.name] = configurationVariable.value;
			if (configurationVariable.name === "AllowRegistration")
				$scope.allowRegistration = configurationVariable.value;
		});
	};
	var convertConfigurationForPost = function () {
		var configurationPost = [];
		for (var name in $scope.configuration)
			configurationPost.push({name: name, value: $scope.configuration[name]});
		return configurationPost;
	};
	var update = function () {
		return HTTPService.get("service/configuration").then(function (data) {
			updateConfigurationVariables(data.data);
		});
	};
	update();
	$scope.submitEditing = function () {
		HTTPService.post("service/configuration", convertConfigurationForPost($scope.configuration)).then(NavigationService.navigateBack(), NavigationService.navigateBack());
	};
	$scope.cancelEditing = function () {
		NavigationService.navigateBack();
	};
});