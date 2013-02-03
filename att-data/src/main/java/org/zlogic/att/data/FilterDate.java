/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;

/**
 * Tasks filter which filters the task date.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
@Entity
public class FilterDate extends Filter {

	/**
	 * The date filter type
	 */
	public enum DateType {

		/**
		 * This is an unknown date filter
		 */
		DATE_UNKNOWN,
		/**
		 * This is a start date filter
		 */
		DATE_START,
		/**
		 * This is an end date filter
		 */
		DATE_END
	};
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
}
