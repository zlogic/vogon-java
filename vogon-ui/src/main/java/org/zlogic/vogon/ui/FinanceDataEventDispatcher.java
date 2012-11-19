/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.util.LinkedList;
import java.util.List;

import org.zlogic.vogon.data.events.AccountEventHandler;
import org.zlogic.vogon.data.events.CurrencyEventHandler;
import org.zlogic.vogon.data.events.TransactionEventHandler;

/**
 * Class for dispatching a single FinanceData event to multiple listeners
 *
 * @author Dmitry Zolotukhin
 */
public class FinanceDataEventDispatcher implements TransactionEventHandler, AccountEventHandler, CurrencyEventHandler {

	/**
	 * The list of Transaction event handlers
	 */
	protected List<TransactionEventHandler> transactionEventHandler = new LinkedList<>();
	/**
	 * The list of Account event handlers
	 */
	protected List<AccountEventHandler> accountEventHandler = new LinkedList<>();
	/**
	 * The list of Currency event handlers
	 */
	protected List<CurrencyEventHandler> currencyEventHandler = new LinkedList<>();

	/**
	 * Default constructor
	 */
	public FinanceDataEventDispatcher() {
	}

	/**
	 * Adds an Transaction event handler
	 *
	 * @param handler the new Transaction event handler
	 */
	public void addTransactionEventHandler(TransactionEventHandler handler) {
		transactionEventHandler.add(handler);
	}

	/**
	 * Adds an Account event handler
	 *
	 * @param handler the new Account event handler
	 */
	public void addAccountEventHandler(AccountEventHandler handler) {
		accountEventHandler.add(handler);
	}

	/**
	 * Adds a Currency event handler
	 *
	 * @param handler the new Currency event handler
	 */
	public void addCurrencyEventHandler(CurrencyEventHandler handler) {
		currencyEventHandler.add(handler);
	}

	@Override
	public void transactionCreated(long transactionId) {
		for (TransactionEventHandler handler : transactionEventHandler)
			handler.transactionCreated(transactionId);
	}

	@Override
	public void transactionUpdated(long transactionId) {
		for (TransactionEventHandler handler : transactionEventHandler)
			handler.transactionUpdated(transactionId);
	}

	@Override
	public void transactionsUpdated() {
		for (TransactionEventHandler handler : transactionEventHandler)
			handler.transactionsUpdated();
	}

	@Override
	public void transactionDeleted(long transactionId) {
		for (TransactionEventHandler handler : transactionEventHandler)
			handler.transactionDeleted(transactionId);
	}

	@Override
	public void accountCreated(long accountId) {
		for (AccountEventHandler handler : accountEventHandler)
			handler.accountCreated(accountId);
	}

	@Override
	public void accountUpdated(long accountId) {
		for (AccountEventHandler handler : accountEventHandler)
			handler.accountUpdated(accountId);
	}

	@Override
	public void accountsUpdated() {
		for (AccountEventHandler handler : accountEventHandler)
			handler.accountsUpdated();
	}

	@Override
	public void accountDeleted(long accountId) {
		for (AccountEventHandler handler : accountEventHandler)
			handler.accountDeleted(accountId);
	}

	@Override
	public void currenciesUpdated() {
		for (CurrencyEventHandler handler : currencyEventHandler)
			handler.currenciesUpdated();
	}
}
