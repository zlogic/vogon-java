/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.adapters;

import java.util.Date;
import java.util.LinkedList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.persistence.EntityManager;
import org.joda.time.format.PeriodFormat;
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
	private StringProperty totalTime = new SimpleStringProperty();
	private Task task;
	private TaskManager taskManager;
	private ObservableList<TimeSegmentAdapter> timeSegments = FXCollections.observableList(new LinkedList<TimeSegmentAdapter>());
	private ObjectProperty<Date> firstTime = new SimpleObjectProperty<>();
	private ObjectProperty<Date> lastTime = new SimpleObjectProperty<>();

	public TaskAdapter(Task task, TaskManager taskManager) {
		this.task = task;
		this.taskManager = taskManager;

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
							setTask(getTaskManager().getPersistenceHelper().getTaskFromDatabase(getTask().getId(), entityManager));
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
							setTask(getTaskManager().getPersistenceHelper().getTaskFromDatabase(getTask().getId(), entityManager));
							getTask().setName(newValue);
						}
					}.setNewValue(newValue));
					updateFxProperties();
				}
			}
		});

		this.completed.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
				if (!oldValue.equals(newValue)) {
					//TODO: detect if the change was actually initiated by us
					getTaskManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
						private boolean newValue;

						public TransactedChange setNewValue(boolean newValue) {
							this.newValue = newValue;
							return this;
						}

						@Override
						public void performChange(EntityManager entityManager) {
							setTask(getTaskManager().getPersistenceHelper().getTaskFromDatabase(getTask().getId(), entityManager));
							getTask().setCompleted(newValue);
						}
					}.setNewValue(newValue));
					updateFxProperties();
				}
			}
		});
	}

	public StringProperty descriptionProperty() {
		return description;
	}

	public StringProperty nameProperty() {
		return name;
	}

	public StringProperty totalTimeProperty() {
		return totalTime;
	}
	public ObjectProperty<Date> firstTimeProperty() {
		return firstTime;
	}
	public ObjectProperty<Date> lastTimeProperty() {
		return lastTime;
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

	public void updateFromDatabase() {
		getTaskManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
			@Override
			public void performChange(EntityManager entityManager) {
				setTask(getTaskManager().getPersistenceHelper().getTaskFromDatabase(getTask().getId(), entityManager));
			}
		});
		updateFxProperties();
	}

	private void updateFxProperties() {
		description.setValue(task.getDescription());
		name.setValue(task.getName());
		completed.setValue(task.getCompleted());
		timeSegments.retainAll(task.getTimeSegments());
		for (TimeSegment segment : task.getTimeSegments())
			if (findTimeSegment(segment) == null)
				timeSegments.add(new TimeSegmentAdapter(segment, this, taskManager));
		updateTimeProperty();
	}

	protected void updateTimeProperty() {
		firstTime.setValue(getEarliestTime());
		lastTime.setValue(getLatestTime());
		totalTime.setValue(task.getTotalTime().toString(PeriodFormat.wordBased()));
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

	public Date getLatestTime(){
		Date lastDate = null;
		for(TimeSegment segment : task.getTimeSegments())
			if(lastDate==null || segment.getEndTime().after(lastDate))
				lastDate = segment.getEndTime();
		return lastDate;
	}
	
	public Date getEarliestTime(){
		Date firstDate = null;
		for(TimeSegment segment : task.getTimeSegments())
			if(firstDate==null || segment.getStartTime().before(firstDate))
				firstDate = segment.getStartTime();
		return firstDate;
	}
	
	/*
	 * Getters/Setters
	 */
	public Task getTask() {
		return task;
	}

	private void setTask(Task task) {
		this.task = task;
	}
}
