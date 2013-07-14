/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
 */
package org.zlogic.att.data;

import javax.persistence.Entity;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Tasks filter which filters the task completed state.
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
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

	@Override
	public Predicate getFilterPredicate(CriteriaBuilder criteriaBuilder, Root<Task> taskRoot) {
		return criteriaBuilder.equal(taskRoot.get(Task_.completed), taskCompleted);
	}
}
