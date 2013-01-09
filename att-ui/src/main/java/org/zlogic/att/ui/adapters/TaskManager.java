/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.adapters;

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

	public TaskManager(ObservableList<TaskAdapter> tasks) {
		this.tasks = tasks;
	}

	public ObservableList<TaskAdapter> getTaskList() {
		return tasks;
	}

	public PersistenceHelper getPersistenceHelper() {
		return persistenceHelper;
	}

	public void reloadTasks() {
		tasks.clear();
		for (Task task : persistenceHelper.getAllTasks())
			tasks.add(new TaskAdapter(task, this));
	}

	public TaskAdapter findTaskAdapter(Task task) {
		for (TaskAdapter taskAdapter : tasks)
			if (taskAdapter.getTask().equals(task))
				return taskAdapter;
		return null;
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
