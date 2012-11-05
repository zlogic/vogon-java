/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zlogic.vogon.ui;

import java.text.MessageFormat;
import java.util.Currency;
import java.util.ResourceBundle;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.ui.cell.CellStatus;

/**
 *
 * @author Dmitry
 */
public class AmountAdapter implements Comparable<AmountAdapter>,CellStatus {
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	
	/**
	 * The balance/amount
	 */
	protected double balance;
	/**
	 * The currency
	 */
	protected Currency currency;
	/**
	 * true if the currency was converted and should be displayed differently
	 */
	protected boolean isCurrencyConverted;
	/**
	 * The amount is OK (e.g. zero sum for a transfer transaction)
	 */
	protected boolean isOk;
	/**
	 * The transaction type (for special rendering)
	 */
	protected FinanceTransaction.Type transactionType;
	
	/**
	 * Constructs a AmountAdapter
	 *
	 * @param balance the initial cell balance
	 * @param isOk if the cell data is OK (e.g. zero sum for a transfer
	 * transaction)
	 * @param currency the currency
	 * @param isCurrencyConverted true if the currency was converted and should
	 * be displayed differently
	 * @param transactionType the transaction type
	 */
	public AmountAdapter(double balance, boolean isOk, Currency currency, boolean isCurrencyConverted, FinanceTransaction.Type transactionType) {
		this.balance = balance;
		this.currency = currency;
		this.isOk = isOk;
		this.isCurrencyConverted = isCurrencyConverted;
		this.transactionType = transactionType;
	}

	/**
	 * Constructs a SumTableCell. Currency will be invalid.
	 *
	 * @param balance the initial cell balance
	 * @param isOk if the cell data is OK (e.g. zero sum for a transfer
	 * transaction)
	 */
	public AmountAdapter(double balance, boolean isOk) {
		this.balance = balance;
		this.isOk = isOk;
	}

	/**
	 * Constructs a SumTableCell. Currency will be invalid, balance will be
	 * considered to be OK.
	 *
	 * @param balance the initial cell balance
	 */
	public AmountAdapter(double balance) {
		this.balance = balance;
		this.isOk = true;
	}

	@Override
	public String toString() {
		String formattedSum = MessageFormat.format(messages.getString("FORMAT_SUM"),
				transactionType == FinanceTransaction.Type.TRANSFER ? messages.getString("TRANSFER_TRANSACTION_FLAG") : messages.getString("EXPENSE_TRANSACTION_FLAG"),
				balance,
				currency != null ? currency.getCurrencyCode() : messages.getString("INVALID_CURRENCY"),
				isCurrencyConverted ? messages.getString("CURRENCY_CONVERTED") : messages.getString("CURRENCY_NOT_CONVERTED"));
		return formattedSum;
	}

	@Override
	public int compareTo(AmountAdapter o) {
		int currencyComparison = currency.getCurrencyCode().compareTo(o.currency.getCurrencyCode());
		if (currencyComparison != 0)
			return currencyComparison;
		return new Double(balance).compareTo(o.balance);
	}
	
	public Double getBalance(){
		return balance;
	}

	@Override
	public boolean isOK() {
		return isOK();
	}
}
