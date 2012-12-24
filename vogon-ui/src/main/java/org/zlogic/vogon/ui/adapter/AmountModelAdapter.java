/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import java.text.MessageFormat;
import java.util.Currency;
import java.util.ResourceBundle;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.ui.cell.CellStatus;

/**
 * Class for storing an amount with an overloaded toString method for better
 * customization of how it's rendered in a control.
 *
 * @author Dmitry Zolotukhin
 */
public class AmountModelAdapter implements Comparable<AmountModelAdapter>, CellStatus {

	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The balance/amount
	 */
	protected double amount;
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
	 * Constructs an AmountModelAdapter
	 *
	 * @param amount the initial cell amount
	 * @param isOk if the cell data is OK (e.g. zero sum for a transfer
	 * transaction)
	 * @param currency the currency
	 * @param isCurrencyConverted true if the currency was converted and should
	 * be displayed differently
	 * @param transactionType the transaction type
	 */
	public AmountModelAdapter(double amount, boolean isOk, Currency currency, boolean isCurrencyConverted, FinanceTransaction.Type transactionType) {
		this.amount = amount;
		this.currency = currency;
		this.isOk = isOk;
		this.isCurrencyConverted = isCurrencyConverted;
		this.transactionType = transactionType;
	}

	/**
	 * Constructs an AmountModelAdapter. Currency will be invalid.
	 *
	 * @param amount the initial cell amount
	 * @param isOk if the cell data is OK (e.g. zero sum for a transfer
	 * transaction)
	 */
	public AmountModelAdapter(double amount, boolean isOk) {
		this.amount = amount;
		this.isOk = isOk;
	}

	/**
	 * Constructs an AmountModelAdapter. Currency will be invalid, amount will
	 * be considered to be OK.
	 *
	 * @param amount the initial cell amount
	 */
	public AmountModelAdapter(double amount) {
		this.amount = amount;
		this.isOk = true;
	}

	@Override
	public String toString() {
		String formattedSum = MessageFormat.format(messages.getString("FORMAT_SUM"),
				transactionType == FinanceTransaction.Type.TRANSFER ? messages.getString("TRANSFER_TRANSACTION_FLAG") : messages.getString("EXPENSE_TRANSACTION_FLAG"),
				getAmount(),
				currency != null ? currency.getCurrencyCode() : messages.getString("INVALID_CURRENCY"),
				isCurrencyConverted ? messages.getString("CURRENCY_CONVERTED") : messages.getString("CURRENCY_NOT_CONVERTED"));
		return formattedSum;
	}

	@Override
	public int compareTo(AmountModelAdapter o) {
		int currencyComparison = currency.getCurrencyCode().compareTo(o.currency.getCurrencyCode());
		if (currencyComparison != 0)
			return currencyComparison;
		return new Double(amount).compareTo(o.amount);
	}

	/**
	 * Returns the amount double value
	 *
	 * @return the amount double value
	 */
	public Double getAmount() {
		return amount;
	}

	/**
	 * Returns the amount currency
	 *
	 * @return the amount currency
	 */
	public Currency getCurrency() {
		return currency;
	}

	/**
	 * Returns the associated transaction type
	 *
	 * @return the associated transaction type
	 */
	public FinanceTransaction.Type getTransactionType() {
		return transactionType;
	}

	/**
	 * Sets the amount double value
	 *
	 * @param amount the amount double value
	 */
	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Override
	public boolean isOK() {
		return isOk;
	}

	/**
	 * Returns true if currency is converted
	 *
	 * @return true if currency is converted
	 */
	public boolean isCurrencyConverted() {
		return isCurrencyConverted;
	}
}
