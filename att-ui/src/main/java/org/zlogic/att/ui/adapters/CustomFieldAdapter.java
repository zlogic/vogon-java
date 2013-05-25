/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
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
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
 */
public class CustomFieldAdapter implements Comparable<CustomFieldAdapter> {

	/**
	 * Associated entity
	 */
	private CustomField customField;
	/**
	 * DataManager reference
	 */
	private DataManager dataManager;
	/*
	 * Java FX
	 */
	/**
	 * Name property
	 */
	private StringProperty name = new SimpleStringProperty();
	/*
	 * Change listeners
	 */
	/**
	 * Name change listener
	 */
	private ChangeListener<String> nameChangeListener = new ChangeListener<String>() {
		@Override
		public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
			oldValue = oldValue == null ? "" : oldValue; //NOI18N
			newValue = newValue == null ? "" : newValue; //NOI18N
			if (!oldValue.equals(newValue) && getDataManager() != null) {
				getDataManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
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
	};

	/**
	 * Creates a CustomFieldValueAdapter instance
	 *
	 * @param customField the associated entity
	 * @param dataManager the DataManager reference
	 */
	public CustomFieldAdapter(CustomField customField, DataManager dataManager) {
		this.customField = customField;
		this.dataManager = dataManager;

		updateFxProperties();
	}

	/*
	 * Internal methods
	 */
	/**
	 * Returns the DataManager reference
	 *
	 * @return the DataManager reference
	 */
	private DataManager getDataManager() {
		return dataManager;
	}

	/*
	 * Java FX properties
	 */
	/**
	 * Name property
	 *
	 * @return the name property
	 */
	public StringProperty nameProperty() {
		return name;
	}

	/**
	 * Custom field property
	 *
	 * @return the associated CustomField entity property
	 */
	public CustomField getCustomField() {
		return customField;
	}

	/**
	 * Updates the associated custom field
	 *
	 * @param customField the new (or updated) custom field
	 */
	private void setCustomField(CustomField customField) {
		this.customField = customField;
	}

	@Override
	public int compareTo(CustomFieldAdapter o) {
		return customField.compareTo(o.customField);
	}

	/*
	 * Internal methods
	 */
	/**
	 * Updates Java FX properties from the associated entity
	 */
	private void updateFxProperties() {
		//Remove listener since the update is initiated by us
		name.removeListener(nameChangeListener);
		//Perform update
		name.setValue(customField.getName());
		//Restore listener
		name.addListener(nameChangeListener);
	}
}
