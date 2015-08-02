<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="org.zlogic.vogon.web.webmessages" />
<form name="transactionEditorForm" ng-init="scrollToEditor()" novalidate>
	<div class="modal-header">
		<h3 class="modal-title"><fmt:message key="EDIT_TRANSACTION_TITLE"/></h3>
	</div>
	<div class="modal-body">
		<div class="row form-control-static">
			<div class="form-inline col-md-12">
				<span ng-class="{ 'has-error': transactionEditorForm.description.$invalid }" >
					<input type="text" ng-model="transaction.description" class="form-control" placeholder="<fmt:message key="ENTER_TRANSACTION_DESCRIPTION"/>" name="description" required />
				</span>
				<select ng-model="transaction.type" ng-init="transaction.type = transaction.type || transactionTypes[0]" ng-options="transactionType.value as transactionType.name for transactionType in transactionTypes" class="form-control"></select>
				<div class="input-group" ng-class="{ 'has-error': transactionEditorForm.date.$invalid }" >
					<input type="text" class="form-control" datepicker-popup ng-model="transaction.date" name="date" is-open="calendarOpened" />
					<span class="input-group-btn">
						<button type="button" class="btn btn-default" ng-click="openCalendar($event)"><span class="glyphicon glyphicon-calendar"></span></button>
					</span>
				</div>
				<tags-input class="bootstrap" ng-model="tags" placeholder="<fmt:message key="ADD_TAGS"/>" on-tag-added="syncTags()" on-tag-removed="syncTags()" replace-spaces-with-dashes="false" add-on-comma="false">
					<auto-complete source="tagsService.autocompleteQuery($query)"></auto-complete>
				</tags-input>
			</div>
		</div>
		<div class="row form-control-static">
			<div class="col-md-12">
				<button ng-click="addTransactionComponent()" class="btn btn-default" type="button"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> <fmt:message key="ADD_COMPONENT"/></button>
			</div>
		</div>
		<div class="row form-control-static" ng-repeat="component in transaction.components">
			<div class="form-inline col-md-12">
				<ng-form name="transactionForm">
					<span ng-class="{ 'has-error': transactionForm.amount.$invalid }" >
						<input type="text" ng-model="component.amount" placeholder="<fmt:message key="ENTER_AMOUNT"/>" class="text-right form-control" name="amount" smart-float required/>
					</span>
					<span style="form-control">{{accountService.getAccount(component.accountId).currency}}</span>
					<span ng-class="{ 'has-error': transactionForm.account.$invalid }" >
						<select ng-model="component.accountId" ng-options="account.id as account.name for account in accountService.accounts | filter:isAccountVisible" class="form-control" name="account" required></select>
					</span>
					<button ng-click="deleteTransactionComponent(component)" class="btn btn-default" type="button"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span> <fmt:message key="DELETE"/></button>
				</ng-form>
			</div>
		</div>
	</div>
	<div class="modal-footer">
		<button ng-click="deleteTransaction()" class="btn btn-danger" type="button"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span> <fmt:message key="DELETE"/></button>
		<button ng-click="cancelEditing()" class="btn btn-default" type="button"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> <fmt:message key="CANCEL"/></button>
		<button ng-click="submitEditing()" class="btn btn-primary" type="submit" ng-disabled="transactionEditorForm.$invalid"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span> <fmt:message key="APPLY"/></button>
	</div>
</form>