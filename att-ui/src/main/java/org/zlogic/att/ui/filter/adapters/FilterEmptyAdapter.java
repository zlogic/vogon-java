/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter.adapters;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.zlogic.att.data.Filter;

/**
 * Empty filter (default value for new filters)
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class FilterEmptyAdapter implements FilterAdapter<Void> {

	/**
	 * The filter value
	 */
	private ObjectProperty<Void> value = new SimpleObjectProperty<>();

	/**
	 * Constructs an FilterEmptyAdapter
	 *
	 */
	public FilterEmptyAdapter() {
	}

	@Override
	public ObjectProperty<Void> valueProperty() {
		return value;
	}

	@Override
	public Filter getFilter() {
		return null;
	}
}
