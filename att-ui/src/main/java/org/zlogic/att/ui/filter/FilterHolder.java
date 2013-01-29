/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.zlogic.att.ui.filter.ui.Filter;

/**
 * Class for storing a filter and allowing to change the underlying filter and
 * access its properties
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class FilterHolder {

	/**
	 * The associated filter
	 */
	private ObjectProperty<Filter> filterProperty = new SimpleObjectProperty<>();
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
	 */
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

	/**
	 * The associated filter property
	 *
	 * @return the associated filter property
	 */
	public ObjectProperty<Filter> filterProperty() {
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
