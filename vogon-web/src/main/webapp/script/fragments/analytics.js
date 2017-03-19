app.controller("AnalyticsController", function ($scope, AccountsService, TransactionsService, CurrencyService, HTTPService, UserService, TagsService) {
	var that = this;
	$scope.accountService = AccountsService;
	$scope.transactionsService = TransactionsService;
	$scope.currencyService = CurrencyService;
	$scope.tags = [];
	$scope.accounts = {};
	$scope.startDateCalendar = {opened: false};
	$scope.endDateCalendar = {opened: false};
	var currentTime = new Date();
	$scope.dateRange = {
		start: new Date(currentTime.getFullYear(), currentTime.getMonth(), 1),
		end: new Date((new Date(currentTime.getFullYear(), currentTime.getMonth() + 1, 1)) - 1)
	};
	$scope.transactionTypeEnabled = {
		transfer: false,
		income: true,
		expense: true
	};
	this.report = undefined;
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
	$scope.report = undefined;
	$scope.updateTags = function () {
		$scope.tags = [];
		TagsService.tags.forEach(function (tag) {
			$scope.tags.push({tag: tag, selected: true});
		});
	};
	$scope.updateAccounts = function () {
		$scope.accounts = {};
		AccountsService.accounts.forEach(function (account) {
			$scope.accounts[account.id] = true;
		});
	};
	$scope.selectAllTags = function () {
		for (var tag in $scope.tags)
			$scope.tags[tag].selected = true;
	};
	$scope.deselectAllTags = function () {
		for (var tag in $scope.tags)
			$scope.tags[tag].selected = false;
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
		$scope.startDateCalendar.opened = true;
	};
	$scope.openEndDateCalendar = function ($event) {
		$event.preventDefault();
		$event.stopPropagation();
		$scope.endDateCalendar.opened = true;
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
			if ($scope.tags[tag].selected)
				reportConfiguration.selectedTags.push($scope.tags[tag].tag);
		reportConfiguration.selectedAccounts = [];
		for (var accountId in $scope.accounts) {
			if ($scope.accounts[accountId]) {
				reportConfiguration.selectedAccounts.push({id: accountId});
			}
		}
		HTTPService.post("service/analytics", reportConfiguration).then(function (data) {
			that.report = data.data;
			updateCurrencies();
			$scope.reportCompleted = true;
		});
	};
	var updateCurrencies = function () {
		$scope.currencies = Object.keys(that.report);
		$scope.report = $scope.currencies.length > 0 ? {selectedCurrency: $scope.currencies[0]} : {};
		$scope.currencyChanged();
	};
	$scope.currencyChanged = function () {
		var currency = $scope.report.selectedCurrency;
		$scope.report = (currency !== undefined) ? {
			transactions: that.report[currency].transactions,
			tagExpenses: that.report[currency].tagExpenses,
			accountsBalanceGraph: that.report[currency].accountsBalanceGraph,
			selectedCurrency: currency
		} : {};
		updateTagsChart();
		updateBalanceChart();
	};
	var updateTagsChart = function () {
		var newChartData = [];
		var allZero = true;
		if($scope.report.tagExpenses !== undefined)
			$scope.report.tagExpenses.forEach(function (tagExpense) {
				var amount = tagExpense.amount;
				if (amount !== 0) {
					allZero = false;
					newChartData.push(tagExpense);
				}
			});
		if (allZero)
			newChartData = [];
		$scope.tagsChart.data = newChartData;
	};
	var updateBalanceChart = function () {
		var newChartData = [];
		var accountGraph = $scope.report.accountsBalanceGraph;
		if (accountGraph !== undefined)
			for (var date in accountGraph)
				newChartData.push({x: new Date(date), y: accountGraph[date]});
		if (accountGraph === undefined || Object.keys(accountGraph).length <= 0)
			$scope.balanceChart.data = [];
		else
			$scope.balanceChart.data = [{
					key: messages.BALANCE,
					values: newChartData
				}];
	};
	$scope.filterCurrency = function (currency) {
		return that.report !== undefined && $scope.currencies.indexOf(currency.currencyCode) !== -1;
	};
	$scope.updateAccounts();
	$scope.$watch(function () {
		return AccountsService.accounts;
	}, function () {
		$scope.$applyAsync($scope.updateAccounts);
	});
	$scope.$watch(function () {
		return TagsService.tags;
	}, function () {
		$scope.$applyAsync($scope.updateTags);
	});
});
