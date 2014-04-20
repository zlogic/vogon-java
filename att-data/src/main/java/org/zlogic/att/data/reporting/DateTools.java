/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data.reporting;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * Class with useful date-related functions
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class DateTools {

	/**
	 * The singleton class instance
	 */
	private static DateTools instance;

	/**
	 * Protected default constructor (for singleton instance)
	 */
	private DateTools() {
	}

	/**
	 * Returns the singleton instance of this class
	 *
	 * @return the singleton instance of this class
	 */
	public static DateTools getInstance() {
		if (instance == null)
			instance = new DateTools();
		return instance;
	}

	/**
	 * Returns the date with the time set to the start of the day (00:00:00)
	 *
	 * @param date the date
	 * @return the date with the time set to the start of the day
	 */
	public Date convertDateToStartOfDay(LocalDate date) {
		return Date.from(date.atStartOfDay().toInstant(ZoneOffset.UTC));
	}

	/**
	 * Returns the date with the time set to the end of the day (23:59:59)
	 *
	 * @param date the date
	 * @return the date with the time set to the end of the day
	 */
	public Date convertDateToEndOfDay(LocalDate date) {
		return Date.from(date.plusDays(1).atStartOfDay().minusSeconds(1).toInstant(ZoneOffset.UTC));
	}

	/**
	 * Returns the date with the time set to the end of the month (last day,
	 * 23:59:59)
	 *
	 * @param date the date
	 * @return the date with the time set to the end of the month
	 */
	public LocalDate convertDateToStartOfMonth(LocalDate date) {
		return date.withDayOfMonth(1);
	}

	/**
	 * Returns the date with the time set to the end of the month (last day,
	 * 23:59:59)
	 *
	 * @param date the date
	 * @return the date with the time set to the end of the month
	 */
	public LocalDate convertDateToEndOfMonth(LocalDate date) {
		return date.withDayOfMonth(date.lengthOfMonth());
	}
}
