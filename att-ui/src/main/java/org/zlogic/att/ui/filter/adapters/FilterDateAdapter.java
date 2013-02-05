/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter.adapters;

import java.util.Date;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.StringConverter;
import javafx.util.converter.DateStringConverter;
import javax.persistence.EntityManager;
import org.zlogic.att.data.FilterDate;
import org.zlogic.att.data.TransactedChange;
import org.zlogic.att.ui.adapters.DataManager;

/**
 * Filter for before/after dates
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class FilterDateAdapter implements FilterAdapter<Object> {

	/**
	 * The filter
	 */
	private FilterDate filter;
	/**
	 * DataManager reference
	 */
	private DataManager dataManager;
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
	 * The filter value
	 */
	private ObjectProperty<Object> value = new SimpleObjectProperty<>();
	/**
	 * Value change listener
	 */
	private ChangeListener<Object> valueChangeListener = new ChangeListener<Object>() {
		@Override
		public void changed(ObservableValue<? extends Object> ov, Object oldValue, Object newValue) {
			if (newValue instanceof Date && !newValue.equals(oldValue) && getDataManager() != null) {
				Date newValueDate = (Date) newValue;
				getDataManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
					private Date newValue;

					public TransactedChange setNewValue(Date newValue) {
						this.newValue = newValue;
						return this;
					}

					@Override
					public void performChange(EntityManager entityManager) {
						setFilter(entityManager.find(FilterDate.class, getFilter().getId()));
						getFilter().setAppliedDate(newValue);
					}
				}.setNewValue(newValueDate));
				updateFxProperties();
			}
		}
	};

	/**
	 * Constructs a CompletedFilter
	 *
	 * @param dataManager the DataManager reference
	 * @param filter the underlying filter
	 */
	public FilterDateAdapter(DataManager dataManager, FilterDate filter) {
		this.dataManager = dataManager;
		this.filter = filter;
		updateFxProperties();
		value.addListener(valueChangeListener);
	}

	/*
	 * Java FX properties
	 */
	@Override
	public ObjectProperty<Object> valueProperty() {
		return value;
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the value converter
	 *
	 * @return the value converter
	 */
	public StringConverter<Object> getConverter() {
		return converter;
	}

	@Override
	public FilterDate getFilter() {
		return filter;
	}
	/*
	 * Internal methods
	 */

	/**
	 * Updates the associated filter
	 *
	 * @param filter the new (or updated) filter
	 */
	private void setFilter(FilterDate filter) {
		this.filter = filter;
	}

	/**
	 * Returns the DataManager reference
	 *
	 * @return the DataManager reference
	 */
	private DataManager getDataManager() {
		return dataManager;
	}

	/**
	 * Updates Java FX properties from the associated entity
	 */
	private void updateFxProperties() {
		//Remove listener since the update is initiated by us
		value.removeListener(valueChangeListener);
		//Perform update
		value.setValue(filter != null
				? (filter.getAppliedDate() != null ? filter.getAppliedDate() : new Date())
				: new Date());
		//Restore listener
		value.addListener(valueChangeListener);
	}
}
