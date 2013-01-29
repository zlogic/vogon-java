/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter;

import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.StringConverter;
import javafx.util.converter.DateStringConverter;

/**
 * Filter for start/end dates
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class DateFilter implements FilterTextValue<Object> {

	private final static Logger log = Logger.getLogger(DateFilter.class.getName());
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	private DateType type;
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
	private FilterTypeFactory filterType;
	private ObjectProperty<Object> value = new SimpleObjectProperty<>();

	public enum DateType {

		DATE_START, DATE_END
	};

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
