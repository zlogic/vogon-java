/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter.adapters;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javax.persistence.EntityManager;
import org.zlogic.att.data.FilterTaskCompleted;
import org.zlogic.att.data.TransactedChange;
import org.zlogic.att.ui.adapters.DataManager;

/**
 * Filter for a task's completed state
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class FilterTaskCompletedAdapter implements FilterAdapter<Boolean> {

	/**
	 * The filter
	 */
	private FilterTaskCompleted filter;
	/**
	 * DataManager reference
	 */
	private DataManager dataManager;
	/**
	 * The filter value
	 */
	private ObjectProperty<Boolean> value = new SimpleObjectProperty<>();
	/**
	 * Value change listener
	 */
	private ChangeListener<Boolean> valueChangeListener = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
			if (!newValue.equals(oldValue) && getDataManager() != null) {
				getDataManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
					private boolean newValue;

					public TransactedChange setNewValue(boolean newValue) {
						this.newValue = newValue;
						return this;
					}

					@Override
					public void performChange(EntityManager entityManager) {
						setFilter(entityManager.find(FilterTaskCompleted.class, getFilter().getId()));
						getFilter().setTaskCompleted(newValue);
					}
				}.setNewValue(newValue));
				updateFxProperties();
			}
		}
	};

	/**
	 * Constructs a FilterTaskCompletedAdapter
	 *
	 * @param dataManager the DataManager reference
	 * @param filter the associated filter
	 */
	public FilterTaskCompletedAdapter(DataManager dataManager, FilterTaskCompleted filter) {
		this.dataManager = dataManager;
		this.filter = filter;
		value.addListener(valueChangeListener);
		updateFxProperties();
	}

	/*
	 * Java FX properties
	 */
	@Override
	public ObjectProperty<Boolean> valueProperty() {
		return value;
	}

	/*
	 * Getters/setters
	 */
	@Override
	public FilterTaskCompleted getFilter() {
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
	private void setFilter(FilterTaskCompleted filter) {
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
		value.set(filter != null ? filter.getTaskCompleted() : true);
		//Restore listener
		value.addListener(valueChangeListener);
	}
}
