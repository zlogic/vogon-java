<!DOCTYPE html>
<html>
	<head>
		<title>Vogon finance tracker</title>
		<link rel="stylesheet" type="text/css" href="webjars/bootstrap/<%= org.zlogic.vogon.web.WebProperties.getProperty("bootstrap") %>/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="webjars/bootstrap/<%= org.zlogic.vogon.web.WebProperties.getProperty("bootstrap") %>/css/bootstrap-theme.min.css">
		<script type="text/javascript" src="webjars/angularjs/<%= org.zlogic.vogon.web.WebProperties.getProperty("angularjs") %>/angular.min.js"></script>
		<script type="text/javascript" src="webjars/angularjs/<%= org.zlogic.vogon.web.WebProperties.getProperty("angularjs") %>/angular-cookies.js"></script>
		<script type="text/javascript" src="webjars/angular-ui-bootstrap/<%= org.zlogic.vogon.web.WebProperties.getProperty("angularuibootstrap") %>/ui-bootstrap-tpls.min.js"></script>
		<script type="text/javascript" src="webjars/jquery/<%= org.zlogic.vogon.web.WebProperties.getProperty("jquery") %>/jquery.min.js"></script>
		<script type="text/javascript" src="webjars/bootstrap/<%= org.zlogic.vogon.web.WebProperties.getProperty("bootstrap") %>/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="script/main.js"></script>
	</head>
	<body ng-app="vogon">
		<div ng-controller="AuthController">
			<div ng-hide="authorization.authorized">
				<form>
					<input type="text" ng-model="username" placeholder="Enter username"/>
					<input type="password" ng-model="password" placeholder="Enter password" />
					<button ng-click="login()" class="btn btn-default">Login</button>
				</form>
			</div>
			<div ng-show="authorization.authorized">
				Vogon for {{authorization.username}}
				<button ng-click="logout()" class="btn btn-default">Logout</button>
			</div>
		</div>
		<div ng-controller="AccountsController">
			<div ng-show="authorization.authorized">
				<div class="panel panel-default">
					<div class="panel-heading">Accounts for {{authorization.username}}</div>
					<div class="panel-body">
						<table class="table">
							<thead>
								<tr>
									<th>Account name</th>
									<th class="text-right">Balance</th>
									<th>Currency</th>
								</tr>
							</thead>
							<tbody>
								<tr ng-repeat="account in accountService.accounts">
									<td>{{account.name}}</td>
									<td class="text-right">{{account.balance| number:2}}</td>
									<td>{{account.currency}}</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
		<div ng-controller="TransactionsController">
			<div ng-show="authorization.authorized">
				<div class="navbar-fixed-top">
					<div class="alert alert-warning" role="alert" ng-show="isLoading"><span class="glyphicon glyphicon-refresh"></span> Loading...</div>
				</div>
				<div class="panel panel-default">
					<div class="panel-heading">Transactions for {{authorization.username}}</div>
					<div class="panel-body">
						<button ng-click="addTransaction()" class="btn btn-default">Add transaction</button>
						<table class="table">
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
							<tbody ng-repeat-start="transaction in transactions" ng-repeat-end>
								<tr ng-hide="transaction.isEditing">
									<td ng-click="startEditing(transaction)">{{transaction.description}}</td>
									<td ng-click="startEditing(transaction)">{{transaction.date| date}}</td>
									<td ng-click="startEditing(transaction)">
										<div ng-repeat="tag in transaction.tags">
											{{tag}}{{$last ? '' : ', '}}
										</div>
									</td>
									<td ng-click="startEditing(transaction)" class="text-right">{{transaction.amount| number:2}}</td>
									<td ng-click="startEditing(transaction)">
										<div ng-repeat="component in transaction.components">
											{{accounts.getAccount(component.accountId).name}}{{$last ? '' : ', '}}
										</div>
									</td>
									<td>
										<button ng-click="duplicateTransaction(transaction)" class="btn btn-default">Duplicate</button>
									</td>
								</tr>
								<tr ng-show="transaction.isEditing">
									<td colspan="6">
										<div class="row">
											<div class="col-md-5">
												<input type="text" ng-model="transaction.description" placeholder="Enter transaction description"/>
											</div>
											<div class="col-md-2">
												<select ng-model="transaction.type" ng-init="transaction.type = transaction.type || transactionTypes[0]" ng-options="transactionType.value as transactionType.name for transactionType in transactionTypes"></select>
											</div>
											<div class="col-md-2">
												<input type="date" ng-model="transaction.date" />
											</div>
											<div class="col-md-2">
												<input type="text" ng-model="transaction.tags" placeholder="Enter tags" />
											</div>
											<div class="col-md-3 pull-right text-right">
												<button ng-click="submitEditing(transaction)" class="btn btn-default">Apply</button>
												<button ng-click="cancelEditing(transaction)" class="btn btn-default">Cancel</button>
												<button ng-click="deleteTransaction(transaction)" class="btn btn-default">Delete</button>
											</div>
										</div>
										<div class="row">
											<div class="col-md-6">
												<button ng-click="addTransactionComponent(transaction)" class="btn btn-default">Add component</button>
											</div>
										</div>
										<div class="row" ng-repeat="component in transaction.components">
											<div class="col-md-6">
												<input type="text" ng-model="component.amount" placeholder="Enter amount" class="text-right" smart-float/>
												<select ng-model="component.accountId" ng-options="account.id as account.name for account in accounts.accounts"></select>
												<button ng-click="deleteTransactionComponent(transaction, component)" class="btn btn-default">Delete</button>
											</div>
										</div>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
					<div class="panel-footer">
						<pagination total-items="totalPages" items-per-page="1" boundary-links="true" ng-model="currentPage" ng-change="pageChanged()">
						</pagination>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>
