<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="org.zlogic.vogon.web.webmessages" />
<form name="analyticsForm" novalidate>
	<div class="panel panel-default">
		<div class="panel-body">
			<div class="row">
				<div class="col-md-6">
					<div class="form-group">
						<label>Tags</label>
						<div class="form-group">
							<button ng-click="selectAllTags()" class="btn btn-default" type="button"><span class="glyphicon glyphicon-check" aria-hidden="true"></span> <fmt:message key="SELECT_ALL_TAGS"/></button>
							<button ng-click="deselectAllTags()" class="btn btn-default" type="button"><span class="glyphicon glyphicon-unchecked" aria-hidden="true"></span> <fmt:message key="DESELECT_ALL_TAGS"/></button>
						</div>
						<div class="pre-scrollable">
							<div class="checkbox" ng-repeat="(tag, selected) in tags | orderBy:'tag'">
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
					<div class="input-group" ng-class="{ 'has-error': analyticsForm.startDate.$invalid }">
						<input type="text" class="form-control" uib-datepicker-popup ng-model="dateRange.start" name="startDate" is-open="startDateCalendar.opened" />
						<span class="input-group-btn">
							<button type="button" class="btn btn-default" ng-click="openStartDateCalendar($event)" type="button"><span class="glyphicon glyphicon-calendar"></span></button>
						</span>
					</div>
				</div>
				<div class="col-md-6">
					<label><fmt:message key="END_DATE"/></label>
					<div class="input-group" ng-class="{ 'has-error': analyticsForm.endDate.$invalid }">
						<input type="text" class="form-control" uib-datepicker-popup ng-model="dateRange.end" name="endDate" is-open="endDateCalendar.opened" />
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
					<button ng-click="buildReport()" class="btn btn-default btn-primary form-control" type="submit" ng-disabled="analyticsForm.$invalid"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span> <fmt:message key="BUILD_REPORT"/></button>
				</div>
			</div>
			<div class="well well-sm form-control-static" ng-if="report">
				<div class="row">
					<div class="col-md-6">
						<label class="form-control-static"><fmt:message key="REPORT_BY_TRANSACTIONS"/></label>
						<div class="pre-scrollable">
							<div class="container-fluid">
								<div class="row">
									<div class="col-md-6">
										<label><fmt:message key="TRANSACTION"/></label>
									</div>
									<div class="col-md-3 text-right">
										<label><fmt:message key="AMOUNT"/></label>
									</div>
									<div class="col-md-3">
										<label><fmt:message key="DATE"/></label>
									</div>
								</div>
								<div class="row" ng-repeat="transaction in report.transactions">
									<hr/>
									<div class="col-md-6">{{transaction.description}}</div>
									<div class="col-md-3 text-right">
										<div ng-repeat="(currencyCode, total) in totals = (transactionsService.totalsByCurrency(transaction))">
											<span ng-show="transactionsService.isTransferTransaction(transaction)">
												&sum;
											</span>
											{{total| number:2}} {{currencyCode}}
										</div>
									</div>
									<div class="col-md-3">{{transaction.date| date}}</div>
								</div>
							</div>
						</div>
					</div>
					<div class="col-md-6 form-group">
						<label class="form-control-static"><fmt:message key="REPORT_BY_TAGS"/></label>
						<div class="pre-scrollable">

							<div class="container-fluid">
								<div class="row">
									<div class="col-md-9">
										<label><fmt:message key="TAG"/></label>
									</div>
									<div class="col-md-3 text-right">
										<label><fmt:message key="AMOUNT"/></label>
									</div>
								</div>

								<div class="row" ng-repeat="tagExpense in report.tagExpenses">
									<hr/>
									<div class="col-md-9">{{tagExpense.tag}}</div>
									<div class="col-md-3 text-right">
										<div ng-repeat="(currencyCode, total) in tagExpense.amounts">
											{{total| number:2}} {{currencyCode}}
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
				<div class="row form-control-static">
					<div class="col-md-12 form-inline">
						<label><fmt:message key="SELECT_CHARTS_CURRENCY"/> </label>
						<select ng-model="report.selectedCurrency" ng-change="currencyChanged()" ng-options="currency.currencyCode as currency.displayName for currency in currencyService.currencies|filter:filterCurrency" class="form-control"></select>
					</div>
				</div>
				<div class="row form-control-static">
					<div class="col-md-12">
						<label><fmt:message key="TAGS_CHART"/></label>
						<nvd3 options="tagsChart.options" data="tagsChart.data"></nvd3>
					</div>
				</div>
				<div class="row form-control-static">
					<div class="col-md-12">
						<label><fmt:message key="BALANCE_CHART"/></label>
						<nvd3 options="balanceChart.options" data="balanceChart.data"></nvd3>
					</div>
				</div>
			</div>
		</div>
	</div>
</form>