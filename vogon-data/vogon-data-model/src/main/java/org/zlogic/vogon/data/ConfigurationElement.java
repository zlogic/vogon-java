/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * Class for storing a configuration fragment
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
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
	 * JPA version
	 */
	@Version
	private long version = 0L;
	/**
	 * The configuration element value
	 */
	protected Serializable configurationValue;

	/**
	 * Creates a currency exchange rate
	 */
	protected ConfigurationElement() {
	}

	/**
	 * Creates a currency exchange rate
	 *
	 * @param name the configuration element name
	 */
	public ConfigurationElement(String name) {
		this.name = name;
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the configuration element value
	 *
	 * @return the configuration element value
	 */
	public Object getValue() {
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

	/**
	 * Returns the version for this class instance
	 *
	 * @return the version for this class instance
	 */
	public long getVersion() {
		return version;
	}

	/**
	 * Sets the version of this class instance
	 *
	 * @param version the version of this class instance
	 */
	protected void setVersion(long version) {
		this.version = version;
	}
}
