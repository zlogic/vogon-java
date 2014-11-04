/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.report;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for storing expenses for a specific tag
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class TagExpense {

	/**
	 * The tag name
	 */
	protected String tag;
	/**
	 * The tag's amount
	 */
	protected Map<Currency, Double> amounts = new HashMap<>();

	/**
	 * Default constructor
	 *
	 * @param tag the tag name
	 */
	protected TagExpense(String tag) {
		this.tag = tag;
	}

	/**
	 * Returns the set tag
	 *
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * Adds an amount to this tag
	 *
	 * @param currency the currency to be updated
	 * @param amount the amount to be added
	 */
	protected void addAmount(Currency currency, double amount) {
		if (amounts.containsKey(currency))
			amounts.put(currency, amounts.get(currency) + amount);
		else
			amounts.put(currency, amount);
	}

	;
	
	/**
	 * Returns the tag amount
	 *
	 * @return the tag amount
	 */
	public Map<Currency, Double> getAmounts() {
		return amounts;
	}

}
