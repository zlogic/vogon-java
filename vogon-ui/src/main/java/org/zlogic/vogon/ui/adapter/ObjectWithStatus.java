/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.util.StringConverter;

/**
 * Helper class for storing an object and a status (e.g. for validation).
 *
 * @param <TypeObject> the object's type
 * @param <TypeStatus> the object's status type (e.g. Boolean or enum)
 * @author Dmitry Zolotukhin
 */
public class ObjectWithStatus<TypeObject, TypeStatus> {

	/**
	 * The object
	 */
	protected final TypeObject value;
	/**
	 * The object's status
	 */
	protected final TypeStatus status;

	public ObjectWithStatus() {
		this(null,null);
	}
	/**
	 * Constructor for ObjectWithStatus
	 *
	 * @param value the object's value
	 * @param status the object's status
	 */
	public ObjectWithStatus(TypeObject value, TypeStatus status) {
		this.value = value;
		this.status = status;
	}

	/**
	 * Returns the object's value
	 *
	 * @return the object's value
	 */
	public TypeObject getValue() {
		return value;
	}

	/**
	 * Returns the object's status
	 *
	 * @return the object's status
	 */
	public TypeStatus getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return value.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ObjectWithStatus) {
			ObjectWithStatus<?, ?> other = (ObjectWithStatus<?, ?>) obj;
			return (this.value != null ? this.value.equals(other.value) : false) && (this.status != null ? this.status.equals(other.status) : false);
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

	/**
	 * Returns the StringConverter for this class (uses only the object's value)
	 *
	 * @return the StringConverter for this class
	 */
	public static <T,S> StringConverter<ObjectWithStatus<T, S>> getConverter(Class<T> objectClass,Class<S> statusClass) {
		return new StringConverter<ObjectWithStatus<T, S>>() {
			private Class<T> objectClass;
			public StringConverter<ObjectWithStatus<T, S>> setObjectClass(Class<T> objectClass){
				this.objectClass = objectClass;
				return this;
			}
			@Override
			public String toString(ObjectWithStatus<T, S> t) {
				return t.getValue().toString();
			}

			@Override
			public ObjectWithStatus<T,S> fromString(String string) {
				if(objectClass.isAssignableFrom(String.class))
					try {
						return new ObjectWithStatus<T,S>(objectClass.getConstructor(String.class).newInstance(string), null);
					} catch (InstantiationException | IllegalAccessException
							| IllegalArgumentException
							| InvocationTargetException | NoSuchMethodException
							| SecurityException ex) {
						Logger.getLogger(ObjectWithStatus.class.getName()).log(Level.SEVERE, null, ex);
					}
				return new ObjectWithStatus<T,S>(null, null);
			}
		}.setObjectClass(objectClass);
	}
}
