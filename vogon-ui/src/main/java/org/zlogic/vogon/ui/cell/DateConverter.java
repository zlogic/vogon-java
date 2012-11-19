/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.util.StringConverter;

/**
 * Simple date converter for table cells.
 *
 * @author Dmitry Zolotukhin
 */
public class DateConverter extends StringConverter<Date> {

	/**
	 * The date format
	 */
	protected DateFormat format;

	/**
	 * Constructs the converter with a specific date format
	 *
	 * @param format
	 */
	public DateConverter(DateFormat format) {
		this.format = format;
	}

	/**
	 * Converts the date to string
	 *
	 * @param t the date
	 * @return the date formatted to string
	 */
	@Override
	public String toString(Date t) {
		return format.format(t);
	}

	/**
	 * Parses a string to get the date
	 *
	 * @param string the string to be parsed
	 * @return the date (or null in case parsing failed)
	 */
	@Override
	public Date fromString(String string) {
		try {
			return format.parse(string);
		} catch (ParseException ex) {
			Logger.getLogger(DateConverter.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
}
