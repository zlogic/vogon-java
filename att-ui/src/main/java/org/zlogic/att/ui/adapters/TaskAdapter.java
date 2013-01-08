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
import org.zlogic.att.data.Task;
import org.zlogic.att.data.TimeSegment;
import org.zlogic.att.data.TransactedChange;

/**
 * Adapter to interface JPA with Java FX observable properties for Task classes.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class TaskAdapter {

	private StringProperty description = new SimpleStringProperty();
	private StringProperty name = new SimpleStringProperty();
	private BooleanProperty completed = new SimpleBooleanProperty();
	private Task task;
	private TaskManager taskManager;
	private ObservableList<TimeSegmentAdapter> timeSegments = FXCollections.observableList(new LinkedList<TimeSegmentAdapter>());

	public TaskAdapter(Task task, TaskManager taskList) {
		this.task = task;
		this.taskManager = taskList;

		updateFxProperties();

		//Change listeners
		this.description.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (!oldValue.equals(newValue)) {
					//TODO: detect if the change was actually initiated by us
					getTaskManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
						private String newValue;

						public TransactedChange setNewValue(String newValue) {
							this.newValue = newValue;
							return this;
						}

						@Override
						public void performChange(EntityManager entityManager) {
							entityManager.find(Task.class, getTask().getId()).setDescription(newValue);
						}
					}.setNewValue(newValue));
					getTask().setDescription(newValue);
					updateFxProperties();
				}
			}
		});

		this.name.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (!oldValue.equals(newValue))
					getTaskManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
						private String newValue;

						public TransactedChange setNewValue(String newValue) {
							this.newValue = newValue;
							return this;
						}

						@Override
						public void performChange(EntityManager entityManager) {
							entityManager.find(Task.class, getTask().getId()).setName(newValue);
						}
					}.setNewValue(newValue));
				getTask().setName(newValue);
				updateFxProperties();
			}
		});

		this.completed.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
				if (!oldValue.equals(newValue))
					getTaskManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
						private boolean newValue;

						public TransactedChange setNewValue(boolean newValue) {
							this.newValue = newValue;
							return this;
						}

						@Override
						public void performChange(EntityManager entityManager) {
							entityManager.find(Task.class, getTask().getId()).setCompleted(newValue);
						}
					}.setNewValue(newValue));
				getTask().setCompleted(newValue);
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

	private TaskManager getTaskManager() {
		return taskManager;
	}

	private void updateFxProperties() {
		description.setValue(task.getDescription());
		name.setValue(task.getName());
		completed.setValue(task.getCompleted());
		timeSegments.retainAll(task.getTimeSegments());
		for (TimeSegment segment : task.getTimeSegments())
			if (findTimeSegment(segment) == null)
				timeSegments.add(new TimeSegmentAdapter(segment, taskManager));
	}

	private TimeSegmentAdapter findTimeSegment(TimeSegment findSegment) {
		for (TimeSegmentAdapter segment : timeSegments)
			if (segment.getTimeSegment().equals(findSegment))
				return segment;
		return null;
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
}
