/*
 * Awesome Time Tracker project.
 * License TBD.
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
import org.zlogic.att.ui.adapters.TaskManager;

/**
 * Filter for a task's completed state
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class FilterTaskCompletedAdapter implements FilterAdapter<Boolean> {

	/**
	 * The filter
	 */
	private FilterTaskCompleted filter;
	/**
	 * TaskManager reference
	 */
	private TaskManager taskManager;
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
			if (!newValue.equals(oldValue) && getTaskManager() != null) {
				getTaskManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
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
	 * @param taskManager the TaskManager reference
	 * @param filter the associated filter
	 */
	public FilterTaskCompletedAdapter(TaskManager taskManager, FilterTaskCompleted filter) {
		this.taskManager = taskManager;
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
	 * Returns the TaskManager reference
	 *
	 * @return the TaskManager reference
	 */
	private TaskManager getTaskManager() {
		return taskManager;
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
