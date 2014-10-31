<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="org.zlogic.vogon.web.webmessages" />
<fmt:setBundle basename="org.zlogic.vogon.web.webjars" var="webjars"/>
<!DOCTYPE html>
<html>
	<head>
		<title><fmt:message key="VOGON_PAGE_TITLE"/></title>
		<link rel="stylesheet" type="text/css" href="webjars/bootstrap/<fmt:message key="bootstrap" bundle="${webjars}"/>/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="webjars/ng-tags-input/<fmt:message key="ngtagsinput" bundle="${webjars}"/>/ng-tags-input.min.css">
		<link rel="stylesheet" type="text/css" href="webjars/nvd3/<fmt:message key="nvd3" bundle="${webjars}"/>/nv.d3.min.css">
		<!--<link rel="stylesheet" type="text/css" href="webjars/bootstrap/<fmt:message key="bootstrap" bundle="${webjars}"/>/css/bootstrap-theme.min.css">-->
		<script type="text/javascript" src="webjars/jquery/<fmt:message key="jquery" bundle="${webjars}"/>/jquery.min.js"></script>
		<script type="text/javascript" src="webjars/angularjs/<fmt:message key="angularjs" bundle="${webjars}"/>/angular.min.js"></script>
		<script type="text/javascript" src="webjars/angularjs/<fmt:message key="angularjs" bundle="${webjars}"/>/angular-cookies.js"></script>
		<script type="text/javascript" src="webjars/angular-ui-bootstrap/<fmt:message key="angularuibootstrap" bundle="${webjars}"/>/ui-bootstrap-tpls.min.js"></script>
		<script type="text/javascript" src="webjars/ngInfiniteScroll/<fmt:message key="nginfinitescroll" bundle="${webjars}"/>/ng-infinite-scroll.min.js"></script>
		<script type="text/javascript" src="webjars/ng-tags-input/<fmt:message key="ngtagsinput" bundle="${webjars}"/>/ng-tags-input.min.js"></script>
		<script type="text/javascript" src="webjars/bootstrap/<fmt:message key="bootstrap" bundle="${webjars}"/>/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="webjars/d3js/<fmt:message key="d3js" bundle="${webjars}"/>/d3.min.js"></script>
		<script type="text/javascript" src="webjars/nvd3/<fmt:message key="nvd3" bundle="${webjars}"/>/nv.d3.min.js"></script>
		<script type="text/javascript" src="webjars/angularjs-nvd3-directives/<fmt:message key="angularjsnvd3directives" bundle="${webjars}"/>/angularjs-nvd3-directives.js"></script>
		<script type="text/javascript" src="script/messages.js"></script>
		<script type="text/javascript" src="script/main.js"></script>
		<link rel="stylesheet" type="text/css" href="css/style.css">
		<link rel="stylesheet" type="text/css" href="css/tags-bootstrap.css">
		<link rel="icon" type="image/png" href="images/vogon-favicon.png" />
	</head>
	<body ng-app="vogon">
		<div ng-controller="NotificationController">
			<div class="navbar-fixed-top alert-over-modal" ng-show="alertService.enabled()">
				<div class="alert alert-warning" role="alert" ng-show="httpService.isLoading"><span class="glyphicon glyphicon-refresh"></span> <fmt:message key="LOADING_ALERT"/></div>
				<alert ng-repeat="alert in alertService.alerts" type="{{alert.type}}" close="alertService.closeAlert($index)"><span class="glyphicon glyphicon-exclamation-sign"></span> {{alert.msg}}</alert>
			</div>
		</div>
		<div ng-controller="LoginController">
			<div class="container modal-dialog" ng-hide="authorizationService.authorized">
				<form class="form-inline" submit="login()">
					<div class="panel panel-default">
						<div class="panel-heading"><h3><fmt:message key="LOG_IN_TITLE"/></h3></div>
						<div class="panel-body">
							<input type="text" class="form-control" ng-model="authorizationService.username" ng-disabled="$eval(loginLocked)" placeholder="<fmt:message key="ENTER_USERNAME"/>" />
							<input type="password" class="form-control" ng-model="authorizationService.password" ng-disabled="$eval(loginLocked)" placeholder="<fmt:message key="ENTER_PASSWORD"/>" />
						</div>
						<div class="panel-body" ng-show="failed">
							<alert type="danger"><fmt:message key="LOGIN_FAILED"/></alert>
						</div>
						<div class="panel-footer">
							<button ng-click="login()" ng-disabled="$eval(loginLocked) || !authorizationService.username || !authorizationService.password" class="btn btn-primary"><span class="glyphicon glyphicon-log-in"></span> <fmt:message key="LOGIN"/></button>
						</div>
					</div>
				</form>
			</div>
		</div>
		<div ng-controller="AuthController" class="well well-sm" ng-show="authorizationService.authorized">
			<span class="control-label"><fmt:message key="WELCOME_MESSAGE"/> </span>
			<button ng-click="showUserSettingsDialog()" class="btn btn-default"><span class="glyphicon glyphicon-edit"></span> <fmt:message key="EDIT_SETTINGS"/></button>
			<button ng-click="showAnalyticsDialog()" class="btn btn-default"><span class="glyphicon glyphicon-stats"></span> <fmt:message key="SHOW_ANALYTICS"/></button>
			<button ng-click="logout()" ng-disabled="$eval(logoutLocked)" class="btn btn-default"><span class="glyphicon glyphicon-log-out"></span> <fmt:message key="LOGOUT"/></button>
		</div>
		<script type="text/ng-template" id="userSettingsDialog">
			<form name="userSettingsForm" novalidate>
				<div class="modal-header">
				<h3 class="modal-title"><fmt:message key="USER_SETTINGS_TITLE"/></h3>
				</div>
				<div class="modal-body">
					<div class="form-group" ng-class="{ 'has-error': userSettingsForm.username.$invalid }">
						<label><fmt:message key="USERNAME"/></label>
						<input type="text" name="username" class="form-control" ng-model="user.username" placeholder="<fmt:message key="ENTER_USERNAME"/>" required />
					</div>
					<div class="form-group">
						<label><fmt:message key="PASSWORD"/></label>
						<input type="password" class="form-control" ng-model="user.password" placeholder="<fmt:message key="ENTER_NEW_PASSWORD"/>" />
					</div>
					<div class="form-group" ng-class="{ 'has-error': userSettingsForm.currency.$invalid }">
						<label><fmt:message key="DEFAULT_CURRENCY"/></label>
						<select name="currency" ng-model="user.defaultCurrency" ng-options="currency.symbol as currency.displayName for currency in currencies.currencies" class="form-control" required></select>
					</div>
					<div class="form-group">
						<div class="form-inline">
							<button ng-click="importData()" ng-disabled="!file" class="btn btn-default"><span class="glyphicon glyphicon-import"></span> <fmt:message key="IMPORT_DATA"/></button>
							<input type="file" onchange="angular.element(this).scope().setFile(this)" class="form-control-file" />
						</div>
					</div>
					<div class="form-group">
						<button ng-click="exportData()" class="btn btn-default"><span class="glyphicon glyphicon-export"></span> <fmt:message key="EXPORT_DATA"/></button>
					</div>
					<div class="form-group">
						<div class="form-inline">
							<button ng-click="performRecalculateBalance()" class="btn btn-default"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="RECALCULATE_BALANCE"/></button>
							<button ng-click="performCleanup()" class="btn btn-default"><span class="glyphicon glyphicon-flash"></span> <fmt:message key="CLEANUP_DATABASE"/></button>
						</div>
					</div>
					<div class="form-group" ng-show="operationSuccessful">
						<alert type="success"><span class="glyphicon glyphicon-ok"></span> <fmt:message key="DONE_ALERT"/></alert>
					</div>
				</div>
				<div class="modal-footer">
					<button ng-click="cancelEditing()" class="btn btn-default"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="CANCEL"/></button>
					<button ng-click="submitEditing()" class="btn btn-primary" ng-disabled="userSettingsForm.$invalid"><span class="glyphicon glyphicon-ok"></span> <fmt:message key="APPLY"/></button>
				</div>
			</form>
		</script>
		<script type="text/ng-template" id="editTransactionDialog">
			<form name="transactionEditorForm" novalidate>
				<div class="modal-header">
					<h3 class="modal-title"><fmt:message key="EDIT_TRANSACTION_TITLE"/></h3>
				</div>
				<div class="modal-body">
					<div class="row form-control-static">
						<div class="form-inline col-md-12">
							<span ng-class="{ 'has-error': transactionEditorForm.description.$invalid }" >
								<input type="text" ng-model="transaction.description" class="form-control" placeholder="<fmt:message key="ENTER_TRANSACTION_DESCRIPTION"/>" name="description" required />
							</span>
							<select ng-model="transaction.type" ng-init="transaction.type = transaction.type || transactionTypes[0]" ng-options="transactionType.value as transactionType.name for transactionType in transactionTypes" class="form-control"></select>
							<div class="input-group">
								<input type="text" class="form-control" datepicker-popup ng-model="transaction.date" is-open="calendarOpened" />
								<span class="input-group-btn">
									<button type="button" class="btn btn-default" ng-click="openCalendar($event)"><span class="glyphicon glyphicon-calendar"></span></button>
								</span>
							</div>
							<tags-input class="bootstrap" ng-model="tags" placeholder="<fmt:message key="ADD_TAGS"/>" on-tag-added="syncTags()" on-tag-removed="syncTags()" replace-spaces-with-dashes="false" add-on-comma="false">
								<auto-complete source="tagsService.autocompleteQuery($query)"></auto-complete>
							</tags-input>
						</div>
					</div>
					<div class="row form-control-static">
						<div class="col-md-12">
							<button ng-click="addTransactionComponent()" class="btn btn-default"><span class="glyphicon glyphicon-plus"></span> <fmt:message key="ADD_COMPONENT"/></button>
						</div>
					</div>
					<div class="row form-control-static" ng-repeat="component in transaction.components">
						<div class="form-inline col-md-12">
							<ng-form name="transactionForm">
								<span ng-class="{ 'has-error': transactionForm.amount.$invalid }" >
									<input type="text" ng-model="component.amount" placeholder="<fmt:message key="ENTER_AMOUNT"/>" class="text-right form-control" name="amount" smart-float required/>
								</span>
								<span style="form-control">{{accountService.getAccount(component.accountId).currency}}</span>
								<span ng-class="{ 'has-error': transactionForm.account.$invalid }" >
									<select ng-model="component.accountId" ng-options="account.id as account.name for account in accountService.accounts | filter:isAccountVisible" class="form-control" name="account" required></select>
								</span>
								<button ng-click="deleteTransactionComponent(component)" class="btn btn-default"><span class="glyphicon glyphicon-trash"></span> <fmt:message key="DELETE"/></button>
							</ng-form>
						</div>
					</div>
				</div>
				<div class="modal-footer">
					<button ng-click="deleteTransaction()" class="btn btn-danger"><span class="glyphicon glyphicon-trash"></span> <fmt:message key="DELETE"/></button>
					<button ng-click="cancelEditing()" class="btn btn-default"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="CANCEL"/></button>
					<button ng-click="submitEditing()" class="btn btn-primary" ng-disabled="transactionEditorForm.$invalid"><span class="glyphicon glyphicon-ok"></span> <fmt:message key="APPLY"/></button>
				</div>
			</form>
		</script>
		<script type="text/ng-template" id="editAccountsDialog">
			<form name="accountEditorForm" novalidate>
				<div class="modal-header">
					<h3 class="modal-title"><fmt:message key="EDIT_ACCOUNTS_TITLE"/></h3>
				</div>
				<div class="modal-body">
					<div class="row form-control-static">
						<div class="col-md-12">
							<button ng-click="addAccount()" class="btn btn-default"><span class="glyphicon glyphicon-plus"></span> <fmt:message key="ADD_ACCOUNT"/></button>
						</div>
					</div>
					<div class="row form-control-static" ng-repeat="account in accounts.accounts | orderBy:'id'">
						<ng-form name="accountForm">
							<div class="form-inline col-md-9">
								<div class="row">
									<div class="form-inline col-md-12">
										<span ng-class="{ 'has-error': accountForm.accountName.$invalid }" >
											<input type="text" ng-model="account.name" placeholder="<fmt:message key="ENTER_ACCOUNT_NAME"/>" class="form-control" name="accountName" required/>
										</span>
										<span ng-class="{ 'has-error': accountForm.accountCurrency.$invalid }" >
											<select ng-model="account.currency" ng-options="currency.symbol as currency.displayName for currency in currencies.currencies" class="form-control" name="accountCurrency" required></select>
										</span>
									</div>
								</div>
								<div class="row">
									<div class="form-inline col-md-12">
										<label class="checkbox-inline">
											<input type="checkbox" ng-model="account.includeInTotal"/> <fmt:message key="INCLUDE_IN_TOTAL"/>
										</label>
										<label class="checkbox-inline">
											<input type="checkbox" ng-model="account.showInList"/> <fmt:message key="SHOW_IN_ACCOUNTS_LIST"/>
										</label>
									</div>
								</div>
							</div>
							<div class="form-inline col-md-3 text-right">
								<button ng-click="deleteAccount(account)" class="btn btn-default"><span class="glyphicon glyphicon-trash"></span> <fmt:message key="DELETE"/></button>
							</div>
						</ng-form>
					</div>
				</div>
				<div class="modal-footer">
					<button ng-click="cancelEditing()" class="btn btn-default"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="CANCEL"/></button>
					<button ng-click="submitEditing()" class="btn btn-primary" ng-disabled="accountEditorForm.$invalid"><span class="glyphicon glyphicon-ok"></span> <fmt:message key="APPLY"/></button>
				</div>
			</form>
		</script>
		<script type="text/ng-template" id="analyticsDialog">
			<div class="modal-header">
				<h3 class="modal-title"><fmt:message key="ANALYTICS_TITLE"/></h3>
			</div>
			<div class="modal-body">
				<div class="row">
					<div class="col-md-6">
						<div class="form-group">
							<label>Tags</label>
							<div class="form-group">
								<button ng-click="selectAllTags()" class="btn btn-default"><span class="glyphicon glyphicon-check"></span> <fmt:message key="SELECT_ALL_TAGS"/></button>
								<button ng-click="deselectAllTags()" class="btn btn-default"><span class="glyphicon glyphicon-unchecked"></span> <fmt:message key="DESELECT_ALL_TAGS"/></button>
							</div>
							<div class="pre-scrollable">
								<div class="checkbox" ng-repeat="(tag,selected) in tags | orderBy:'tag'">
									<label>
										<input type="checkbox" ng-model="tags[tag]" /> {{tag.length>0?tag:"&nbsp;"}}
									</label>
								</div>
							</div>
						</div>
					</div>
					<div class="col-md-6">
						<div class="form-group">
							<label>Accounts</label>
							<div class="form-group">
								<button ng-click="selectAllAccounts()" class="btn btn-default"><span class="glyphicon glyphicon-check"></span> <fmt:message key="SELECT_ALL_ACCOUNTS"/></button>
								<button ng-click="deselectAllAccounts()" class="btn btn-default"><span class="glyphicon glyphicon-unchecked"></span> <fmt:message key="DESELECT_ALL_ACCOUNTS"/></button>
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
						<label><fmt:message key="START_DATE"/></label>
						<div class="input-group">
							<input type="text" class="form-control" datepicker-popup ng-model="startDate" is-open="startDateCalendarOpened" />
							<span class="input-group-btn">
								<button type="button" class="btn btn-default" ng-click="openStartDateCalendar($event)"><span class="glyphicon glyphicon-calendar"></span></button>
							</span>
						</div>
					</div>
					<div class="col-md-6">
						<label><fmt:message key="END_DATE"/></label>
						<div class="input-group">
							<input type="text" class="form-control" datepicker-popup ng-model="endDate" is-open="endDateCalendarOpened" />
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
								<input type="checkbox" ng-model="transactionTypeEnabled.transfer"/> <fmt:message key="TRANSFER_TRANSACTIONS"/>
							</label>
						</div>
						<div class="checkbox">
							<label class="checkbox">
								<input type="checkbox" ng-model="transactionTypeEnabled.income"/> <fmt:message key="INCOME_TRANSACTIONS"/>
							</label>
						</div>
						<div class="checkbox">
							<label class="checkbox">
								<input type="checkbox" ng-model="transactionTypeEnabled.expense"/> <fmt:message key="EXPENSE_TRANSACTIONS"/>
							</label>
						</div>
					</div>
				</div>
				<div class="row form-control-static">
					<div class="col-md-12">
						<button ng-click="buildReport()" class="btn btn-default btn-primary form-control"><span class="glyphicon glyphicon-ok"></span> <fmt:message key="BUILD_REPORT"/></button>
					</div>
				</div>
				<div class="well well-sm form-control-static" ng-show="report">
					<div class="row">
						<div class="col-md-6">
							<label class="form-control-static"><fmt:message key="REPORT_BY_TRANSACTIONS"/></label>
							<div class="pre-scrollable">
								<table class="table table-hover">
									<thead>
										<tr>
											<th><fmt:message key="TRANSACTION"/></th>
											<th class="text-right"><fmt:message key="AMOUNT"/></th>
											<th><fmt:message key="DATE"/></th>
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
							<label class="form-control-static"><fmt:message key="REPORT_BY_TAGS"/></label>
							<div class="pre-scrollable">
								<table class="table table-hover">
									<thead>
										<tr>
											<th><fmt:message key="TAG"/></th>
											<th class="text-right"><fmt:message key="AMOUNT"/></th>
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
							<label><fmt:message key="SELECT_CHARTS_CURRENCY"/> </label>
							<select ng-model="report.selectedCurrency" ng-change="currencyChanged()" ng-options="currency.symbol as currency.displayName for currency in currencyService.currencies|filter:filterCurrency" class="form-control"></select>
						</div>
					</div>
					<div class="row form-control-static">
						<div class="col-md-12">
							<label><fmt:message key="TAGS_CHART"/></label>
							<nvd3-pie-chart id="tags-chart" class="form-control-static" data="tagsChartData" height="400" showLabels="false" donut="true" tooltips="true" tooltipcontent="tagsChartToolTipContentFunction()" donutLabelsOutside="true" showLegend="true">
								<svg></svg>
							</nvd3-pie-chart>
						</div>
					</div>
					<div class="row form-control-static">
						<div class="col-md-12">
							<label><fmt:message key="BALANCE_CHART"/></label>
							<nvd3-line-chart id="balanceChartId" data="balanceChartData" height="300" showXAxis="true" showYAxis="true" tooltips="true" xAxisTickFormat="balanceChartXTickFormat()" useInteractiveGuideline="true" yaxisshowmaxmin="true">
								<svg></svg>
							</nvd3-line-chart>
						</div>
					</div>
				</div>
			</div>
			<div class="modal-footer">
				<button ng-click="close()" class="btn btn-default"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="CLOSE"/></button>
			</div>
		</script>
		<div ng-controller="AccountsController">
			<div ng-show="authorizationService.authorized" class="panel panel-default">
				<div class="panel-heading"><fmt:message key="ACCOUNTS_LIST_TITLE"/></div>
				<div class="panel-body">
					<button ng-click="editAccounts()" class="btn btn-primary"><span class="glyphicon glyphicon-pencil"></span> <fmt:message key="EDIT_ACCOUNTS"/></button>
					<table class="table table-hover">
						<thead>
							<tr>
								<th><fmt:message key="ACCOUNT_NAME"/></th>
								<th class="text-right"><fmt:message key="BALANCE"/></th>
								<th><fmt:message key="CURRENCY"/></th>
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
								<td><fmt:message key="TOTAL_FOR_CURRENCY"/></td>
								<td class="text-right">{{data.total | number:2}}</td>
								<td>{{currency}}</td>
							</tr>
						</tfoot>
					</table>
				</div>
			</div>
		</div>
		<div ng-controller="TransactionsController">
			<div ng-show="authorizationService.authorized">
				<div class="panel panel-default">
					<div class="panel-heading"><fmt:message key="TRANSACTIONS_LIST_TITLE"/></div>
					<div class="panel-body">
						<button ng-click="addTransaction()" class="btn btn-primary"><span class="glyphicon glyphicon-plus"></span> <fmt:message key="ADD_TRANSACTION"/></button>
						<div infinite-scroll="transactionsService.nextPage()" infinite-scroll-disabled="transactionsService.loadingNextPage">
							<table class="table table-hover">
								<thead>
									<tr>
										<th>
											<div class="editable" ng-click="transactionsService.applySort('description')"><fmt:message key="TRANSACTION_NAME"/>
												<span ng-show="transactionsService.sortColumn==='description'" class="glyphicon glyphicon-sort-by-alphabet" ng-class="transactionsService.sortAsc?'glyphicon-sort-by-alphabet':'glyphicon-sort-by-alphabet-alt'"></span>
											</div>
										</th>
										<th width="20%">
											<div class="editable" ng-click="transactionsService.applySort('date')"><fmt:message key="DATE"/>
												<span ng-show="transactionsService.sortColumn==='date'" class="glyphicon glyphicon-sort-by-alphabet" ng-class="transactionsService.sortAsc?'glyphicon-sort-by-order':'glyphicon-sort-by-order-alt'"></span>
											</div>
										</th>
										<th width="20%"><fmt:message key="TAGS"/></th>
										<th class="text-right" width="10%">
											<div class="editable" ng-click="transactionsService.applySort('amount')"><fmt:message key="AMOUNT"/>
												<span ng-show="transactionsService.sortColumn==='amount'" class="glyphicon glyphicon-sort-by-alphabet" ng-class="transactionsService.sortAsc?'glyphicon-sort-by-order':'glyphicon-sort-by-order-alt'"></span>
											</div>
										</th>
										<th width="10%"><fmt:message key="ACCOUNT"/></th>
										<th width="10%"></th>
									</tr>
								</thead>
								<tbody>
									<tr>
										<td>
											<div class="form-horizontal">
												<div class="input-group">
													<span class="input-group-addon"><span class="glyphicon glyphicon-filter"></span></span>
													<input type="text" class="form-control" placeholder="<fmt:message key="ENTER_DESCRIPTION_FILTER"/>" ng-model="transactionsService.filterDescription" ng-change="applyFilter()"/>
												</div>
											</div>
										</td>
										<td>
											<div class="form-horizontal">
												<div class="input-group">
													<span class="input-group-addon"><span class="glyphicon glyphicon-filter"></span></span>
													<input type="text" class="form-control" datepicker-popup ng-model="transactionsService.filterDate" ng-change="applyFilter()" is-open="filterDateCalendarOpened" placeholder="<fmt:message key="ENTER_DATE_FILTER"/>" />
													<span class="input-group-btn">
														<button type="button" class="btn btn-default" ng-click="openFilterDateCalendar($event)"><span class="glyphicon glyphicon-calendar"></span></button>
													</span>
												</div>
											</div>
										</td>
										<td>
											<div class="form-horizontal">
												<div class="input-group">
													<span class="input-group-addon"><span class="glyphicon glyphicon-filter"></span></span>
													<tags-input class="bootstrap" ng-model="transactionsService.filterTags" placeholder="<fmt:message key="ADD_FILTER_TAGS"/>" on-tag-added="applyFilter()" on-tag-removed="applyFilter()" replace-spaces-with-dashes="false" add-on-comma="false">
														<auto-complete source="tagsService.autocompleteQuery($query)"></auto-complete>
													</tags-input>
												</div>
											</div>
										</td>
										<td></td>
										<td></td>
										<td></td>
									</tr>
									<tr ng-repeat="transaction in transactionsService.transactions" ng-class="{danger:!transactionsService.isAmountOk(transaction)}">
										<td ng-click="startEditing(transaction)" class="editable">{{transaction.description}}</td>
										<td ng-click="startEditing(transaction)" class="editable">{{transaction.date | date}}</td>
										<td ng-click="startEditing(transaction)" class="editable">
											<div ng-repeat="tag in transaction.tags">
												{{tag}}{{$last ? "" : <fmt:message key="TAGS_SEPARATOR" />}}
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
												<span class="glyphicon glyphicon-chevron-down"></span>
												<div ng-repeat="account in accounts=(transactionsService.getAccounts(transaction,transactionsService.toAccountsPredicate))">
													{{$first && accounts.length>1?'(':''}}{{account.name}}{{$last ? '' : ', '}}{{$last && accounts.length>1?')':''}}
												</div>
											</div>
										</td>
										<td>
											<button ng-click="duplicateTransaction(transaction)" class="btn btn-default"><span class="glyphicon glyphicon-asterisk"></span> <fmt:message key="DUPLICATE"/></button>
										</td>
									</tr>
									<tr class="text-center" ng-show="transactionsService.loadingNextPage">
										<td colspan="6"><span class="glyphicon glyphicon-refresh"></span> <fmt:message key="LOADING_ALERT"/></td>
									</tr>
								</tbody>
							</table>
						</div>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>
