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

	private StringProperty description = new SimpleStringProperty();
	private ObjectProperty<Date> start = new SimpleObjectProperty<>(), end = new SimpleObjectProperty<>();
	private TimeSegment segment;
	private TaskManager taskManager;
	private Timer timer;
	private ObjectProperty<TaskAdapter> ownerTask = new SimpleObjectProperty<>();
	private BooleanProperty timingProperty = new SimpleBooleanProperty(false);

	public TimeSegmentAdapter(TimeSegment segment, TaskAdapter ownerTask, TaskManager taskManager) {
		this.segment = segment;
		this.taskManager = taskManager;
		this.ownerTask.setValue(ownerTask);

		updateFxProperties();
		setChangeListeners();
	}

	private void setChangeListeners() {
		this.description.addListener(new ChangeListener<String>() {
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
							setTimeSegment(entityManager.find(TimeSegment.class, getTimeSegment().getId()));
							getTimeSegment().setDescription(newValue);
						}
					}.setNewValue(newValue));
					updateFxProperties();
					getTaskManager().signalTaskUpdate();
				}
			}
		});

		this.start.addListener(new ChangeListener<Date>() {
			@Override
			public void changed(ObservableValue<? extends Date> observableValue, Date oldValue, Date newValue) {
				if (!oldValue.equals(newValue) && getTaskManager() != null) {
					//TODO: detect if the change was actually initiated by us
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
		});

		this.end.addListener(new ChangeListener<Date>() {
			@Override
			public void changed(ObservableValue<? extends Date> observableValue, Date oldValue, Date newValue) {
				if (!oldValue.equals(newValue) && getTaskManager() != null) {
					//TODO: detect if the change was actually initiated by us
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
		});

		this.ownerTask.addListener(new ChangeListener<TaskAdapter>() {
			private TimeSegmentAdapter adapter;

			public ChangeListener<TaskAdapter> setAdapter(TimeSegmentAdapter adapter) {
				this.adapter = adapter;
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends TaskAdapter> ov, TaskAdapter oldValue, TaskAdapter newValue) {
				if (!oldValue.equals(newValue) && getTaskManager() != null) {
					//TODO: detect if the change was actually initiated by us
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
					oldValue.updateFromDatabase();
					newValue.updateFromDatabase();
					updateFxProperties();
				}
			}
		}.setAdapter(this));
	}

	public ObjectProperty<Date> startProperty() {
		return start;
	}

	public ObjectProperty<Date> endProperty() {
		return end;
	}

	public StringProperty descriptionProperty() {
		return description;
	}

	public BooleanProperty isTimingProperty() {
		return timingProperty;
	}

	public ObjectProperty<TaskAdapter> ownerTaskProperty() {
		return ownerTask;
	}

	public void startTiming() {
		timingProperty.set(true);
		Date currentDate = new Date();
		endProperty().setValue(currentDate);
		startProperty().setValue(currentDate);
		if (timer != null)
			timer.cancel();
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

	public void stopTiming() {
		timer.cancel();
		timer = null;
		endProperty().setValue(new Date());
		timingProperty.set(false);
	}

	private TaskManager getTaskManager() {
		return taskManager;
	}

	private void updateFxProperties() {
		description.setValue(segment.getDescription());
		if (start.get() == null || !start.get().equals(segment.getStartTime()))
			start.setValue(segment.getStartTime());
		if (end.get() == null || !end.get().equals(segment.getEndTime()))
			end.setValue(segment.getEndTime());
	}

	/*
	 * Getters/setters
	 */
	public TimeSegment getTimeSegment() {
		return segment;
	}

	private void setTimeSegment(TimeSegment segment) {
		this.segment = segment;
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
