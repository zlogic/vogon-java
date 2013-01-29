/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter.ui;

import javafx.beans.property.ObjectProperty;
import org.zlogic.att.ui.filter.FilterTypeFactory;

/**
 * Task filter interface
 *
 * @param <ValueType> type of filter values
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public interface Filter<ValueType> {

	/**
	 * Returns the filter type used to construct this filter
	 *
	 * @return the filter type
	 */
	public FilterTypeFactory getType();

	/**
	 * The filter's value property
	 *
	 * @return the filter's value property
	 */
	public ObjectProperty<ValueType> valueProperty();
}
