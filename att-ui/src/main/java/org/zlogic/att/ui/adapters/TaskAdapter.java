/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.adapters;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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

	/**
	 * Assigned entity
	 */
	private Task task;
	/*
	 * Java FX properties (some are extracted directly, some are generated on-the-fly)
	 */
	/**
	 * Description property
	 */
	private StringProperty description = new SimpleStringProperty();
	/**
	 * Name property
	 */
	private StringProperty name = new SimpleStringProperty();
	/**
	 * Completed state property
	 */
	private BooleanProperty completed = new SimpleBooleanProperty();
	/**
	 * Total time property (generated)
	 */
	private StringProperty totalTime = new SimpleStringProperty();
	/**
	 * Associated time segments (generated)
	 */
	private ObservableList<TimeSegmentAdapter> timeSegments = FXCollections.observableList(new LinkedList<TimeSegmentAdapter>());
	/**
	 * First (earliest) time assigned property (generated)
	 */
	private ObjectProperty<Date> firstTime = new SimpleObjectProperty<>();
	/**
	 * Last (latest) time assigned property (generated)
	 */
	private ObjectProperty<Date> lastTime = new SimpleObjectProperty<>();
	/**
	 * TaskManager reference
	 */
	private TaskManager taskManager;

	/*
	 * Change listeners
	 */
	/**
	 * Description change listener
	 */
	private ChangeListener<String> descriptionChangeListener = new ChangeListener<String>() {
		@Override
		public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
			oldValue = oldValue == null ? "" : oldValue;
			newValue = newValue == null ? "" : newValue;
			if (!oldValue.equals(newValue) && getTaskManager() != null) {
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
				getTaskManager().signalTaskUpdate();
			}
		}
	};
	/**
	 * Name change listener
	 */
	private ChangeListener<String> nameChangeListener = new ChangeListener<String>() {
		@Override
		public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
			oldValue = oldValue == null ? "" : oldValue;
			newValue = newValue == null ? "" : newValue;
			if (!oldValue.equals(newValue) && getTaskManager() != null) {
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
				getTaskManager().signalTaskUpdate();
			}
		}
	};
	/**
	 * Completed change listener
	 */
	private ChangeListener<Boolean> completedChangeListener = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
			if (!oldValue.equals(newValue) && getTaskManager() != null) {
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
				getTaskManager().signalTaskUpdate();
			}
		}
	};

	/**
	 * Creates a TaskAdapter instance
	 *
	 * @param task the associated task
	 * @param taskManager the TaskManager reference
	 */
	public TaskAdapter(Task task, TaskManager taskManager) {
		this.task = task;
		this.taskManager = taskManager;

		updateFxProperties();
		//Change listeners
		this.description.addListener(descriptionChangeListener);
		this.name.addListener(nameChangeListener);
		this.completed.addListener(completedChangeListener);
	}

	/**
	 * Updates the associated entity from database
	 */
	public void updateFromDatabase() {
		getTaskManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
			@Override
			public void performChange(EntityManager entityManager) {
				setTask(getTaskManager().getPersistenceHelper().getTaskFromDatabase(getTask().getId(), entityManager));
			}
		});
		updateFxProperties();
	}

	/*
	 * JavaFX properties
	 */
	/**
	 * Description property
	 *
	 * @return the description property
	 */
	public StringProperty descriptionProperty() {
		return description;
	}

	/**
	 * Name property
	 *
	 * @return the name property
	 */
	public StringProperty nameProperty() {
		return name;
	}

	/**
	 * Completed state property
	 *
	 * @return the completed state property
	 */
	public BooleanProperty completedProperty() {
		return completed;
	}

	/**
	 * Associated time segments (generated)
	 *
	 * @return the associated time segments property
	 */
	public ObservableList<TimeSegmentAdapter> timeSegmentsProperty() {
		return timeSegments;
	}

	/**
	 * Total time property (generated)
	 *
	 * @return the total time property
	 */
	public StringProperty totalTimeProperty() {
		return totalTime;
	}

	/**
	 * First (earliest) time assigned property (generated) This is the earliest
	 * registered time for this task (earliest Start time of all TimeSegments)
	 *
	 * @return the first time assigned property
	 */
	public ObjectProperty<Date> firstTimeProperty() {
		return firstTime;
	}

	/**
	 * Last (latest) time assigned property (generated) This is the latest
	 * registered time for this task (latest End time of all TimeSegments)
	 *
	 * @return the last time assigned property
	 */
	public ObjectProperty<Date> lastTimeProperty() {
		return lastTime;
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the associated Task entity
	 *
	 * @return the associated Task entity
	 */
	public Task getTask() {
		return task;
	}

	/**
	 * Changes the associated Task entity
	 *
	 * @param task the new (or updated) task
	 */
	private void setTask(Task task) {
		this.task = task;
	}

	/**
	 * Returns true if one of this task's time segments is currently timing
	 *
	 * @return true if one of this task's time segments is currently timing
	 */
	public boolean isTiming() {
		for (TimeSegmentAdapter segment : timeSegments)
			if (segment.isTimingProperty().get())
				return true;
		return false;
	}
	//TODO: other Getters/setters

	/*
	 * Internal methods
	 */
	/**
	 * Returns the TaskManager reference
	 *
	 * @return the TaskManager reference
	 */
	private TaskManager getTaskManager() {
		return taskManager;
	}

	/**
	 * Updates Java FX properties from the associated entity
	 */
	private void updateFxProperties() {
		//Remove changeListeners
		this.description.removeListener(descriptionChangeListener);
		this.name.removeListener(nameChangeListener);
		this.completed.removeListener(completedChangeListener);
		//Perform update
		description.setValue(task.getDescription());
		name.setValue(task.getName());
		completed.setValue(task.getCompleted());
		List<TimeSegmentAdapter> orphanedSegments = new LinkedList<>();
		for (TimeSegmentAdapter segment : timeSegments)
			if (!task.getTimeSegments().contains(segment.getTimeSegment()))
				orphanedSegments.add(segment);
		timeSegments.removeAll(orphanedSegments);
		for (TimeSegment segment : task.getTimeSegments())
			if (findTimeSegment(segment) == null)
				timeSegments.add(new TimeSegmentAdapter(segment, this, taskManager));
		updateTimeProperty();
		//Restore listeners
		this.description.addListener(descriptionChangeListener);
		this.name.addListener(nameChangeListener);
		this.completed.addListener(completedChangeListener);
	}

	/**
	 * Updates time-associated (generated) properties
	 */
	protected void updateTimeProperty() {
		firstTime.setValue(getEarliestTime());
		lastTime.setValue(getLatestTime());
		totalTime.setValue(task.getTotalTime().toString(PeriodFormat.wordBased()));
	}

	/**
	 * Returns a TimeSegmentAdapter associated with a specific TimeSegment
	 *
	 * @param findSegment the TimeSegment to find
	 * @return the associated TimeSegmentAdapter or null if not found
	 */
	private TimeSegmentAdapter findTimeSegment(TimeSegment findSegment) {
		for (TimeSegmentAdapter segment : timeSegments)
			if (segment.getTimeSegment().equals(findSegment))
				return segment;
		return null;
	}

	/**
	 * Searches for the earliest registered time for this task (earliest Start
	 * time of all TimeSegments)
	 *
	 * @return the earliest registered time for this task or null if task has no
	 * TimeSegments
	 */
	private Date getEarliestTime() {
		Date firstDate = null;
		for (TimeSegment segment : task.getTimeSegments())
			if (firstDate == null || segment.getStartTime().before(firstDate))
				firstDate = segment.getStartTime();
		return firstDate;
	}

	/**
	 * Searches for the latest registered time for this task (latest End time of
	 * all TimeSegments)
	 *
	 * @return the latest registered time for this task or null if task has no
	 * TimeSegments
	 */
	private Date getLatestTime() {
		Date lastDate = null;
		for (TimeSegment segment : task.getTimeSegments())
			if (lastDate == null || segment.getEndTime().after(lastDate))
				lastDate = segment.getEndTime();
		return lastDate;
	}

	/**
	 * Creates a new TimeSegment which has the associated Task entity as the
	 * owner. Persists it in the database.
	 *
	 * @return the new TimeSegment
	 */
	public TimeSegmentAdapter createTimeSegment() {
		TimeSegment newSegment = taskManager.getPersistenceHelper().createTimeSegment(task);
		TimeSegmentAdapter newSegmentAdapter = new TimeSegmentAdapter(newSegment, this, taskManager);
		if (!timeSegments.contains(newSegmentAdapter))
			timeSegments.add(newSegmentAdapter);
		updateTimeProperty();
		return newSegmentAdapter;
	}
}
