/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zlogic.att.ui.filter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Dmitry
 */
public class FilterHolder {

	private ObjectProperty<Filter> filterProperty = new SimpleObjectProperty<>();
	private ObjectProperty<FilterTypeFactory> type = new SimpleObjectProperty<>();
	private ObjectProperty<Object> valueProperty = new SimpleObjectProperty<>();

	public FilterHolder(Filter filter) {
		filterProperty.set(filter);
		valueProperty.bindBidirectional(filter.valueProperty());
		type.set(filter.getType());
		type.addListener(new ChangeListener<FilterTypeFactory>() {
			@Override
			public void changed(ObservableValue<? extends FilterTypeFactory> ov, FilterTypeFactory oldValue, FilterTypeFactory newValue) {
				valueProperty.unbindBidirectional(filterProperty.get().valueProperty());
				filterProperty.set(newValue.createFilter());
				valueProperty.bindBidirectional(filterProperty.get().valueProperty());
			}
		});
	}

	public ObjectProperty<Filter> filterProperty() {
		return filterProperty;
	}

	public ObjectProperty<FilterTypeFactory> typeProperty() {
		return type;
	}

	public ObjectProperty<Object> valueProperty() {
		return valueProperty;
	}
}
