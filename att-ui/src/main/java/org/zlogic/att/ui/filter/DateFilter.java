/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter;

import java.util.Date;
import java.util.ResourceBundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import javafx.util.converter.DateStringConverter;
import org.zlogic.att.ui.filter.FilterBuilder.FilterTypeComboItem;

/**
 * Filter for start/end dates
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class DateFilter implements Filter<Date> {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	//private ObservableList<Date> values = FXCollections.emptyObservableList();
	private ObservableList<Date> values = FXCollections.observableArrayList(new Date[]{new Date(), new Date()});
	private DateType type;
	private StringConverter<Date> converter = new DateStringConverter();
	private FilterBuilder.FilterTypeComboItem filterType;
	private ObjectProperty<Date> value = new SimpleObjectProperty<>();

	public enum DateType {

		DATE_START, DATE_END
	};

	public DateFilter(FilterTypeComboItem filterType, DateType type) {
		this.filterType = filterType;
		this.type = type;
	}

	@Override
	public StringConverter<Date> getConverter() {
		return converter;
	}

	@Override
	public ObservableList<Date> getAvailableValues() {
		return values;
	}

	@Override
	public boolean isAllowAnyValue() {
		return true;
	}

	@Override
	public FilterTypeComboItem getType() {
		return filterType;
	}

	@Override
	public ObjectProperty<Date> valueProperty() {
		return value;
	}
}
