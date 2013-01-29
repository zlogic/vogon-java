/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.zlogic.att.ui.filter.ui.FilterBooleanValue;

/**
 * Filter for a task's compeled state
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class CompletedFilter implements FilterBooleanValue {

	/**
	 * The filter type
	 */
	private FilterTypeFactory filterType;
	/**
	 * The filter value
	 */
	private ObjectProperty<Boolean> value = new SimpleObjectProperty<>();

	/**
	 * Constructs a CompletedFilter
	 *
	 * @param filterType the filter type (creator of this object)
	 */
	public CompletedFilter(FilterTypeFactory filterType) {
		value.set(true);
		this.filterType = filterType;
	}

	@Override
	public FilterTypeFactory getType() {
		return filterType;
	}

	@Override
	public ObjectProperty<Boolean> valueProperty() {
		return value;
	}
}
