/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.report;

import java.util.Collection;
import java.util.Date;
import org.zlogic.vogon.data.Constants;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.FinanceTransaction.Type;
import org.zlogic.vogon.data.TransactionComponent;

/**
 * Light version of the FinanceTransaction class, for use in reports
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class ReportTransaction {

	/**
	 * Contains the expense description string
	 */
	private final String description;

	/**
	 * Contains the transaction date
	 */
	private final Date transactionDate;

	/**
	 * The transaction type
	 */
	private final Type type;

	/**
	 * The transaction amount
	 */
	private long amount;

	public ReportTransaction(FinanceTransaction transaction, Collection<FinanceAccount> selectedAccounts) {
		description = transaction.getDescription();
		transactionDate = transaction.getDate();
		type = transaction.getType();
		calculateAmount(transaction, selectedAccounts);
	}

	private void calculateAmount(FinanceTransaction transaction, Collection<FinanceAccount> selectedAccounts) {
		if (transaction.getType() == Type.TRANSFER) {
			long amountPositive = 0L;
			long amountNegative = 0L;
			for (TransactionComponent component : transaction.getComponents()) {
				if (selectedAccounts.contains(component.getAccount())) {
					amountPositive += Math.max(0, component.getRawAmount());
					amountNegative += Math.min(0, component.getRawAmount());
				}
			}
			amount = amountPositive > -amountNegative ? amountPositive : -amountNegative;
		} else {
			long componentsAmount = 0;
			for (TransactionComponent component : transaction.getComponents()) {
				if (selectedAccounts.contains(component.getAccount())) {
					componentsAmount += component.getRawAmount();
				}
			}
			amount = componentsAmount;
		}
	}

	/**
	 * Returns the transaction's description
	 *
	 * @return the transaction's description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the transaction date
	 *
	 * @return the transaction date
	 */
	public Date getDate() {
		return transactionDate;
	}

	/**
	 * Returns the transaction type
	 *
	 * @return the transaction type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns the transaction amount
	 *
	 * @return the transaction amount
	 */
	public double getAmount() {
		return amount / Constants.RAW_AMOUNT_MULTIPLIER;
	}

	/**
	 * Returns the raw amount (should be divided by
	 * Constants.rawAmountMultiplier to get the real amount)
	 *
	 * @return the raw amount
	 */
	public long getRawAmount() {
		return amount;
	}
}
