/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.adapters;

import java.util.LinkedList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.persistence.EntityManager;
import org.zlogic.att.data.PersistenceHelper;
import org.zlogic.att.data.Task;
import org.zlogic.att.data.TimeSegment;
import org.zlogic.att.data.TransactedChange;

/**
 * Adapter to interface JPA with Java FX observable properties for Task classes.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class TaskAdapter {

	protected static PersistenceHelper persistenceHelper = new PersistenceHelper();
	private StringProperty description = new SimpleStringProperty();
	private StringProperty name = new SimpleStringProperty();
	private BooleanProperty completed = new SimpleBooleanProperty();
	private Task task;
	private ObservableList<TimeSegmentAdapter> timeSegments = FXCollections.observableList(new LinkedList<TimeSegmentAdapter>());

	public TaskAdapter(Task task) {
		this.task = task;

		updateFxProperties();

		//Change listeners
		this.description.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (!oldValue.equals(newValue)) {
					//TODO: detect if the change was actually initiated by us
					persistenceHelper.performTransactedChange(new TransactedChange() {
						private String newValue;

						public TransactedChange setNewValue(String newValue) {
							this.newValue = newValue;
							return this;
						}

						@Override
						public void performChange(EntityManager entityManager) {
							setTask(entityManager.find(Task.class, getTask().getId()));
							getTask().setDescription(newValue);
						}
					}.setNewValue(newValue));
					updateFxProperties();
				}
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
							setTask(entityManager.find(Task.class, getTask().getId()));
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
							setTask(entityManager.find(Task.class, getTask().getId()));
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

	public ObservableList<TimeSegmentAdapter> timeSegmentsProperty() {
		return timeSegments;
	}

	private void updateFxProperties() {
		description.setValue(task.getDescription());
		name.setValue(task.getName());
		completed.setValue(task.getCompleted());
		timeSegments.retainAll(task.getTimeSegments());
		for (TimeSegment segment : task.getTimeSegments())
			if (!timeSegments.contains(segment))
				timeSegments.add(new TimeSegmentAdapter(segment));
	}

	public boolean isTiming() {
		for (TimeSegmentAdapter segment : timeSegments)
			if (segment.isTimingProperty().get())
				return true;
		return false;
	}

	/*
	 * Getters/Setters
	 */
	public Task getTask() {
		return task;
	}

	protected void setTask(Task task) {
		this.task = task;//Doesn't update any properties!
	}
}
