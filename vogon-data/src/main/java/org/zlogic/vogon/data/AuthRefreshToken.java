/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 * Class for storing an authentication refresh token
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Entity
public class AuthRefreshToken implements Serializable {

	/**
	 * The token ID
	 */
	@Id
	private String id;

	/**
	 * Token object
	 */
	@Lob
	private byte[] token;
	/**
	 * Authentication object
	 */
	@Lob
	private byte[] authentication;

	/**
	 * Creates a refresh token
	 */
	protected AuthRefreshToken() {
	}

	/**
	 * Constructs the AuthRefreshToken entity
	 *
	 * @param id the token ID
	 * @param token the token object
	 * @param authentication the authentication object
	 */
	public AuthRefreshToken(String id, byte[] token, byte[] authentication) {
		this.id = id;
		this.token = token;
		this.authentication = authentication;
	}

	/**
	 * Returns the token object
	 *
	 * @return the token object
	 */
	public byte[] getToken() {
		return token;
	}

	/**
	 * Returns the authentication object
	 *
	 * @return the authentication object
	 */
	public byte[] getAuthentication() {
		return authentication;
	}
}
