/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.security;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.zlogic.vogon.data.Constants;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.web.data.UserRepository;

/**
 * The user details service for Spring Security
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Service
public class UserService implements UserDetailsService, InitializingBean {

	/**
	 * The users repository
	 */
	@Autowired
	private UserRepository userRepository;
	/**
	 * The default user username
	 */
	private String defaultUserUsername = Constants.defaultUserUsername;//TODO: load this from config or environment instead
	/**
	 * The default user password
	 */
	private String defaultUserPassword = Constants.defaultUserPassword;//TODO: load this from config or environment instead

	/**
	 * Loads a user by username
	 *
	 * @param username the username
	 * @return the loaded user
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		VogonUser user = userRepository.findByUsername(username);
		if (user == null)
			throw new UsernameNotFoundException("User cannot be found");
		else
			return new VogonSecurityUser(user);
	}

	/**
	 * Updates user from database (e.g. to reflect
	 *
	 * @param securityUser user to update
	 */
	public void refreshUser(VogonSecurityUser securityUser) {
		if (securityUser.getUser() != null) {
			VogonUser user = userRepository.findOne(securityUser.getUser().getId());
			securityUser.setUser(user);
		}
	}

	/**
	 * Applies default properties and creates default user if needed
	 *
	 * @throws Exception in case of errors
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (defaultUserUsername != null && defaultUserPassword != null) {
			if (userRepository.count() == 0) {
				VogonUser defaultUser = new VogonUser(defaultUserUsername, defaultUserPassword);
				userRepository.saveAndFlush(defaultUser);
			}
		}
	}
	/*
	 * Getters/setters
	 */

	/**
	 * Returns the default username
	 *
	 * @return the default username
	 */
	public String getDefaultUserUsername() {
		return defaultUserUsername;
	}

	/**
	 * Sets the default username
	 *
	 * @param defaultUserUsername the default username
	 */
	public void setDefaultUserUsername(String defaultUserUsername) {
		this.defaultUserUsername = defaultUserUsername;
	}

	/**
	 * Returns the default password
	 *
	 * @return the default password
	 */
	public String getDefaultUserPassword() {
		return defaultUserPassword;
	}

	/**
	 * Sets the default password
	 *
	 * @param defaultUserPassword the default password
	 */
	public void setDefaultUserPassword(String defaultUserPassword) {
		this.defaultUserPassword = defaultUserPassword;
	}
}
