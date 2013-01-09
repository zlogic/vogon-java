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

	private TaskManager taskManager;
	private StringProperty value = new SimpleStringProperty();
	private CustomFieldAdapter customFieldAdapter;
	private TaskAdapter taskAdapter;

	public CustomFieldValueAdapter(CustomFieldAdapter customFieldAdapter, TaskManager taskManager) {
		this.customFieldAdapter = customFieldAdapter;
		this.taskManager = taskManager;

		updateFxProperties();

		//Change listeners
		this.value.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (oldValue != null && newValue != null && !oldValue.equals(newValue)) {
					//TODO: detect if the change was actually initiated by us
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
							getTask().getTask().setCustomField(customFieldAdapter.getCustomField(), newValue);
						}
					}.setNewValue(getCustomField(), newValue));
					updateFxProperties();
				}
			}
		});
	}

	private TaskManager getTaskManager() {
		return taskManager;
	}

	private void updateFxProperties() {
		if (taskAdapter != null && taskAdapter.getTask() != null) {
			String customFieldValue = taskAdapter.getTask().getCustomField(customFieldAdapter.getCustomField());
			value.setValue(customFieldValue == null ? "" : customFieldValue);
		}
	}

	public CustomFieldAdapter getCustomField() {
		return customFieldAdapter;
	}

	public void setTask(TaskAdapter taskAdapter) {
		this.taskAdapter = taskAdapter;
		this.value.setValue(null);
		updateFxProperties();
	}

	public TaskAdapter getTask() {
		return taskAdapter;
	}

	public StringProperty nameProperty() {
		return customFieldAdapter.nameProperty();
	}

	public StringProperty valueProperty() {
		return value;
	}
}
