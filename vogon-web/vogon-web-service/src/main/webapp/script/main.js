var app = angular.module("vogon", ["ngCookies", "ui.bootstrap", "nvd3", "infinite-scroll", "ngTagsInput"]);

var dateToJson = function (date) {
	if (date instanceof Date)
		return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate())).toJSON().split("T")[0];
	else
		return date;
};

var tagsToJson = function (tags) {
	var tagsJson = [];
	for (var tag in tags)
		tagsJson.push(tags[tag].text);
	return tagsJson;
};

var encodeForm = function (data) {
	var buffer = [];
	for (var name in data)
		buffer.push([encodeURIComponent(name), encodeURIComponent(data[name])].join("="));
	return buffer.join("&");
};

app.service("AlertService", function ($timeout) {
	var that = this;
	this.alerts = [];
	this.closeAlert = function (alertIndex) {
		that.alerts.splice(alertIndex, 1);
	};
	this.enabled = function () {
		return true;
	};
	this.addAlert = function (message) {
		if (!that.enabled())
			return;
		var alert = {msg: message, type: "danger"};
		that.alerts.push(alert);
		$timeout(function () {
			var alertIndex = that.alerts.indexOf(alert);
			if (alertIndex !== -1)
				that.closeAlert(alertIndex);
		}, 30000);
	};
});

app.service("HTTPService", function ($http, $q, AlertService) {
	var that = this;
	var tokenRegex = /^oauth\/token$/;
	this.pendingRequests = 0;
	this.isLoading = false;
	this.authorizationHeaders = {};
	this.authorized = false;
	var mergeHeaders = function (extraHeaders) {
		var headers = {};
		if (extraHeaders !== undefined)
			merge(headers, extraHeaders);
		merge(headers, that.authorizationHeaders);
		return headers;
	};
	var merge = function (a, b) {
		for (var prop in b)
			a[prop] = b[prop];
	};
	var startRequest = function () {
		that.pendingRequests++;
		that.isLoading = true;
	};
	var endRequest = function () {
		that.pendingRequests--;
		that.isLoading = that.pendingRequests > 0;
	};
	var retryRequest = function (config) {
		config.headers = mergeHeaders(config.headers);
		return $http(config).then(
				function (data) {
					that.updateAllData();
					return data;
				}
		);
	};
	var isTokenURL = function (url) {
		return tokenRegex.test(url);
	};
	var errorHandler = function (data) {
		endRequest();
		var deferred = $q.defer();
		if (data.status === 401) {
			var fixAuthCall = that.fixAuthorization();
			if (fixAuthCall !== undefined)
				fixAuthCall
						.then(function (authData) {
							if (that.authorized)
								retryRequest(data.config).then(deferred.resolve, deferred.reject);
							else
								deferred.reject(authData);
						}, deferred.reject);
			else
				deferred.reject();
			return deferred.promise;
		} else {
			AlertService.addAlert(complex_messages.HTTP_ERROR_FORMAT(data.status, data.data));
			if (that.authorized && data.config.customData.updateOnFailure)
				that.updateAllData();
			//TODO: else refresh page to reset state?
		}
		deferred.reject(data);
		return deferred.promise;
	};
	var successHandler = function (data) {
		endRequest();
		return data;
	};
	this.buildRequestParams = function (updateOnFailure) {
		return {
			updateOnFailure: updateOnFailure !== undefined ? updateOnFailure : true
		};
	};
	this.get = function (url, extraHeaders, requestParams) {
		startRequest();
		var headers = isTokenURL(url) ? extraHeaders : mergeHeaders(extraHeaders);
		if (requestParams === undefined)
			requestParams = that.buildRequestParams();
		return $http.get(url, {headers: headers, customData: requestParams}).then(successHandler, errorHandler);
	};
	this.delete = function (url, extraHeaders, requestParams) {
		startRequest();
		var headers = isTokenURL(url) ? extraHeaders : mergeHeaders(extraHeaders);
		if (requestParams === undefined)
			requestParams = that.buildRequestParams();
		return $http.delete(url, {headers: headers, customData: requestParams}).then(successHandler, errorHandler);
	};
	this.post = function (url, data, extraHeaders, requestParams, transformRequest) {
		startRequest();
		var headers = isTokenURL(url) ? extraHeaders : mergeHeaders(extraHeaders);
		var params = {headers: headers};
		if (transformRequest !== undefined)
			params.transformRequest = transformRequest;
		if (requestParams === undefined)
			requestParams = that.buildRequestParams();
		params.customData = requestParams;
		return $http.post(url, data, params).then(successHandler, errorHandler);
	};
	this.fixAuthorization = function () {
		throw messages.FIX_AUTHORIZATION_NOT_INITIALIZED;
	};
	this.setAccessToken = function (access_token) {
		if (access_token !== undefined)
			that.authorizationHeaders = {Authorization: "Bearer " + access_token};
		else
			that.authorizationHeaders = {};
	};
	this.updateAllData = function () {
		that.updateAccounts();
		that.updateTransactions();
		that.updateUser();
	};
	this.updateAccounts = function () {
		throw messages.UPDATE_ACCOUNTS_NOT_INITIALIZED;
	};
	this.updateTransactions = function () {
		throw messages.UPDATE_TRANSACTIONS_NOT_INITIALIZED;
	};
	this.updateUser = function () {
		throw messages.UPDATE_USER_NOT_INITIALIZED;
	};
});

app.service("AuthorizationService", function ($q, AlertService, HTTPService) {
	var that = this;
	var clientId = "vogonweb";
	var postHeaders = {"Content-Type": "application/x-www-form-urlencoded"};
	var defaultRememberToken = false;
	this.authorized = false;
	this.access_token = undefined;
	this.username = undefined;
	this.password = undefined;
	this.rememberToken = defaultRememberToken;
	var setToken = function (access_token, username, password) {
		if (access_token !== undefined) {
			that.access_token = access_token;
			if (that.rememberToken)
				localStorage.setItem("access_token", access_token);
			HTTPService.setAccessToken(access_token);
			setAuthorized(true);
			if (username !== undefined)
				that.username = username;
			if (password !== undefined)
				that.password = password;
		}
	};
	var setAuthorized = function (authorized) {
		that.authorized = authorized;
		HTTPService.authorized = authorized;
	};
	this.performAuthorization = function (username, password) {
		var params = {username: username, password: password, client_id: clientId, grant_type: "password"};
		return HTTPService.post("oauth/token", encodeForm(params), postHeaders)
				.then(function (data) {
					data = data.data;
					setToken(data.access_token, username, password);
					return data;
				}, function (data) {
					that.resetAuthorization(messages.UNABLE_TO_AUTHENTICATE);
					var deferred = $q.defer();
					deferred.reject(data);
					return deferred.promise;
				});
	};
	this.logout = function () {
		if (that.access_token !== undefined) {
			var params = {token: that.access_token};
			return HTTPService.post("logout", encodeForm(params), postHeaders)
					.then(that.resetAuthorization(), that.resetAuthorization());
		} else {
			var deferred = $q.defer();
			deferred.reject({data: {error_description: messages.ALREADY_LOGGED_OUT}});
			that.resetAuthorization();
			return deferred.promise;
		}
	};
	this.fixAuthorization = function () {
		if (that.username !== undefined && that.password !== undefined) {
			return that.performAuthorization(that.username, that.password);
		} else {
			var message;
			if (that.authorized)
				if (that.username !== undefined && that.password !== undefined)
					message = messages.USERNAME_PASSWORD_NOT_ACCEPTED;
				else if (that.access_token !== undefined)
					message = messages.ACCESS_TOKEN_REJECTED;
			that.resetAuthorization(that.authorized ? messages.CANT_FIX_AUTHORIZATION : undefined);
		}
	};
	this.resetAuthorization = function (message) {
		that.username = undefined;
		that.password = undefined;
		that.access_token = undefined;
		HTTPService.setAccessToken();
		setAuthorized(false);
		localStorage.removeItem("access_token");
		this.rememberToken = defaultRememberToken;
		if (message !== undefined)
			AlertService.addAlert(message);
	};
	HTTPService.fixAuthorization = this.fixAuthorization;
	AlertService.enabled = function () {
		return that.authorized;
	};

	this.access_token = localStorage["access_token"];
	if (this.access_token !== undefined) {
		HTTPService.setAccessToken(this.access_token);
		setAuthorized(true);
	} else {
		setAuthorized(false);
	}
});

app.controller("NotificationController", function ($scope, HTTPService, AlertService) {
	$scope.httpService = HTTPService;
	$scope.alertService = AlertService;
	$scope.closeAlert = AlertService.closeAlert;
});

app.controller("LoginController", function ($scope, $http, AuthorizationService, HTTPService) {
	$scope.authorizationService = AuthorizationService;
	$scope.httpService = HTTPService;
	$scope.loginLocked = "authorizationService.authorized || httpService.isLoading";
	$scope.loginError = undefined;
	$scope.registrationError = undefined;
	var displayLoginError = function (data) {
		$scope.loginError = data.data.error_description;
	};
	var displayRegistrationError = function (data) {
		$scope.registrationError = data.data.exception;
	};
	var reset = function () {
		$scope.loginError = undefined;
		$scope.registrationError = undefined;
	};
	$scope.login = function () {
		reset();
		AuthorizationService.performAuthorization(AuthorizationService.username, AuthorizationService.password)
				.catch(displayLoginError);
	};
	$scope.register = function () {
		reset();
		var user = {username: AuthorizationService.username, password: AuthorizationService.password};
		return $http.post("register", user)
				.then($scope.login, displayRegistrationError);
	};
	$scope.doSelectedAction = function () {
		if ($scope.selectedTab === "login")
			$scope.login();
		else if ($scope.selectedTab === "register")
			$scope.register();
	};
	$scope.$watch(function () {
		return AuthorizationService.authorized;
	}, reset);
});

app.controller("AnalyticsController", function ($scope, $modalInstance, AccountsService, TransactionsService, CurrencyService, HTTPService, UserService, TagsService) {
	$scope.accountService = AccountsService;
	$scope.transactionsService = TransactionsService;
	$scope.currencyService = CurrencyService;
	$scope.tags = {};
	$scope.accounts = {};
	var currentTime = new Date();
	$scope.startDate = dateToJson(new Date(currentTime.getFullYear(), currentTime.getMonth(), 1));
	$scope.endDate = dateToJson(new Date((new Date(currentTime.getFullYear(), currentTime.getMonth() + 1, 1)) - 1));
	$scope.transactionTypeEnabled = {
		transfer: false,
		income: true,
		expense: true
	};
	$scope.report = undefined;
	$scope.tagsChart = {
		data: [],
		options: {
			chart: {
				type: "pieChart",
				donut: true,
				height: 400,
				showLabels: false,
				x: function (d) {
					return d.tag;
				},
				y: function (d) {
					return Math.abs(d.amount);
				},
				tooltipContent: function (key, y, e) {
					return complex_messages.TAGS_CHART_TOOLTIP(key, e.point.amount);
				}
			}
		}
	};
	$scope.balanceChart = {
		data: [],
		options: {
			chart: {
				type: "lineChart",
				height: 500,
				showLegend: false,
				margin: {
					top: 20,
					right: 100,
					bottom: 20,
					left: 100
				},
				x: function (d) {
					return d.x;
				},
				y: function (d) {
					return d.y;
				},
				xAxis: {
					tickFormat: function (d) {
						return dateToJson(new Date(d));
					},
					axisLabelDistance: 30
				},
				yAxis: {
					showMaxMin: true
				},
				lines: {
					forceY: [0]
				}
			}
		}
	};
	$scope.currencies = [];
	$scope.reportCompleted = false;
	TagsService.update().then(function () {
		$scope.tags = {};
		TagsService.tags.forEach(function (tag) {
			$scope.tags[tag] = true;
		});
	});
	$scope.updateAccounts = function () {
		$scope.accounts = {};
		AccountsService.accounts.forEach(function (account) {
			$scope.accounts[account.id] = true;
		});
	};
	$scope.selectAllTags = function () {
		for (var tag in $scope.tags)
			$scope.tags[tag] = true;
	};
	$scope.deselectAllTags = function () {
		for (var tag in $scope.tags)
			$scope.tags[tag] = false;
	};
	$scope.selectAllAccounts = function () {
		for (var account in $scope.accounts)
			$scope.accounts[account] = true;
	};
	$scope.deselectAllAccounts = function () {
		for (var account in $scope.accounts)
			$scope.accounts[account] = false;
	};
	$scope.openStartDateCalendar = function ($event) {
		$event.preventDefault();
		$event.stopPropagation();
		$scope.startDateCalendarOpened = true;
	};
	$scope.openEndDateCalendar = function ($event) {
		$event.preventDefault();
		$event.stopPropagation();
		$scope.endDateCalendarOpened = true;
	};
	$scope.buildReport = function () {
		var reportConfiguration = {
			earliestDate: dateToJson($scope.startDate),
			latestDate: dateToJson($scope.endDate),
			enabledTransferTransactions: $scope.transactionTypeEnabled.transfer,
			enabledIncomeTransactions: $scope.transactionTypeEnabled.income,
			enabledExpenseTransactions: $scope.transactionTypeEnabled.expense
		};
		reportConfiguration.selectedTags = [];
		for (var tag in $scope.tags)
			if ($scope.tags[tag])
				reportConfiguration.selectedTags.unshift(tag);
		reportConfiguration.selectedAccounts = [];
		for (var accountId in $scope.accounts) {
			if ($scope.accounts[accountId]) {
				var account = AccountsService.getAccount(Number(accountId));
				if (account !== undefined)
					reportConfiguration.selectedAccounts.unshift(account);
			}
		}
		HTTPService.post("service/analytics", reportConfiguration).then(function (data) {
			$scope.report = data.data;
			updateCurrencies();
			updateTagsChart();
			updateBalanceChart();
		});
	};
	var updateCurrencies = function () {
		var newCurrencies = {};
		$scope.report.tagExpenses.forEach(function (tagExpense) {
			for (var currency in tagExpense.amounts)
				if (newCurrencies[currency] === undefined)
					newCurrencies[currency] = currency;
		});
		var newCurrenciesList = [];
		for (var currency in newCurrencies)
			newCurrenciesList.unshift(currency);
		$scope.report.currencies = newCurrenciesList;
		if (newCurrenciesList.indexOf(UserService.userData.defaultCurrency) !== -1)
			$scope.report.selectedCurrency = UserService.userData.defaultCurrency;
		else
			$scope.report.selectedCurrency = newCurrenciesList[0];
	};
	$scope.currencyChanged = function () {
		updateTagsChart();
		updateBalanceChart();
	};
	var updateTagsChart = function () {
		var newChartData = [];
		var allZero = true;
		$scope.report.tagExpenses.forEach(function (tagExpense) {
			var amount = tagExpense.amounts[$scope.report.selectedCurrency] !== undefined ? tagExpense.amounts[$scope.report.selectedCurrency] : 0;
			if (amount !== 0)
				allZero = false;
			var chartEntry = {
				tag: tagExpense.tag, amount: amount
			};
			newChartData.push(chartEntry);
		});
		if (allZero)
			$scope.tagsChart.data = [];
		else
			$scope.tagsChart.data = newChartData;
		$scope.$applyAsync(function () {
			$scope.tagsChart.options.chart.width = $("#tagsChartContainer").width();
		});//FIXME: remove this workaround after angular-nvd3 is fixed
	};
	var updateBalanceChart = function () {
		var newChartData = [];
		var accountGraph = $scope.report.accountsBalanceGraph[$scope.report.selectedCurrency];
		if (accountGraph !== undefined)
			for (var date in accountGraph.data) {
				var entry = {x: new Date(date), y: accountGraph.data[date]};
				newChartData.push(entry);
			}
		if (accountGraph === undefined || Object.keys(accountGraph.data).length <= 0)
			$scope.balanceChart.data = [];
		else
			$scope.balanceChart.data = [{
					key: messages.BALANCE,
					values: newChartData
				}];
		$scope.$applyAsync(function () {
			$scope.balanceChart.options.chart.width = $("#balanceChartContainer").width();
		});//FIXME: remove this workaround after angular-nvd3 is fixed
	};
	$scope.filterCurrency = function (currency) {
		return $scope.report !== undefined && $scope.report.currencies.indexOf(currency.symbol) !== -1;
	};
	$scope.close = function () {
		$modalInstance.dismiss();
	};
	$scope.updateAccounts();
	$scope.accountListener = $scope.$watch(function () {
		return AccountsService.accounts;
	}, $scope.updateAccounts);
	$modalInstance.result.then($scope.accountListener, $scope.accountListener);
});

app.controller("UserSettingsController", function ($scope, $modalInstance, AuthorizationService, UserService, CurrencyService, HTTPService) {
	$scope.userService = UserService;
	$scope.user = UserService.userData;
	$scope.currencies = CurrencyService;
	$scope.file = undefined;
	$scope.operationSuccessful = false;
	var importPostHeaders = {"Content-Type": undefined};
	$scope.submitEditing = function () {
		AuthorizationService.username = $scope.user.username;
		if ($scope.user.password !== undefined)
			AuthorizationService.password = $scope.user.password;
		UserService.submit($scope.user).then($scope.importData);
	};
	$scope.cancelEditing = function () {
		UserService.update();
		$modalInstance.dismiss();
	};
	$scope.setFile = function (file) {
		$scope.$apply(function () {
			$scope.file = file.files[0];
		});
	};
	$scope.importData = function () {
		if ($scope.file === undefined) {
			$modalInstance.dismiss();
			return;
		}
		var formData = new FormData();
		formData.append("file", $scope.file);
		return HTTPService.post("service/import", formData, importPostHeaders, undefined, angular.identity).then(function () {
			$modalInstance.dismiss();
			HTTPService.updateAllData();
		});
	};
	$scope.exportData = function () {
		var form = $('<form>', {
			html: '<input type="hidden" name="access_token" value="' + AuthorizationService.access_token + '" />',
			action: "service/export",
			method: "post"
		});
		form.appendTo(document.body).submit().remove();
	};
	$scope.performCleanup = function () {
		$scope.operationSuccessful = false;
		$scope.userService.performCleanup().then(function () {
			$scope.operationSuccessful = true;
		});
	};
	$scope.performRecalculateBalance = function () {
		$scope.operationSuccessful = false;
		$scope.userService.performRecalculateBalance().then(function () {
			$scope.operationSuccessful = true;
		});
	};
});

app.controller("AdminSettingsController", function ($scope, $modalInstance, HTTPService) {
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
		HTTPService.post("service/configuration", convertConfigurationForPost($scope.configuration)).then($modalInstance.close, $modalInstance.close);
	};
	$scope.cancelEditing = function () {
		$modalInstance.dismiss();
	};
});

app.controller("AuthController", function ($scope, $modal, AuthorizationService, UserService, HTTPService) {
	$scope.authorizationService = AuthorizationService;
	$scope.userService = UserService;
	$scope.httpService = HTTPService;
	$scope.logoutLocked = "!authorizationService.authorized";
	$scope.loginDialog = undefined;
	$scope.userSettingsDialog = undefined;
	$scope.analyticsDialog = undefined;
	$scope.adminSettingsDialog = undefined;
	$scope.logout = function () {
		AuthorizationService.logout();
	};
	var closeUserSettingsDialog = function () {
		if ($scope.userSettingsDialog !== undefined) {
			var deleteFunction = function () {
				$scope.userSettingsDialog = undefined;
			};
			$scope.userSettingsDialog.then(deleteFunction, deleteFunction);
		}
	};
	var closeAnalyticsDialog = function () {
		if ($scope.analyticsDialog !== undefined) {
			var deleteFunction = function () {
				$scope.analyticsDialog = undefined;
			};
			$scope.analyticsDialog.then(deleteFunction, deleteFunction);
		}
	};
	var closeAdminSettingsDialog = function () {
		if ($scope.adminSettingsDialog !== undefined) {
			var deleteFunction = function () {
				$scope.adminSettingsDialog = undefined;
			};
			$scope.adminSettingsDialog.then(deleteFunction, deleteFunction);
		}
	};
	$scope.showUserSettingsDialog = function () {
		closeUserSettingsDialog();
		$scope.userSettingsDialog = $modal.open({
			templateUrl: "userSettingsDialog",
			controller: "UserSettingsController"
		}).result.then(closeUserSettingsDialog, closeUserSettingsDialog);
	};
	$scope.showAnalyticsDialog = function () {
		closeAnalyticsDialog();
		$scope.userSettingsDialog = $modal.open({
			templateUrl: "analyticsDialog",
			controller: "AnalyticsController",
			size: "lg"
		}).result.then(closeAnalyticsDialog, closeAnalyticsDialog);
	};
	$scope.showAdminSettingsDialog = function () {
		closeAdminSettingsDialog();
		$scope.userSettingsDialog = $modal.open({
			templateUrl: "adminSettingsDialog",
			controller: "AdminSettingsController",
			size: "lg"
		}).result.then(closeAdminSettingsDialog, closeAdminSettingsDialog);
	};
	$scope.isAdmin = function () {
		return UserService.userData !== undefined && UserService.userData.authorities.some(function (authority) {
			return authority === "ROLE_VOGON_ADMIN";
		});
	};
});

app.service("TagsService", function ($q, AuthorizationService, HTTPService) {
	var that = this;
	this.tags = [];
	var convertTagsForAutocomplete = function (tags, query) {
		var tagsAutocomplete = [];
		tags.forEach(function (tag) {
			if (query === undefined || tag.toLowerCase().indexOf(query.toLowerCase()) >= 0)
				tagsAutocomplete.push({text: tag});
		});
		return tagsAutocomplete;
	};
	this.update = function () {
		if (AuthorizationService.authorized) {
			return HTTPService.get("service/analytics/tags", undefined, HTTPService.buildRequestParams(false)).then(function (data) {
				that.tags = data.data;
				convertTagsForAutocomplete(that.tags);
			});
		} else {
			var deferred = $q.defer();
			deferred.reject();
			return deferred.promise;
		}
	};
	this.mergeTags = function (newTags) {
		newTags.forEach(
				function (newTag) {
					if (!that.tags.some(function (tag) {
						return tag === newTag;
					}))
						that.tags.push(newTag);
				});
	};
	this.autocompleteQuery = function (query) {
		var deferred = $q.defer();
		deferred.resolve(convertTagsForAutocomplete(that.tags, query));
		return deferred.promise;
	};
});

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
	}, that.update);
});

app.controller("AccountsEditorController", function ($scope, $modalInstance, AccountsService, CurrencyService) {
	$scope.accounts = AccountsService;
	$scope.currencies = CurrencyService;
	$scope.addAccount = function () {
		var account = {includeInTotal: true, showInList: true};
		AccountsService.accounts.unshift(account);
	};
	$scope.deleteAccount = function (account) {
		AccountsService.accounts = AccountsService.accounts.filter(function (comp) {
			return comp !== account;
		});
	};
	$scope.cancelEditing = function () {
		$modalInstance.dismiss();
	};
	$scope.submitEditing = function () {
		$modalInstance.close(AccountsService.accounts);
	};
});

app.controller("AccountsController", function ($scope, $modal, AuthorizationService, AccountsService, UserService) {
	$scope.authorizationService = AuthorizationService;
	$scope.accountService = AccountsService;
	$scope.editor = undefined;
	$scope.userService = UserService;
	$scope.editAccounts = function () {
		closeEditor();
		$scope.editor = $modal.open({
			templateUrl: "editAccountsDialog",
			controller: "AccountsEditorController",
			size: "lg"
		}).result.then(function (accounts) {
			AccountsService.submitAccounts(accounts);
		}, function () {
			AccountsService.update();
		});
	};
	var closeEditor = function () {
		if ($scope.editor !== undefined) {
			var deleteFunction = function () {
				$scope.editor = undefined;
			};
			$scope.editor.then(deleteFunction, deleteFunction);
		}
	};
});

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
			templateUrl: "editTransactionDialog",
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
