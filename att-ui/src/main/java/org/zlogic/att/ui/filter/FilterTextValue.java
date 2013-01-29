/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter;

import javafx.util.StringConverter;

/**
 * Task filter interface for filters whose value can be edited in a text box
 *
 * @param <ValueType> type of filter values
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public interface FilterTextValue<ValueType> extends Filter<ValueType> {

	/**
	 * Returns the value converter
	 *
	 * @return the value converter
	 */
	public StringConverter<ValueType> getConverter();
}
