/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.util.Arrays;
import java.util.Currency;
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
public class TransferTransaction extends FinanceTransaction {

	/**
	 * Version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor for a transfer transaction
	 */
	protected TransferTransaction() {
		super();
	}

	/**
	 * Constructor for a transfer transaction
	 *
	 * @param description the transaction description
	 * @param tags the transaction tags
	 * @param date the transaction date
	 */
	public TransferTransaction(String description, String[] tags, Date date) {
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
		long amountPositive = 0, amountNegative = 0;
		for (TransactionComponent component : components) {
			amountPositive += component.getRawAmount() > 0 ? component.getRawAmount() : 0;
			amountNegative += component.getRawAmount() < 0 ? component.getRawAmount() : 0;
		}
		amount = amountPositive > -amountNegative ? amountPositive : -amountNegative;
	}

	/**
	 * Returns if the amount is OK (e.g. sum is zero or accounts use different
	 * currencies)
	 *
	 * @return true if amount is OK
	 */
	public boolean isAmountOk() {
		long amount = 0;
		Currency commonCurrency = null;
		for (TransactionComponent component : components) {
			if (commonCurrency == null)
				commonCurrency = component.getAccount().getCurrency();
			else if (component.getAccount().getCurrency() != commonCurrency)
				return true;
			amount += component.getRawAmount();
		}
		return amount == 0;
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the account accountFrom which money was transferred
	 *
	 * @return the account
	 */
	public FinanceAccount[] getFromAccounts() {
		HashSet<FinanceAccount> accounts = new HashSet<>();
		for (TransactionComponent component : components)
			if (component.getRawAmount() < 0)
				accounts.add(component.getAccount());
		return accounts.toArray(new FinanceAccount[0]);
	}

	/**
	 * Returns the accounts accountTo which money was transferred
	 *
	 * @return the accounts
	 */
	public FinanceAccount[] getToAccounts() {
		HashSet<FinanceAccount> accounts = new HashSet<>();
		for (TransactionComponent component : components)
			if (component.getRawAmount() > 0)
				accounts.add(component.getAccount());
		return accounts.toArray(new FinanceAccount[0]);
	}
}
