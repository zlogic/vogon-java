/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.util.Date;
import java.util.HashMap;

/**
 * Implements an expense transaction
 *
 * @author Dmitry Zolotukhin
 */
public class ExpenseTransaction extends FinanceTransaction {

    protected double amount;
    /**
     * Contains the related account
     */
    protected HashMap<FinanceAccount, Double> accounts;

    /**
     * Constructor for an expense transaction
     *
     * @param description The transaction description
     * @param tags The transaction tags
     * @param date The transaction's date
     * @param amount The transaction amount
     */
    public ExpenseTransaction(String description, String[] tags, Date date, double amount, HashMap<FinanceAccount, Double> accounts) {
	this.description = description;
	this.tags = tags;
	this.amount = amount;
	this.date = date;
	this.accounts = new HashMap<>();
	this.accounts.putAll(accounts);
    }

    @Override
    public double getAccountAction(FinanceAccount account) {
	if (accounts.containsKey(account))
	    return accounts.get(account);
	return 0;
    }

    @Override
    public java.lang.String toString() {
	java.text.MessageFormat form = new java.text.MessageFormat("{0} <{1}> {2} {3,number,#.##}");
	Object[] objects = {getDescription(), getTags().toString(), getDate(), getAmount()};
	return form.format(objects);
    }

    /*
     * Getters/setters
     */
    /**
     * Returns the transaction amount
     *
     * @return the transaction amount
     */
    public double getAmount() {
	return amount;
    }

    /**
     * Sets the transaction amount
     *
     * @param amount the amount to set
     */
    public void setAmount(double amount) {
	this.amount = amount;
    }

    /**
     * Returns the account
     *
     * @return the account
     */
    public FinanceAccount[] getAccounts() {
	FinanceAccount[] accountsOut = new FinanceAccount[accounts.size()];
	return accounts.keySet().toArray(accountsOut);
    }
}
