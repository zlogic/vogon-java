/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.util.LinkedList;
import java.util.List;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.events.AccountEventHandler;
import org.zlogic.vogon.data.events.CurrencyEventHandler;
import org.zlogic.vogon.data.events.TransactionEventHandler;

/**
 *
 * @author Dmitry Zolotukhin
 */
public class FinanceDataEventDispatcher implements TransactionEventHandler,AccountEventHandler,CurrencyEventHandler {
	protected List<TransactionEventHandler> transactionEventHandler = new LinkedList<>();
	protected List<AccountEventHandler> accountEventHandler = new LinkedList<>();
	protected List<CurrencyEventHandler> currencyEventHandler = new LinkedList<>();
	public FinanceDataEventDispatcher(){
		
	}

	public void addTransactionEventHandler(TransactionEventHandler handler){
		transactionEventHandler.add(handler);
	}
	
	public void addAccountEventHandler(AccountEventHandler handler){
		accountEventHandler.add(handler);
	}
	
	public void addCurrencyEventHandler(CurrencyEventHandler handler){
		currencyEventHandler.add(handler);
	}
	
	@Override
	public void transactionCreated(FinanceTransaction newTransaction) {
		for(TransactionEventHandler handler : transactionEventHandler)
			handler.transactionCreated(newTransaction);
	}

	@Override
	public void transactionUpdated(FinanceTransaction updatedTransaction) {
		for(TransactionEventHandler handler : transactionEventHandler)
			handler.transactionUpdated(updatedTransaction);
	}

	@Override
	public void transactionsUpdated() {
		for(TransactionEventHandler handler : transactionEventHandler)
			handler.transactionsUpdated();
	}

	@Override
	public void transactionDeleted(FinanceTransaction deletedTransaction) {
		for(TransactionEventHandler handler : transactionEventHandler)
			handler.transactionDeleted(deletedTransaction);
	}

	@Override
	public void accountCreated(FinanceAccount newAccount) {
		for(AccountEventHandler handler : accountEventHandler)
			handler.accountCreated(newAccount);
	}

	@Override
	public void accountUpdated(FinanceAccount updatedAccount) {
		for(AccountEventHandler handler : accountEventHandler)
			handler.accountUpdated(updatedAccount);
	}

	@Override
	public void accountsUpdated() {
		for(AccountEventHandler handler : accountEventHandler)
			handler.accountsUpdated();
	}

	@Override
	public void accountDeleted(FinanceAccount deletedAccount) {
		for(AccountEventHandler handler : accountEventHandler)
			handler.accountDeleted(deletedAccount);
	}

	@Override
	public void currenciesUpdated() {
		for(CurrencyEventHandler handler : currencyEventHandler)
			handler.currenciesUpdated();
	}
}
