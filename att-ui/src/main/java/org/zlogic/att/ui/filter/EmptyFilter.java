/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Empty filter (default value for new filters)
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class EmptyFilter implements Filter<Void> {

	private FilterTypeFactory type;
	private ObjectProperty<Void> value = new SimpleObjectProperty<>();

	public EmptyFilter(FilterTypeFactory type) {
		this.type = type;
	}

	@Override
	public FilterTypeFactory getType() {
		return type;
	}

	@Override
	public ObjectProperty<Void> valueProperty() {
		return value;
	}
}
