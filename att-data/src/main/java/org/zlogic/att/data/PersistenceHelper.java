/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import org.zlogic.att.data.converters.Importer;

/**
 * Helper class to perform routine entity modifications and create a single
 * point where EntityManager is used.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class PersistenceHelper {

	/**
	 * Default constructor
	 */
	public PersistenceHelper() {
	}

	/**
	 * Creates a Task entity
	 *
	 * @return the new Task entity, persisted in JPA
	 */
	public Task createTask() {
		EntityManager entityManager = DatabaseTools.getInstance().createEntityManager();
		entityManager.getTransaction().begin();
		Task task = createTask(entityManager);
		entityManager.getTransaction().commit();
		entityManager.close();
		return task;
	}

	/**
	 * Creates a Task entity inside an existing EntityManager/transaction
	 *
	 * @param entityManager the EntityManager where the new Task will be
	 * persisted
	 * @return the new Task entity, persisted in JPA
	 */
	public Task createTask(EntityManager entityManager) {
		Task task = new Task();
		entityManager.persist(task);
		return task;
	}

	/**
	 * Creates a TimeSegment entity
	 *
	 * @param parent the parent Task
	 * @return the new TimeSegment entity, persisted in JPA
	 */
	public TimeSegment createTimeSegment(Task parent) {
		EntityManager entityManager = DatabaseTools.getInstance().createEntityManager();
		entityManager.getTransaction().begin();
		TimeSegment segment = createTimeSegment(entityManager, parent);
		entityManager.getTransaction().commit();
		entityManager.close();
		return segment;
	}

	/**
	 * Creates a TimeSegment entity inside an existing EntityManager/transaction
	 *
	 * @param entityManager the EntityManager where the new TimeSegment will be
	 * persisted
	 * @param parent the parent Task
	 * @return the new TimeSegment entity, persisted in JPA
	 */
	public TimeSegment createTimeSegment(EntityManager entityManager, Task parent) {
		parent = entityManager.find(Task.class, parent.getId());
		TimeSegment segment = new TimeSegment(parent);
		entityManager.persist(segment);
		return segment;
	}

	/**
	 * Creates a CustomField entity
	 *
	 * @return the new CustomField entity, persisted in JPA
	 */
	public CustomField createCustomField() {
		EntityManager entityManager = DatabaseTools.getInstance().createEntityManager();
		entityManager.getTransaction().begin();
		CustomField customField = createCustomField();
		entityManager.getTransaction().commit();
		entityManager.close();
		return customField;
	}

	/**
	 * Creates a CustomField entity inside an existing EntityManager/transaction
	 *
	 * @param entityManager the EntityManager where the new CustomField will be
	 * persisted
	 * @return the new CustomField entity, persisted in JPA
	 */
	public CustomField createCustomField(EntityManager entityManager) {
		CustomField customField = new CustomField();
		entityManager.persist(customField);
		return customField;
	}

	/**
	 * Performs a requested change with a supplied TransactedChange
	 *
	 * @param requestedChange a TransactedChange implementation
	 */
	public void performTransactedChange(TransactedChange requestedChange) {
		EntityManager entityManager = DatabaseTools.getInstance().createEntityManager();
		entityManager.getTransaction().begin();
		requestedChange.performChange(entityManager);
		entityManager.getTransaction().commit();
		entityManager.close();
	}

	/**
	 * Returns all tasks from database
	 *
	 * @param entityManager the EntityManager where the new CustomField will be
	 * persisted
	 * @return all tasks from database
	 */
	public List<Task> getAllTasks() {
		EntityManager entityManager = DatabaseTools.getInstance().createEntityManager();

		List<Task> result = getAllTasks(entityManager);

		entityManager.close();
		return result;
	}

	/**
	 * Returns all tasks from database inside an existing
	 * EntityManager/transaction
	 *
	 * @return all tasks from database
	 */
	public List<Task> getAllTasks(EntityManager entityManager) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Task> tasksCriteriaQuery = criteriaBuilder.createQuery(Task.class);
		Root<Task> taskRoot = tasksCriteriaQuery.from(Task.class);

		List<Task> result = entityManager.createQuery(tasksCriteriaQuery).getResultList();

		//Post-fetch dependencies
		if (!result.isEmpty()) {
			CriteriaQuery<Task> timeSegmentFetchCriteriaQuery = criteriaBuilder.createQuery(Task.class);
			Root<Task> taskFetch = timeSegmentFetchCriteriaQuery.from(Task.class);
			timeSegmentFetchCriteriaQuery.where(taskRoot.in(result));
			taskFetch.fetch(Task_.timeSegments, JoinType.LEFT);
			taskFetch.fetch(Task_.customFields, JoinType.LEFT);
			entityManager.createQuery(timeSegmentFetchCriteriaQuery).getResultList();
		}
		return result;
	}

	/**
	 * Returns all custom fields from database
	 *
	 * @return all custom fields from database
	 */
	public List<CustomField> getCustomFields() {
		EntityManager entityManager = DatabaseTools.getInstance().createEntityManager();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<CustomField> fieldsCriteriaQuery = criteriaBuilder.createQuery(CustomField.class);
		fieldsCriteriaQuery.from(CustomField.class);

		List<CustomField> result = entityManager.createQuery(fieldsCriteriaQuery).getResultList();
		entityManager.close();
		return result;
	}

	/**
	 * Deletes a custom field from the database
	 *
	 * @param customField the custom field to be deleted
	 */
	public void deleteCustomField(CustomField customField) {
		EntityManager entityManager = DatabaseTools.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		customField = entityManager.find(CustomField.class, customField.getId());
		if (customField != null)
			entityManager.remove(customField);
		//TODO: remove field from all tasks

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	/**
	 * Prepares the EntityManager, transaction and calls the Importer's
	 * importData method with the created EntityManager
	 *
	 * @param importer the importer to be used
	 */
	public void importData(Importer importer) {
		EntityManager entityManager = DatabaseTools.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		importer.importData(entityManager);

		entityManager.getTransaction().commit();
		entityManager.close();
	}
}
