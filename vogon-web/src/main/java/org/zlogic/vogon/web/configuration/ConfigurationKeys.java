/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.zlogic.vogon.data.ConfigurationElement;

/**
 * ConfigurationElement keys
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class ConfigurationKeys {

	/**
	 * AllowRegistration key
	 */
	public static final String ALLOW_REGISTRATION = "AllowRegistration"; //NOI18N
	/**
	 * Default ConfigurationElement values
	 */
	public static final Set<ConfigurationElement> DEFAULT_VALUES = new HashSet<>(Arrays.asList(new ConfigurationElement(ALLOW_REGISTRATION, true)));
}
