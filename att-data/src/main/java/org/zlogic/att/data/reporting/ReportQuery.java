/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data.reporting;

import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.zlogic.att.data.PersistenceHelper;
import org.zlogic.att.data.Task;
import org.zlogic.att.data.TimeSegment;
import org.zlogic.att.data.TimeSegment_;
import org.zlogic.att.data.TransactedChange;

/**
 * Class for retrieving data for reporting purposes.
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class ReportQuery {

	/**
	 * The PersistenceHelper instance
	 */
	private PersistenceHelper persistenceHelper;
	/**
	 * The report start date
	 */
	private Date startDate;
	/**
	 * The report end date
	 */
	private Date endDate;

	/**
	 * Default constructor
	 *
	 * @param persistenceHelper the PersistenceHelper instance
	 */
	public ReportQuery(PersistenceHelper persistenceHelper) {
		this.persistenceHelper = persistenceHelper;
	}

	/**
	 * Returns the report starting date
	 *
	 * @return the report starting date
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * Sets the report starting date. Only the date will be used, time is
	 * ignored.
	 *
	 * @param startDate the report starting date
	 */
	public void setStartDate(LocalDate startDate) {
		this.startDate = DateTools.getInstance().convertDateToStartOfDay(startDate);
	}

	/**
	 * Returns the report ending date.
	 *
	 * @return the report ending date
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * Sets the report ending date. Only the date will be used, time is ignored.
	 *
	 * @param endDate the report ending date
	 */
	public void setEndDate(LocalDate endDate) {
		this.endDate = DateTools.getInstance().convertDateToEndOfDay(endDate);
	}

	/**
	 * Returns the list of all tasks matching the criteria
	 *
	 * @return the list of all tasks matching the criteria
	 */
	public List<Task> queryTasks() {
		List<TimeSegment> timeSegments = queryTimeSegments();
		List<Task> tasks = new LinkedList<>();
		for (TimeSegment timeSegment : timeSegments)
			if (!tasks.contains(timeSegment.getOwner()))
				tasks.add(timeSegment.getOwner());
		return tasks;
	}

	/**
	 * Returns the list of all time segments matching the criteria
	 *
	 * @return the list of all time segments matching the criteria
	 */
	public List<TimeSegment> queryTimeSegments() {
		List<TimeSegment> timeSegments = new LinkedList<>();
		//Perform query
		TransactedChange retreiveTimeSegments = new TransactedChange() {
			private List<TimeSegment> targetTimeSegments;

			public TransactedChange setTargetTimeSegments(List<TimeSegment> targetTimeSegments) {
				this.targetTimeSegments = targetTimeSegments;
				return this;
			}

			@Override
			public void performChange(EntityManager entityManager) {
				CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
				CriteriaQuery<TimeSegment> timeSegmentsCriteriaQuery = criteriaBuilder.createQuery(TimeSegment.class);
				Root<TimeSegment> timeSegmentRoot = timeSegmentsCriteriaQuery.from(TimeSegment.class);
				Predicate datePredicate = criteriaBuilder.conjunction();
				Expression<Date> startTime = timeSegmentRoot.get(TimeSegment_.startTime);
				Expression<Date> endTime = timeSegmentRoot.get(TimeSegment_.endTime);
				if (getStartDate() != null)
					datePredicate
							= criteriaBuilder.and(
									criteriaBuilder.or(
											(getEndDate() != null) ? criteriaBuilder.between(startTime, getStartDate(), getEndDate()) : criteriaBuilder.greaterThanOrEqualTo(startTime, getStartDate()),
											criteriaBuilder.between(criteriaBuilder.literal(getStartDate()), startTime, endTime)),
									datePredicate);

				if (getEndDate() != null)
					datePredicate
							= criteriaBuilder.and(
									criteriaBuilder.or(
											(getEndDate() != null) ? criteriaBuilder.between(endTime, getStartDate(), getEndDate()) : criteriaBuilder.lessThanOrEqualTo(endTime, getEndDate()),
											criteriaBuilder.between(criteriaBuilder.literal(getEndDate()), startTime, endTime)),
									datePredicate);
				timeSegmentsCriteriaQuery.distinct(true);
				timeSegmentsCriteriaQuery.where(datePredicate);

				targetTimeSegments.addAll(entityManager.createQuery(timeSegmentsCriteriaQuery).getResultList());
			}
		}.setTargetTimeSegments(timeSegments);
		persistenceHelper.performTransactedChange(retreiveTimeSegments);
		return timeSegments;
	}
}
