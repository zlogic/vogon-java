package org.zlogic.att.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Entity class for a custom field.
 * Each task's time is tracked with TimeSegments.
 * <p/>
 * Date: 31.12.12
 * Time: 1:24
 */
@Entity
public class CustomField implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long id;
	private String name;

	protected CustomField() {
		setName("");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof CustomField && ((CustomField) obj).getId() == getId();
	}

}
