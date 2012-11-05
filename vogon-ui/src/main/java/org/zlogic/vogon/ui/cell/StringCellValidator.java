/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

/**
 * Cell validator interface.
 *
 * @author Dmitry
 */
public interface StringCellValidator {

	public boolean isValid(String value);
}
