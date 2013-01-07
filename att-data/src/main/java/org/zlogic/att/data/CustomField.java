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
 * Entity class for a custom field. Each task's time is tracked with
 * TimeSegments.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
@Entity
public class CustomField implements Serializable, Comparable<CustomField> {

	/**
	 * JPA ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long id;
	/**
	 * Custom field name
	 */
	private String name;

	/**
	 * Default constructor
	 */
	protected CustomField() {
		setName("");
	}

	/**
	 * Returns the field name
	 *
	 * @return the field name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the field name
	 *
	 * @param name the field name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the JPA ID
	 *
	 * @return the JPA ID
	 */
	public long getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof CustomField && ((CustomField) obj).id == id;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 17 * hash + (int) (this.id ^ (this.id >>> 32));
		return hash;
	}

	@Override
	public int compareTo(CustomField customField) {
		return Long.compare(id, customField.id);
	}
}
