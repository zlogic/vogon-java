<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="org.zlogic.vogon.web.webmessages" />
<form name="adminSettingsForm">
	<div class="modal-header">
		<h3 class="modal-title"><fmt:message key="ADMINISTRATIVE_SETTINGS_TITLE"/></h3>
	</div>
	<div class="modal-body">
		<label class="checkbox-inline">
			<input type="checkbox" ng-model="configuration['AllowRegistration']"/> <fmt:message key="ALLOW_REGISTRATION"/>
		</label>
	</div>
	<div class="modal-footer">
		<button ng-click="cancelEditing()" class="btn btn-default" type="button"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> <fmt:message key="CANCEL"/></button>
		<button ng-click="submitEditing()" class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span> <fmt:message key="APPLY"/></button>
	</div>
</form>
