/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

import javafx.beans.property.BooleanProperty;

/**
 * Interface supporting output of status
 *
 * @author Dmitry Zolotukhin
 */
public interface CellStatus {

	/**
	 * Property which equals to true true if this cell/value is OK
	 *
	 * @return true if this cell/value is OK
	 */
	BooleanProperty okProperty();
}
