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

	private ObservableList<TaskAdapter> tasks;
	private PersistenceHelper persistenceHelper = new PersistenceHelper();
	private ObjectProperty<Date> lastTaskUpdate = new SimpleObjectProperty<>();
	private Map<CustomFieldAdapter, ObservableList<String>> customFieldValues = new TreeMap<>();
	private ObservableList<CustomFieldAdapter> customFields = FXCollections.observableList(new LinkedList<CustomFieldAdapter>());

	public TaskManager(ObservableList<TaskAdapter> tasks) {
		this.tasks = tasks;//TODO: load from database instead
		((TaskManager) this).reloadCustomFields();
	}

	public ObservableList<TaskAdapter> getTaskList() {
		return tasks;
	}

	public ObjectProperty<Date> taskUpdatedProperty() {
		return lastTaskUpdate;
	}

	public ObservableList<CustomFieldAdapter> getCustomFields() {
		return customFields;
	}

	public PersistenceHelper getPersistenceHelper() {
		return persistenceHelper;
	}

	public void reloadTasks() {
		tasks.clear();
		for (Task task : persistenceHelper.getAllTasks())
			tasks.add(new TaskAdapter(task, this));
		reloadCustomFields();
	}

	public void reloadCustomFields() {
		customFieldValues.clear();
		customFields.clear();
		for (CustomField customField : persistenceHelper.getCustomFields())
			customFields.add(new CustomFieldAdapter(customField, this));
		for (TaskAdapter task : tasks)
			for (CustomFieldAdapter adapter : customFields)
				addCustomFieldValue(adapter, task.getTask().getCustomField(adapter.getCustomField()));
	}

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

	protected boolean removeCustomFieldValue(CustomFieldAdapter adapter, String value) {
		if (!customFieldValues.containsKey(adapter))
			customFieldValues.put(adapter, FXCollections.observableList(new LinkedList<String>()));
		List<String> values = customFieldValues.get(adapter);
		return values.remove(value);
	}

	public TaskAdapter findTaskAdapter(Task task) {
		for (TaskAdapter taskAdapter : tasks)
			if (taskAdapter.getTask().equals(task))
				return taskAdapter;
		return null;
	}

	public ObservableList<String> getCustomFieldValues(CustomFieldAdapter adapter) {
		return customFieldValues.get(adapter);
	}

	protected void signalTaskUpdate() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				lastTaskUpdate.set(new Date());
			}
		});
	}

	/*
	 * TODO: keep all entity-properties here and update from database if necessary
	 */
	/*
	 * TODO: handle creations here as well
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
}
