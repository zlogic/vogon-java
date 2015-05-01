/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller;

import java.util.ResourceBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.web.configuration.VogonConfiguration;
import org.zlogic.vogon.web.security.UserService;
import org.zlogic.vogon.web.security.UsernameExistsException;

/**
 * Spring MVC controller for registration
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Controller
@RequestMapping(value = "/register")
@Transactional
public class RegistrationController {

	/**
	 * The users service
	 */
	@Autowired
	private UserService userService;
	/**
	 * The configuration service
	 */
	@Autowired
	private VogonConfiguration configuration;
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/web/messages");

	/**
	 * Registers a new user
	 *
	 * @param registerUser the user to register
	 * @return the registered user
	 * @throws org.zlogic.vogon.web.security.UsernameExistsException in case the
	 * new username is already in use
	 */
	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	VogonUser register(@RequestBody VogonUser registerUser) throws UsernameExistsException {
		if (!configuration.isAllowRegistration())
			throw new SecurityException(messages.getString("REGISTRATION_IS_NOT_ALLOWED"));
		return userService.createUser(registerUser);
	}
}
