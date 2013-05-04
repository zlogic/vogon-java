/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Abstract entity class for a tasks filter.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
@Entity
public abstract class Filter implements Serializable {

	/**
	 * JPA ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	/**
	 * Returns the JPA ID
	 *
	 * @return the JPA ID
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Returns the predicate which can be used for applying this filter to a
	 * task query
	 *
	 * @param criteriaBuilder the query CriteriaBuilder
	 * @param taskRoot the task root (CriteriaQuery<Task>.from())
	 * @return the predicate
	 */
	public abstract Predicate getFilterPredicate(CriteriaBuilder criteriaBuilder, Root<Task> taskRoot);
}
