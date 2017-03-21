<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="core" uri="http://java.sun.com/jsp/jstl/core" %>
<fmt:setBundle basename="org.zlogic.vogon.web.webmessages" />
<fmt:setBundle basename="org.zlogic.vogon.web.webjars" var="webjars"/>
<!DOCTYPE html>
<html>
	<head>
		<title><fmt:message key="VOGON_PAGE_TITLE"/></title>
		<link rel="stylesheet" type="text/css" href="webjars/bootstrap/dist/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="webjars/ng-tags-input/ng-tags-input.min.css">
		<link rel="stylesheet" type="text/css" href="webjars/nvd3/nv.d3.min.css">
		<!--<link rel="stylesheet" type="text/css" href="webjars/bootstrap/bootstrap-theme.min.css">-->
		<script type="text/javascript" src="webjars/jquery/jquery.min.js"></script>
		<script type="text/javascript" src="webjars/angular/angular.min.js"></script>
		<script type="text/javascript" src="webjars/angular-cookies/angular-cookies.min.js"></script>
		<script type="text/javascript" src="webjars/angular-route/angular-route.min.js"></script>
		<script type="text/javascript" src="webjars/angular-bootstrap/ui-bootstrap-tpls.min.js"></script>
		<script type="text/javascript" src="webjars/ngInfiniteScroll/ng-infinite-scroll.min.js"></script>
		<script type="text/javascript" src="webjars/ng-tags-input/ng-tags-input.min.js"></script>
		<script type="text/javascript" src="webjars/bootstrap/bootstrap.min.js"></script>
		<script type="text/javascript" src="webjars/d3/d3.min.js"></script>
		<script type="text/javascript" src="webjars/nvd3/nv.d3.min.js"></script>
		<script type="text/javascript" src="webjars/angular-nvd3/angular-nvd3.min.js"></script>
		<script type="text/javascript" src="script/messages.js"></script>
		<script type="text/javascript" src="script/helpers.js"></script>
		<script type="text/javascript" src="script/main.js"></script>
		<script type="text/javascript" src="script/service/auth.js"></script>
		<script type="text/javascript" src="script/service/tags.js"></script>
		<script type="text/javascript" src="script/service/user.js"></script>
		<script type="text/javascript" src="script/service/accounts.js"></script>
		<script type="text/javascript" src="script/service/currency.js"></script>
		<script type="text/javascript" src="script/service/transactions.js"></script>
		<script type="text/javascript" src="script/fragments/usersettings.js"></script>
		<script type="text/javascript" src="script/fragments/transactioneditor.js"></script>
		<script type="text/javascript" src="script/fragments/analytics.js"></script>
		<script type="text/javascript" src="script/fragments/accounts.js"></script>
		<script type="text/javascript" src="script/fragments/transactions.js"></script>
		<link rel="stylesheet" type="text/css" href="css/style.css">
		<link rel="stylesheet" type="text/css" href="css/tags-bootstrap.css">
		<link rel="icon" type="image/png" href="images/vogon-favicon.png" />
		<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1"/>
	</head>
	<body ng-app="vogon">
		<div ng-controller="NotificationController">
			<div class="navbar-fixed-top">
				<div class="alert alert-warning" ng-show="alertService.isLoading"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span> <fmt:message key="LOADING_ALERT"/></div>
				<div uib-alert ng-show="alertService.enabled()" ng-repeat="alert in alertService.alerts" ng-class="alert.class" class="ng-hide" close="alertService.closeAlert($index)"><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span> {{alert.msg}}</div>
			</div>
		</div>
		<div ng-controller="UserController">
			<nav class="navbar navbar-default" ng-show="authorizationService.authorized">
				<div class="container-fluid">
					<!-- Brand and toggle get grouped for better mobile display -->
					<div class="navbar-header">
						<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbarMain" aria-expanded="false">
							<span class="sr-only"><fmt:message key="TOGGLE_NAVIGATION"/></span>
							<span class="icon-bar"></span>
							<span class="icon-bar"></span>
							<span class="icon-bar"></span>
						</button>
						<div class="navbar-brand"><fmt:message key="VOGON_NAME"/></div>
					</div>
					<div class="collapse navbar-collapse" id="navbarMain">
						<ul class="nav navbar-nav">
							<li ng-class="{active: isActivePath('transactions')}"><a href="#!/transactions"><span class="glyphicon glyphicon-list" aria-hidden="true"></span> <fmt:message key="TRANSACTIONS"/></a></li>
							<li ng-class="{active: isActivePath('accounts')}"><a href="#!/accounts"><span class="glyphicon glyphicon-piggy-bank" aria-hidden="true"></span> <fmt:message key="ACCOUNTS"/></a></li>
							<li ng-class="{active: isActivePath('analytics')}"><a href="#!/analytics"><span class="glyphicon glyphicon-list" aria-hidden="true"></span> <fmt:message key="ANALYTICS"/></a></li>
							<li ng-class="{active: isActivePath('usersettings')}"><a href="#!/usersettings"><span class="glyphicon glyphicon-edit" aria-hidden="true"></span> <fmt:message key="USER_SETTINGS"/></a></li>
						</ul>
						<div class="nav navbar-right">
							<div class="navbar-text"><fmt:message key="SIGNED_IN_AS"/></div>
							<button ng-click="logout()" ng-disabled="$eval(logoutLocked)" class="btn btn-default navbar-btn"><span class="glyphicon glyphicon-log-out" aria-hidden="true"></span> <fmt:message key="LOGOUT"/></button>
						</div>
					</div>
				</div>
			</nav>
		</div>
		<div ng-controller="LoginController">
			<div class="container-fluid form-control-static" ng-hide="authorizationService.authorized" ng-init="selectedTab = 'login'">
				<div class="row">
					<div class="col-md-12">
						<form>
							<div class="panel panel-default">
								<div class="panel-heading"><h3><fmt:message key="AUTHORIZATION_TITLE"/></h3></div>
								<div class="panel-body">
									<core:if test="${configuration.allowRegistration}">
										<ul class="nav nav-pills" role="tablist">
											<li ng-class="{active:selectedTab === 'login'}"><a href ng-click="selectedTab = 'login'"><fmt:message key="LOGIN"/></a></li>
											<li ng-class="{active:selectedTab === 'register'}"><a href ng-click="selectedTab = 'register'"><fmt:message key="REGISTER"/></a></li>
										</ul>
									</core:if>
									<div class="media">
										<div class="form-inline">
											<div class="form-group">
												<input type="text" class="form-control" ng-model="authorizationService.username" ng-disabled="$eval(loginLocked)" placeholder="<fmt:message key="ENTER_USERNAME"/>" />
											</div>
											<div class="form-group">
												<input type="password" class="form-control" ng-model="authorizationService.password" ng-disabled="$eval(loginLocked)" placeholder="<fmt:message key="ENTER_PASSWORD"/>" />
											</div>
											<div class="form-group">
												<div class="checkbox"><label><input type="checkbox" ng-model="authorizationService.rememberToken"> <fmt:message key="REMEMBER_TOKEN"/></label></div>
											</div>
										</div>
									</div>
								</div>
								<div class="panel-body" ng-show="loginError || registrationError">
									<div uib-alert class="alert-danger ng-hide" ng-show="loginError"><fmt:message key="LOGIN_FAILED"/>: {{loginError}}</div>
									<div uib-alert class="alert-danger ng-hide" ng-show="registrationError"><fmt:message key="REGISTRATION_FAILED"/>: {{registrationError}}</div>
								</div>
								<div class="panel-footer">
									<div class="text-right">
										<button class="btn btn-default" type="button" data-toggle="collapse" data-target="#collapseIntro" aria-expanded="false" aria-controls="collapseIntro"><span class="glyphicon glyphicon-question-sign" aria-hidden="true"></span> <fmt:message key="HELP_INFO"/></button>
										<button ng-click="doSelectedAction(selectedTab)" ng-disabled="$eval(loginLocked) || !authorizationService.username || !authorizationService.password" class="btn btn-primary" type="submit">
											<span class="glyphicon" ng-class="{'glyphicon-log-in':selectedTab === 'login', 'glyphicon-send':selectedTab === 'register'}" aria-hidden="true"></span>
											<span ng-show="selectedTab === 'login'"><fmt:message key="LOGIN"/></span>
											<span ng-show="selectedTab === 'register'"><fmt:message key="REGISTER"/></span>
										</button>
									</div>
									<div class="collapse" id="collapseIntro" ng-include="'fragments/intro.fragment'"></div>
								</div>
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>
		<div ng-controller="ContentController">
			<div ng-show="authorizationService.authorized">
				<div ng-view></div>
			</div>
		</div>
	</body>
</html>
