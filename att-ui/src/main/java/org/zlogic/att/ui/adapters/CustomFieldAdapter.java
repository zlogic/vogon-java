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
import org.zlogic.att.data.TransactedChange;

/**
 * Adapter to interface JPA with Java FX observable properties for CustomField
 * classes.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class CustomFieldAdapter {

	private TaskManager taskManager;
	private StringProperty name = new SimpleStringProperty();
	private CustomField customField;

	public CustomFieldAdapter(CustomField customField, TaskManager taskManager) {
		this.customField = customField;
		this.taskManager = taskManager;

		updateFxProperties();

		//Change listeners
		this.name.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				oldValue = oldValue == null ? "" : oldValue;
				newValue = newValue == null ? "" : newValue;
				if (!oldValue.equals(newValue) && getTaskManager() != null) {
					//TODO: detect if the change was actually initiated by us
					getTaskManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
						private String newValue;

						public TransactedChange setNewValue(String newValue) {
							this.newValue = newValue;
							return this;
						}

						@Override
						public void performChange(EntityManager entityManager) {
							setCustomField(entityManager.find(CustomField.class, getCustomField().getId()));
							getCustomField().setName(newValue);
						}
					}.setNewValue(newValue));
					updateFxProperties();
				}
			}
		});
	}

	private TaskManager getTaskManager() {
		return taskManager;
	}

	private void updateFxProperties() {
		name.setValue(customField.getName());
	}

	public StringProperty nameProperty() {
		return name;
	}

	public CustomField getCustomField() {
		return customField;
	}

	private void setCustomField(CustomField task) {
		this.customField = task;
	}
}
