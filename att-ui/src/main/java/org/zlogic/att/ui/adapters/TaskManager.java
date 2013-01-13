/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.adapters;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.persistence.EntityManager;
import org.zlogic.att.data.CustomField;
import org.zlogic.att.data.PersistenceHelper;
import org.zlogic.att.data.Task;
import org.zlogic.att.data.TimeSegment;
import org.zlogic.att.data.TransactedChange;

/**
 * Placeholder class to contain an ObservableList of all TaskAdapters. This
 * class is a central point for contacting the Data Storage layer.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class TaskManager {
	/**
	 * List of all tasks
	 */
	private ObservableList<TaskAdapter> tasks = FXCollections.observableList(new LinkedList<TaskAdapter>());
	/*
	 * The persistence helper instance
	 */
	private PersistenceHelper persistenceHelper = new PersistenceHelper();
	/*
	 * The last update date
	 */
	//FIXME: Remove one Java FX 3.0 fixes sorting
	private ObjectProperty<Date> lastTaskUpdate = new SimpleObjectProperty<>();
	/**
	 * Possible values of custom fields, used for autocomplete
	 */
	private Map<CustomFieldAdapter, ObservableList<String>> customFieldValues = new TreeMap<>();
	/**
	 * List of all custom fields
	 */
	private ObservableList<CustomFieldAdapter> customFields = FXCollections.observableList(new LinkedList<CustomFieldAdapter>());

	/**
	 * Creates a TaskManager instance
	 */
	public TaskManager() {
		/*((TaskManager) this).reloadTasks();*/
	}

	/**
	 * Adds a new value to the list of possible CustomField values.
	 * @param adapter the custom field 
	 * @param value the value to be added
	 */
	protected void addCustomFieldValue(CustomFieldAdapter adapter, String value) {
		if (value == null)
			return;
		if (!customFieldValues.containsKey(adapter))
			customFieldValues.put(adapter, FXCollections.observableList(new LinkedList<String>()));
		ObservableList<String> values = customFieldValues.get(adapter);
		if (!values.contains(value)) {
			values.add(value);
			FXCollections.sort(values);
		}
	}

	/**
	 * Removes a value from the list of possible CustomField values. Performs this only if the value is not used in any custom field.
	 * @param adapter the custom field
	 * @param value the value of the Custom Field
	 * @return true if the value was removed
	 */
	protected boolean removeCustomFieldValue(CustomFieldAdapter adapter, String value) {
		if (!customFieldValues.containsKey(adapter))
			customFieldValues.put(adapter, FXCollections.observableList(new LinkedList<String>()));
		//Check if value is no longer used
		for(CustomFieldAdapter customField : customFields)
			for(TaskAdapter  task : tasks){
				String taskCustomFieldValue = task.getTask().getCustomField(customField.getCustomField());
				if(taskCustomFieldValue!=null && taskCustomFieldValue.equals(value))
					return false;
			}
		//Remove the value
		List<String> values = customFieldValues.get(adapter);
		return values.remove(value);
	}

	/**
	 * Finds the TaskAdapter associated with a Task entity
	 * @param task the Task entity to be searched
	 * @return the associated TaskAdapter instance, or null
	 */
	public TaskAdapter findTaskAdapter(Task task) {
		for (TaskAdapter taskAdapter : tasks)
			if (taskAdapter.getTask().equals(task))
				return taskAdapter;
		return null;
	}
	/**
	 * Returns a list of all possible CustomField values for a specific CustomField. Used for autocomplete.
	 * @param adapter the CustomFieldAdapter for which values will be retrieved
	 * @return the list of all possible CustomField values
	 */
	public ObservableList<String> getCustomFieldValues(CustomFieldAdapter adapter) {
		return customFieldValues.get(adapter);
	}
	/**
	 * Updates the lastTaskUpdate property, signaling that the task list has updated.
	 */
	protected void signalTaskUpdate() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				lastTaskUpdate.set(new Date());
			}
		});
	}
	/*
	 * Database functions
	 */
	/**
	 * Reloads the tasks from database. Forgets old TaskAdapters.
	 */
	public void reloadTasks() {
		tasks.clear();
		for (Task task : persistenceHelper.getAllTasks())
			tasks.add(new TaskAdapter(task, this));
		reloadCustomFields();
	}
	/**
	 * Reloads the custom fields from database. Forgets old CustomFieldAdapters, replaces CustomFieldValueAdapters in tasks with the updated version.
	 */
	public void reloadCustomFields() {
		customFieldValues.clear();
		customFields.clear();
		for (CustomField customField : persistenceHelper.getCustomFields())
			customFields.add(new CustomFieldAdapter(customField, this));
		for (TaskAdapter task : tasks)
			for (CustomFieldAdapter adapter : customFields)
				addCustomFieldValue(adapter, task.getTask().getCustomField(adapter.getCustomField()));
	}
	//TODO: keep all entity-properties here and update from database if necessary
	//TODO: handle creations here as well
	/**
	 * Deletes a time segment
	 * @param segment the segment to be deleted
	 */
	public void deleteSegment(TimeSegmentAdapter segment) {
		TaskAdapter ownerTask = findTaskAdapter(segment.getTimeSegment().getOwner());
		ownerTask.timeSegmentsProperty().remove(segment);
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
		ownerTask.getTask().removeSegment(segment.getTimeSegment());
	}

	/**
	 * Deletes a task
	 * @param task the task to be deleted
	 */
	public void deleteTask(TaskAdapter task) {
		persistenceHelper.performTransactedChange(new TransactedChange() {
			private Task deleteTask;

			public TransactedChange setDeleteTask(Task deleteTask) {
				this.deleteTask = deleteTask;
				return this;
			}

			@Override
			public void performChange(EntityManager entityManager) {
				Task task = entityManager.find(Task.class, deleteTask.getId());
				//TODO: check that time segments will be deleted
				entityManager.remove(task);
			}
		}.setDeleteTask(task.getTask()));
		tasks.remove(task);
	}

	/**
	 * Deletes a custom field. Removes values for this field stored in tasks.
	 * @param customField the custom field to be deleted
	 */
	public void deleteCustomField(CustomFieldAdapter customField) {
		persistenceHelper.performTransactedChange(new TransactedChange() {
			private CustomField deleteCustomField;

			public TransactedChange setDeleteCustomField(CustomField deleteCustomField) {
				this.deleteCustomField = deleteCustomField;
				return this;
			}

			@Override
			public void performChange(EntityManager entityManager) {
				CustomField customField = entityManager.find(CustomField.class, deleteCustomField.getId());
				for (Task task : persistenceHelper.getAllTasks(entityManager))
					task.setCustomField(customField, null);
				entityManager.remove(customField);
			}
		}.setDeleteCustomField(customField.getCustomField()));
		//TODO: Remove from custom fields observableList in the future
	}
	
	/*
	 * Getters/setters
	 */
	/**
	 * Returns the list of all tasks
	 * @return the list of all tasks
	 */
	public ObservableList<TaskAdapter> getTaskList() {
		return tasks;
	}

	/**
	 * Last task update date property
	 * @return the last task update date property
	 */
	public ObjectProperty<Date> taskUpdatedProperty() {
		return lastTaskUpdate;
	}

	/**
	 * Custom fields list property
	 * @return the custom fields property
	 */
	public ObservableList<CustomFieldAdapter> getCustomFields() {
		return customFields;
	}

	/**
	 * Returns the PersistenceHelper instance
	 * @return the PersistenceHelper instance
	 */
	public PersistenceHelper getPersistenceHelper() {
		return persistenceHelper;
	}
}
