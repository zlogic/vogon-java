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
	this.transactions= new java.util.ArrayList<>();
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
     * Returns a transaction at position i
     * @param i The index
     * @return A transaction
     */
    public FinanceTransaction getTransaction(int i){
	return transactions.get(i);
    }
    
    /**
     * Returns the number of transactions
     * @return The number of transactions
     */
    public int getNumTransactions(){ return transactions.size(); }
}
