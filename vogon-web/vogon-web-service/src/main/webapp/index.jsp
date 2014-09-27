<%@ page session="false" %><!DOCTYPE html>
<html>
	<head>
		<title>Vogon finance tracker</title>
		<link rel="stylesheet" type="text/css" href="webjars/bootstrap/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("bootstrap") %>/css/bootstrap.min.css">
		<!--<link rel="stylesheet" type="text/css" href="webjars/bootstrap/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("bootstrap") %>/css/bootstrap-theme.min.css">-->
		<script type="text/javascript" src="webjars/angularjs/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("angularjs") %>/angular.min.js"></script>
		<script type="text/javascript" src="webjars/angularjs/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("angularjs") %>/angular-cookies.js"></script>
		<script type="text/javascript" src="webjars/angular-ui-bootstrap/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("angularuibootstrap") %>/ui-bootstrap-tpls.min.js"></script>
		<script type="text/javascript" src="webjars/jquery/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("jquery") %>/jquery.min.js"></script>
		<script type="text/javascript" src="webjars/bootstrap/<%= org.zlogic.vogon.web.utils.WebProperties.getProperty("bootstrap") %>/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="script/main.js"></script>
		<link rel="stylesheet" type="text/css" href="css/style.css">
		<link rel="icon" type="image/png" href="images/vogon-favicon.png" />
	</head>
	<body ng-app="vogon">
		<div ng-controller="AuthController" class="well well-sm" ng-show="authorizationService.authorized">
			<span class="control-label">Vogon for {{userService.userData.username}} </span>
			<button ng-click="showUserSettingsDialog()" class="btn btn-default"><span class="glyphicon glyphicon-edit"></span> Edit settings</button>
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
