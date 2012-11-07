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
 * Validator which validates dates.
 *
 * @author Dmitry Zolotukhin
 */
public class StringValidatorDate implements StringCellValidator {
	/*
	 * The date format to be used
	 */

	private String format;

	/**
	 * Constructs a StringValidatorDate with a specific date format
	 *
	 * @param format the date format for validation
	 */
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
