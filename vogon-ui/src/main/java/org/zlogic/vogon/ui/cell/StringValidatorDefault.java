/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

/**
 * Validator which always returns true.
 *
 * @author Dmitry
 */
public class StringValidatorDefault implements StringCellValidator {

	@Override
	public boolean isValid(String value) {
		return true;
	}
}
