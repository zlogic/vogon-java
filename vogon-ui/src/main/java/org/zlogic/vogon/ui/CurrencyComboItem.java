/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zlogic.vogon.ui;

import java.util.Currency;
import java.util.ResourceBundle;

/**
 * Class for storing a Currency with an overloaded toString method for better
 * customization of how it's rendered in a combo box.
 *
 * @author Zlogic
 */
public class CurrencyComboItem {

	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The currency
	 */
	protected Currency currency;

	/**
	 * Default constructor
	 *
	 * @param currency the currency for this item
	 */
	public CurrencyComboItem(Currency currency) {
		this.currency = currency;
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
		if (obj instanceof CurrencyComboItem)
			return currency == ((CurrencyComboItem) obj).currency;
		if (obj instanceof Currency)
			return currency == (Currency) obj;
		return false;
	}

	/**
	 * Returns the currency
	 *
	 * @return the currency
	 */
	public Currency getCurrency() {
		return currency;
	}
}
