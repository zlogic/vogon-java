package org.zlogic.att.data;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Helper class to perform routine entity modifications and create a single point where EntityManager is used.
 * <p/>
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 29.12.12
 * Time: 23:13
 */
public class PersistenceHelper {
	public PersistenceHelper() {
	}

	public Task createTask() {
		EntityManager entityManager = DatabaseTools.getInstance().createEntityManager();
		entityManager.getTransaction().begin();
		Task task = new Task();
		entityManager.persist(task);
		entityManager.getTransaction().commit();
		entityManager.close();
		return task;
	}

	public TimeSegment createTimeSegment(Task parent) {
		EntityManager entityManager = DatabaseTools.getInstance().createEntityManager();
		entityManager.getTransaction().begin();
		TimeSegment segment = new TimeSegment(parent);
		entityManager.persist(segment);
		if (!entityManager.contains(parent))
			entityManager.refresh(parent);
		entityManager.merge(parent);
		entityManager.getTransaction().commit();
		entityManager.close();
		return segment;
	}

	public void performTransactedChange(TransactedChange requestedChange) {
		EntityManager entityManager = DatabaseTools.getInstance().createEntityManager();
		entityManager.getTransaction().begin();
		requestedChange.performChange(entityManager);
		entityManager.getTransaction().commit();
		entityManager.close();
	}

	public List<Task> getAllTasks() {
		EntityManager entityManager = DatabaseTools.getInstance().createEntityManager();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Task> accountsCriteriaQuery = criteriaBuilder.createQuery(Task.class);
		accountsCriteriaQuery.from(Task.class);

		List<Task> result = entityManager.createQuery(accountsCriteriaQuery).getResultList();
		entityManager.close();
		return result;
	}
}
