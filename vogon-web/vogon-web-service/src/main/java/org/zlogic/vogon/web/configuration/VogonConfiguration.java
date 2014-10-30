/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.configuration;

import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zlogic.vogon.data.ConfigurationElement;
import org.zlogic.vogon.web.data.ConfigurationRepository;

/**
 * Various global configuration options
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Service
public class VogonConfiguration {

	/**
	 * The configuration elements repository
	 */
	@Autowired
	private ConfigurationRepository configurationRepository;

	/**
	 * Finds or creates a ConfigurationElement
	 *
	 * @param name the ConfigurationElement name
	 * @return the found or created ConfigurationElement
	 */
	protected ConfigurationElement findOrCreate(String name) {
		ConfigurationElement element = configurationRepository.findOne(name);
		if (element == null) {
			element = new ConfigurationElement(name);
			configurationRepository.saveAndFlush(element);
		}
		return element;
	}

	/**
	 * Gets the value of a ConfigurationElement or the default value if the
	 * ConfigurationElement does not exist
	 *
	 * @param <T> the ConfigurationElement type
	 * @param name the ConfigurationElement name
	 * @param defaultValue the value to be returned in case the
	 * ConfigurationElement doesn't exist
	 * @return the value of a ConfigurationElement or the default value if the
	 * ConfigurationElement does not exist
	 */
	protected <T extends Serializable> T getValue(String name, T defaultValue) {
		ConfigurationElement element = configurationRepository.findOne(name);
		if (element == null)
			return defaultValue;
		if (element.getValue() == null)
			configurationRepository.delete(element);
		return element.getValue() != null ? ((T) element.getValue()) : defaultValue;
	}

	/**
	 * Sets the value of a ConfigurationElement
	 *
	 * @param <T> the ConfigurationElement type
	 * @param name the ConfigurationElement name
	 * @param value the value to set
	 */
	protected <T extends Serializable> void setValue(String name, T value) {
		ConfigurationElement element = findOrCreate(name);
		element.setValue(value);
		configurationRepository.saveAndFlush(element);
	}

	/**
	 * Returns true if registration is allowed
	 *
	 * @return true if registration is allowed
	 */
	public boolean isAllowRegistration() {
		return getValue(ConfigurationKeys.ALLOW_REGISTRATION, true);
	}

	/**
	 * Sets if if registration is allowed
	 *
	 * @param allowRegistration true if registration is allowed
	 */
	public void setAllowRegistration(boolean allowRegistration) {
		setValue(ConfigurationKeys.ALLOW_REGISTRATION, allowRegistration);
	}
}
