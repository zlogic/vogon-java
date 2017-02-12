/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.web.data.UserRepository;
import org.zlogic.vogon.web.security.UserService;
import org.zlogic.vogon.web.security.UsernameExistsException;
import org.zlogic.vogon.web.security.VogonSecurityUser;

/**
 * Spring MVC controller for users
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Controller
@RequestMapping(value = "/service/user")
@Transactional
public class UsersController {

	/**
	 * The users repository
	 */
	@Autowired
	private UserRepository userRepository;
	/**
	 * The Spring user service
	 */
	@Autowired
	private UserService userService;

	/**
	 * Returns user details for the authenticated user
	 *
	 * @param userPrincipal the authenticated user
	 * @return the user details
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	VogonUser getUserData(@AuthenticationPrincipal VogonSecurityUser userPrincipal) {
		VogonUser user = userRepository.findByUsernameIgnoreCase(userPrincipal.getUsername());
		return user;
	}

	/**
	 * Updates or the current user details
	 *
	 * @param updatedUser the updated user
	 * @param userPrincipal the authenticated user
	 * @return the user details
	 * @throws org.zlogic.vogon.web.security.UsernameExistsException in case the
	 * new username is already in use
	 */
	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	VogonUser submitUser(@RequestBody VogonUser updatedUser, @AuthenticationPrincipal VogonSecurityUser userPrincipal) throws UsernameExistsException {
		return userService.updateUser(userPrincipal, updatedUser).getUser();
	}
}
