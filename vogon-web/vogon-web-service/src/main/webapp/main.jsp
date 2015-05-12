<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="core" uri="http://java.sun.com/jsp/jstl/core" %>
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
		<script type="text/javascript" src="webjars/angular-nvd3/<fmt:message key="angularnvd3" bundle="${webjars}"/>/angular-nvd3.min.js"></script>
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
		<script type="text/javascript" src="script/fragments/accounteditor.js"></script>
		<script type="text/javascript" src="script/fragments/transactioneditor.js"></script>
		<script type="text/javascript" src="script/fragments/analytics.js"></script>
		<script type="text/javascript" src="script/fragments/adminsettings.js"></script>
		<script type="text/javascript" src="script/fragments/accounts.js"></script>
		<script type="text/javascript" src="script/fragments/transactions.js"></script>
		<link rel="stylesheet" type="text/css" href="css/style.css">
		<link rel="stylesheet" type="text/css" href="css/tags-bootstrap.css">
		<link rel="icon" type="image/png" href="images/vogon-favicon.png" />
	</head>
	<body ng-app="vogon">
		<div ng-controller="NotificationController">
			<div class="navbar-fixed-top alert-over-modal">
				<div class="alert alert-warning" role="alert" ng-show="alertService.isLoading"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span> <fmt:message key="LOADING_ALERT"/></div>
				<alert ng-show="alertService.enabled()" ng-repeat="alert in alertService.alerts" type="{{alert.type}}" close="alertService.closeAlert($index)"><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span> {{alert.msg}}</alert>
			</div>
		</div>
		<div ng-controller="LoginController">
			<div class="container modal-dialog" ng-hide="authorizationService.authorized" ng-init="selectedTab = 'login'">
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
									<input type="text" class="form-control" ng-model="authorizationService.username" ng-disabled="$eval(loginLocked)" placeholder="<fmt:message key="ENTER_USERNAME"/>" />
									<input type="password" class="form-control" ng-model="authorizationService.password" ng-disabled="$eval(loginLocked)" placeholder="<fmt:message key="ENTER_PASSWORD"/>" />
									<div class="checkbox"><label><input type="checkbox" ng-model="authorizationService.rememberToken"> <fmt:message key="REMEMBER_TOKEN"/></label></div>
								</div>
							</div>
						</div>
						<div class="panel-body" ng-show="loginError || registrationError">
							<alert type="danger" ng-show="loginError"><fmt:message key="LOGIN_FAILED"/>: {{loginError}}</alert>
							<alert type="danger" ng-show="registrationError"><fmt:message key="REGISTRATION_FAILED"/>: {{registrationError}}</alert>
						</div>
						<div class="panel-footer text-right">
							<button ng-click="showIntroDialog()" class="btn btn-default" type="button"><span class="glyphicon glyphicon-question-sign" aria-hidden="true"></span> <fmt:message key="HELP"/></button>
							<button ng-click="doSelectedAction(selectedTab)" ng-disabled="$eval(loginLocked) || !authorizationService.username || !authorizationService.password" class="btn btn-primary" type="submit">
								<span class="glyphicon" ng-class="{'glyphicon-log-in':selectedTab === 'login','glyphicon-send':selectedTab === 'register'}" aria-hidden="true"></span>
								<span ng-show="selectedTab === 'login'"><fmt:message key="LOGIN"/></span>
								<span ng-show="selectedTab === 'register'"><fmt:message key="REGISTER"/></span>
							</button>
						</div>
					</div>
				</form>
			</div>
		</div>
		<div ng-controller="UserController" class="well well-sm" ng-show="authorizationService.authorized">
			<span class="control-label"><fmt:message key="WELCOME_MESSAGE"/> </span>
			<button ng-click="showUserSettingsDialog()" class="btn btn-default"><span class="glyphicon glyphicon-edit" aria-hidden="true"></span> <fmt:message key="EDIT_SETTINGS"/></button>
			<button ng-click="showAnalyticsDialog()" class="btn btn-default"><span class="glyphicon glyphicon-stats" aria-hidden="true"></span> <fmt:message key="SHOW_ANALYTICS"/></button>
			<button ng-click="showAdminSettingsDialog()" ng-show="isAdmin()" class="btn btn-default"><span class="glyphicon glyphicon-wrench" aria-hidden="true"></span> <fmt:message key="ADMINISTRATIVE_SETTINGS"/></button>
			<button ng-click="logout()" ng-disabled="$eval(logoutLocked)" class="btn btn-default"><span class="glyphicon glyphicon-log-out" aria-hidden="true"></span> <fmt:message key="LOGOUT"/></button>
		</div>
		<div ng-controller="AccountsController" ng-include="'fragments/accounts.fragment'"></div>
		<div ng-controller="TransactionsController" ng-include="'fragments/transactions.fragment'"></div>
	</body>
</html>
