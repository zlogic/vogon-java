/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zlogic.vogon.data.ConfigurationElement;
import org.zlogic.vogon.web.configuration.ConfigurationKeys;
import org.zlogic.vogon.web.data.ConfigurationRepository;
import org.zlogic.vogon.web.security.VogonSecurityUser;

/**
 * Spring MVC controller for configuration management
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Controller
@RequestMapping(value = "/service/configuration")
@Transactional
public class ConfigurationController {

	/**
	 * The configuration repository
	 */
	@Autowired
	private ConfigurationRepository configurationRepository;

	/**
	 * Sets configuration properties
	 *
	 * @param values the values to set
	 */
	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	@Secured({VogonSecurityUser.AUTHORITY_ADMIN})
	public @ResponseBody
	void setProperties(@RequestBody Collection<ConfigurationElement> values) {
		for (ConfigurationElement element : values)
			if (element.getName().equals(ConfigurationKeys.ALLOW_REGISTRATION))
				element.setValue(Boolean.parseBoolean((String) element.getValue()));
		configurationRepository.save(values);
		configurationRepository.flush();
	}

	/**
	 * Returns configuration properties
	 *
	 * @return the configuration properties
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	//@PreAuthorize("hasAuthority('" + VogonSecurityUser.AUTHORITY_ADMIN + "')") //NOI18N
	@Secured({VogonSecurityUser.AUTHORITY_ADMIN})
	public @ResponseBody
	Collection<ConfigurationElement> getProperties() {
		Set<ConfigurationElement> configuration = new HashSet<>(configurationRepository.findAll());
		configuration.addAll(ConfigurationKeys.DEFAULT_VALUES);//Will only add missing values
		return configuration;
	}
}
