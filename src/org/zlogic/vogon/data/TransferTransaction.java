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
public class TransferTransaction extends FinanceTransaction {

    /**
     * Contains the transfer amount (from source account)
     */
    protected double amount;
    /**
     * Source account
     */
    protected FinanceAccount from;
    /**
     * Destination account and amounts
     */
    protected HashMap<FinanceAccount, Double> to;

    /**
     * Constructor for a transfer transaction
     *
     * @param description The transaction description
     * @param tags The transaction tags
     * @param date The transaction date
     * @param amount The transaction amount (withdrawn from source account)
     * @param from The source account
     * @param to The destination accounts and their amounts
     */
    public TransferTransaction(String description, String[] tags, Date date, double amount, FinanceAccount from, HashMap<FinanceAccount, Double> to) {
	this.description = description;
	this.tags = tags;
	this.date = date;
	this.amount = amount;
	this.from = from;
	this.to = new HashMap<>();
	this.to.putAll(to);
    }

    @Override
    public double getAccountAction(FinanceAccount account) {
	if (account == from)
	    return -amount;
	if (to.containsKey(account))
	    return to.get(account);
	return 0;
    }

    /*
     * Getters/setters
     */
    /**
     * Returns the amount withdrawn from the source account
     *
     * @return the transaction amount
     */
    public double getAmount() {
	return amount;
    }

    /**
     * Returns the account from which money was transferred
     *
     * @return the account
     */
    public FinanceAccount getFromAccount() {
	return from;
    }

    /**
     * Returns the accounts to which money was transferred
     *
     * @return the accounts
     */
    public FinanceAccount[] getToAccounts() {
	FinanceAccount[] accounts = new FinanceAccount[to.size()];
	return to.keySet().toArray(accounts);
    }
}
