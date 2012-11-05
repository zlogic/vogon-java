/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zlogic.vogon.ui;

import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;

/**
 *
 * @author Dmitry
 */
public class TransactionComponentModelAdapter {
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	
	protected FinanceData financeData;
	protected TransactionComponent transactionComponent;
	
	public TransactionComponentModelAdapter(TransactionComponent transactionComponent, FinanceData financeData) {
		this.transactionComponent = transactionComponent;
		this.financeData = financeData;
	}
	
	public FinanceAccountModelAdapter getAccount(){
		return new FinanceAccountModelAdapter(transactionComponent.getAccount());
	}
	
	public void setAccount(FinanceAccount account){
		financeData.setTransactionComponentAccount(transactionComponent, account);
	}
	
	public AmountAdapter getAmount(){
		return new AmountAdapter(transactionComponent.getAmount(),true, transactionComponent.getAccount() != null ? transactionComponent.getAccount().getCurrency() : null, false, FinanceTransaction.Type.UNDEFINED);
	}
	
	public void setAmount(double amount){
		financeData.setTransactionComponentAmount(transactionComponent, amount);
	}
	
	public TransactionComponent getTransactionComponent(){
		return transactionComponent;
	}
}
