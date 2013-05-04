/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data;

import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

/**
 * Tasks filter which filters the task date.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
@Entity
public class FilterDate extends Filter {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(FilterDate.class.getName());

	/**
	 * The date filter type
	 */
	public enum DateType {

		/**
		 * This is an unknown date filter
		 */
		DATE_UNKNOWN,
		/**
		 * This is a after date filter
		 */
		DATE_AFTER,
		/**
		 * This is an before date filter
		 */
		DATE_BEFORE
	};
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/data/messages");
	/**
	 * The date type
	 */
	@Enumerated(EnumType.STRING)
	private DateType type = DateType.DATE_UNKNOWN;
	/**
	 * The boundary date for this filter
	 */
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	private Date appliedDate;

	/**
	 * Default constructor
	 */
	protected FilterDate() {
	}

	/**
	 * Constructs a date filter with a specific type
	 *
	 * @param type the date filter type
	 */
	protected FilterDate(DateType type) {
		this.type = type;
	}

	/**
	 * Returns the date filter type
	 *
	 * @return the date filter type
	 */
	public DateType getType() {
		return type;
	}

	/**
	 * Returns the boundary date for this filter
	 *
	 * @return the boundary date for this filter
	 */
	public Date getAppliedDate() {
		return appliedDate;
	}

	/**
	 * Sets the boundary date for this filter
	 *
	 * @param appliedDate the new boundary date for this filter
	 */
	public void setAppliedDate(Date appliedDate) {
		this.appliedDate = appliedDate;
	}

	@Override
	public Predicate getFilterPredicate(CriteriaBuilder criteriaBuilder, Root<Task> taskRoot) {
		if (type == DateType.DATE_AFTER) {
			SetJoin<Task, TimeSegment> timeSegmentsJoin = taskRoot.join(Task_.timeSegments);

			return criteriaBuilder.or(
					criteriaBuilder.greaterThanOrEqualTo(timeSegmentsJoin.get(TimeSegment_.endTime), appliedDate),
					criteriaBuilder.greaterThanOrEqualTo(timeSegmentsJoin.get(TimeSegment_.startTime), appliedDate));
		} else if (type == DateType.DATE_BEFORE) {
			SetJoin<Task, TimeSegment> timeSegmentsJoin = taskRoot.join(Task_.timeSegments);

			return criteriaBuilder.or(
					criteriaBuilder.lessThanOrEqualTo(timeSegmentsJoin.get(TimeSegment_.endTime), appliedDate),
					criteriaBuilder.lessThanOrEqualTo(timeSegmentsJoin.get(TimeSegment_.startTime), appliedDate));
		}
		log.log(Level.SEVERE, messages.getString("UNKNOWN_DATE_TYPE"), type.toString());
		return criteriaBuilder.conjunction();
	}
}
