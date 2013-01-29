/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter.ui;

import javafx.collections.ObservableList;

/**
 * Task filter interface for filters whose value can be edited selected from a
 * list (combo box)
 *
 * @param <ValueType> type of filter values
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public interface FilterSelectableValue<ValueType> extends Filter<ValueType> {

	/**
	 * Returns the list of allowed values
	 *
	 * @return the list of allowed values
	 */
	public ObservableList<ValueType> getAllowedValues();
}
