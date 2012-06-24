/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.util.Arrays;
import java.util.Date;

/**
 * Interface for storing a single finance transaction
 *
 * @author Dmitry Zolotukhin
 */
public abstract class FinanceTransaction {

    /**
     * Contains the expense description string
     */
    protected String description;
    /**
     * Contains the expense tags
     */
    protected String[] tags;
    /**
     * Contains the transaction date
     */
    protected Date date;

    /**
     * Returns the amount this transaction modifies an account E.g. how this
     * transaction increased or decreases the balance of a particular account
     *
     * @param the account
     * @return the amount an account is changed by this transaction
     */
    public abstract double getAccountAction(FinanceAccount account);

    /*
     * Getters/setters
     */
    /**
     * Adds a tag
     */
    void addTag(String tag) {
	tags = Arrays.copyOf(tags, tags.length + 1);
	tags[tags.length - 1] = tag;
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
     * Sets the transaction's description
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
	this.description = description;
    }

    /**
     * Returns the transaction's tags
     *
     * @return the transaction's tags
     */
    public String[] getTags() {
	return tags;
    }

    /**
     * Returns the transaction date
     *
     * @return the transaction date
     */
    public Date getDate() {
	return date;
    }

    /**
     * Sets the transaction date
     *
     * @param date the date to set
     */
    public void setDate(Date date) {
	this.date = date;
    }
}
