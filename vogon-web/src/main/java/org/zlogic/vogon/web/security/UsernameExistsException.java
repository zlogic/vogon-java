/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.security;

import java.util.ResourceBundle;

/**
 * Exception thrown when a username is already in use by another user
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class UsernameExistsException extends RuntimeException {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/web/messages");

	/**
	 * Creates the UsernameExistsException
	 */
	public UsernameExistsException() {
		super(messages.getString("USER_ALREADY_EXISTS"));
	}
}
