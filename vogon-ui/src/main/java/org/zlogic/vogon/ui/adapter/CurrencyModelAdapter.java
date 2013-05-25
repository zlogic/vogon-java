/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import java.util.Collections;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Class for storing an currency with an overloaded toString method for better
 * customization of how it's rendered in a control.
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
 */
public class CurrencyModelAdapter implements Comparable<CurrencyModelAdapter> {

	/**
	 * Localization messages
	 */
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The currency
	 */
	protected Currency currency;
	/**
	 * The currency property (formatted as a string)
	 */
	private final StringProperty value = new SimpleStringProperty();

	/**
	 * Default constructor
	 *
	 * @param currency the currency for this item
	 */
	public CurrencyModelAdapter(Currency currency) {
		this.currency = currency;
		updateProperties();
	}

	@Override
	public String toString() {
		if (currency != null)
			return currency.getDisplayName();
		else
			return messages.getString("INVALID_CURRENCY");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CurrencyModelAdapter)
			return currency == ((CurrencyModelAdapter) obj).currency && value.get().equals(((CurrencyModelAdapter) obj).value.get());
		if (obj instanceof Currency)
			return currency == (Currency) obj;
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 59 * hash + Objects.hashCode(this.currency);
		hash = 59 * hash + Objects.hashCode(this.value);
		return hash;
	}

	/**
	 * Returns the currency
	 *
	 * @return the currency
	 */
	public Currency getCurrency() {
		return currency;
	}

	/**
	 * Sets the currency and updates the currency property
	 *
	 * @param currency the currency
	 */
	public void setCurrency(Currency currency) {
		this.currency = currency;
		updateProperties();
	}

	/**
	 * Returns the currency name property
	 *
	 * @return the currency name property
	 */
	public StringProperty currencyProperty() {
		return value;
	}

	/**
	 * Updates the properties from the current currency, causing ChangeListeners
	 * to trigger.
	 */
	private void updateProperties() {
		value.set(toString());
	}

	/**
	 * Returns a sorted list of all currencies
	 *
	 * @return a sorted list of all currencies
	 */
	public static List<CurrencyModelAdapter> getCurrenciesList() {
		List<CurrencyModelAdapter> items = new LinkedList<>();
		for (Currency curr : Currency.getAvailableCurrencies())
			items.add(new CurrencyModelAdapter(curr));

		Collections.sort(items);
		return items;
	}

	@Override
	public int compareTo(CurrencyModelAdapter obj) {
		if (currency == null && obj.currency != null)
			return -1;
		else if (currency != null && (obj == null || obj.currency == null))
			return 1;
		else
			return currency.getCurrencyCode().compareTo(obj.currency.getCurrencyCode());
	}
}
