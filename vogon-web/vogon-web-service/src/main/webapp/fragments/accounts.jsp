<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="org.zlogic.vogon.web.webmessages" />
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
				<tr ng-repeat="account in accountService.accounts| filter:{showInList:true} | orderBy:'id'">
					<td>{{account.name}}</td>
					<td class="text-right">{{account.balance| number:2}}</td>
					<td>{{account.currency}}</td>
				</tr>
			</tbody>
			<tfoot>
				<tr class="total-amount" ng-repeat="(currency,data) in accountService.totalsForCurrency | orderBy:'id'">
					<td><fmt:message key="TOTAL_FOR_CURRENCY"/></td>
					<td class="text-right">{{data.total| number:2}}</td>
					<td>{{currency}}</td>
				</tr>
			</tfoot>
		</table>
	</div>
</div>