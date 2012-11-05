/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author Dmitry
 */
public class DateCellEditor<BaseType> extends StringCellEditor<BaseType, Date> {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	protected SimpleDateFormat dateFormat;

	public DateCellEditor(StringCellValidator validator) {
		super(validator);
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
			return "";
		if (isEditing())
			return MessageFormat.format(messages.getString("FORMAT_DATE"), new Object[]{getItem()});
		else
			return DateFormat.getDateInstance(DateFormat.LONG).format(getItem());
	}
}
