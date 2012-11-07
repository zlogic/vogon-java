/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.text.MessageFormat;
import java.util.Currency;
import java.util.ResourceBundle;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.ui.cell.CellStatus;

/**
 *
 * @author Dmitry Zolotukhin
 */
public class AmountAdapter implements Comparable<AmountAdapter>, CellStatus {

	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/*
	 * These values are use if owner is null
	 */
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
	 * Constructs an AmountAdapter
	 *
	 * @param amount the initial cell amount
	 * @param isOk if the cell data is OK (e.g. zero sum for a transfer
	 * transaction)
	 * @param currency the currency
	 * @param isCurrencyConverted true if the currency was converted and should
	 * be displayed differently
	 * @param transactionType the transaction type
	 */
	public AmountAdapter(double balance, boolean isOk, Currency currency, boolean isCurrencyConverted, FinanceTransaction.Type transactionType) {
		this.amount = balance;
		this.currency = currency;
		this.isOk = isOk;
		this.isCurrencyConverted = isCurrencyConverted;
		this.transactionType = transactionType;
	}

	/**
	 * Constructs an AmountAdapter. Currency will be invalid.
	 *
	 * @param amount the initial cell amount
	 * @param isOk if the cell data is OK (e.g. zero sum for a transfer
	 * transaction)
	 */
	public AmountAdapter(double balance, boolean isOk) {
		this.amount = balance;
		this.isOk = isOk;
	}

	/**
	 * Constructs an AmountAdapter. Currency will be invalid, amount will be
	 * considered to be OK.
	 *
	 * @param amount the initial cell amount
	 */
	public AmountAdapter(double balance) {
		this.amount = balance;
		this.isOk = true;
	}

	@Override
	public String toString() {
		String formattedSum = MessageFormat.format(messages.getString("FORMAT_SUM"),
				transactionType == FinanceTransaction.Type.TRANSFER ? messages.getString("TRANSFER_TRANSACTION_FLAG") : messages.getString("EXPENSE_TRANSACTION_FLAG"),
				getAmount(),
				getCurrency() != null ? getCurrency().getCurrencyCode() : messages.getString("INVALID_CURRENCY"),
				isCurrencyConverted ? messages.getString("CURRENCY_CONVERTED") : messages.getString("CURRENCY_NOT_CONVERTED"));
		return formattedSum;
	}

	@Override
	public int compareTo(AmountAdapter o) {
		int currencyComparison = currency.getCurrencyCode().compareTo(o.currency.getCurrencyCode());
		if (currencyComparison != 0)
			return currencyComparison;
		return new Double(amount).compareTo(o.amount);
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public Currency getCurrency() {
		return currency;
	}

	@Override
	public boolean isOK() {
		return isOk;
	}
}
