/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
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
		super();
	}

	/**
	 * Constructor for an expense transaction
	 *
	 * @param description the transaction description
	 * @param tags the transaction tags
	 * @param date the transaction date
	 */
	public ExpenseTransaction(String description, String[] tags, Date date) {
		this.description = description;
		this.tags = tags != null ? Arrays.asList(tags) : new LinkedList<String>();
		this.transactionDate = date;
		this.components = new LinkedList<>();
	}

	/**
	 * Updates the transaction's amount from its components
	 */
	@Override
	public void updateAmounts() {
		amount = 0;
		for (TransactionComponent component : components)
			amount += component.getRawAmount();
	}
}
