/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter.adapters;

import javafx.beans.property.ObjectProperty;
import org.zlogic.att.data.Filter;

/**
 * Task filter adapter interface
 *
 * @param <ValueType> type of filter values
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
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
