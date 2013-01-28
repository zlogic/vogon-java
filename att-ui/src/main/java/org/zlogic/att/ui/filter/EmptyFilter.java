/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

/**
 * Empty filter (default value for new filters)
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class EmptyFilter implements Filter<Void> {

	private FilterBuilder.FilterTypeComboItem type;
	private ObjectProperty<Void> value = new SimpleObjectProperty<>();

	public EmptyFilter(FilterBuilder.FilterTypeComboItem type) {
		this.type = type;
	}

	@Override
	public StringConverter<Void> getConverter() {
		return null;
	}

	@Override
	public ObservableList<Void> getAvailableValues() {
		return null;
	}

	@Override
	public boolean isAllowAnyValue() {
		return false;
	}

	@Override
	public FilterBuilder.FilterTypeComboItem getType() {
		return type;
	}

	@Override
	public ObjectProperty<Void> valueProperty() {
		return value;
	}
}
