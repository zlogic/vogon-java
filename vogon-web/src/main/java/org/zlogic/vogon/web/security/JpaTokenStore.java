/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.transaction.annotation.Transactional;
import org.zlogic.vogon.data.AuthAccessToken;
import org.zlogic.vogon.data.AuthRefreshToken;
import org.zlogic.vogon.web.data.AuthAccessTokenRepository;
import org.zlogic.vogon.web.data.AuthRefreshTokenRepository;

/**
 * TokenStore implementation which uses JPA for token storage
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Transactional
public class JpaTokenStore implements TokenStore {

	/**
	 * Generator used for extracting authentication ids
	 */
	private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

	/**
	 * The access token repository 
	 */
	@Autowired
	private AuthAccessTokenRepository accessTokenRepository;
	
	/**
	 * The refresh token repository 
	 */
	@Autowired
	private AuthRefreshTokenRepository refreshTokenRepository;
	
	/**
	 * Read the authentication stored under the specified token value.
	 *
	 * @param token The token value under which the authentication is stored.
	 * @return The authentication, or null if none.
	 */
	@Override
	public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
		return readAuthentication(token.getValue());
	}

	/**
	 * Read the authentication stored under the specified token value.
	 * 
	 * @param token The token value under which the authentication is stored.
	 * @return The authentication, or null if none.
	 */
	@Override
	public OAuth2Authentication readAuthentication(String token) {
		return accessTokenRepository.findById(token)
				.map(accessToken -> SerializationUtils.<OAuth2Authentication>deserialize(accessToken.getAuthentication()))
				.orElse(null);
	}
	
	/**
	 * Store an access token.
	 * 
	 * @param token The token to store.
	 * @param authentication The authentication associated with the token.
	 */
	@Override
	public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
		String tokenId = token.getValue();
		String clientId = authentication.getOAuth2Request().getClientId();
		String authenticationId = authenticationKeyGenerator.extractKey(authentication);
		String username = authentication.getName();
		String refreshToken = token.getRefreshToken() != null ? token.getRefreshToken().getValue() : null;
		Date expires = token.getExpiration();
		byte[] tokenBytes = SerializationUtils.serialize(token);
		byte[] authenticationBytes = SerializationUtils.serialize(authentication);

		AuthAccessToken storeToken = new AuthAccessToken(tokenId, clientId, authenticationId, username, refreshToken, expires, tokenBytes, authenticationBytes);
		accessTokenRepository.save(storeToken);
	}

	/**
	 * Read an access token from the store.
	 * 
	 * @param tokenValue The token value.
	 * @return The access token to read.
	 */
	@Override
	public OAuth2AccessToken readAccessToken(String tokenValue) {
		return accessTokenRepository.findById(tokenValue)
				.map(accessToken -> SerializationUtils.<OAuth2AccessToken>deserialize(accessToken.getToken()))
				.orElse(null);
	}

	/**
	 * Remove an access token from the database.
	 * 
	 * @param token The token to remove from the database.
	 */
	@Override
	public void removeAccessToken(OAuth2AccessToken token) {
		accessTokenRepository.deleteById(token.getValue());
	}

	/**
	 * Store the specified refresh token in the database.
	 * 
	 * @param refreshToken The refresh token to store.
	 * @param authentication The authentication associated with the refresh token.
	 */
	@Override
	public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
		AuthRefreshToken storeToken = new AuthRefreshToken(refreshToken.getValue(), SerializationUtils.serialize(refreshToken), SerializationUtils.serialize(authentication));
		refreshTokenRepository.save(storeToken);
	}

	/**
	 * Read a refresh token from the store.
	 * 
	 * @param tokenValue The value of the token to read.
	 * @return The token.
	 */
	@Override
	public OAuth2RefreshToken readRefreshToken(String tokenValue) {
		return refreshTokenRepository.findById(tokenValue)
				.map(token -> SerializationUtils.<OAuth2RefreshToken>deserialize(token.getToken()))
				.orElse(null);
	}

	/**
	 * Read the authentication stored under the specified refresh token value.
	 * 
	 * @param token a refresh token
	 * @return the authentication originally used to grant the refresh token
	 */
	@Override
	public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
		AuthAccessToken refreshToken = accessTokenRepository.findByAuthenticationId(token.getValue());
		if (refreshToken == null)
			return null;
		return SerializationUtils.deserialize(refreshToken.getAuthentication());
	}

	/**
	 * Remove a refresh token from the database.
	 * 
	 * @param token The token to remove from the database.
	 */
	@Override
	public void removeRefreshToken(OAuth2RefreshToken token) {
		refreshTokenRepository.deleteById(token.getValue());
	}

	/**
	 * Remove an access token using a refresh token. This functionality is necessary so refresh tokens can't be used to
	 * create an unlimited number of access tokens.
	 * 
	 * @param refreshToken The refresh token.
	 */
	@Override
	public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
		accessTokenRepository.deleteByRefreshToken(refreshToken.getValue());
	}

	/**
	 * Retrieve an access token stored against the provided authentication key, if it exists.
	 * 
	 * @param authentication the authentication key for the access token
	 * 
	 * @return the access token or null if there was none
	 */
	@Override
	public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
		String authenticationId = authenticationKeyGenerator.extractKey(authentication);
		AuthAccessToken token = accessTokenRepository.findByAuthenticationId(authenticationId);
		if (token == null)
			return null;
		return SerializationUtils.deserialize(token.getToken());
	}

	/**
	 * Converts AuthAccessTokens into OAuth2AccessTokens
	 *
	 * @param tokens AuthAccessTokens collection to convert
	 * @return tokens converted to OAuth2AccessTokens
	 */
	private Collection<OAuth2AccessToken> convertTokens(Collection<AuthAccessToken> tokens) {
		List<OAuth2AccessToken> convertedTokens = new ArrayList<>(tokens.size());
		for (AuthAccessToken token : tokens) {
			convertedTokens.add(SerializationUtils.deserialize(token.getToken()));
		}
		return convertedTokens;
	}
	/**
	 * Finds tokens by their client id and username.
	 * 
	 * @param clientId the client id to search
	 * @param userName the user name to search
	 * @return a collection of access tokens
	 */
	@Override
	public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
		Collection<AuthAccessToken> tokens = accessTokenRepository.findTokensByClientIdAndUsername(clientId, userName);
		return convertTokens(tokens);
	}

	/**
	 * Finds tokens by their client id.
	 *
	 * @param clientId the client id to search
	 * @return a collection of access tokens
	 */
	@Override
	public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
		Collection<AuthAccessToken> tokens = accessTokenRepository.findTokensByClientId(clientId);
		return convertTokens(tokens);
	}

}
