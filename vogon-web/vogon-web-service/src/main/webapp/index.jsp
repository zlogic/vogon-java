<!DOCTYPE html>
<html>
	<head>
		<title>Hello World</title>
		<link rel="stylesheet" type="text/css" href="webjars/bootstrap/<%= org.zlogic.vogon.web.WebProperties.getProperty("bootstrap") %>/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="webjars/bootstrap/<%= org.zlogic.vogon.web.WebProperties.getProperty("bootstrap") %>/css/bootstrap-theme.min.css">
		<script type="text/javascript" src="webjars/angularjs/<%= org.zlogic.vogon.web.WebProperties.getProperty("angularjs") %>/angular.min.js"></script>
		<script type="text/javascript" src="webjars/angular-ui-bootstrap/<%= org.zlogic.vogon.web.WebProperties.getProperty("angularuibootstrap") %>/ui-bootstrap-tpls.min.js"></script>
		<script type="text/javascript" src="webjars/jquery/<%= org.zlogic.vogon.web.WebProperties.getProperty("jquery") %>/jquery.min.js"></script>
		<script type="text/javascript" src="webjars/bootstrap/<%= org.zlogic.vogon.web.WebProperties.getProperty("bootstrap") %>/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="script/main.js"></script>
	</head>
	<body ng-app="vogon">
		<div ng-controller="AuthController">
			<div ng-hide="authorization.authorized">
				Enter username: <input type="text" ng-model="username" />
				<button ng-click="login(username)" class="btn btn-default">Login</button>
			</div>
			<div ng-show="authorization.authorized">
				Vogon for {{authorization.username}}
				<button ng-click="logout()" class="btn btn-default">Logout</button>
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
						<div class="container">
							<div class="row" ng-repeat="transaction in transactions">
								<div class="col-md-8">{{transaction.description}}</div>
								<div class="col-md-4 text-right">{{transaction.amount| number:2}}</div>
							</div>
						</div>
					</div>
					<div class="panel-footer">
						<pagination total-items="totalItems" items-per-page="itemsPerPage" boundary-links="true" ng-model="currentPage" ng-change="pageChanged()">
						</pagination>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>
