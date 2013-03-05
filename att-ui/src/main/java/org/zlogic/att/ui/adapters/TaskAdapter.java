/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.adapters;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.joda.time.format.PeriodFormatterBuilder;
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
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(TaskAdapter.class.getName());
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/adapters/messages");
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
	 * Property to indicate if the task is currently timing
	 */
	private BooleanProperty timingProperty = new SimpleBooleanProperty(false);
	/**
	 * DataManager reference
	 */
	private DataManager dataManager;

	/*
	 * Change listeners
	 */
	/**
	 * Description change listener
	 */
	private ChangeListener<String> descriptionChangeListener = new ChangeListener<String>() {
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
						setTask(getDataManager().getPersistenceHelper().getTaskFromDatabase(getTask().getId(), entityManager));
						getTask().setDescription(newValue);
					}
				}.setNewValue(newValue));
				updateFxProperties();
				getDataManager().signalTaskUpdate();
			}
		}
	};
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
						setTask(getDataManager().getPersistenceHelper().getTaskFromDatabase(getTask().getId(), entityManager));
						getTask().setName(newValue);
					}
				}.setNewValue(newValue));
				updateFxProperties();
				getDataManager().signalTaskUpdate();
			}
		}
	};
	/**
	 * Completed change listener
	 */
	private ChangeListener<Boolean> completedChangeListener = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
			if (!oldValue.equals(newValue) && getDataManager() != null) {
				getDataManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
					private boolean newValue;

					public TransactedChange setNewValue(boolean newValue) {
						this.newValue = newValue;
						return this;
					}

					@Override
					public void performChange(EntityManager entityManager) {
						setTask(getDataManager().getPersistenceHelper().getTaskFromDatabase(getTask().getId(), entityManager));
						getTask().setCompleted(newValue);
					}
				}.setNewValue(newValue));
				updateFxProperties();
				getDataManager().signalTaskUpdate();
			}
		}
	};

	/**
	 * Creates a TaskAdapter instance
	 *
	 * @param task the associated task
	 * @param dataManager the DataManager reference
	 */
	public TaskAdapter(Task task, DataManager dataManager) {
		this.task = task;
		this.dataManager = dataManager;

		//Populate time segments
		for (TimeSegment timeSegment : task.getTimeSegments())
			this.dataManager.getTimeSegments().add(new TimeSegmentAdapter(timeSegment, this, this.dataManager));

		updateFxProperties();
	}

	/**
	 * Updates the associated entity from database
	 */
	public void updateFromDatabase() {
		getDataManager().getPersistenceHelper().performTransactedChange(new TransactedChange() {
			@Override
			public void performChange(EntityManager entityManager) {
				setTask(getDataManager().getPersistenceHelper().getTaskFromDatabase(getTask().getId(), entityManager));
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

	/**
	 * The task currently timing state property (true if is timing, false if
	 * timer is not running)
	 *
	 * @return the task timing state property
	 */
	public BooleanProperty isTimingProperty() {
		return timingProperty;
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
	//TODO: other Getters/setters

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
		updateTimeSegments();
		updateTimeProperty();
		//Restore listeners
		this.description.addListener(descriptionChangeListener);
		this.name.addListener(nameChangeListener);
		this.completed.addListener(completedChangeListener);
	}

	/**
	 * Updated the timeSegments array from the entity
	 */
	protected void updateTimeSegments() {
		List<TimeSegmentAdapter> orphanedSegments = new LinkedList<>();
		for (TimeSegmentAdapter segment : timeSegments)
			if (!task.getTimeSegments().contains(segment.getTimeSegment()))
				orphanedSegments.add(segment);
		timeSegments.removeAll(orphanedSegments);
		for (TimeSegment segment : task.getTimeSegments()) {
			TimeSegmentAdapter segmentAdapter = dataManager.findTimeSegmentAdapter(segment);
			if (segmentAdapter == null) {
				log.log(Level.SEVERE, messages.getString("CANNOT_FIND_TIME_SEGMENT_DURING_UPDATETIMESEGMENTS"), new Object[]{segment.getId(), segment.getDescription()});
				continue;
			}
			if (!timeSegments.contains(segmentAdapter))
				timeSegments.add(segmentAdapter);
		}
	}

	/**
	 * Updates time-associated (generated) properties
	 */
	protected void updateTimeProperty() {
		firstTime.setValue(getEarliestTime());
		lastTime.setValue(getLatestTime());
		totalTime.setValue(task.getTotalTime().toString(new PeriodFormatterBuilder().printZeroIfSupported().appendHours().appendSeparator(":").minimumPrintedDigits(2).appendMinutes().appendSeparator(":").appendSeconds().toFormatter()));//TODO: reuse this formatter
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
		TimeSegmentAdapter newSegment = dataManager.createTimeSegment(this);
		updateFromDatabase();
		return newSegment;
	}
}
