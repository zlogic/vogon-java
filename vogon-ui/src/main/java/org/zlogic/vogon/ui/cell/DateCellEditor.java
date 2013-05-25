/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import org.zlogic.vogon.ui.ExceptionHandler;

/**
 * String cell editor with date validation & parsing
 *
 * @param <BaseType> the row type
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
 */
public class DateCellEditor<BaseType> extends StringCellEditor<BaseType, Date> {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(DateCellEditor.class.getName());
	/**
	 * Localization messages
	 */
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * Exception handler
	 */
	private ObjectProperty<ExceptionHandler> exceptionHandler;
	/**
	 * The date format to be used for validation
	 */
	protected SimpleDateFormat dateFormat;

	/**
	 * Creates a date editor
	 *
	 * @param exceptionHandler the exception handler
	 */
	public DateCellEditor(ObjectProperty<ExceptionHandler> exceptionHandler) {
		super(new StringValidatorDate(java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages").getString("PARSER_DATE"), exceptionHandler), Date.class, exceptionHandler);
		this.exceptionHandler = exceptionHandler;
		dateFormat = new SimpleDateFormat(messages.getString("PARSER_DATE"));
	}

	@Override
	protected Date propertyFromString(String value) {
		try {
			return dateFormat.parse(value);
		} catch (ParseException ex) {
			log.log(Level.SEVERE, null, ex);
			if (exceptionHandler.get() != null)
				exceptionHandler.get().showException(MessageFormat.format(messages.getString("CANNOT_PARSE_DATE"), new Object[]{value, ex.getMessage()}), ex);
		}
		return null;
	}

	@Override
	protected String getString() {
		if (getItem() == null)
			return "";//NOI18N
		if (isEditing())
			return MessageFormat.format(messages.getString("FORMAT_DATE"), new Object[]{getItem()});
		else
			return DateFormat.getDateInstance(DateFormat.LONG).format(getItem());
	}
}
