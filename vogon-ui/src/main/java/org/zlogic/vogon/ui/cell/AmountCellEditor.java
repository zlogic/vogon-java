/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.zlogic.vogon.ui.adapter.AmountModelAdapter;

/**
 * Cell editor capable of displaying and editing an amount.
 *
 * @param <BaseType> the cell type
 * @author Dmitry Zolotukhin
 */
public class AmountCellEditor<BaseType> extends StringCellEditor<BaseType, AmountModelAdapter> {

	/**
	 * Creates an AmountCellEditor with a value validator
	 *
	 * @param validator the value validator
	 */
	public AmountCellEditor(StringCellValidator validator) {
		super(validator, AmountModelAdapter.class);
	}

	@Override
	protected AmountModelAdapter propertyFromString(String value) {
		try {
			return new AmountModelAdapter(Double.parseDouble(value), getItem().isOK(), getItem().getCurrency(), getItem().isCurrencyConverted(), getItem().getTransactionType());
		} catch (NumberFormatException ex) {
			Logger.getLogger(StringValidatorDate.class.getName()).log(Level.SEVERE, null, ex);
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
