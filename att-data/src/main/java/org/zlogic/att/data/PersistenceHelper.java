/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Predicate;
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
	 * Entity manager factory
	 */
	EntityManagerFactory entityManagerFactory = javax.persistence.Persistence.createEntityManagerFactory("AwesomeTimeTrackerPersistenceUnit"); //NOI18N

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
		EntityManager entityManager = entityManagerFactory.createEntityManager();
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
		EntityManager entityManager = entityManagerFactory.createEntityManager();
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
		TimeSegment segment = parent.createSegment();
		entityManager.persist(segment);
		entityManager.merge(parent);
		return segment;
	}

	/**
	 * Creates a CustomField entity
	 *
	 * @return the new CustomField entity, persisted in JPA
	 */
	public CustomField createCustomField() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		CustomField customField = createCustomField(entityManager);
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
	 * Creates a FilterDate entity
	 *
	 * @param type the FilterDate type
	 * @return the new FilterDate entity, persisted in JPA
	 */
	public FilterDate createFilterDate(FilterDate.DateType type) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		FilterDate filter = createFilterDate(entityManager, type);
		entityManager.getTransaction().commit();
		entityManager.close();
		return filter;
	}

	/**
	 * Creates a FilterDate entity inside an existing EntityManager/transaction
	 *
	 * @param entityManager the EntityManager where the new CustomField will be
	 * persisted
	 * @param type the FilterDate type
	 * @return the new FilterDate entity, persisted in JPA
	 */
	public FilterDate createFilterDate(EntityManager entityManager, FilterDate.DateType type) {
		FilterDate filter = new FilterDate(type);
		entityManager.persist(filter);
		return filter;
	}

	/**
	 * Creates a FilterCustomField entity
	 *
	 * @param customField the associated CustomField
	 * @return the new FilterCustomField entity, persisted in JPA
	 */
	public FilterCustomField createFilterCustomField(CustomField customField) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		FilterCustomField filter = createFilterCustomField(entityManager, customField);
		entityManager.getTransaction().commit();
		entityManager.close();
		return filter;
	}

	/**
	 * Creates a FilterCustomField entity inside an existing
	 * EntityManager/transaction
	 *
	 * @param entityManager the EntityManager where the new CustomField will be
	 * persisted
	 * @param customField the associated CustomField
	 * @return the new FilterCustomField entity, persisted in JPA
	 */
	public FilterCustomField createFilterCustomField(EntityManager entityManager, CustomField customField) {
		FilterCustomField filter = new FilterCustomField(customField);
		entityManager.persist(filter);
		return filter;
	}

	/**
	 * Creates a createFilterTaskCompleted entity
	 *
	 * @return the new createFilterTaskCompleted entity, persisted in JPA
	 */
	public FilterTaskCompleted createFilterTaskCompleted() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		FilterTaskCompleted filter = createFilterTaskCompleted(entityManager);
		entityManager.getTransaction().commit();
		entityManager.close();
		return filter;
	}

	/**
	 * Creates a createFilterTaskCompleted entity inside an existing
	 * EntityManager/transaction
	 *
	 * @param entityManager the EntityManager where the new CustomField will be
	 * persisted
	 * @return the new createFilterTaskCompleted entity, persisted in JPA
	 */
	public FilterTaskCompleted createFilterTaskCompleted(EntityManager entityManager) {
		FilterTaskCompleted filter = new FilterTaskCompleted();
		entityManager.persist(filter);
		return filter;
	}

	/**
	 * Performs a requested change with a supplied TransactedChange
	 *
	 * @param requestedChange a TransactedChange implementation
	 */
	public void performTransactedChange(TransactedChange requestedChange) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		requestedChange.performChange(entityManager);
		entityManager.getTransaction().commit();
		entityManager.close();
	}

	/**
	 * Performs an EntityManager.merge operation in a new EntityManager
	 * instance/transaction
	 *
	 * @param entity the entity to be merged
	 */
	public void mergeEntity(Object entity) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.merge(entity);
		entityManager.getTransaction().commit();
		entityManager.close();
	}

	/**
	 * Returns all tasks from database
	 *
	 * @param applyFilters apply filters currently in the database to the
	 * resulting list
	 * @return all tasks from database
	 */
	public List<Task> getAllTasks(boolean applyFilters) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		List<Task> result = getAllTasks(entityManager, applyFilters);

		entityManager.close();
		return result;
	}

	/**
	 * Returns all filters from database
	 *
	 * @return all filters from database
	 */
	public List<Filter> getAllFilters() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		List<Filter> result = getAllFilters(entityManager);

		entityManager.close();
		return result;
	}

	/**
	 * Returns all filters from database inside an existing
	 * EntityManager/transaction
	 *
	 * @param entityManager the EntityManager which will be used for lookup
	 * @return all filters from database
	 */
	public List<Filter> getAllFilters(EntityManager entityManager) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Filter> fitlersCriteriaQuery = criteriaBuilder.createQuery(Filter.class);
		fitlersCriteriaQuery.from(Filter.class);

		List<Filter> result = entityManager.createQuery(fitlersCriteriaQuery).getResultList();

		return result;
	}

	/**
	 * Fetches the latest version of the task in the database, along with
	 * associated objects
	 *
	 * @param id the task's ID
	 * @return the task or null if it's not found
	 */
	public Task getTaskFromDatabase(long id) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		Task task = getTaskFromDatabase(id, entityManager);

		entityManager.close();
		return task;
	}

	/**
	 * Fetches the latest version of the task in the database, along with
	 * associated objects inside an existing EntityManager/transaction
	 *
	 * @param id the task's ID
	 * @param entityManager the EntityManager which will be used for lookup
	 * @return the task or null if it's not found
	 */
	public Task getTaskFromDatabase(long id, EntityManager entityManager) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Task> tasksCriteriaQuery = criteriaBuilder.createQuery(Task.class);
		Root<Task> taskRoot = tasksCriteriaQuery.from(Task.class);
		tasksCriteriaQuery.where(criteriaBuilder.equal(taskRoot.get(Task_.id), id));

		List<Task> result = entityManager.createQuery(tasksCriteriaQuery).getResultList();

		if (result.size() == 1)
			return result.get(0);
		else
			return null;
	}

	/**
	 * Returns all tasks from database inside an existing
	 * EntityManager/transaction
	 *
	 * @param entityManager the EntityManager which will be used for lookup
	 * @param applyFilters apply filters currently in the database to the
	 * resulting list
	 * @return all tasks from database
	 */
	public List<Task> getAllTasks(EntityManager entityManager, boolean applyFilters) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Task> tasksCriteriaQuery = criteriaBuilder.createQuery(Task.class);
		Root<Task> taskRoot = tasksCriteriaQuery.from(Task.class);

		if (applyFilters) {
			Predicate filtersPredicate = criteriaBuilder.conjunction();
			for (Filter filter : getAllFilters())
				filtersPredicate = criteriaBuilder.and(filtersPredicate, filter.getFilterPredicate(criteriaBuilder, taskRoot));
			tasksCriteriaQuery.where(filtersPredicate);
			tasksCriteriaQuery.distinct(true);
		}

		List<Task> result = entityManager.createQuery(tasksCriteriaQuery).getResultList();

		return result;
	}

	/**
	 * Returns all custom fields from database
	 *
	 * @return all custom fields from database
	 */
	public List<CustomField> getCustomFields() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<CustomField> fieldsCriteriaQuery = criteriaBuilder.createQuery(CustomField.class);
		fieldsCriteriaQuery.from(CustomField.class);

		List<CustomField> result = entityManager.createQuery(fieldsCriteriaQuery).getResultList();
		entityManager.close();
		return result;
	}

	/**
	 * Returns all custom field values from database
	 *
	 * @return all custom field values from database
	 */
	public Map<CustomField, Set<String>> getAllCustomFieldValues() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> fieldsCriteriaQuery = criteriaBuilder.createTupleQuery();
		Root<Task> taskRoot = fieldsCriteriaQuery.from(Task.class);
		Root<CustomField> customFieldRoot = fieldsCriteriaQuery.from(CustomField.class);
		MapJoin<Task, CustomField, String> customFieldJoin = taskRoot.join(Task_.customFields);
		fieldsCriteriaQuery.where(criteriaBuilder.equal(customFieldJoin.key(), customFieldRoot));
		fieldsCriteriaQuery.multiselect(customFieldRoot, customFieldJoin.value()).distinct(true);

		List<Tuple> resultList = entityManager.createQuery(fieldsCriteriaQuery).getResultList();
		entityManager.close();

		TreeMap<CustomField, Set<String>> result = new TreeMap<>();
		for (Tuple entry : resultList) {
			CustomField customField = entry.get(0, CustomField.class);
			String customFieldValue = entry.get(1, String.class);
			if (!result.containsKey(customField))
				result.put(customField, new TreeSet<String>());
			result.get(customField).add(customFieldValue);
		}
		return result;
	}

	/**
	 * Prepares the EntityManager, transaction and calls the Importer's
	 * importData method with the created EntityManager
	 *
	 * @param importer the importer to be used
	 */
	public void importData(Importer importer) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		importer.importData(this, entityManager);

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	/**
	 * Removes any orphaned entities
	 */
	public void cleanupDB() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		List<Task> tasks = getAllTasks(entityManager, false);

		//Cleanup time segments
		Set<TimeSegment> ownedTimeSegments = new TreeSet<>();
		Set<TimeSegment> allTimeSegments = new TreeSet<>();
		for (Task task : tasks)
			ownedTimeSegments.addAll(task.getTimeSegments());

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<TimeSegment> timeSegmentsCriteriaQuery = criteriaBuilder.createQuery(TimeSegment.class);
		timeSegmentsCriteriaQuery.from(TimeSegment.class);

		allTimeSegments.addAll(entityManager.createQuery(timeSegmentsCriteriaQuery).getResultList());
		allTimeSegments.removeAll(ownedTimeSegments);

		for (TimeSegment timeSegment : allTimeSegments)
			entityManager.remove(timeSegment);

		entityManager.getTransaction().commit();
		entityManager.close();
	}
}
