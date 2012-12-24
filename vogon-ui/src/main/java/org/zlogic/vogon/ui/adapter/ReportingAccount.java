/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
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
 * @author Dmitry Zolotukhin
 */
public class ReportingAccount implements AccountInterface {

	/**
	 * The account name property
	 */
	private final ObjectProperty<ObjectWithStatus<String, Boolean>> name = new SimpleObjectProperty<>();
	/**
	 * The account balance property (formatted string)
	 */
	private final StringProperty balance = new SimpleStringProperty();
	/**
	 * The currency property
	 */
	private final ObjectProperty<ObjectWithStatus<CurrencyModelAdapter, Boolean>> currency = new SimpleObjectProperty<>();
	/**
	 * The property indicating if account should be included in the reporting
	 * account's total balance
	 */
	private final ObjectProperty<ObjectWithStatus<BooleanProperty, Boolean>> includeInTotal = new SimpleObjectProperty<ObjectWithStatus<BooleanProperty, Boolean>>(new ObjectWithStatus<BooleanProperty, Boolean>(new SimpleBooleanProperty(true), false));

	/**
	 * Constructor for ReportingAccount
	 *
	 * @param description the account's description
	 * @param balance the account's balance
	 * @param currency the account's currency
	 */
	public ReportingAccount(String description, double balance, Currency currency) {
		this.name.set(new ObjectWithStatus<>(description, false));
		this.balance.set(new AmountModelAdapter(balance, true, currency, false, FinanceTransaction.Type.UNDEFINED).toString());
		this.currency.set(new ObjectWithStatus<>(new CurrencyModelAdapter(currency), false));
	}

	@Override
	public StringProperty balanceProperty() {
		return balance;
	}

	@Override
	public ObjectProperty<ObjectWithStatus<String, Boolean>> nameProperty() {
		return name;
	}

	@Override
	public ObjectProperty<ObjectWithStatus<CurrencyModelAdapter, Boolean>> currencyProperty() {
		return currency;
	}

	@Override
	public ObjectProperty<ObjectWithStatus<BooleanProperty, Boolean>> includeInTotalProperty() {
		return includeInTotal;
	}
}
