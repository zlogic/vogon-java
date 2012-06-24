/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.util.Date;

/**
 * Implements an expense transaction
 * @author Dmitry Zolotukhin
 */
public class ExpenseTransaction implements FinanceTransaction{
    /**
     * Contains the expense description string
     */
    private String description;
    /**
     * Contains the expense tags
     */
    private String tags;
    /**
     * Contains the expense amount
     */
    private double amount;
    
    /*
     * Contains the transaction date
     */
    private Date date;
    
    /**
     * Constructor for an expense transaction
     * @param description The transaction description
     * @param tags The transaction tags
     * @param date The transaction's date
     * @param amount The transaction amount
     */
    public ExpenseTransaction(String description,String tags,Date date,double amount){
	this.description= description;
	this.tags= tags;
	this.amount= amount;
	this.date= date;
    }
    
    @Override
    public java.lang.String toString(){
	java.text.MessageFormat form = new java.text.MessageFormat("{0} <{1}> {2} {3,number,#.##}");
	Object[] objects= {getDescription(), getTags(), getDate(), getAmount()};
	return form.format(objects);
    }

    /**
     * @return the transaction's description
     */
    public String getDescription() {
	return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
	this.description = description;
    }

    /**
     * @return the transaction's tags
     */
    public String getTags() {
	return tags;
    }

    /**
     * @return the transaction amount
     */
    public double getAmount() {
	return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(double amount) {
	this.amount = amount;
    }

    /**
     * @return the transaction date
     */
    public Date getDate() {
	return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
	this.date = date;
    }
}
