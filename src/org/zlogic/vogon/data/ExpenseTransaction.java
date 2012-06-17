/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

/**
 * Implements an expense transaction
 * @author Dmitry Zolotukhin
 */
public class ExpenseTransaction implements FinanceTransaction{
    /**
     * Contains the expense description string
     */
    protected String description;
    /**
     * Contains the expense tags
     */
    protected String tags;
    /**
     * Contains the expense amount
     */
    protected double amount;
    
    /**
     * Constructor for an expense transaction
     * @param description The transaction description
     * @param tags The transaction tags
     * @param amount The transaction amount
     */
    public ExpenseTransaction(String description,String tags,double amount){
	this.description= description;
	this.tags= tags;
	this.amount= amount;
    }
    
    @Override
    public java.lang.String toString(){
	java.text.MessageFormat form = new java.text.MessageFormat("{0} <{1}> {2,number,#.##}");
	Object[] objects= {description,tags,amount};
	return form.format(objects);
    }
}
