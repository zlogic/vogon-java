/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
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

/**
 * String cell editor with date validation & parsing
 *
 * @param <BaseType> the row type
 * @author Dmitry Zolotukhin
 */
public class DateCellEditor<BaseType> extends StringCellEditor<BaseType, Date> {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The date format to be used for validation
	 */
	protected SimpleDateFormat dateFormat;

	/**
	 * Creates a date editor
	 */
	public DateCellEditor() {
		super(new StringValidatorDate(java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages").getString("PARSER_DATE")),Date.class);
		dateFormat = new SimpleDateFormat(messages.getString("PARSER_DATE"));
	}

	@Override
	protected Date propertyFromString(String value) {
		try {
			return dateFormat.parse(value);
		} catch (ParseException ex) {
			Logger.getLogger(StringValidatorDate.class.getName()).log(Level.SEVERE, null, ex);
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
