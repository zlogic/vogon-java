/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import org.zlogic.vogon.ui.ExceptionHandler;
import org.zlogic.vogon.ui.adapter.AmountModelAdapter;

/**
 * Cell editor capable of displaying and editing an amount.
 *
 * @param <BaseType> the cell type
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
 */
public class AmountCellEditor<BaseType> extends StringCellEditor<BaseType, AmountModelAdapter> {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(AmountCellEditor.class.getName());
	/**
	 * Localization messages
	 */
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * Exception handler
	 */
	private ObjectProperty<ExceptionHandler> exceptionHandler;

	/**
	 * Creates an AmountCellEditor with a value validator
	 *
	 * @param validator the value validator
	 * @param exceptionHandler the exception handler
	 */
	public AmountCellEditor(StringCellValidator validator, ObjectProperty<ExceptionHandler> exceptionHandler) {
		super(validator, AmountModelAdapter.class, exceptionHandler);
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	protected AmountModelAdapter propertyFromString(String value) {
		try {
			return new AmountModelAdapter(Double.parseDouble(value), getItem().okProperty().get(), getItem().getCurrency(), getItem().isCurrencyConverted(), getItem().getTransactionType());
		} catch (NumberFormatException ex) {
			log.log(Level.SEVERE, null, ex);
			if (exceptionHandler.get() != null)
				exceptionHandler.get().showException(MessageFormat.format(messages.getString("CANNOT_PARSE_NUMBER"), new Object[]{value, ex.getMessage()}), ex);
		}
		return null;
	}

	@Override
	protected String getString() {
		if (getItem() == null)
			return "";//NOI18N
		if (isEditing())
			return Double.toString(getItem().getAmount());
		else
			return getItem().toString();
	}
}
