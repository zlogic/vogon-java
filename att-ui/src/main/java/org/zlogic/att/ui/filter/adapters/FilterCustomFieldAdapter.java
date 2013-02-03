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
import javafx.collections.ObservableList;
import javax.persistence.EntityManager;
import org.zlogic.att.data.FilterCustomField;
import org.zlogic.att.data.TransactedChange;
import org.zlogic.att.ui.adapters.CustomFieldAdapter;
import org.zlogic.att.ui.adapters.TaskManager;

/**
 * Filter for custom fields
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class FilterCustomFieldAdapter implements FilterAdapter<String> {

	/**
	 * The filter
	 */
	private FilterCustomField filter;
	/**
	 * TaskManager reference
	 */
	private TaskManager taskManager;
	/**
	 * The associated custom field
	 */
	private CustomFieldAdapter customField;
	/**
	 * The filter value
	 */
	private ObjectProperty<String> value = new SimpleObjectProperty<>();
	/**
	 * Value change listener
	 */
	private ChangeListener<String> valueChangeListener = new ChangeListener<String>() {
		@Override
		public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
			oldValue = oldValue == null ? "" : oldValue; //NOI18N
			newValue = newValue == null ? "" : newValue; //NOI18N
			if (!oldValue.equals(newValue) && getTaskManager() != null) {
				getTaskManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
					private String newValue;

					public TransactedChange setNewValue(String newValue) {
						this.newValue = newValue;
						return this;
					}

					@Override
					public void performChange(EntityManager entityManager) {
						setFilter(entityManager.find(FilterCustomField.class, getFilter().getId()));
						getFilter().setCustomFieldValue(newValue);
					}
				}.setNewValue(newValue));
				updateFxProperties();
			}
		}
	};

	/**
	 * Constructs a CompletedFilter
	 *
	 * @param taskManager the TaskManager reference
	 * @param filter the associated filter
	 */
	public FilterCustomFieldAdapter(TaskManager taskManager, FilterCustomField filter) {
		this.taskManager = taskManager;
		this.filter = filter;
		((FilterCustomFieldAdapter) this).updateCustomFieldAdapter();
		updateFxProperties();
		this.value.addListener(valueChangeListener);
	}

	/**
	 * Updates the CustomFieldAdapter form TaskManager (in case the list of
	 * CustomFieldAdapters is reloaded).
	 */
	public void updateCustomFieldAdapter() {
		for (CustomFieldAdapter customFieldAdapter : taskManager.getCustomFields())
			if (customFieldAdapter.getCustomField().equals(filter.getCustomField())) {
				this.customField = customFieldAdapter;
				break;
			}
	}

	/*
	 * Java FX properties
	 */
	@Override
	public ObjectProperty<String> valueProperty() {
		return value;
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the list of allowed values
	 *
	 * @return the list of allowed values
	 */
	public ObservableList<String> getAllowedValues() {
		return taskManager.getCustomFieldValues(customField);
	}

	@Override
	public FilterCustomField getFilter() {
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
	private void setFilter(FilterCustomField filter) {
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
		this.value.set(filter != null
				? (filter.getCustomFieldValue() != null ? filter.getCustomFieldValue() : "")//NOI18N
				: ""); //NOI18N
		//Restore listener
		value.addListener(valueChangeListener);
	}
}
