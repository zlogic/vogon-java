/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.security;

import java.util.ResourceBundle;
import javax.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
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
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/web/messages");
	/**
	 * The users repository
	 */
	@Autowired
	private UserRepository userRepository;
	/**
	 * The Spring PlatformTransactionManager instance
	 */
	@Resource
	private PlatformTransactionManager transactionManager;
	/**
	 * The PasswordEncoder instance
	 */
	@Autowired
	private PasswordEncoder passwordEncoder;

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
			throw new UsernameNotFoundException(messages.getString("USER_CANNOT_BE_FOUND"));
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
	 * Returns true if the username is already in use
	 *
	 * @param username the username to check
	 * @return true if the username is already in use
	 */
	private boolean isUsernameExists(final String username) {
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setReadOnly(true);
		transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		return transactionTemplate.execute(new TransactionCallback<Boolean>() {

			@Override
			public Boolean doInTransaction(TransactionStatus ts) {
				return userRepository.findByUsername(username) != null;
			}
		});
	}

	/**
	 * Saves a user
	 *
	 * @param user the VogonUser to save
	 * @return the saved user
	 * @throws UsernameExistsException if username is already in use
	 */
	private VogonUser saveUser(VogonUser user) throws UsernameExistsException {
		try {
			return userRepository.saveAndFlush(user);
		} catch (JpaSystemException ex) {
			if (isUsernameExists(user.getUsername()))
				throw new UsernameExistsException();
			else
				throw ex;
		}
	}

	/**
	 * Creates a new user
	 *
	 * @param createUser the user parameters to use
	 * @return the persisted user
	 * @throws org.zlogic.vogon.web.security.UsernameExistsException in case the
	 * username is already in use
	 */
	public VogonUser createUser(VogonUser createUser) throws UsernameExistsException {
		VogonUser user = new VogonUser(createUser.getUsername(), passwordEncoder.encode(createUser.getPassword()));
		user.setAuthorities(VogonSecurityUser.AUTHORITY_USER);
		saveUser(user);
		return user;
	}

	/**
	 * Updates a user
	 *
	 * @param userPrincipal the user principal to update
	 * @param updatedUser the user parameters to use
	 * @return the persisted user
	 * @throws org.zlogic.vogon.web.security.UsernameExistsException in case the
	 * new username is already in use
	 */
	public VogonSecurityUser updateUser(VogonSecurityUser userPrincipal, VogonUser updatedUser) throws UsernameExistsException {
		VogonUser user = userRepository.findByUsername(userPrincipal.getUsername());
		user.setDefaultCurrency(updatedUser.getDefaultCurrency());
		if (updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty() && !updatedUser.getUsername().equals(user.getUsername()))
			user.setUsername(updatedUser.getUsername());
		if (updatedUser.getPassword() != null)
			user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
		saveUser(user);
		refreshUser(userPrincipal);
		return userPrincipal;
	}

	/**
	 * Applies default properties and creates default user if needed
	 *
	 * @throws Exception in case of errors
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (userRepository.count() == 0) {
			VogonUser defaultUser = new VogonUser(Constants.DEFAULT_USERNAME, passwordEncoder.encode(Constants.DEFAULT_PASSWORD));
			defaultUser.setAuthorities(VogonSecurityUser.AUTHORITY_ADMIN, VogonSecurityUser.AUTHORITY_USER);
			userRepository.saveAndFlush(defaultUser);
		}
	}
}
