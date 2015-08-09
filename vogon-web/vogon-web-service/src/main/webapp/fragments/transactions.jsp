<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="org.zlogic.vogon.web.webmessages" />
<div class="panel panel-default">
	<div class="panel-body">
		<button ng-click="addTransaction()" class="btn btn-primary"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> <fmt:message key="ADD_TRANSACTION"/></button>
		<div infinite-scroll="transactionsService.nextPage()" infinite-scroll-disabled="transactionsService.loadingNextPage">
			<div class="container-fluid">
				<div class="row" id="transactionsTable">
					<hr/>
					<div class="col-md-5 col-sm-12">
						<label>
							<div class="clickable" ng-click="transactionsService.applySort('description')"><fmt:message key="TRANSACTION_NAME"/>
								<span ng-show="transactionsService.sortColumn === 'description'" class="glyphicon glyphicon-sort-by-alphabet" ng-class="{'glyphicon-sort-by-alphabet': transactionsService.sortAsc, 'glyphicon-sort-by-alphabet-alt': !transactionsService.sortAsc}"></span>
							</div>
						</label>
						<div class="form-horizontal">
							<div class="input-group">
								<span class="input-group-addon"><span class="glyphicon glyphicon-filter" aria-hidden="true"></span></span>
								<input type="text" class="form-control" placeholder="<fmt:message key="ENTER_DESCRIPTION_FILTER"/>" ng-model="transactionsService.filterDescription" ng-change="applyFilter()"/>
							</div>
						</div>
					</div>
					<div class="col-md-2 col-sm-4">
						<label>
							<div class="clickable" ng-click="transactionsService.applySort('date')"><fmt:message key="DATE"/>
								<span ng-show="transactionsService.sortColumn === 'date'" class="glyphicon glyphicon-sort-by-alphabet" ng-class="{'glyphicon-sort-by-order': transactionsService.sortAsc, 'glyphicon-sort-by-order-alt': !transactionsService.sortAsc}"></span>
							</div>
						</label>
						<div class="form-horizontal">
							<div class="input-group" ng-class="{ 'has-error': transactionsDateFilterForm.filterDate.$invalid }">
								<span class="input-group-addon"><span class="glyphicon glyphicon-filter" aria-hidden="true"></span></span>
								<input type="text" class="form-control" datepicker-popup ng-model="transactionsService.filterDate" name="filterDate" ng-change="applyFilter()" is-open="filterDateCalendarOpened" placeholder="<fmt:message key="ENTER_DATE_FILTER"/>" />
								<span class="input-group-btn">
									<button type="button" class="btn btn-default" ng-click="openFilterDateCalendar($event)"><span class="glyphicon glyphicon-calendar"></span></button>
								</span>
							</div>
						</div>
					</div>
					<div class="col-md-2 col-sm-4">
						<label>
							<fmt:message key="TAGS"/>
						</label>
						<div class="form-horizontal">
							<div class="input-group">
								<span class="input-group-addon"><span class="glyphicon glyphicon-filter" aria-hidden="true"></span></span>
								<tags-input class="bootstrap" ng-model="transactionsService.filterTags" placeholder="<fmt:message key="ADD_FILTER_TAGS"/>" on-tag-added="applyFilter()" on-tag-removed="applyFilter()" replace-spaces-with-dashes="false" add-on-comma="false">
									<auto-complete source="tagsService.autocompleteQuery($query)"></auto-complete>
								</tags-input>
							</div>
						</div>
					</div>
					<div class="col-md-1 col-sm-12 text-right">
						<label>
							<div class="clickable" ng-click="transactionsService.applySort('amount')"><fmt:message key="AMOUNT"/>
								<span ng-show="transactionsService.sortColumn === 'amount'" class="glyphicon glyphicon-sort-by-alphabet" ng-class="{'glyphicon-sort-by-order': transactionsService.sortAsc, 'glyphicon-sort-by-order-alt': !transactionsService.sortAsc}"></span>
							</div>
						</label>
					</div>
					<div class="col-md-1 col-sm-4">
						<label><fmt:message key="ACCOUNT"/></label>
					</div>
				</div>
				<div class="row" id="transactionsTable"  ng-repeat="transaction in transactionsService.transactions" ng-class="{danger:!transactionsService.isAmountOk(transaction)}" ng-switch on="editingTransaction == transaction">
					<hr/>
					<div ng-switch-when="false">
						<div class="col-md-5 col-sm-12">
							<label>{{transaction.description}}</label>
							<div class="form-inline">
								<div class="form-group">
									<button ng-click="startEditing(transaction)" class="btn btn-default"><span class="glyphicon glyphicon-edit" aria-hidden="true"></span> <fmt:message key="EDIT"/></button>
									<button ng-click="duplicateTransaction(transaction)" class="btn btn-default"><span class="glyphicon glyphicon-asterisk" aria-hidden="true"></span> <fmt:message key="DUPLICATE"/></button>
								</div>
							</div>
						</div>
						<div class="col-md-2 col-sm-4">
							<p>{{transaction.date| date}}</p>
						</div>
						<div class="col-md-2 col-sm-4">
							<div ng-repeat="tag in transaction.tags">
								<p>{{tag}}{{$last ? "" : <fmt:message key="TAGS_SEPARATOR" />}}</p>
							</div>
						</div>
						<div class="col-md-1 text-right">
							<div ng-repeat="(symbol,total) in totals = (transactionsService.totalsByCurrency(transaction))">
								<span ng-show="transactionsService.isTransferTransaction(transaction)">&sum;</span>{{total| number:2}} {{symbol}}
							</div>
						</div>
						<div class="col-md-1 col-sm-4">
							<div ng-show="transactionsService.isExpenseIncomeTransaction(transaction)">
								<div ng-repeat="account in accounts = (transactionsService.getAccounts(transaction, transactionsService.allAccountsPredicate))">
									{{account.name}}{{$last ? '' : ', '}}
								</div>
							</div>
							<div ng-show="transactionsService.isTransferTransaction(transaction)">
								<div ng-repeat="account in accounts = (transactionsService.getAccounts(transaction, transactionsService.fromAccountsPredicate))">
									{{$first && accounts.length > 1 ? '(' : ''}}{{account.name}}{{$last ? '' : ', '}}{{$last && accounts.length>1?')':''}}
								</div>
								<span class="glyphicon glyphicon-chevron-down" aria-hidden="true"></span>
								<div ng-repeat="account in accounts = (transactionsService.getAccounts(transaction, transactionsService.toAccountsPredicate))">
									{{$first && accounts.length > 1 ? '(' : ''}}{{account.name}}{{$last ? '' : ', '}}{{$last && accounts.length>1?')':''}}
								</div>
							</div>
						</div>
					</div>
					<div ng-switch-when="true" class="col-md-12 well">
						<div ng-controller="TransactionEditorController" ng-include="'fragments/transactioneditor.fragment'" ng-init="transaction = transaction"></div>
					</div>
				</div>
				<div class="row text-center" ng-show="transactionsService.loadingNextPage">
					<hr/>
					<div class="col-md-12">
						<span class="glyphicon glyphicon-refresh" aria-hidden="true"></span> <fmt:message key="LOADING_ALERT"/>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>