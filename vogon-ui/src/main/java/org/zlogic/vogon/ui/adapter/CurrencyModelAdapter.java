/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
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
 * customization of how it's rendered.
 *
 * @author Dmitry Zolotukhin
 */
public class CurrencyModelAdapter implements Comparable<CurrencyModelAdapter> {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The currency
	 */
	protected Currency currency;
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
			return currency == ((CurrencyModelAdapter) obj).currency;
		if (obj instanceof Currency)
			return currency == (Currency) obj;
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 71 * hash + Objects.hashCode(this.currency);
		return hash;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
		updateProperties();
	}

	public StringProperty currencyProperty() {
		return value;
	}

	private void updateProperties() {
		value.set(toString());
	}

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
