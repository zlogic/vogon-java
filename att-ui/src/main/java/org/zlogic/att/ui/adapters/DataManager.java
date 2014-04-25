/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.adapters;

import java.time.Duration;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javax.persistence.EntityManager;
import org.zlogic.att.data.CustomField;
import org.zlogic.att.data.Filter;
import org.zlogic.att.data.FilterCustomField;
import org.zlogic.att.data.FilterDate;
import org.zlogic.att.data.FilterTaskCompleted;
import org.zlogic.att.data.PersistenceHelper;
import org.zlogic.att.data.Task;
import org.zlogic.att.data.TimeSegment;
import org.zlogic.att.data.TransactedChange;
import org.zlogic.att.ui.filter.FilterFactory;
import org.zlogic.att.ui.filter.FilterHolder;
import org.zlogic.att.ui.filter.FilterTypeFactory;
import org.zlogic.att.ui.filter.adapters.FilterAdapter;
import org.zlogic.att.ui.filter.adapters.FilterCustomFieldAdapter;
import org.zlogic.att.ui.filter.adapters.FilterDateAdapter;
import org.zlogic.att.ui.filter.adapters.FilterTaskCompletedAdapter;

/**
 * Placeholder class to contain an ObservableList of all TaskAdapters. This
 * class is a central point for contacting the Data Storage layer.
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class DataManager {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(DataManager.class.getName());
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	/**
	 * The persistence helper instance
	 */
	private PersistenceHelper persistenceHelper = new PersistenceHelper();
	/**
	 * List of all tasks
	 */
	private ObservableList<TaskAdapter> tasks = FXCollections.observableList(new LinkedList<TaskAdapter>());
	/**
	 * Map of all time segments, id=key
	 */
	private ObservableList<TimeSegmentAdapter> timeSegments = FXCollections.observableList(new LinkedList<TimeSegmentAdapter>());
	/**
	 * The last update date
	 */
	private ObjectProperty<Duration> filteredTotalTime = new SimpleObjectProperty<>();
	/**
	 * Possible values of custom fields, used for autocomplete, with filter
	 * applied
	 */
	private Map<CustomFieldAdapter, ObservableList<String>> filteredCustomFieldValues = new TreeMap<>();
	/**
	 * Possible values of custom fields, used for autocomplete, without filter
	 * applied
	 */
	private Map<CustomFieldAdapter, ObservableList<String>> allCustomFieldValues = new TreeMap<>();
	/**
	 * List of all custom fields
	 */
	private ObservableList<CustomFieldAdapter> customFields = FXCollections.observableList(new LinkedList<CustomFieldAdapter>());
	/**
	 * The currently active (timing) segment
	 */
	private ObjectProperty<TimeSegmentAdapter> timingSegment = new SimpleObjectProperty<>();
	/**
	 * The currently active filters
	 */
	private ObservableList<FilterHolder> filters = FXCollections.observableList(new LinkedList<FilterHolder>());
	/**
	 * The filter factory used to create filters and display the list of
	 * available filters
	 */
	private FilterFactory filterBuilder = new FilterFactory(this);
	/**
	 * Lock to prevent reloading of tasks from interfering with other threads
	 */
	private ReadWriteLock reloadLock = new ReentrantReadWriteLock();
	/**
	 * Property indicating that task updates affecting sort order should be
	 * paused
	 */
	private ReadOnlyBooleanWrapper pauseUpdates = new ReadOnlyBooleanWrapper();
	/**
	 * Property indicating the cells being edited
	 */
	private ObservableSet<Node> editingCells = FXCollections.observableSet();
	/**
	 * Property indicating that a task is not being edited
	 */
	private BooleanProperty editingCellsEmpty = new SimpleBooleanProperty(true);
	/**
	 * Property indicating that a task is being dragged'n'dropped
	 */
	private BooleanProperty draggingTask = new SimpleBooleanProperty(false);
	/**
	 * Tasks updated event
	 */
	private EventType tasksUpdatedEvent = new EventType<>("Tasks updated");
	/**
	 * List of listeners for task updates (e.g. to update sort order)
	 */
	private List<EventHandler<Event>> tasksUpdatedListeners = new LinkedList<>();

	/**
	 * Creates a DataManager instance
	 */
	public DataManager() {
		/*((DataManager) this).reloadTasks();*/
		editingCells.addListener(new SetChangeListener<Node>() {

			@Override
			public void onChanged(SetChangeListener.Change<? extends Node> change) {
				editingCellsEmpty.set(editingCells.isEmpty());
			}
		});
		pauseUpdates.bind(editingCellsEmpty.not().or(draggingTask));
	}

	/**
	 * Begins the shutdown process. Stops any timing tasks and blocks access to
	 * the database.
	 */
	public void shutdown() {
		try {
			reloadLock.writeLock().lock();
			persistenceHelper.shutdown();
			if (timingSegment.get() != null)
				timingSegment.get().stopTiming();
		} finally {
			reloadLock.writeLock().unlock();
		}
	}

	/**
	 * Adds a new value to the list of possible CustomField values.
	 *
	 * @param adapter the custom field
	 * @param value the value to be added
	 */
	protected void addFilteredCustomFieldValue(CustomFieldAdapter adapter, String value) {
		if (value == null)
			value = ""; //NOI18N
		if (!filteredCustomFieldValues.containsKey(adapter))
			filteredCustomFieldValues.put(adapter, FXCollections.observableList(new LinkedList<String>()));
		if (!allCustomFieldValues.containsKey(adapter))
			allCustomFieldValues.put(adapter, FXCollections.observableList(new LinkedList<String>()));
		ObservableList<String> values = filteredCustomFieldValues.get(adapter);
		if (!values.contains(value)) {
			values.add(value);
			FXCollections.sort(values);
		}
		values = allCustomFieldValues.get(adapter);
		if (!values.contains(value)) {
			values.add(value);
			FXCollections.sort(values);
		}
	}

	/**
	 * Removes a value from the list of possible CustomField values. Performs
	 * this only if the value is not used in any custom field.
	 *
	 * @param adapter the custom field
	 * @param value the value of the Custom Field
	 * @return true if the value was removed
	 */
	protected boolean removeFilteredCustomFieldValue(CustomFieldAdapter adapter, String value) {
		if (!filteredCustomFieldValues.containsKey(adapter))
			filteredCustomFieldValues.put(adapter, FXCollections.observableList(new LinkedList<String>()));
		//Check if value is no longer used
		for (CustomFieldAdapter customField : customFields)
			for (TaskAdapter task : tasks) {
				String taskCustomFieldValue = task.getTask().getCustomField(customField.getCustomField());
				if (taskCustomFieldValue != null && taskCustomFieldValue.equals(value))
					return false;
			}
		//Remove the value
		List<String> values = filteredCustomFieldValues.get(adapter);
		return values.remove(value);
	}

	/**
	 * Finds the TaskAdapter associated with a Task entity
	 *
	 * @param task the Task entity to be searched
	 * @return the associated TaskAdapter instance, or null
	 */
	public TaskAdapter findTaskAdapter(Task task) {
		try {
			reloadLock.readLock().lock();
			for (TaskAdapter taskAdapter : tasks)
				if (taskAdapter.getTask().equals(task))
					return taskAdapter;
			return null;
		} finally {
			reloadLock.readLock().unlock();
		}
	}

	/**
	 * Finds the TimeSegmentAdapter associated with a TimeSegment entity
	 *
	 * @param timeSegment the TimeSegment entity to be searched
	 * @return the associated TimeSegmentAdapter instance, or null
	 */
	public TimeSegmentAdapter findTimeSegmentAdapter(TimeSegment timeSegment) {
		try {
			reloadLock.readLock().lock();
			for (TimeSegmentAdapter timeSegmentAdapter : timeSegments)
				if (timeSegmentAdapter.getTimeSegment().equals(timeSegment))
					return timeSegmentAdapter;
			return null;
		} finally {
			reloadLock.readLock().unlock();
		}
	}

	/**
	 * Returns the start date filter (if it exists)
	 *
	 * @return the start date, or null if no start filters exist
	 */
	public Date getFilterStartDate() {
		Date startDate = null;
		for (FilterHolder filter : filters)
			if (filter.filterProperty().get() != null && filter.filterProperty().get().getFilter() instanceof FilterDate) {
				FilterDate filterDate = (FilterDate) filter.filterProperty().get().getFilter();
				if (filterDate.getType() == FilterDate.DateType.DATE_AFTER)
					startDate = (startDate == null || filterDate.getAppliedDate().after(startDate)) ? filterDate.getAppliedDate() : startDate;
			}
		return startDate;
	}

	/**
	 * Returns the end date filter (if it exists)
	 *
	 * @return the end date, or null if no end filters exist
	 */
	public Date getFilterEndDate() {
		Date endDate = null;
		for (FilterHolder filter : filters)
			if (filter.filterProperty().get() != null && filter.filterProperty().get().getFilter() instanceof FilterDate) {
				FilterDate filterDate = (FilterDate) filter.filterProperty().get().getFilter();
				if (filterDate.getType() == FilterDate.DateType.DATE_BEFORE)
					endDate = (endDate == null || filterDate.getAppliedDate().before(endDate)) ? filterDate.getAppliedDate() : endDate;
			}
		return endDate;
	}

	/**
	 * Starts timing a TimeSegment, stopping any already timed tasks
	 *
	 * @param segment the segment to be timed
	 */
	public void startTiming(TimeSegmentAdapter segment) {
		//Stop existing task (if any)
		stopTiming();
		timingSegment.setValue(segment);
		timingSegment.get().startTiming();
	}

	/**
	 * Stops timing the active TimeSegment (if any)
	 */
	public void stopTiming() {
		if (timingSegment.get() != null) {
			timingSegment.get().stopTiming();
			timingSegment.setValue(null);
		}
	}

	/**
	 * Returns a list of all possible CustomField values for a specific
	 * CustomField. Used for autocomplete. Filter is applied.
	 *
	 * @param adapter the CustomFieldAdapter for which values will be retrieved
	 * @return the list of all possible CustomField values
	 */
	public ObservableList<String> getFilteredCustomFieldValues(CustomFieldAdapter adapter) {
		return filteredCustomFieldValues.get(adapter);
	}

	/**
	 * Returns a list of all possible CustomField values for a specific
	 * CustomField. Used for autocomplete. Filter is not applied (returns all
	 * possible values).
	 *
	 * @param adapter the CustomFieldAdapter for which values will be retrieved
	 * @return the list of all possible CustomField values
	 */
	public ObservableList<String> getAllCustomFieldValues(CustomFieldAdapter adapter) {
		return allCustomFieldValues.get(adapter);
	}

	/**
	 * Updates the lastTaskUpdate property, signaling that the task list has
	 * updated.
	 */
	protected void signalTaskUpdate() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (!pauseUpdates.get()) {
					for (EventHandler listener : tasksUpdatedListeners)
						listener.handle(new Event(tasksUpdatedEvent));
				}
			}
		});
	}

	/**
	 * Adds the specified period to the filtered total time.
	 *
	 * @param addTime time to be added (can be negative)
	 */
	protected void addFilteredTotalTime(Duration addTime) {
		if (filteredTotalTime.get() != null)
			filteredTotalTime.setValue(filteredTotalTime.get().plus(addTime));
		else
			filteredTotalTime.setValue(addTime);
	}

	/**
	 * Updates the filtered total time from existing task list.
	 */
	protected void updateFilteredTotalTime() {
		Duration totalTime = Duration.ZERO;
		//Get the start/end dates
		Date startDate = getFilterStartDate();
		Date endDate = getFilterEndDate();
		for (TaskAdapter task : tasks)
			totalTime = totalTime.plus(task.getTask().getTotalTime(startDate, endDate));
		filteredTotalTime.setValue(totalTime);
	}
	/*
	 * Database functions
	 */

	/**
	 * Reloads the tasks from database. Forgets old TaskAdapters.
	 */
	public void reloadTasks() {
		try {
			reloadLock.writeLock().lock();
			tasks.clear();
			timeSegments.clear();
			if (timingSegment.get() != null) {
				TaskAdapter ownerTask = timingSegment.get().ownerTaskProperty().get();
				//Keep the currently timing segment's task
				tasks.add(ownerTask);

				//Restore the currently timing segment's task's segments
				timeSegments.add(timingSegment.get());
				for (TimeSegmentAdapter segment : ownerTask.timeSegmentsProperty())
					addTimeSegmentAdapter(segment);
			}
			//Load all other tasks except for the timing segment task
			for (Task task : persistenceHelper.getAllTasks(true))
				if (findTaskAdapter(task) == null)
					tasks.add(new TaskAdapter(task, this));
			reloadCustomFields();
			reloadFilters();
			updateFilteredTotalTime();
			signalTaskUpdate();
		} finally {
			reloadLock.writeLock().unlock();
		}
	}

	/**
	 * Applies task filters.
	 */
	public void applyFilters() {
		reloadTasks();
		signalTaskUpdate();
	}

	/**
	 * Reloads all custom field values
	 */
	public void reloadAllCustomFieldValues() {
		allCustomFieldValues.clear();
		for (Map.Entry<CustomField, Set<String>> entry : persistenceHelper.getAllCustomFieldValues().entrySet())
			for (CustomFieldAdapter customFieldAdapter : customFields)
				if (customFieldAdapter.getCustomField().equals(entry.getKey())) {
					ObservableList<String> customFieldValues = FXCollections.observableArrayList(entry.getValue());
					if (!customFieldValues.contains("")) //NOI18N
						customFieldValues.add(""); //NOI18N
					FXCollections.sort(customFieldValues);
					allCustomFieldValues.put(customFieldAdapter, customFieldValues);
					break;
				}
	}

	/**
	 * Reloads the custom fields from database. Forgets old CustomFieldAdapters,
	 * replaces CustomFieldValueAdapters in tasks and filters with the updated
	 * version.
	 */
	public void reloadCustomFields() {
		filteredCustomFieldValues.clear();
		customFields.clear();
		for (CustomField customField : persistenceHelper.getCustomFields())
			customFields.add(new CustomFieldAdapter(customField, this));
		for (TaskAdapter task : tasks)
			for (CustomFieldAdapter adapter : customFields)
				addFilteredCustomFieldValue(adapter, task.getTask().getCustomField(adapter.getCustomField()));
		for (FilterHolder filter : filters)
			if (filter.filterProperty().get() instanceof FilterCustomFieldAdapter)
				((FilterCustomFieldAdapter) filter.filterProperty().get()).updateCustomFieldAdapter();

		reloadAllCustomFieldValues();
	}

	/**
	 * Reloads the filters from database. Forgets old FilterAdapters.
	 */
	private void reloadFilters() {
		filters.clear();
		for (Filter filter : persistenceHelper.getAllFilters()) {
			FilterAdapter filterAdapter = null;
			if (filter instanceof FilterDate) {
				filterAdapter = new FilterDateAdapter(this, (FilterDate) filter);
			} else if (filter instanceof FilterCustomField) {
				filterAdapter = new FilterCustomFieldAdapter(this, (FilterCustomField) filter);
			} else if (filter instanceof FilterTaskCompleted) {
				filterAdapter = new FilterTaskCompletedAdapter(this, (FilterTaskCompleted) filter);
			}

			FilterTypeFactory filterType = filterBuilder.getFilterTypeFor(filterAdapter);
			if (filterType != null)
				filters.add(new FilterHolder(filterAdapter, filterType));
			else
				log.log(Level.WARNING, messages.getString("CANNOT_FIND_A_SUITABLE_FILTERADAPTER_CONSTRUCTOR"), filter.getId());
		}
	}

	/**
	 * Creates and persists a new TimeSegment. It's recommended to use
	 * TaskAdapter.createTimeSegment() instead.
	 *
	 * @param owner the owner TaskAdapter
	 * @return the TimeSegmentAdapter associated with the new TimeSegment
	 */
	public TimeSegmentAdapter createTimeSegment(TaskAdapter owner) {
		TimeSegmentAdapter newSegment = new TimeSegmentAdapter(persistenceHelper.createTimeSegment(owner.getTask()), owner, this);
		if (!timeSegments.contains(newSegment))
			timeSegments.add(newSegment);
		signalTaskUpdate();
		return newSegment;
	}

	/**
	 * Adds a time segment to the list of all time segments in case it's not
	 * already added
	 *
	 * @param timeSegment the time segment to add
	 */
	public void addTimeSegmentAdapter(TimeSegmentAdapter timeSegment) {
		if (!timeSegments.contains(timeSegment))
			timeSegments.add(timeSegment);
	}

	/**
	 * Creates and persists a new Task
	 *
	 * @return the TaskAdapter associated with the new Task
	 */
	public TaskAdapter createTask() {
		TaskAdapter newTask = new TaskAdapter(persistenceHelper.createTask(), this);
		tasks.add(newTask);
		signalTaskUpdate();
		return newTask;
	}

	/**
	 * Creates and persists a new CustomField
	 *
	 * @return the CustomFieldAdapter associated with the new CustomField
	 */
	public CustomFieldAdapter createCustomField() {
		CustomFieldAdapter customField = new CustomFieldAdapter(persistenceHelper.createCustomField(), this);
		customFields.add(customField);
		return customField;
	}

	/**
	 * Deletes a time segment
	 *
	 * @param segment the segment to be deleted
	 */
	public void deleteSegment(TimeSegmentAdapter segment) {
		if (segment.isTimingProperty().get())
			segment.stopTiming();
		TaskAdapter ownerTask = findTaskAdapter(segment.getTimeSegment().getOwner());
		persistenceHelper.performTransactedChange(new TransactedChange() {
			private TimeSegment deleteSegment;

			public TransactedChange setDeleteSegment(TimeSegment deleteSegment) {
				this.deleteSegment = deleteSegment;
				return this;
			}

			@Override
			public void performChange(EntityManager entityManager) {
				Task task = entityManager.find(Task.class, deleteSegment.getOwner().getId());
				TimeSegment segment = entityManager.find(TimeSegment.class, deleteSegment.getId());
				task.removeSegment(segment);
				entityManager.remove(segment);
			}
		}.setDeleteSegment(segment.getTimeSegment()));
		timeSegments.remove(segment);
		ownerTask.updateFromDatabase();
		addFilteredTotalTime(Duration.ZERO.minus(segment.getTimeSegment().getClippedDuration(getFilterStartDate(), getFilterEndDate())));
	}

	/**
	 * Deletes a task
	 *
	 * @param task the task to be deleted
	 */
	public void deleteTask(TaskAdapter task) {
		for (TimeSegmentAdapter timeSegment : task.timeSegmentsProperty())
			if (timeSegment.isTimingProperty().get())
				timeSegment.stopTiming();
		persistenceHelper.performTransactedChange(new TransactedChange() {
			private Task deleteTask;

			public TransactedChange setDeleteTask(Task deleteTask) {
				this.deleteTask = deleteTask;
				return this;
			}

			@Override
			public void performChange(EntityManager entityManager) {
				Task task = entityManager.find(Task.class, deleteTask.getId());
				//TODO: check that time segments will be deleted from database
				entityManager.remove(task);
			}
		}.setDeleteTask(task.getTask()));
		for (TimeSegmentAdapter timeSegment : task.timeSegmentsProperty())
			timeSegments.remove(timeSegment);
		tasks.remove(task);
		addFilteredTotalTime(Duration.ZERO.minus(task.getTask().getTotalTime(getFilterStartDate(), getFilterEndDate())));
		signalTaskUpdate();
	}

	/**
	 * Deletes a custom field. Removes values for this field stored in tasks.
	 *
	 * @param customField the custom field to be deleted
	 */
	public void deleteCustomField(CustomFieldAdapter customField) {
		List<Task> affectedTasks = new LinkedList<>();
		persistenceHelper.performTransactedChange(new TransactedChange() {
			private CustomField deleteCustomField;
			private List<Task> affectedTasks;

			public TransactedChange setParameters(CustomField deleteCustomField, List<Task> affectedTasks) {
				this.deleteCustomField = deleteCustomField;
				this.affectedTasks = affectedTasks;
				return this;
			}

			@Override
			public void performChange(EntityManager entityManager) {
				CustomField customField = entityManager.find(CustomField.class, deleteCustomField.getId());
				for (Task task : persistenceHelper.getAllTasks(entityManager, false)) {
					if (task.getCustomField(customField) != null && !affectedTasks.contains(task))
						affectedTasks.add(task);
					task.setCustomField(customField, null);
				}
				entityManager.remove(customField);
			}
		}.setParameters(customField.getCustomField(), affectedTasks));
		customFields.remove(customField);
		filteredCustomFieldValues.remove(customField);
		allCustomFieldValues.remove(customField);
		for (Task affectedTask : affectedTasks) {
			TaskAdapter taskAdapter = findTaskAdapter(affectedTask);
			if (taskAdapter != null)
				taskAdapter.updateFromDatabase();
		}
		for (FilterHolder filter : filters)
			if (filter.filterProperty().get() instanceof FilterCustomFieldAdapter) {
				FilterCustomFieldAdapter customFieldFilter = (FilterCustomFieldAdapter) filter.filterProperty().get();
				if (customFieldFilter.getFilter().getCustomField().equals(customField.getCustomField())) {
					filters.remove(filter);
					filterBuilder.deleteFilter(customFieldFilter);
				}
			}
	}

	/**
	 * Adds a listener to tasks updated event
	 *
	 * @param listener
	 */
	public void addTasksUpdatedListener(EventHandler listener) {
		if (!tasksUpdatedListeners.contains(listener))
			tasksUpdatedListeners.add(listener);
	}
	/*
	 * Java FX properties
	 */

	/**
	 * The currently timing TimeSegment property (value may be null if nothing's
	 * being timed)
	 *
	 * @return currently timing TimeSegment
	 */
	public ObjectProperty<TimeSegmentAdapter> timingSegmentProperty() {
		return timingSegment;
	}

	/**
	 * Returns the list of all tasks
	 *
	 * @return the list of all tasks
	 */
	public ObservableList<TaskAdapter> getTasks() {
		return tasks;
	}

	/**
	 * Returns the list of all time segments
	 *
	 * @return the list of all time segments
	 */
	public ObservableList<TimeSegmentAdapter> getTimeSegments() {
		return timeSegments;
	}

	/**
	 * Returns the list of all custom fields
	 *
	 * @return the list of all custom fields
	 */
	public ObservableList<CustomFieldAdapter> getCustomFields() {
		return customFields;
	}

	/**
	 * Returns the list of all filters
	 *
	 * @return the list of all filters
	 */
	public ObservableList<FilterHolder> getFilters() {
		return filters;
	}

	/**
	 * Total time for filtered tasks property
	 *
	 * @return the total time for filtered tasks property
	 */
	public ObjectProperty<Duration> filteredTotalTimeProperty() {
		return filteredTotalTime;
	}

	/**
	 * Property indicating the cells being edited
	 *
	 * @return the property indicating the cells being edited
	 */
	public ObservableSet<Node> editingCellsProperty() {
		return editingCells;
	}

	/**
	 * Property indicating that a task is being dragged'n'dropped
	 *
	 * @return the property indicating that a task is being dragged'n'dropped
	 */
	public BooleanProperty draggingTaskProperty() {
		return draggingTask;
	}

	/**
	 * Property indicating that task updates affecting sort order should be
	 * paused
	 *
	 * @return the property indicating that task updates affecting sort order
	 * should be paused
	 */
	public ReadOnlyBooleanProperty pauseUpdatesProperty() {
		return pauseUpdates.getReadOnlyProperty();
	}
	/*
	 * Getters/setters
	 */

	/**
	 * Returns the PersistenceHelper instance
	 *
	 * @return the PersistenceHelper instance
	 */
	public PersistenceHelper getPersistenceHelper() {
		return persistenceHelper;
	}

	/**
	 * Returns the FilterFactory instance
	 *
	 * @return the FilterFactory instance
	 */
	public FilterFactory getFilterBuilder() {
		return filterBuilder;
	}
}
