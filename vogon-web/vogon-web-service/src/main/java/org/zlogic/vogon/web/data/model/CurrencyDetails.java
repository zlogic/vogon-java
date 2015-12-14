/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.data.model;

import java.util.Currency;

/**
 * JSON wrapper for Currency class, used to provide full currency data. Jackson
 * uses special serializers for currencies and it's very difficult to replace
 * them without breaking anything, so the next best thing is to create a
 * Currency-like class.
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class CurrencyDetails implements Comparable<CurrencyDetails> {

	/**
	 * The currency to wrap
	 */
	private Currency currency;

	/**
	 * Creates a CurrencyDetails wrapper for a Currency
	 *
	 * @param currency the Currency to wrap
	 */
	public CurrencyDetails(Currency currency) {
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
	 * Returns the currency symbol (Currency.getSymbol())
	 *
	 * @return the currency symbol
	 */
	public String getSymbol() {
		return currency.getSymbol();
	}

	/**
	 * Returns the currency code (Currency.getCurrencyCode())
	 *
	 * @return the currency code
	 */
	public String getCurrencyCode() {
		return currency.getCurrencyCode();
	}

	@Override
	public int compareTo(CurrencyDetails o) {
		return currency.getDisplayName().compareTo(o.currency.getDisplayName());
	}
}
