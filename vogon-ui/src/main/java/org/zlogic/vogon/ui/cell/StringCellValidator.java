/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

/**
 * Cell validator interface.
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
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
