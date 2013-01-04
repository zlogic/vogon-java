package org.zlogic.att.data;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Helper class to perform routine entity modifications and create a single point where EntityManager is used.
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 29.12.12
 * Time: 23:13
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
		Task task = new Task();
		entityManager.persist(task);
		entityManager.getTransaction().commit();
		entityManager.close();
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
		parent = entityManager.find(Task.class, parent.getId());
		TimeSegment segment = new TimeSegment(parent);
		entityManager.persist(segment);
		entityManager.merge(parent);
		entityManager.getTransaction().commit();
		entityManager.close();
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
		CustomField customField = new CustomField();
		entityManager.persist(customField);
		entityManager.getTransaction().commit();
		entityManager.close();
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
	 * @return all tasks from database
	 */
	public List<Task> getAllTasks() {
		EntityManager entityManager = DatabaseTools.getInstance().createEntityManager();

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

		entityManager.close();
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
}
