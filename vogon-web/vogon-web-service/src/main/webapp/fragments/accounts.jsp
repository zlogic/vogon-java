<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="org.zlogic.vogon.web.webmessages" />
<div class="panel panel-default">
	<div class="panel-body">
		<div ng-if="!editingAccounts">
			<button ng-click="editAccounts()" class="btn btn-primary"><span class="glyphicon glyphicon-pencil" aria-hidden="true"></span> <fmt:message key="EDIT_ACCOUNTS"/></button>
			<table class="table table-hover">
				<thead>
					<tr>
						<th><fmt:message key="ACCOUNT_NAME"/></th>
						<th class="text-right"><fmt:message key="BALANCE"/></th>
						<th><fmt:message key="CURRENCY"/></th>
					</tr>
				</thead>
				<tbody>
					<tr ng-repeat="account in accountService.accounts| filter:{showInList:true}">
						<td>{{account.name}}</td>
						<td class="text-right">{{account.balance| number:2}}</td>
						<td>{{account.currency}}</td>
					</tr>
				</tbody>
				<tfoot>
					<tr class="total-amount" ng-repeat="(currency,data) in accountService.totalsForCurrency">
						<td><fmt:message key="TOTAL_FOR_CURRENCY"/></td>
						<td class="text-right">{{data.total| number:2}}</td>
						<td>{{currency}}</td>
					</tr>
				</tfoot>
			</table>
		</div>
		<div ng-if="editingAccounts">
			<form name="accountEditorForm" novalidate>
				<div class="row form-control-static">
					<div class="col-md-12">
						<button ng-click="cancelEditing()" class="btn btn-default" type="button"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> <fmt:message key="CANCEL"/></button>
						<button ng-click="submitEditing()" class="btn btn-primary" type="submit" ng-disabled="accountEditorForm.$invalid"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span> <fmt:message key="APPLY"/></button>
					</div>
				</div>
				<div class="row form-control-static">
					<div class="col-md-12">
						<button ng-click="addAccount()" class="btn btn-default" type="button"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> <fmt:message key="ADD_ACCOUNT"/></button>
					</div>
				</div>
				<div class="row form-control-static" ng-repeat="account in accountService.accounts">
					<ng-form name="accountForm">
						<div class="form-inline col-md-9">
							<div class="row">
								<div class="form-inline col-md-12">
									<span ng-class="{ 'has-error': accountForm.accountName.$invalid }" >
										<input type="text" ng-model="account.name" placeholder="<fmt:message key="ENTER_ACCOUNT_NAME"/>" class="form-control" name="accountName" required/>
									</span>
									<span ng-class="{ 'has-error': accountForm.accountCurrency.$invalid }" >
										<select ng-model="account.currency" ng-options="currency.symbol as currency.displayName for currency in currencies.currencies" class="form-control" name="accountCurrency" required></select>
									</span>
								</div>
							</div>
							<div class="row">
								<div class="form-inline col-md-12">
									<label class="checkbox-inline">
										<input type="checkbox" ng-model="account.includeInTotal"/> <fmt:message key="INCLUDE_IN_TOTAL"/>
									</label>
									<label class="checkbox-inline">
										<input type="checkbox" ng-model="account.showInList"/> <fmt:message key="SHOW_IN_ACCOUNTS_LIST"/>
									</label>
								</div>
							</div>
						</div>
						<div class="form-inline col-md-3 text-right">
							<button ng-click="deleteAccount(account)" class="btn btn-default" type="button"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span> <fmt:message key="DELETE"/></button>
						</div>
					</ng-form>
				</div>
			</form>
		</div>
	</div>
</div>