/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.adapters;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javax.persistence.EntityManager;
import org.zlogic.att.data.CustomField;
import org.zlogic.att.data.Task;
import org.zlogic.att.data.TransactedChange;

/**
 * Adapter to interface JPA with Java FX observable properties for CustomField
 * classes with values.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class CustomFieldValueAdapter {

	/**
	 * Associated Custom Field adapter
	 */
	private CustomFieldAdapter customFieldAdapter;
	/**
	 * Associated Task
	 */
	private TaskAdapter taskAdapter;
	/*
	 * Java FX
	 */
	/**
	 * The field's value
	 */
	private StringProperty value = new SimpleStringProperty();
	/*
	 * Change listeners
	 */
	/**
	 * Change listener for value
	 */
	private ChangeListener<String> valueChangeListener = new ChangeListener<String>() {
		@Override
		public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
			oldValue = oldValue == null ? "" : oldValue;
			if (newValue != null && !oldValue.equals(newValue) && getTaskManager() != null) {
				getTaskManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
					private String newValue;
					private CustomFieldAdapter customFieldAdapter;

					public TransactedChange setNewValue(CustomFieldAdapter customFieldAdapter, String newValue) {
						this.customFieldAdapter = customFieldAdapter;
						this.newValue = newValue;
						return this;
					}

					@Override
					public void performChange(EntityManager entityManager) {
						CustomField foundCustomField = entityManager.find(CustomField.class, customFieldAdapter.getCustomField().getId());
						(entityManager.find(Task.class, getTask().getTask().getId())).setCustomField(foundCustomField, newValue);
						getTask().getTask().setCustomField(foundCustomField, newValue);
					}
				}.setNewValue(getCustomField(), newValue));
				getTask().updateFromDatabase();
				updateFxProperties();
				getTaskManager().removeCustomFieldValue(getCustomField(), oldValue);
				getTaskManager().addCustomFieldValue(getCustomField(), newValue);
			}
		}
	};
	/**
	 * TaskManager reference
	 */
	private TaskManager taskManager;

	/**
	 * Creates a CustomFieldValueAdapter instance
	 *
	 * @param customFieldAdapter the associated CustomFieldAdapter reference
	 * @param taskManager the TaskManager reference
	 */
	public CustomFieldValueAdapter(CustomFieldAdapter customFieldAdapter, TaskManager taskManager) {
		this.customFieldAdapter = customFieldAdapter;
		this.taskManager = taskManager;

		updateFxProperties();

		//Set change listeners
		this.value.addListener(valueChangeListener);
	}
	/*
	 * JavaFX properties
	 */

	/**
	 * Name property (retrieved from associated CustomFieldAdapter)
	 *
	 * @return the name property
	 */
	public StringProperty nameProperty() {
		return customFieldAdapter.nameProperty();
	}

	/**
	 * Value property
	 *
	 * @return the value property
	 */
	public StringProperty valueProperty() {
		return value;
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the associated CustomField
	 *
	 * @return the associated CustomField
	 */
	public CustomFieldAdapter getCustomField() {
		return customFieldAdapter;
	}

	/**
	 * Returns the associated task
	 *
	 * @return the associated task
	 */
	public TaskAdapter getTask() {
		return taskAdapter;
	}

	/**
	 * Changes the associated task and updates this field's value from that task
	 *
	 * @param taskAdapter the new TaskAdapter
	 */
	public void setTask(TaskAdapter taskAdapter) {
		if (this.taskAdapter == null)
			this.value.setValue(null);
		this.taskAdapter = taskAdapter;
		updateFxProperties();
	}
	//TODO: other Getters/setters

	/*
	 * Internal methods
	 */
	/**
	 * Updates Java FX properties from the associated entity
	 */
	private void updateFxProperties() {
		//Remove listener since the update is initiated by us
		value.removeListener(valueChangeListener);
		//Perform update
		if (taskAdapter != null && taskAdapter.getTask() != null) {
			String customFieldValue = taskAdapter.getTask().getCustomField(customFieldAdapter.getCustomField());
			value.setValue(customFieldValue == null ? "" : customFieldValue);
		}
		//Restore listener
		value.addListener(valueChangeListener);
	}

	/**
	 * Returns the TaskManager reference
	 *
	 * @return the TaskManager reference
	 */
	private TaskManager getTaskManager() {
		return taskManager;
	}
}
