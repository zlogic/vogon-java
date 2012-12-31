package org.zlogic.att.ui.adapters;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 30.12.12
 * Time: 0:51
 */
public class TaskAdapter {
	protected static PersistenceHelper persistenceHelper = new PersistenceHelper();
	private StringProperty description = new SimpleStringProperty();
	private StringProperty name = new SimpleStringProperty();
	private BooleanProperty completed = new SimpleBooleanProperty();
	private Task task;

	public TaskAdapter(Task task) {
		this.task = task;

		updateFxProperties();

		//Change listeners
		this.description.addListener(new ChangeListener<String>() {
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
							setTask(entityManager.merge(getTask()));
							getTask().setDescription(newValue);
						}
					}.setNewValue(newValue));
				updateFxProperties();
			}
		});

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
							setTask(entityManager.merge(getTask()));
							getTask().setName(newValue);
						}
					}.setNewValue(newValue));
				updateFxProperties();
			}
		});

		this.completed.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
				if (!oldValue.equals(newValue))
					persistenceHelper.performTransactedChange(new TransactedChange() {
						private boolean newValue;

						public TransactedChange setNewValue(boolean newValue) {
							this.newValue = newValue;
							return this;
						}

						@Override
						public void performChange(EntityManager entityManager) {
							setTask(entityManager.merge(getTask()));
							getTask().setCompleted(newValue);
						}
					}.setNewValue(newValue));
				updateFxProperties();
			}
		});
	}

	public StringProperty descriptionProperty() {
		return description;
	}

	public StringProperty nameProperty() {
		return name;
	}

	public BooleanProperty completedProperty() {
		return completed;
	}

	public void updateFxProperties() {
		description.setValue(task.getDescription());
		name.setValue(task.getName());
		completed.setValue(task.getCompleted());
	}

	public Task getTask() {
		return task;
	}

	private void setTask(Task task) {
		this.task = task;
	}
}
