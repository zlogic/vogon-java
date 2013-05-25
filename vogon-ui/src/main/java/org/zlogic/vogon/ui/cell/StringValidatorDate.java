/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
 */
package org.zlogic.vogon.ui.cell;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import org.zlogic.vogon.ui.ExceptionHandler;

/**
 * Validator which validates dates.
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
 */
public class StringValidatorDate implements StringCellValidator {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(StringValidatorDate.class.getName());
	/**
	 * Localization messages
	 */
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * Exception handler
	 */
	private ObjectProperty<ExceptionHandler> exceptionHandler;
	/**
	 * The date format to be used
	 */
	private String format;

	/**
	 * Constructs a StringValidatorDate with a specific date format
	 *
	 * @param format the date format for validation
	 * @param exceptionHandler the exception handler
	 */
	public StringValidatorDate(String format, ObjectProperty<ExceptionHandler> exceptionHandler) {
		this.format = format;
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	public boolean isValid(String date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		try {
			dateFormat.parse(date);
			return true;
		} catch (ParseException ex) {
			log.log(Level.SEVERE, null, ex);
			if (exceptionHandler.get() != null)
				exceptionHandler.get().showException(MessageFormat.format(messages.getString("CANNOT_PARSE_DATE"), new Object[]{date, ex.getMessage()}), ex);
			return false;
		}
	}
}
