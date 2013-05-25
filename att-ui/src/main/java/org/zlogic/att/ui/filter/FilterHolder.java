/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
 */
package org.zlogic.att.ui.filter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.zlogic.att.ui.filter.adapters.FilterAdapter;

/**
 * Class for storing a filter and allowing to change the associated filter and
 * access its properties
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
 */
public class FilterHolder {

	/**
	 * The associated filter
	 */
	private ObjectProperty<FilterAdapter> filterProperty = new SimpleObjectProperty<>();
	/**
	 * The associated filter type
	 */
	private ObjectProperty<FilterTypeFactory> type = new SimpleObjectProperty<>();
	/**
	 * The associated filter value
	 */
	private ObjectProperty<Object> valueProperty = new SimpleObjectProperty<>();

	/**
	 * Created a FilterHolder
	 *
	 * @param filter the initially associated filter
	 * @param type the initially associated filter type (TODO: remove the need
	 * for this parameter)
	 */
	public FilterHolder(FilterAdapter filter, FilterTypeFactory type) {
		filterProperty.set(filter);
		valueProperty.bindBidirectional(filter.valueProperty());
		this.type.set(type);
		this.type.addListener(new ChangeListener<FilterTypeFactory>() {
			@Override
			public void changed(ObservableValue<? extends FilterTypeFactory> ov, FilterTypeFactory oldValue, FilterTypeFactory newValue) {
				valueProperty.unbindBidirectional(filterProperty.get().valueProperty());
				filterProperty.set(newValue.createFilter());
				valueProperty.bindBidirectional(filterProperty.get().valueProperty());
			}
		});
	}

	/**
	 * The associated filter property
	 *
	 * @return the associated filter property
	 */
	public ObjectProperty<FilterAdapter> filterProperty() {
		return filterProperty;
	}

	/**
	 * The associated filter type property
	 *
	 * @return the associated filter type property
	 */
	public ObjectProperty<FilterTypeFactory> typeProperty() {
		return type;
	}

	/**
	 * The associated filter value property
	 *
	 * @return the associated filter value property
	 */
	public ObjectProperty<Object> valueProperty() {
		return valueProperty;
	}
}
