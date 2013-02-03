/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter.adapters;

import javafx.beans.property.ObjectProperty;
import org.zlogic.att.data.Filter;

/**
 * Task filter adapter interface
 *
 * @param <ValueType> type of filter values
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public interface FilterAdapter<ValueType> {

	/**
	 * Returns the associated filter
	 *
	 * @return the associated filter
	 */
	public Filter getFilter();

	/**
	 * The filter's value property
	 *
	 * @return the filter's value property
	 */
	public ObjectProperty<ValueType> valueProperty();
}
