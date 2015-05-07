<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="org.zlogic.vogon.web.webmessages" />
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