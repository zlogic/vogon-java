/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

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
	 * Contains the transfer amount
	 */
	protected double amount;

	/**
	 * Default constructor for a transfer transaction
	 */
	public TransferTransaction() {
	}

	/**
	 * Constructor for a transfer transaction
	 *
	 * @param description The transaction description
	 * @param tags The transaction tags
	 * @param date The transaction date
	 * @param components The transfer components
	 */
	public TransferTransaction(String description, String[] tags, Date date, List<TransactionComponent> components) {
		this.description = description;
		this.tags = tags;
		this.transactionDate = date;
		this.components = new LinkedList<>();
		this.components.addAll(components);

		updateAmounts();

		for (TransactionComponent component : components)
			component.getAccount().updateRawBalance(component.getRawAmount());
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the amount withdrawn accountFrom the source account
	 *
	 * @return the transaction amount
	 */
	public double getAmount() {
		return amount;
	}

	/**
	 * Returns the account accountFrom which money was transferred
	 *
	 * @return the account
	 */
	public FinanceAccount[] getFromAccounts() {
		HashSet<FinanceAccount> accounts = new HashSet<>();
		for (TransactionComponent component : components)
			if (component.getAmount() < 0)
				accounts.add(component.getAccount());
		FinanceAccount[] accountsOut = new FinanceAccount[accounts.size()];
		return accounts.toArray(accountsOut);
	}

	/**
	 * Returns the accounts accountTo which money was transferred
	 *
	 * @return the accounts
	 */
	public FinanceAccount[] getToAccounts() {
		HashSet<FinanceAccount> accounts = new HashSet<>();
		for (TransactionComponent component : components)
			if (component.getAmount() > 0)
				accounts.add(component.getAccount());
		FinanceAccount[] accountsOut = new FinanceAccount[accounts.size()];
		return accounts.toArray(accountsOut);
	}
}
