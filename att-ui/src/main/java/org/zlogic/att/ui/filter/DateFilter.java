/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter;

import java.util.Date;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.StringConverter;
import javafx.util.converter.DateStringConverter;
import org.zlogic.att.ui.filter.ui.FilterTextValue;

/**
 * Filter for start/end dates
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class DateFilter implements FilterTextValue<Object> {

	/**
	 * The date type
	 */
	private DateType type;
	/**
	 * The Date <-> String converter
	 */
	private StringConverter<Object> converter = new StringConverter<Object>() {
		private DateStringConverter delegateDateConverter = new DateStringConverter();

		@Override
		public String toString(Object t) {
			if (t instanceof String)
				return delegateDateConverter.toString(new Date());
			if (t instanceof Date)
				return delegateDateConverter.toString((Date) t);

			return null;
		}

		@Override
		public Object fromString(String string) {
			return delegateDateConverter.fromString(string);
		}
	};
	/**
	 * The filter type
	 */
	private FilterTypeFactory filterType;
	/**
	 * The filter value
	 */
	private ObjectProperty<Object> value = new SimpleObjectProperty<>();

	/**
	 * The date filter type
	 */
	public enum DateType {

		/**
		 * This is a start date filter
		 */
		DATE_START,
		/**
		 * This is an end date filter
		 */
		DATE_END
	};

	/**
	 * Constructs a CompletedFilter
	 *
	 * @param filterType the filter type (creator of this object)
	 * @param type the date type
	 */
	public DateFilter(FilterTypeFactory filterType, DateType type) {
		this.filterType = filterType;
		this.type = type;
		this.value.set(new Date());
	}

	@Override
	public FilterTypeFactory getType() {
		return filterType;
	}

	@Override
	public ObjectProperty<Object> valueProperty() {
		return value;
	}

	@Override
	public StringConverter<Object> getConverter() {
		return converter;
	}
}
