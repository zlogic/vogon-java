/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validator which validates integer/double values.
 *
 * @author Dmitry Zolotukhin
 */
public class StringValidatorDouble implements StringCellValidator {

	@Override
	public boolean isValid(String value) {
		try {
			Double.parseDouble(value);
			return true;
		} catch (NumberFormatException ex) {
			Logger.getLogger(StringValidatorDouble.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
	}
}
