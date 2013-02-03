/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
}
