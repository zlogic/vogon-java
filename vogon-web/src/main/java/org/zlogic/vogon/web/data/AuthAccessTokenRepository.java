/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.data;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.zlogic.vogon.data.AuthAccessToken;

/**
 * The OAuth2 Access Token JpaRepository
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public interface AuthAccessTokenRepository extends JpaRepository<AuthAccessToken, String> {

	/**
	 * Finds an Access Token by its Authentication ID
	 *
	 * @param authenticationId the Authentication ID
	 * @return Access Token for Authentication ID
	 */
	public AuthAccessToken findByAuthenticationId(String authenticationId);

	/**
	 * Deletes an Access Token by its Refresh Token ID
	 *
	 * @param refreshToken the Refresh Token ID
	 */
	public void deleteByRefreshToken(String refreshToken);

	/**
	 * Finds all Access Tokens associates with a Client ID
	 *
	 * @param clientId the Client ID
	 * @return Access Tokens associated with the Client ID
	 */
	public Collection<AuthAccessToken> findTokensByClientId(String clientId);

	/**
	 * Finds all Access Tokens associates with a Client ID and Username
	 *
	 * @param clientId the Client ID
	 * @param username the Username
	 * @return Access Tokens associated with the Client ID and Username
	 */
	public Collection<AuthAccessToken> findTokensByClientIdAndUsername(String clientId, String username);
}
