/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import java.util.Objects;
import java.util.ResourceBundle;
import javafx.util.StringConverter;

/**
 * Helper class for storing an object and a status (e.g. for validation).
 *
 * @author Dmitry Zolotukhin
 */
public class ObjectWithStatus<ObjectType, StatusType> {

	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	protected final ObjectType value;
	protected final StatusType status;

	public ObjectWithStatus(ObjectType value, StatusType status) {
		this.value = value;
		this.status = status;
	}

	public ObjectType getValue() {
		return value;
	}

	public StatusType getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return value.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ObjectWithStatus) {
			ObjectWithStatus other = (ObjectWithStatus) obj;
			return this.value != null ? this.value.equals(other.value) : false && this.status != null ? this.status.equals(other.status) : false;
		} else if (obj != null && obj.getClass().equals(value.getClass()))
			return obj.equals(value);
		else
			return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 83 * hash + Objects.hashCode(this.value);
		hash = 83 * hash + Objects.hashCode(this.status);
		return hash;
	}

	public static StringConverter getConverter() {
		return new StringConverter<ObjectWithStatus>() {
			@Override
			public String toString(ObjectWithStatus t) {
				return t.getValue().toString();
			}

			@Override
			public ObjectWithStatus fromString(String string) {
				return new ObjectWithStatus<>(string, null);
			}
		};
	}
}
