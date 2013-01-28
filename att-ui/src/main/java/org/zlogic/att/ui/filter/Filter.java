/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

/**
 * Task filter interface
 *
 * @param <ValueType> type of filter values
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public interface Filter<ValueType> {

	/**
	 * Returns the value converter
	 *
	 * @return the value converter
	 */
	public StringConverter<ValueType> getConverter();

	/**
	 * Returns the list of suggested available values
	 *
	 * @return
	 */
	public ObservableList<ValueType> getAvailableValues();

	/**
	 * Returns true if this filter allows any values, even ones not listed in
	 * getAvailableValues()
	 *
	 * @return true if this filter allows any values
	 */
	public boolean isAllowAnyValue();

	/**
	 * Returns the filter type used to construct this filter
	 *
	 * @return the filter type
	 */
	public FilterBuilder.FilterTypeComboItem getType();

	/**
	 * The filter's value property
	 *
	 * @return the filter's value property
	 */
	public ObjectProperty<ValueType> valueProperty();
}
