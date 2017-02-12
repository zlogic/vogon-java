<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="org.zlogic.vogon.web.webmessages" />
<form name="accountEditorForm" novalidate>
	<div class="panel panel-default">
		<div class="panel-body">
			<div class="container-fluid">
				<div class="row form-control-static" ng-show="!editingAccounts">
					<div class="col-md-12">
						<button ng-click="editAccounts()" class="btn btn-primary"><span class="glyphicon glyphicon-pencil" aria-hidden="true"></span> <fmt:message key="EDIT_ACCOUNTS"/></button>
					</div>
				</div>
				<div ng-show="editingAccounts">
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
				</div>
				<div class="row">
					<hr/>
					<div class="col-md-3 col-sm-4"><label><fmt:message key="ACCOUNT_NAME"/></label></div>
					<div class="col-md-2 col-sm-3 text-right"><label><fmt:message key="BALANCE"/></label></div>
					<div class="col-md-7 col-sm-5"><label><fmt:message key="CURRENCY"/></label></div>
				</div>
				<div ng-repeat="account in accountService.accounts">
					<div class="row" ng-if="!editingAccounts && account.showInList">
						<hr/>
						<div class="col-md-3 col-sm-4">{{account.name}}</div>
						<div class="col-md-2 col-sm-3 text-right">{{account.balance| number:2}}</div>
						<div class="col-md-7 col-sm-5">{{account.currency}}</div>
					</div>
					<ng-form name="accountForm" ng-if="editingAccounts">
						<div class="row">
							<hr/>
							<div class="col-md-3 col-sm-4">
								<span ng-class="{ 'has-error': accountForm.accountName.$invalid }" >
									<input type="text" ng-model="account.name" placeholder="<fmt:message key="ENTER_ACCOUNT_NAME"/>" class="form-control" name="accountName" required/>
								</span>
								<div class="checkbox">
									<label>
										<input type="checkbox" ng-model="account.includeInTotal"/> <fmt:message key="INCLUDE_IN_TOTAL"/>
									</label>
								</div>
								<div class="checkbox">
									<label>
										<input type="checkbox" ng-model="account.showInList"/> <fmt:message key="SHOW_IN_ACCOUNTS_LIST"/>
									</label>
								</div>
							</div>
							<div class="col-md-2 col-sm-3 text-right"><p>{{account.balance| number:2}}</p></div>
							<div class="col-md-7 col-sm-5">
								<span ng-class="{ 'has-error': accountForm.accountCurrency.$invalid }" >
									<select ng-model="account.currency" ng-options="currency.currencyCode as currency.displayName for currency in currencies.currencies" class="form-control" name="accountCurrency" required></select>
								</span>
							</div>
						</div>
						<div class="row">
							<div class="form-inline col-md-1 col-sm-1">
								<button ng-click="deleteAccount(account)" class="btn btn-danger" type="button"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span> <fmt:message key="DELETE"/></button>
							</div>
						</div>
					</ng-form>
				</div>
				<div class="row total-amount" ng-repeat="(currency,data) in accountService.totalsForCurrency">
					<hr/>
					<div class="col-md-3 col-sm-4"><fmt:message key="TOTAL_FOR_CURRENCY"/></div>
					<div class="col-md-2 col-sm-3 text-right">{{data.total| number:2}}</div>
					<div class="col-md-7 col-xs-5">{{currency}}</div>
				</div>
			</div>
		</div>
	</div>
</form>