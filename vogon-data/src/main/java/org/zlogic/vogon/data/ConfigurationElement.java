/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Class for storing a configuration fragment
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
//TODO: consider removing this
@Entity
public class ConfigurationElement implements Serializable {

	/**
	 * Version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The currency rate ID (only for persistence)
	 */
	@Id
	protected String name;
	/**
	 * The configuration element value
	 */
	@Basic
	protected Serializable configurationValue;

	/**
	 * Creates a currency exchange rate
	 */
	protected ConfigurationElement() {
	}

	/**
	 * Creates a configuration element
	 *
	 * @param name the configuration element name
	 */
	public ConfigurationElement(String name) {
		this.name = name;
	}

	/**
	 * Creates a configuration element
	 *
	 * @param name the configuration element name
	 * @param value the value to set
	 */
	public ConfigurationElement(String name, Serializable value) {
		this.name = name;
		this.configurationValue = value;
	}
	/*
	 * Getters/setters
	 */

	/**
	 * Returns the configuration element value
	 *
	 * @return the configuration element value
	 */
	public Serializable getValue() {
		return configurationValue;
	}

	/**
	 * Sets the configuration element value
	 *
	 * @param configurationValue the configuration element value
	 */
	public void setValue(Serializable configurationValue) {
		this.configurationValue = configurationValue;
	}

	/**
	 * Returns the ID for this configuration fragment
	 *
	 * @return the ID for this configuration fragment
	 */
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 61 * hash + Objects.hashCode(this.name);
		return hash;
	}

	@Override
	public boolean equals(Object el) {
		if (el instanceof ConfigurationElement)
			return (((ConfigurationElement) el).name == null ? name == null : ((ConfigurationElement) el).name.equals(name));
		return false;
	}
}
