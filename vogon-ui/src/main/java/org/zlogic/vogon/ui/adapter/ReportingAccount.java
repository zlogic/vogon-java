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

/**
 *
 * @author Dmitry Zolotukhin
 */
public class ReportingAccount implements AccountInterface {

	private final ObjectProperty<ObjectWithStatus<String, Boolean>> name = new SimpleObjectProperty();
	private final StringProperty balance = new SimpleStringProperty();
	private final ObjectProperty<ObjectWithStatus<CurrencyModelAdapter, Boolean>> currency = new SimpleObjectProperty();
	private final ObjectProperty<ObjectWithStatus<BooleanProperty, Boolean>> includeInTotal = new SimpleObjectProperty(new ObjectWithStatus<>(new SimpleBooleanProperty(true), false));

	public ReportingAccount(String description, double balance, Currency currency) {
		this.name.set(new ObjectWithStatus<>(description, false));
		this.balance.set(Double.toString(balance));
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
