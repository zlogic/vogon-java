package org.zlogic.att.ui.adapters;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.zlogic.att.data.CustomField;
import org.zlogic.att.data.PersistenceHelper;
import org.zlogic.att.data.TransactedChange;

import javax.persistence.EntityManager;

/**
 * Adapter to interface JPA with Java FX observable properties for CustomField classes.
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 31.12.12
 * Time: 3:37
 */
public class CustomFieldAdapter {
	protected static PersistenceHelper persistenceHelper = new PersistenceHelper();
	private StringProperty name = new SimpleStringProperty();
	private CustomField customField;

	public CustomFieldAdapter(CustomField customField) {
		this.customField = customField;

		updateFxProperties();

		//Change listeners
		this.name.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (!oldValue.equals(newValue))
					persistenceHelper.performTransactedChange(new TransactedChange() {
						private String newValue;

						public TransactedChange setNewValue(String newValue) {
							this.newValue = newValue;
							return this;
						}

						@Override
						public void performChange(EntityManager entityManager) {
							setCustomField(entityManager.merge(getCustomField()));
							getCustomField().setName(newValue);
						}
					}.setNewValue(newValue));
				updateFxProperties();
			}
		});
	}

	public void updateFxProperties() {
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
