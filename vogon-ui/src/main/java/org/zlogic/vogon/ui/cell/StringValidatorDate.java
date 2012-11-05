/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validator which always returns true.
 *
 * @author Dmitry
 */
public class StringValidatorDate implements StringCellValidator {

	private String format;

	public StringValidatorDate(String format) {
		this.format = format;
	}

	@Override
	public boolean isValid(String date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		try {
			dateFormat.parse(date);
			return true;
		} catch (ParseException ex) {
			Logger.getLogger(StringValidatorDate.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
	}
}
