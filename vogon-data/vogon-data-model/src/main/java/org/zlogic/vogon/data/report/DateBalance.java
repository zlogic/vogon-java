/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.report;

import java.text.MessageFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Class for storing a date/balance graph
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 * @param <T> balance type
 */
public class DateBalance<T extends Number> {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/data/messages");
	/**
	 * Date/balance storage map
	 */
	private Map<Date, T> dateBalance = new TreeMap<>();

	/**
	 * Balance type class
	 */
	private Class<T> balanceType;

	/**
	 * Default constructor
	 *
	 * @param balanceType the balance type class
	 */
	protected DateBalance(Class<T> balanceType) {
		this.balanceType = balanceType;
	}

	/**
	 * Returns the date/balance map
	 *
	 * @return the date/balance map
	 */
	public Map<Date, T> getData() {
		return dateBalance;
	}

	/**
	 * Sets the balance for a particular date
	 *
	 * @param date the date
	 * @param balance the balance to be set
	 */
	protected void setBalance(Date date, T balance) {
		dateBalance.put(date, balance);
	}

	/**
	 * Adds an amount to the balance for a particular date
	 *
	 * @param date the date
	 * @param add the amount to add
	 */
	protected void addBalance(Date date, T add) {
		if (!dateBalance.containsKey(date))
			dateBalance.put(date, add);
		else {
			T value = dateBalance.get(date);
			if (balanceType == Double.class || balanceType == double.class)
				dateBalance.put(date, balanceType.cast(value.doubleValue() + add.doubleValue()));
			else if (balanceType == Long.class || balanceType == long.class)
				dateBalance.put(date, balanceType.cast(value.longValue() + add.longValue()));
			else if (balanceType == Integer.class || balanceType == int.class)
				dateBalance.put(date, balanceType.cast(value.intValue() + add.intValue()));
			else
				throw new ClassCastException(new MessageFormat(messages.getString("CANNOT_PROCESS_UNSUPPORTED_NUMBER_TYPE")).format(new Object[]{balanceType.getName()}));
		}
	}
}
