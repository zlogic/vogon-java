<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="org.zlogic.vogon.web.webmessages" />
<form name="introForm">
	<div class="panel panel-default">
		<div class="panel-heading">
			<h3><fmt:message key="VOGON_INTRODUCTION"/></h3>
		</div>
		<div class="panel-body">
			<jsp:include page="intro/default.jsp" />
		</div>
		<div class="panel-footer">
			<button ng-click="navigationService.navigateBack()" class="btn btn-default" type="default"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> <fmt:message key="CLOSE"/></button>
		</div>
	</div>
</form>