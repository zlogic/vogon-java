/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.adapters;

import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javax.persistence.EntityManager;
import org.joda.time.format.PeriodFormatterBuilder;
import org.zlogic.att.data.Task;
import org.zlogic.att.data.TimeSegment;
import org.zlogic.att.data.TransactedChange;

/**
 * Adapter to interface JPA with Java FX observable properties for TimeSegment
 * classes.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class TimeSegmentAdapter {

	/**
	 * Assigned entity
	 */
	private TimeSegment segment;
	/*
	 * Java FX 
	 */
	/**
	 * Description property
	 */
	private StringProperty description = new SimpleStringProperty();
	/**
	 * Last (latest) time assigned property (generated)
	 */
	private StringProperty duration = new SimpleStringProperty();
	/**
	 * Start time property
	 */
	private ObjectProperty<Date> start = new SimpleObjectProperty<>();
	/**
	 * End time property
	 */
	private ObjectProperty<Date> end = new SimpleObjectProperty<>();
	/**
	 * Owner task property
	 */
	private ObjectProperty<TaskAdapter> ownerTask = new SimpleObjectProperty<>();
	/**
	 * TaskManager reference
	 */
	private TaskManager taskManager;
	/**
	 * Timer for automatically updating the time
	 */
	private Timer timer;
	/**
	 * Property to indicate if the segment is currently timing
	 */
	private BooleanProperty timingProperty = new SimpleBooleanProperty(false);
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
						setTimeSegment(entityManager.find(TimeSegment.class, getTimeSegment().getId()));
						getTimeSegment().setDescription(newValue);
					}
				}.setNewValue(newValue));
				updateFxProperties();
				getTaskManager().signalTaskUpdate();
			}
		}
	};
	/**
	 * Start time change listener
	 */
	private ChangeListener<Date> startChangeListener = new ChangeListener<Date>() {
		@Override
		public void changed(ObservableValue<? extends Date> observableValue, Date oldValue, Date newValue) {
			if (!oldValue.equals(newValue) && getTaskManager() != null) {
				//TODO: catch exceptions & revert
				getTaskManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
					private Date newValue;

					public TransactedChange setNewValue(Date newValue) {
						this.newValue = newValue;
						return this;
					}

					@Override
					public void performChange(EntityManager entityManager) {
						setTimeSegment(entityManager.find(TimeSegment.class, getTimeSegment().getId()));
						getTimeSegment().setStartTime(newValue);
					}
				}.setNewValue(newValue));
				ownerTaskProperty().get().updateFromDatabase();
				updateFxProperties();
				getTaskManager().signalTaskUpdate();
			}
		}
	};
	/**
	 * End time change listener
	 */
	private ChangeListener<Date> endChangeListener = new ChangeListener<Date>() {
		@Override
		public void changed(ObservableValue<? extends Date> observableValue, Date oldValue, Date newValue) {
			if (!oldValue.equals(newValue) && getTaskManager() != null) {
				//TODO: catch exceptions & revert
				getTaskManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
					private Date newValue;

					public TransactedChange setNewValue(Date newValue) {
						this.newValue = newValue;
						return this;
					}

					@Override
					public void performChange(EntityManager entityManager) {
						setTimeSegment(entityManager.find(TimeSegment.class, getTimeSegment().getId()));
						getTimeSegment().setEndTime(newValue);
					}
				}.setNewValue(newValue));
				ownerTaskProperty().get().updateFromDatabase();
				updateFxProperties();
				getTaskManager().signalTaskUpdate();
			}
		}
	};
	/**
	 * Owner task change listener
	 */
	private ChangeListener<TaskAdapter> ownerTaskChangeListener = new ChangeListener<TaskAdapter>() {
		@Override
		public void changed(ObservableValue<? extends TaskAdapter> ov, TaskAdapter oldValue, TaskAdapter newValue) {
			if (!oldValue.equals(newValue) && getTaskManager() != null) {
				getTaskManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
					private TaskAdapter newValue;

					public TransactedChange setNewValue(TaskAdapter newValue) {
						this.newValue = newValue;
						return this;
					}

					@Override
					public void performChange(EntityManager entityManager) {
						setTimeSegment(entityManager.find(TimeSegment.class, getTimeSegment().getId()));
						Task newTask = entityManager.find(Task.class, newValue.getTask().getId());
						getTimeSegment().setOwner(newTask);
					}
				}.setNewValue(newValue));
				if (isTimingProperty().get()) {
					oldValue.isTimingProperty().unbind();
					oldValue.isTimingProperty().set(false);
					newValue.isTimingProperty().bind(isTimingProperty());
				}
				oldValue.updateFromDatabase();
				newValue.updateFromDatabase();
				updateFxProperties();
			}
		}
	};

	/**
	 * Creates a TimeSegmentAdapter instance
	 *
	 * @param segment the associated entity
	 * @param ownerTask the owner TaskAdapter reference
	 * @param taskManager the TaskManager reference
	 */
	public TimeSegmentAdapter(TimeSegment segment, TaskAdapter ownerTask, TaskManager taskManager) {
		this.segment = segment;
		this.taskManager = taskManager;
		this.ownerTask.setValue(ownerTask);

		updateFxProperties();
		//Set change listeners
		this.description.addListener(descriptionChangeListener);
		this.start.addListener(startChangeListener);
		this.end.addListener(endChangeListener);
		this.ownerTask.addListener(ownerTaskChangeListener);
	}

	/**
	 * Starts timing this segment
	 */
	public void startTiming() {
		ownerTask.get().isTimingProperty().bind(timingProperty);
		timingProperty.set(true);
		if (timer != null)
			timer.cancel();
		//Start the timer
		timer = new Timer(true);
		timer.scheduleAtFixedRate(new TimerTask() {
			private ObjectProperty<Date> endProperty;

			public TimerTask setEndProperty(ObjectProperty<Date> endProperty) {
				this.endProperty = endProperty;
				return this;
			}

			@Override
			public void run() {
				endProperty.setValue(new Date());
			}
		}.setEndProperty(endProperty()), 0, 1000);
	}

	/**
	 * Stops timing this segment
	 */
	public void stopTiming() {
		timer.cancel();
		timer = null;
		endProperty().setValue(new Date());
		timingProperty.set(false);
		taskManager.timingSegmentProperty().set(null);
		ownerTask.get().isTimingProperty().unbind();
	}

	/*
	 * JavaFX properties
	 */
	/**
	 * Start time property
	 *
	 * @return the start time property
	 */
	public ObjectProperty<Date> startProperty() {
		return start;
	}

	/**
	 * End time property
	 *
	 * @return the end time property
	 */
	public ObjectProperty<Date> endProperty() {
		return end;
	}

	/**
	 * Duration property
	 *
	 * @return the duration property
	 */
	public StringProperty durationProperty() {
		return duration;
	}
	
	/**
	 * Description property
	 *
	 * @return the description property
	 */
	public StringProperty descriptionProperty() {
		return description;
	}

	/**
	 * The time segment currently timing state property (true if is timing,
	 * false if timer is not running)
	 *
	 * @return the time segment timing state property
	 */
	public BooleanProperty isTimingProperty() {
		return timingProperty;
	}

	/**
	 * The owner task property
	 *
	 * @return the owner task property
	 */
	public ObjectProperty<TaskAdapter> ownerTaskProperty() {
		return ownerTask;
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the associated TimeSegment entity
	 *
	 * @return the associated TimeSegment entity
	 */
	public TimeSegment getTimeSegment() {
		return segment;
	}

	/**
	 * Changes the associated TimeSegment entity
	 *
	 * @param segment the new (or updated) segment
	 */
	private void setTimeSegment(TimeSegment segment) {
		this.segment = segment;
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
		//Remove listeners since the update is initiated by us
		description.removeListener(descriptionChangeListener);
		start.removeListener(startChangeListener);
		end.removeListener(endChangeListener);
		//Perform update
		if (description.get() == null || !description.get().equals(segment.getDescription()))
			description.setValue(segment.getDescription());
		if (start.get() == null || !start.get().equals(segment.getStartTime()))
			start.setValue(segment.getStartTime());
		if (end.get() == null || !end.get().equals(segment.getEndTime()))
			end.setValue(segment.getEndTime());
		duration.setValue(segment.getDuration().toString(new PeriodFormatterBuilder().printZeroIfSupported().appendHours().appendSeparator(":").minimumPrintedDigits(2).appendMinutes().appendSeparator(":").appendSeconds().toFormatter()));
		//Restore listener
		description.addListener(descriptionChangeListener);
		start.addListener(startChangeListener);
		end.addListener(endChangeListener);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TimeSegmentAdapter)
			return ((TimeSegmentAdapter) obj).getTimeSegment().equals(segment);
		else
			return obj == null && segment == null;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 47 * hash + Objects.hashCode(this.segment.hashCode());
		return hash;
	}
}
