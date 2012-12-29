package org.zlogic.att.ui.adapters;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.zlogic.att.data.PersistenceHelper;
import org.zlogic.att.data.Task;
import org.zlogic.att.data.TransactedChange;

import javax.persistence.EntityManager;

/**
 * Adapter to interface JPA with Java FX observable properties for Task classes.
 * <p/>
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 30.12.12
 * Time: 0:51
 */
public class TaskAdapter {
	protected static PersistenceHelper persistenceHelper = new PersistenceHelper();
	private StringProperty description = new SimpleStringProperty();
	private Task task;

	public TaskAdapter(Task task) {
		this.task = task;

		updateFxProperties();

		this.description.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (!oldValue.equals(newValue))
					persistenceHelper.performTransactedChange(new TransactedChange() {
						private String newDescription;

						public TransactedChange setNewDescription(String newDescription) {
							this.newDescription = newDescription;
							return this;
						}

						@Override
						public void performChange(EntityManager entityManager) {
							setTask(entityManager.merge(getTask()));
							getTask().setDescription(newDescription);
						}
					}.setNewDescription(newValue));
				updateFxProperties();
			}
		});
	}

	public StringProperty descriptionProperty() {
		return description;
	}

	public void updateFxProperties() {
		description.setValue(task.getDescription());
	}

	public Task getTask() {
		return task;
	}

	private void setTask(Task task) {
		this.task = task;
	}
}
