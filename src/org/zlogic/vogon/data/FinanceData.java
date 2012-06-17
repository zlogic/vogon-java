/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

/**
 * Class for storing the complete finance data
 * @author Dmitry Zolotukhin
 */
public class FinanceData {
    /**
     * Contains all finance transactions
     */
    protected java.util.ArrayList<FinanceTransaction> transactions;
    /**
     * Default constructor
     */
    public FinanceData(){
	
    }
    /**
     * Constructs FinanceData from pre-populated arrays (e.g. when importing data)
     * @param transactions Array of financial transactions
     */
    public FinanceData(java.util.ArrayList<FinanceTransaction> transactions){
	this.transactions= new java.util.ArrayList<>();
	this.transactions.addAll(transactions);
    }
    
    /**
     * Prints all transactions to console for debugging purposes
     */
    public void printTransactions(){
	for(FinanceTransaction transaction:transactions)
	    System.out.println(transaction.toString());
    }
}
