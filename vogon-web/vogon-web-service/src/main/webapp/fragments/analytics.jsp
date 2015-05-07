<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="org.zlogic.vogon.web.webmessages" />
<form name="analyticsForm">
	<div class="modal-header">
		<h3 class="modal-title"><fmt:message key="ANALYTICS_TITLE"/></h3>
	</div>
	<div class="modal-body">
		<div class="row">
			<div class="col-md-6">
				<div class="form-group">
					<label>Tags</label>
					<div class="form-group">
						<button ng-click="selectAllTags()" class="btn btn-default" type="button"><span class="glyphicon glyphicon-check" aria-hidden="true"></span> <fmt:message key="SELECT_ALL_TAGS"/></button>
						<button ng-click="deselectAllTags()" class="btn btn-default" type="button"><span class="glyphicon glyphicon-unchecked" aria-hidden="true"></span> <fmt:message key="DESELECT_ALL_TAGS"/></button>
					</div>
					<div class="pre-scrollable">
						<div class="checkbox" ng-repeat="(tag,selected) in tags | orderBy:'tag'">
							<label>
								<input type="checkbox" ng-model="tags[tag]" /> {{tag.length>0?tag:"&nbsp;"}}
							</label>
						</div>
					</div>
				</div>
			</div>
			<div class="col-md-6">
				<div class="form-group">
					<label>Accounts</label>
					<div class="form-group">
						<button ng-click="selectAllAccounts()" class="btn btn-default" type="button"><span class="glyphicon glyphicon-check" aria-hidden="true"></span> <fmt:message key="SELECT_ALL_ACCOUNTS"/></button>
						<button ng-click="deselectAllAccounts()" class="btn btn-default" type="button"><span class="glyphicon glyphicon-unchecked" aria-hidden="true"></span> <fmt:message key="DESELECT_ALL_ACCOUNTS"/></button>
					</div>
					<div class="pre-scrollable">
						<div class="checkbox" ng-repeat="account in accountService.accounts">
							<label>
								<input type="checkbox" ng-model="accounts[account.id]" /> {{account.name}}
							</label>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-md-6">
				<label><fmt:message key="START_DATE"/></label>
				<div class="input-group">
					<input type="text" class="form-control" datepicker-popup ng-model="startDate" is-open="startDateCalendarOpened" />
					<span class="input-group-btn">
						<button type="button" class="btn btn-default" ng-click="openStartDateCalendar($event)" type="button"><span class="glyphicon glyphicon-calendar"></span></button>
					</span>
				</div>
			</div>
			<div class="col-md-6">
				<label><fmt:message key="END_DATE"/></label>
				<div class="input-group">
					<input type="text" class="form-control" datepicker-popup ng-model="endDate" is-open="endDateCalendarOpened" />
					<span class="input-group-btn">
						<button type="button" class="btn btn-default" ng-click="openEndDateCalendar($event)" type="button"><span class="glyphicon glyphicon-calendar"></span></button>
					</span>
				</div>
			</div>
		</div>
		<div class="row form-control-static">
			<div class="col-md-12">
				<div class="checkbox">
					<label class="checkbox">
						<input type="checkbox" ng-model="transactionTypeEnabled.transfer"/> <fmt:message key="TRANSFER_TRANSACTIONS"/>
					</label>
				</div>
				<div class="checkbox">
					<label class="checkbox">
						<input type="checkbox" ng-model="transactionTypeEnabled.income"/> <fmt:message key="INCOME_TRANSACTIONS"/>
					</label>
				</div>
				<div class="checkbox">
					<label class="checkbox">
						<input type="checkbox" ng-model="transactionTypeEnabled.expense"/> <fmt:message key="EXPENSE_TRANSACTIONS"/>
					</label>
				</div>
			</div>
		</div>
		<div class="row form-control-static">
			<div class="col-md-12">
				<button ng-click="buildReport()" class="btn btn-default btn-primary form-control" type="submit"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span> <fmt:message key="BUILD_REPORT"/></button>
			</div>
		</div>
		<div class="well well-sm form-control-static" ng-show="report">
			<div class="row">
				<div class="col-md-6">
					<label class="form-control-static"><fmt:message key="REPORT_BY_TRANSACTIONS"/></label>
					<div class="pre-scrollable">
						<table class="table table-hover">
							<thead>
								<tr>
									<th><fmt:message key="TRANSACTION"/></th>
									<th class="text-right"><fmt:message key="AMOUNT"/></th>
									<th><fmt:message key="DATE"/></th>
								</tr>
							</thead>
							<tbody>
								<tr ng-repeat="transaction in report.transactions">
									<td>{{transaction.description}}</td>
									<td class="text-right">
										<div ng-repeat="(symbol,total) in totals = (transactionsService.totalsByCurrency(transaction))">
											<span ng-show="transactionsService.isTransferTransaction(transaction)">
												&sum;
											</span>
											{{total| number:2}} {{symbol}}
										</div>
									</td>
									<td>{{transaction.date| date}}</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div class="col-md-6 form-group">
					<label class="form-control-static"><fmt:message key="REPORT_BY_TAGS"/></label>
					<div class="pre-scrollable">
						<table class="table table-hover">
							<thead>
								<tr>
									<th><fmt:message key="TAG"/></th>
									<th class="text-right"><fmt:message key="AMOUNT"/></th>
								</tr>
							</thead>
							<tbody>
								<tr ng-repeat="tagExpense in report.tagExpenses">
									<td>{{tagExpense.tag}}</td>
									<td class="text-right">
										<div ng-repeat="(symbol,total) in tagExpense.amounts">
											{{total| number:2}} {{symbol}}
										</div>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
			</div>
			<div class="row form-control-static">
				<div class="col-md-12 form-inline">
					<label><fmt:message key="SELECT_CHARTS_CURRENCY"/> </label>
					<select ng-model="report.selectedCurrency" ng-change="currencyChanged()" ng-options="currency.symbol as currency.displayName for currency in currencyService.currencies|filter:filterCurrency" class="form-control"></select>
				</div>
			</div>
			<div class="row form-control-static">
				<div class="col-md-12">
					<label><fmt:message key="TAGS_CHART"/></label>
					<div id="tagsChartContainer">
						<nvd3 options="tagsChart.options" data="tagsChart.data"></nvd3>
					</div>
				</div>
			</div>
			<div class="row form-control-static">
				<div class="col-md-12">
					<label><fmt:message key="BALANCE_CHART"/></label>
					<div id="balanceChartContainer">
						<nvd3 options="balanceChart.options" data="balanceChart.data"></nvd3>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="modal-footer">
		<button ng-click="close()" class="btn btn-default" type="button"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> <fmt:message key="CLOSE"/></button>
	</div>
</form>