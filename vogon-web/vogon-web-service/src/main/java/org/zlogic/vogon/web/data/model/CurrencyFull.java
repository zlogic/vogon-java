/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.data.model;

import java.util.Currency;

/**
 * JSON wrapper for Currency class class, used to provide full currency data
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class CurrencyFull implements Comparable<CurrencyFull> {

	/**
	 * The currency to wrap
	 */
	private Currency currency;

	/**
	 * Creates a CurrencyFull wrapper for a Currency
	 *
	 * @param currency the Currency to wrap
	 */
	public CurrencyFull(Currency currency) {
		this.currency = currency;
	}

	/**
	 * Returns the currency display name (Currency.getDisplayName())
	 *
	 * @return the currency display name
	 */
	public String getDisplayName() {
		return currency.getDisplayName();
	}

	/**
	 * Returns the currency symbol (Currency.getDisplayName())
	 *
	 * @return the currency symbol
	 */
	public String getSymbol() {
		return currency.getSymbol();
	}

	@Override
	public int compareTo(CurrencyFull o) {
		return currency.getDisplayName().compareTo(o.currency.getDisplayName());
	}
}
