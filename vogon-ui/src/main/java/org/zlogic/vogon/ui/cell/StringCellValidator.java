/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

/**
 * Cell validator interface.
 *
 * @author Dmitry Zolotukhin
 */
public interface StringCellValidator {

	/**
	 * Returns true if the cell is valid
	 *
	 * @param value the value to be validated
	 * @return true if value is correct
	 */
	public boolean isValid(String value);
}
