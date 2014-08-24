/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import java.util.Currency;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.zlogic.vogon.data.FinanceTransaction;

/**
 * Class for displaying a virtual summary/report account (e.g. the total amount
 * for a specific currency).
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class ReportingAccount implements AccountInterface {

	/**
	 * The account name property
	 */
	private final StringProperty name = new SimpleStringProperty();
	/**
	 * The account balance property (formatted string)
	 */
	private final StringProperty balance = new SimpleStringProperty();
	/**
	 * The currency property
	 */
	private final ObjectProperty<CurrencyModelAdapter> currency = new SimpleObjectProperty<>();
	/**
	 * The property indicating if account should be included in the reporting
	 * account's total balance
	 */
	private final BooleanProperty includeInTotal = new SimpleBooleanProperty(true);

	/**
	 * Constructor for ReportingAccount
	 *
	 * @param description the account's description
	 * @param balance the account's balance
	 * @param currency the account's currency
	 */
	public ReportingAccount(String description, double balance, Currency currency) {
		this.name.set(description);
		this.balance.set(new AmountModelAdapter(balance, true, currency, false, FinanceTransaction.Type.UNDEFINED).toString());
		this.currency.set(new CurrencyModelAdapter(currency));
	}

	@Override
	public StringProperty balanceProperty() {
		return balance;
	}

	@Override
	public StringProperty nameProperty() {
		return name;
	}

	@Override
	public ObjectProperty<CurrencyModelAdapter> currencyProperty() {
		return currency;
	}

	@Override
	public BooleanProperty includeInTotalProperty() {
		return includeInTotal;
	}

	@Override
	public String toString() {
		return name.get();
	}
}
