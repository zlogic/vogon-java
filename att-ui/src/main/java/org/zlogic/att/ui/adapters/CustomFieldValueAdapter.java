/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
 */
package org.zlogic.att.ui.adapters;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
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
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
 */
public class CustomFieldValueAdapter {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/adapters/messages");
	/**
	 * Associated Custom Field adapter
	 */
	private CustomFieldAdapter customFieldAdapter;
	/**
	 * Associated Tasks
	 */
	private List<TaskAdapter> taskAdapters = new LinkedList<>();
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
			oldValue = oldValue == null ? "" : oldValue; //NOI18N
			if (newValue != null && !oldValue.equals(newValue) && getDataManager() != null) {
				getDataManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
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
						for (TaskAdapter taskAdapter : getTasks()) {
							(entityManager.find(Task.class, taskAdapter.getTask().getId())).setCustomField(foundCustomField, newValue);
							taskAdapter.getTask().setCustomField(foundCustomField, newValue);
						}
					}
				}.setNewValue(getCustomField(), newValue));
				for (TaskAdapter taskAdapter : getTasks())
					taskAdapter.updateFromDatabase();
				updateFxProperties();
				getDataManager().removeFilteredCustomFieldValue(getCustomField(), oldValue);
				getDataManager().addFilteredCustomFieldValue(getCustomField(), newValue);
			}
		}
	};
	/**
	 * DataManager reference
	 */
	private DataManager dataManager;

	/**
	 * Creates a CustomFieldValueAdapter instance
	 *
	 * @param customFieldAdapter the associated CustomFieldAdapter reference
	 * @param dataManager the DataManager reference
	 */
	public CustomFieldValueAdapter(CustomFieldAdapter customFieldAdapter, DataManager dataManager) {
		this.customFieldAdapter = customFieldAdapter;
		this.dataManager = dataManager;

		updateFxProperties();
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
	 * Returns the associated tasks
	 *
	 * @return the associated tasks
	 */
	protected List<TaskAdapter> getTasks() {
		return taskAdapters;
	}

	/**
	 * Changes the associated tasks and updates this field's value from these
	 * tasks
	 *
	 * @param taskAdapters the new TaskAdapters
	 */
	public void setTasks(List<TaskAdapter> taskAdapters) {
		if (taskAdapters == null || taskAdapters.isEmpty())
			this.value.setValue(null);
		this.taskAdapters.clear();
		if (taskAdapters != null)
			this.taskAdapters.addAll(taskAdapters);
		updateFxProperties();
	}

	/**
	 * Changes the associated task and updates this field's value from that task
	 *
	 * @param taskAdapter the new TaskAdapter
	 */
	public void setTask(TaskAdapter taskAdapter) {
		setTasks(Arrays.asList(taskAdapter));
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
		if (!taskAdapters.isEmpty()) {
			String customFieldValue = null;
			boolean multipleValuesFound = false;
			for (TaskAdapter taskAdapter : taskAdapters) {
				String taskCustomFieldValue = taskAdapter.getTask().getCustomField(customFieldAdapter.getCustomField());
				if (taskCustomFieldValue == null)
					continue;
				if (customFieldValue == null || taskCustomFieldValue.equals(customFieldValue))
					customFieldValue = taskCustomFieldValue;
				else if (!taskCustomFieldValue.equals(customFieldValue))
					multipleValuesFound = true;
			}
			if (multipleValuesFound)
				customFieldValue = messages.getString("MULTIPLE_VALUES");
			value.setValue(customFieldValue == null ? "" : customFieldValue); //NOI18N
		}
		//Restore listener
		value.addListener(valueChangeListener);
	}

	/**
	 * Returns the DataManager reference
	 *
	 * @return the DataManager reference
	 */
	private DataManager getDataManager() {
		return dataManager;
	}
}
