<%@ page session="false" %><!DOCTYPE html>
<html>
	<head>
		<title>Vogon finance tracker</title>
		<link rel="stylesheet" type="text/css" href="webjars/bootstrap/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("bootstrap") %>/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="webjars/nvd3/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("nvd3") %>/nv.d3.min.css">
		<!--<link rel="stylesheet" type="text/css" href="webjars/bootstrap/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("bootstrap") %>/css/bootstrap-theme.min.css">-->
		<script type="text/javascript" src="webjars/angularjs/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("angularjs") %>/angular.min.js"></script>
		<script type="text/javascript" src="webjars/angularjs/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("angularjs") %>/angular-cookies.js"></script>
		<script type="text/javascript" src="webjars/angular-ui-bootstrap/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("angularuibootstrap") %>/ui-bootstrap-tpls.min.js"></script>
		<script type="text/javascript" src="webjars/jquery/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("jquery") %>/jquery.min.js"></script>
		<script type="text/javascript" src="webjars/bootstrap/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("bootstrap") %>/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="webjars/d3js/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("d3js") %>/d3.min.js"></script>
		<script type="text/javascript" src="webjars/nvd3/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("nvd3") %>/nv.d3.min.js"></script>
		<script type="text/javascript" src="webjars/angularjs-nvd3-directives/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("angularjsnvd3directives") %>/angularjs-nvd3-directives.js"></script>
		<script type="text/javascript" src="script/main.js"></script>
		<link rel="stylesheet" type="text/css" href="css/style.css">
		<link rel="icon" type="image/png" href="images/vogon-favicon.png" />
	</head>
	<body ng-app="vogon">
		<div ng-controller="AuthController" class="well well-sm" ng-show="authorizationService.authorized">
			<span class="control-label">Vogon for {{userService.userData.username}} </span>
			<button ng-click="showUserSettingsDialog()" class="btn btn-default"><span class="glyphicon glyphicon-edit"></span> Edit settings</button>
			<button ng-click="showAnalyticsDialog()" class="btn btn-default"><span class="glyphicon glyphicon-stats"></span> Show analytics</button>
			<button ng-click="logout()" ng-disabled="$eval(logoutLocked)" class="btn btn-default"><span class="glyphicon glyphicon-log-out"></span> Logout</button>
		</div>
		<script type="text/ng-template" id="loginDialog">
			<div class="modal-header">
				<h3 class="modal-title">Log in</h3>
			</div>
			<form class="form-inline" submit="login()">
				<div class="modal-body">
					<input type="text" class="form-control" ng-model="authorizationService.username" ng-disabled="$eval(loginLocked)" placeholder="Enter username"/>
					<input type="password" class="form-control" ng-model="authorizationService.password" ng-disabled="$eval(loginLocked)" placeholder="Enter password" />
				</div>
				<div class="modal-body" ng-show="failed">
					<alert type="danger">Login failed</div>
				</div>
				<div class="modal-footer">
					<button ng-click="login()" ng-disabled="$eval(loginLocked) || !authorizationService.username || !authorizationService.password" class="btn btn-primary"><span class="glyphicon glyphicon-log-in"></span> Login</button>
				</div>
			</form>
		</script>
		<script type="text/ng-template" id="userSettingsDialog">
			<div class="modal-header">
				<h3 class="modal-title">Settings for {{user.username}}</h3>
			</div>
			<div class="modal-body">
				<div class="form-group">
					<label>Username</label>
					<input type="text" id="username" class="form-control" ng-model="user.username" placeholder="Enter username"/>
				</div>
				<div class="form-group">
					<label>Password</label>
					<input type="password" class="form-control" ng-model="user.password" placeholder="Enter new password" />
				</div>
				<div class="form-group">
					<label>Default currency</label>
					<select ng-model="user.defaultCurrency" ng-options="currency.symbol as currency.displayName for currency in currencies.currencies" class="form-control"></select>
				</div>
				<div class="form-group">
					<div class="form-inline">
						<button ng-click="importData()" ng-disabled="!file" class="btn btn-default form-control"><span class="glyphicon glyphicon-import"></span> Import data</button>
						<input type="file" onchange="angular.element(this).scope().setFile(this)" class="form-control-file" />
					</div>
				</div>
				<div class="form-group">
					<button ng-click="exportData()" class="btn btn-default"><span class="glyphicon glyphicon-export"></span> Export data</button>
				</div>
			</div>
			<div class="modal-footer">
				<button ng-click="cancelEditing()" class="btn btn-default"><span class="glyphicon glyphicon-remove"></span> Cancel</button>
				<button ng-click="submitEditing()" class="btn btn-primary"><span class="glyphicon glyphicon-ok"></span> Apply</button>
			</div>
		</script>
		<script type="text/ng-template" id="editTransactionDialog">
			<div class="modal-header">
				<h3 class="modal-title">Edit transaction</h3>
			</div>
			<div class="modal-body">
				<div class="row form-control-static">
					<div class="form-inline col-md-12">
						<input type="text" ng-model="transaction.description" class="form-control" placeholder="Enter transaction description"/>
						<select ng-model="transaction.type" ng-init="transaction.type = transaction.type || transactionTypes[0]" ng-options="transactionType.value as transactionType.name for transactionType in transactionTypes" class="form-control"></select>
						<div class="input-group">
							<input type="text" class="form-control" datepicker-popup ng-model="transaction.date" is-open="$parent.calendarOpened" />
							<span class="input-group-btn">
								<button type="button" class="btn btn-default" ng-click="openCalendar($event)"><span class="glyphicon glyphicon-calendar"></span></button>
							</span>
						</div>
						<input type="text" ng-model="$parent.tags" ng-change="syncTags()" placeholder="Enter tags" class="form-control"/>
					</div>
				</div>
				<div class="row form-control-static">
					<div class="col-md-12">
						<button ng-click="addTransactionComponent()" class="btn btn-default"><span class="glyphicon glyphicon-plus"></span> Add component</button>
					</div>
				</div>
				<div class="row form-control-static" ng-repeat="component in transaction.components">
					<div class="form-inline col-md-12">
						<input type="text" ng-model="component.amount" placeholder="Enter amount" class="text-right form-control" smart-float/>
						<span style="form-control">{{accountService.getAccount(component.accountId).currency}}</span>
						<select ng-model="component.accountId" ng-options="account.id as account.name for account in accountService.accounts | filter:isAccountVisible" class="form-control"></select>
						<button ng-click="deleteTransactionComponent(component)" class="btn btn-default"><span class="glyphicon glyphicon-trash"></span> Delete</button>
					</div>
				</div>
			</div>
			<div class="modal-footer">
				<button ng-click="deleteTransaction()" class="btn btn-danger"><span class="glyphicon glyphicon-trash"></span> Delete</button>
				<button ng-click="cancelEditing()" class="btn btn-default"><span class="glyphicon glyphicon-remove"></span> Cancel</button>
				<button ng-click="submitEditing()" class="btn btn-primary"><span class="glyphicon glyphicon-ok"></span> Apply</button>
			</div>
		</script>
		<script type="text/ng-template" id="editAccountsDialog">
			<div class="modal-header">
				<h3 class="modal-title">Edit accounts</h3>
			</div>
			<div class="modal-body">
				<div class="row form-control-static">
					<div class="col-md-12">
						<button ng-click="addAccount()" class="btn btn-default"><span class="glyphicon glyphicon-plus"></span> Add account</button>
					</div>
				</div>
				<div class="row form-control-static" ng-repeat="account in accounts.accounts | orderBy:'id'">
					<div class="form-inline col-md-9">
						<div class="row">
							<div class="form-inline col-md-12">
								<input type="text" ng-model="account.name" placeholder="Enter account name" class="form-control"/>
								<select ng-model="account.currency" ng-options="currency.symbol as currency.displayName for currency in currencies.currencies" class="form-control"></select>
							</div>
						</div>
						<div class="row">
							<div class="form-inline col-md-12">
								<label class="checkbox-inline">
									<input type="checkbox" ng-model="account.includeInTotal" class=""/> Include in total
								</label>
								<label class="checkbox-inline">
									<input type="checkbox" ng-model="account.showInList" class=""/> Show in accounts list
								</label>
							</div>
						</div>
					</div>
					<div class="form-inline col-md-3 text-right">
						<button ng-click="deleteAccount(account)" class="btn btn-default"><span class="glyphicon glyphicon-trash"></span> Delete</button>
					</div>
				</div>
			</div>
			<div class="modal-footer">
				<button ng-click="cancelEditing()" class="btn btn-default"><span class="glyphicon glyphicon-remove"></span> Cancel</button>
				<button ng-click="submitEditing()" class="btn btn-primary"><span class="glyphicon glyphicon-ok"></span> Apply</button>
			</div>
		</script>
		<script type="text/ng-template" id="analyticsDialog">
			<div class="modal-header">
				<h3 class="modal-title">Vogon analytics</h3>
			</div>
			<div class="modal-body">
				<div class="row">
					<div class="col-md-6">
						<div class="form-group">
							<label>Tags</label>
							<div class="form-group">
								<button ng-click="selectAllTags()" class="btn btn-default"><span class="glyphicon glyphicon-check"></span> Select all tags</button>
								<button ng-click="deselectAllTags()" class="btn btn-default"><span class="glyphicon glyphicon-unchecked"></span> Deselect all tags</button>
							</div>
							<div class="pre-scrollable">
								<div class="checkbox" ng-repeat="(tag,selected) in tags | orderBy:'tag'">
									<label>
										<input type="checkbox" ng-model="selected" /> {{tag.length>0?tag:"&nbsp;"}}
									</label>
								</div>
							</div>
						</div>
					</div>
					<div class="col-md-6">
						<div class="form-group">
							<label>Accounts</label>
							<div class="form-group">
								<button ng-click="selectAllAccounts()" class="btn btn-default"><span class="glyphicon glyphicon-check"></span> Select all accounts</button>
								<button ng-click="deselectAllAccounts()" class="btn btn-default"><span class="glyphicon glyphicon-unchecked"></span> Deselect all accounts</button>
							</div>
							<div class="pre-scrollable">
								<div class="checkbox" ng-repeat="account in accountService.accounts">
									<label>
										<input type="checkbox" ng-model="accounts[account.id]" /> {{account.name}}
									</label>
								</div>
							</div>
						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-md-6">
						<label>Start date</label>
						<div class="input-group">
							<input type="text" class="form-control" datepicker-popup ng-model="$parent.startDate" is-open="$parent.startDateCalendarOpened" />
							<span class="input-group-btn">
								<button type="button" class="btn btn-default" ng-click="openStartDateCalendar($event)"><span class="glyphicon glyphicon-calendar"></span></button>
							</span>
						</div>
					</div>
					<div class="col-md-6">
						<label>End date</label>
						<div class="input-group">
							<input type="text" class="form-control" datepicker-popup ng-model="$parent.endDate" is-open="$parent.endDateCalendarOpened" />
							<span class="input-group-btn">
								<button type="button" class="btn btn-default" ng-click="openEndDateCalendar($event)"><span class="glyphicon glyphicon-calendar"></span></button>
							</span>
						</div>
					</div>
				</div>
				<div class="row form-control-static">
					<div class="col-md-12">
						<div class="checkbox">
							<label class="checkbox">
								<input type="checkbox" ng-model="transactionTypeEnabled.transfer"/> Transfer transactions
							</label>
						</div>
						<div class="checkbox">
							<label class="checkbox">
								<input type="checkbox" ng-model="transactionTypeEnabled.income"/> Income transactions
							</label>
						</div>
						<div class="checkbox">
							<label class="checkbox">
								<input type="checkbox" ng-model="transactionTypeEnabled.expense"/> Expense transactions
							</label>
						</div>
					</div>
				</div>
				<div class="row form-control-static">
					<div class="col-md-12">
						<button ng-click="buildReport()" class="btn btn-default btn-primary form-control"><span class="glyphicon glyphicon-ok"></span> Build report</button>
					</div>
				</div>
				<div class="well well-sm form-control-static" ng-show="report">
					<div class="row">
						<div class="col-md-6">
							<label class="form-control-static">Report by transactions</label>
							<div class="pre-scrollable">
								<table class="table table-hover">
									<thead>
										<tr>
											<th>Transaction</th>
											<th class="text-right">Amount</th>
											<th>Date</th>
										</tr>
									</thead>
									<tbody>
										<tr ng-repeat="transaction in report.transactions">
											<td>{{transaction.description}}</td>
											<td class="text-right">
												<div ng-repeat="(symbol,total) in totals=(transactionsService.totalsByCurrency(transaction))">
													<span ng-show="transactionsService.isTransferTransaction(transaction)">
														&sum;
													</span>
													{{total | number:2}} {{symbol}}
												</div>
											</td>
											<td>{{transaction.date | date}}</td>
										</tr>
									</tbody>
								</table>
							</div>
						</div>
						<div class="col-md-6 form-group">
							<label class="form-control-static">Report by tags</label>
							<div class="pre-scrollable">
								<table class="table table-hover">
									<thead>
										<tr>
											<th>Tag</th>
											<th class="text-right">Amount</th>
										</tr>
									</thead>
									<tbody>
										<tr ng-repeat="tagExpense in report.tagExpenses">
											<td>{{tagExpense.tag}}</td>
											<td class="text-right">
												<div ng-repeat="(symbol,total) in tagExpense.amounts">
													{{total | number:2}} {{symbol}}
												</div>
											</td>
										</tr>
									</tbody>
								</table>
							</div>
						</div>
					</div>
					<div class="row form-control-static">
						<div class="col-md-12 form-inline">
							<label>Select charts currency: </label>
							<select ng-model="report.selectedCurrency" ng-change="currencyChanged()" ng-options="currency.symbol as currency.displayName for currency in currencyService.currencies|filter:filterCurrency" class="form-control"></select>
						</div>
					</div>
					<div class="row form-control-static">
						<div class="col-md-12">
							<label>Tags chart</label>
							<nvd3-pie-chart class="form-control-static" data="tagsChartData" height="300" showLabels="false" donut="true" tooltips="true" tooltipcontent="tagsChartToolTipContentFunction()" donutLabelsOutside="true" showLegend="true">
								<svg></svg>
							</nvd3-pie-chart>
						</div>
					</div>
					<div class="row form-control-static">
						<div class="col-md-12">
							<label>Balance chart</label>
							<nvd3-line-chart id="balanceChartId" data="balanceChartData" height="300" showXAxis="true" showYAxis="true" tooltips="true" xAxisTickFormat="balanceChartXTickFormat()" useInteractiveGuideline="true" yaxisshowmaxmin="true">
								<svg></svg>
							</nvd3-line-chart>
						</div>
					</div>
				</div>
			</div>
			<div class="modal-footer">
				<button ng-click="close()" class="btn btn-default"><span class="glyphicon glyphicon-remove"></span> Close</button>
			</div>
		</script>
		<div ng-controller="AccountsController">
			<div ng-show="authorizationService.authorized" class="panel panel-default">
				<div class="panel-heading">Accounts for {{userService.userData.username}}</div>
				<div class="panel-body">
					<button ng-click="editAccounts()" class="btn btn-primary"><span class="glyphicon glyphicon-pencil"></span> Edit accounts</button>
					<table class="table table-hover">
						<thead>
							<tr>
								<th>Account name</th>
								<th class="text-right">Balance</th>
								<th>Currency</th>
							</tr>
						</thead>
						<tbody>
							<tr ng-repeat="account in accountService.accounts | filter:{showInList:true} | orderBy:'id'">
								<td>{{account.name}}</td>
								<td class="text-right">{{account.balance | number:2}}</td>
								<td>{{account.currency}}</td>
							</tr>
						</tbody>
						<tfoot>
							<tr class="total-amount" ng-repeat="(currency,data) in accountService.totalsForCurrency | orderBy:'id'">
								<td>Total for {{data.name}}</td>
								<td class="text-right">{{data.total | number:2}}</td>
								<td>{{currency}}</td>
							</tr>
						</tfoot>
					</table>
				</div>
			</div>
		</div>
		<div ng-controller="NotificationController">
			<div class="navbar-fixed-top" ng-show="alertService.enabled()">
				<div class="alert alert-warning" role="alert" ng-show="httpService.isLoading"><span class="glyphicon glyphicon-refresh"></span> Loading...</div>
				<alert ng-repeat="alert in alertService.alerts" type="{{alert.type}}" close="alertService.closeAlert($index)"><span class="glyphicon glyphicon-exclamation-sign"></span> {{alert.msg}}</alert>
			</div>
		</div>
		<div ng-controller="TransactionsController">
			<div ng-show="authorizationService.authorized">
				<div class="panel panel-default">
					<div class="panel-heading">Transactions for {{userService.userData.username}}</div>
					<div class="panel-body">
						<button ng-click="addTransaction()" class="btn btn-primary"><span class="glyphicon glyphicon-plus"></span> Add transaction</button>
						<table class="table table-hover">
							<thead>
								<tr>
									<th>Transaction name</th>
									<th>Date</th>
									<th>Tags</th>
									<th class="text-right">Amount</th>
									<th>Account</th>
									<th></th>
								</tr>
							</thead>
							<tbody>
								<tr ng-repeat="transaction in transactionsService.transactions" ng-class="{danger:!transactionsService.isAmountOk(transaction)}">
									<td ng-click="startEditing(transaction)" class="editable">{{transaction.description}}</td>
									<td ng-click="startEditing(transaction)" class="editable">{{transaction.date | date}}</td>
									<td ng-click="startEditing(transaction)" class="editable">
										<div ng-repeat="tag in transaction.tags">
											{{tag}}{{$last ? '' : ', '}}
										</div>
									</td>
									<td ng-click="startEditing(transaction)" class="text-right editable">
										<div ng-repeat="(symbol,total) in totals=(transactionsService.totalsByCurrency(transaction))">
											<span ng-show="transactionsService.isTransferTransaction(transaction)">
												&sum;
											</span>
											{{total | number:2}} {{symbol}}
										</div>
									</td>
									<td ng-click="startEditing(transaction)" class="editable">
										<div ng-show="transactionsService.isExpenseIncomeTransaction(transaction)">
											<div ng-repeat="account in accounts=(transactionsService.getAccounts(transaction,transactionsService.allAccountsPredicate))">
												{{account.name}}{{$last ? '' : ', '}}
											</div>
										</div>
										<div ng-show="transactionsService.isTransferTransaction(transaction)">
											<div ng-repeat="account in accounts=(transactionsService.getAccounts(transaction,transactionsService.fromAccountsPredicate))">
												{{$first && accounts.length>1?'(':''}}{{account.name}}{{$last ? '' : ', '}}{{$last && accounts.length>1?')':''}}
											</div>
											<span class="glyphicon glyphicon-arrow-down"></span>
											<div ng-repeat="account in accounts=(transactionsService.getAccounts(transaction,transactionsService.toAccountsPredicate))">
												{{$first && accounts.length>1?'(':''}}{{account.name}}{{$last ? '' : ', '}}{{$last && accounts.length>1?')':''}}
											</div>
										</div>
									</td>
									<td>
										<button ng-click="duplicateTransaction(transaction)" class="btn btn-default"><span class="glyphicon glyphicon-asterisk"></span> Duplicate</button>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
					<div class="panel-footer">
						<pagination total-items="transactionsService.totalPages" items-per-page="1" boundary-links="true" ng-model="transactionsService.currentPage" ng-change="pageChanged()">
						</pagination>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>
