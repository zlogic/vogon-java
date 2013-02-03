/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data;

import javax.persistence.Entity;

/**
 * Tasks filter which filters the task completed state.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
@Entity
public class FilterTaskCompleted extends Filter {

	/**
	 * The task completed state for this filter
	 */
	private Boolean taskCompleted;

	/**
	 * Default constructor
	 */
	protected FilterTaskCompleted() {
		taskCompleted = true;
	}

	/**
	 * Returns the task completed state for this filter
	 *
	 * @return the task completed state for this filter
	 */
	public Boolean getTaskCompleted() {
		return taskCompleted;
	}

	/**
	 * Sets the task completed state for this filter
	 *
	 * @param taskCompleted the new task completed state for this filter
	 */
	public void setTaskCompleted(Boolean taskCompleted) {
		this.taskCompleted = taskCompleted;
	}
}
