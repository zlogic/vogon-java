/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Class for storing an authentication access token
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Entity
public class AuthAccessToken implements Serializable {

	/**
	 * The token ID
	 */
	@Id
	private String id;

	/**
	 * The client ID
	 */
	private String clientId;

	/**
	 * The Authentication object ID
	 */
	private String authenticationId;

	/**
	 * The username
	 */
	private String username;

	/**
	 * Refresh token
	 */
	private String refreshToken;
	/**
	 * Date when the token expires
	 */
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date expires;

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
	 * Creates an access token
	 */
	protected AuthAccessToken() {
	}

	/**
	 * Constructs the AuthAccessToken entity
	 *
	 * @param id the token ID
	 * @param clientId the client ID
	 * @param authenticationId the authentication ID
	 * @param username the username
	 * @param refreshToken the refresh token
	 * @param expires Date when the token expires
	 * @param token the token object
	 * @param authentication the authentication object
	 */
	public AuthAccessToken(String id, String clientId, String authenticationId, String username, String refreshToken, Date expires, byte[] token, byte[] authentication) {
		this.id = id;
		this.clientId = clientId;
		this.authenticationId = authenticationId;
		this.username = username;
		this.refreshToken = refreshToken;
		this.expires = expires;
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
