/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;

import javax.persistence.Entity;

/**
 * Implements an expense transaction
 *
 * @author Dmitry Zolotukhin
 */
@Entity
public class ExpenseTransaction extends FinanceTransaction implements Serializable {
	/**
	 * Version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor for an expense transaction
	 */
	protected ExpenseTransaction() {
	}

	/**
	 * Constructor for an expense transaction
	 *
	 * @param description The transaction description
	 * @param tags The transaction tags
	 * @param date The transaction date
	 */
	public ExpenseTransaction(String description, String[] tags, Date date) {
		this.description = description;
		this.tags = tags;
		this.transactionDate = date;
		this.components = new LinkedList<>();
	}

	/**
	 * Updates the transaction's amount from its components
	 */
	@Override
	public void updateAmounts(){
		amount = 0;
		for (TransactionComponent component : components)
			amount += component.getRawAmount();
	}

	/*
	 * Getters/setters
	 */

	/**
	 * Returns the account
	 *
	 * @return the account
	 */
	public FinanceAccount[] getAccounts() {
		HashSet<FinanceAccount> accounts = new HashSet<>();
		for (TransactionComponent component : components) {
			accounts.add(component.getAccount());
		}
		return accounts.toArray(new FinanceAccount[0]);
	}
}
