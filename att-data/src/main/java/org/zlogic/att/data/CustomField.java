package org.zlogic.att.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Entity class for a custom field.
 * Each task's time is tracked with TimeSegments.
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 31.12.12
 * Time: 1:24
 */
@Entity
public class CustomField implements Serializable {
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
		return new Long(id).hashCode();
	}
}
