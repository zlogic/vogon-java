/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.util.StringConverter;
import org.zlogic.vogon.ui.ExceptionHandler;

/**
 * Simple date converter for table cells.
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class DateConverter extends StringConverter<Date> {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(DateConverter.class.getName());
	/**
	 * Localization messages
	 */
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * Exception handler
	 */
	private ObjectProperty<ExceptionHandler> exceptionHandler;
	/**
	 * The date format
	 */
	protected DateFormat format;

	/**
	 * Constructs the converter with a specific date format
	 *
	 * @param format the date format for parsing
	 * @param exceptionHandler the exception handler
	 */
	public DateConverter(DateFormat format, ObjectProperty<ExceptionHandler> exceptionHandler) {
		this.format = format;
		this.exceptionHandler = exceptionHandler;
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
			log.log(Level.SEVERE, null, ex);
			if (exceptionHandler.get() != null)
				exceptionHandler.get().showException(MessageFormat.format(messages.getString("CANNOT_PARSE_DATE"), new Object[]{string, ex.getMessage()}), ex);
			return null;
		}
	}
}
