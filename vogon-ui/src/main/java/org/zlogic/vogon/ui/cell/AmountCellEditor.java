/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zlogic.vogon.ui.cell;

import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Pos;
import org.zlogic.vogon.ui.AmountAdapter;

/**
 *
 * @author Dmitry
 */
public class AmountCellEditor<BaseType> extends StringCellEditor<BaseType, AmountAdapter> {
	public AmountCellEditor(StringCellValidator validator) {
		super(validator);
	}
	public AmountCellEditor(StringCellValidator validator,Pos alignment) {
		super(validator,alignment);
	}

	@Override
	protected AmountAdapter propertyFromString(String value) {
		try {
			return new AmountAdapter(Double.parseDouble(value));
		} catch (NumberFormatException ex) {
			Logger.getLogger(StringValidatorDate.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	protected String getString() {
		if (getItem() == null)
			return "";
		if (isEditing())
			return Double.toString(getItem().getBalance());
		else
			return getItem().toString();
	}
}
