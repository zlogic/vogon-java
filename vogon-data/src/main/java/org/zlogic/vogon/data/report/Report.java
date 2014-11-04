/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.report;

import java.util.List;
import java.util.Map;
import org.zlogic.vogon.data.FinanceTransaction;

/**
 * Report results class
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class Report {

	/**
	 * List of transactions matching the filter
	 */
	private List<FinanceTransaction> transactions;
	/**
	 * Expenses grouped by tag
	 */
	private List<TagExpense> tagExpenses;
	/**
	 * Accounts balance chart
	 */
	private Map<String, DateBalance<Double>> accountsBalanceGraph;

	/**
	 * Default constructor
	 */
	protected Report() {

	}
	/*
	 * Setters/getters
	 */

	/**
	 * Returns the list of transactions matching the report filter
	 *
	 * @return the list of transactions matching the report filter
	 */
	public List<FinanceTransaction> getTransactions() {
		return transactions;
	}

	/**
	 * Sets the transactions obtained by the report
	 *
	 * @param transactions the transactions to set
	 */
	protected void setTransactions(List<FinanceTransaction> transactions) {
		this.transactions = transactions;
	}

	/**
	 * Returns expenses grouped by tag
	 *
	 * @return expenses grouped by tag
	 */
	public List<TagExpense> getTagExpenses() {
		return tagExpenses;
	}

	/**
	 * Sets the expenses grouped by tag
	 *
	 * @param tagExpenses expenses grouped by tag
	 */
	protected void setTagExpenses(List<TagExpense> tagExpenses) {
		this.tagExpenses = tagExpenses;
	}

	/**
	 * Returns the accounts balance chart
	 *
	 * @return the accounts balance chart
	 */
	public Map<String, DateBalance<Double>> getAccountsBalanceGraph() {
		return accountsBalanceGraph;
	}

	/**
	 * Sets the accounts balance chart
	 *
	 * @param accountsBalanceGraph the accounts balance chart to set
	 */
	protected void setAccountsBalanceGraph(Map<String, DateBalance<Double>> accountsBalanceGraph) {
		this.accountsBalanceGraph = accountsBalanceGraph;
	}
}
