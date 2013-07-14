/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
 */
package org.zlogic.att.data.reporting;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Class with useful date-related functions
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
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
	public Date convertDateToStartOfDay(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
		calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
		calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
		calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
		return calendar.getTime();
	}

	/**
	 * Returns the date with the time set to the end of the day (23:59:59)
	 *
	 * @param date the date
	 * @return the date with the time set to the end of the day
	 */
	public Date convertDateToEndOfDay(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
		calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
		calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
		calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
		return calendar.getTime();
	}

	/**
	 * Returns the date with the time set to the start of the month (day 1,
	 * 00:00:00)
	 *
	 * @param date the date
	 * @return the date with the time set to the end of the month
	 */
	public Date convertDateToStartOfMonth(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
		return convertDateToStartOfDay(calendar.getTime());
	}

	/**
	 * Returns the date with the time set to the end of the month (last day,
	 * 23:59:59)
	 *
	 * @param date the date
	 * @return the date with the time set to the end of the month
	 */
	public Date convertDateToEndOfMonth(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		return convertDateToEndOfDay(calendar.getTime());
	}
}
