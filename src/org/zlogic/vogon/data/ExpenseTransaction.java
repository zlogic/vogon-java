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
import java.util.List;
import javax.persistence.Entity;

/**
 * Implements an expense transaction
 *
 * @author Dmitry Zolotukhin
 */
@Entity
public class ExpenseTransaction extends FinanceTransaction implements Serializable {

    /**
     * The transaction amount
     */
    protected double amount;

    /**
     * Default constructor for an expense transaction
     */
    public ExpenseTransaction() {
    }

    /**
     * Constructor for an expense transaction
     *
     * @param description The transaction description
     * @param tags The transaction tags
     * @param date The transaction date
     * @param components The expense components
     */
    public ExpenseTransaction(String description, String[] tags, Date date, List<TransactionComponent> components) {
	this.description = description;
	this.tags = tags;
	this.transactionDate = date;
	this.components = new LinkedList<>();
	this.components.addAll(components);

	amount = 0;
	for (TransactionComponent component : components)
	    amount += component.getAmount();
    }

    @Override
    public double getAccountAction(FinanceAccount account) {
	double sum = 0;
	for (TransactionComponent component : components)
	    if (component.getAccount().equals(account))
		sum += component.getAmount();
	return sum;
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
	HashSet<FinanceAccount> accounts = new HashSet<>();
	for (TransactionComponent component : components) {
	    accounts.add(component.getAccount());
	}
	FinanceAccount[] accountsOut = new FinanceAccount[accounts.size()];
	return accounts.toArray(accountsOut);
    }
}
