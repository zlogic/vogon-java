app.controller("AnalyticsController", function ($scope, AccountsService, TransactionsService, CurrencyService, HTTPService, UserService, TagsService) {
	$scope.accountService = AccountsService;
	$scope.transactionsService = TransactionsService;
	$scope.currencyService = CurrencyService;
	$scope.tags = {};
	$scope.accounts = {};
	var currentTime = new Date();
	$scope.dateRange = {
		start: dateToJson(new Date(currentTime.getFullYear(), currentTime.getMonth(), 1)),
		end: dateToJson(new Date((new Date(currentTime.getFullYear(), currentTime.getMonth() + 1, 1)) - 1))
	};
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
			earliestDate: dateToJson($scope.dateRange.start),
			latestDate: dateToJson($scope.dateRange.end),
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
		$scope.accountListener;
	};
	$scope.updateAccounts();
	$scope.accountListener = $scope.$watch(function () {
		return AccountsService.accounts;
	}, $scope.updateAccounts);
});