/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.report;

import org.zlogic.vogon.data.Constants;

/**
 * Class for storing expenses for a specific tag
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class TagExpense {

	/**
	 * The tag name
	 */
	private final String tag;
	/**
	 * The tag's amount
	 */
	private long amount;

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
	 * @param amount the amount to be added
	 */
	protected void addRawAmount(long amount) {
		this.amount += amount;
	}

	/**
	 * Returns the tag amount
	 *
	 * @return the tag amount
	 */
	public double getAmount() {
		return amount / Constants.RAW_AMOUNT_MULTIPLIER;
	}

}
