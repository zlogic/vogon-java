/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

/**
 * Interface supporting output of status
 *
 * @author Dmitry Zolotukhin
 */
public interface CellStatus {

	/**
	 * Returns true if this cell/value is OK
	 *
	 * @return true if this cell/value is OK
	 */
	boolean isOK();
}
