<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="org.zlogic.vogon.web.webmessages" />
<form name="introForm">
	<div class="modal-header">
		<h3 class="modal-title"><fmt:message key="VOGON_INTRODUCTION"/></h3>
	</div>
	<div class="modal-body">
		<jsp:include page="intro/default.jsp" />
	</div>
	<div class="modal-footer">
		<button ng-click="$close()" class="btn btn-default" type="default"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> <fmt:message key="CLOSE"/></button>
	</div>
</form>