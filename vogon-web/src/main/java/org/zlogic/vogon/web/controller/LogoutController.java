/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.BadClientCredentialsException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zlogic.vogon.web.security.UsernameExistsException;

/**
 * Spring MVC controller for logging out
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Controller
@RequestMapping(value = "/oauth/logout")
@Transactional
public class LogoutController {

	/**
	 * The TokenStore instance
	 */
	@Autowired
	private TokenStore tokenStore;

	/**
	 * Logs out a user
	 *
	 * @param authentication the authentication associated with the token
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public void logout(OAuth2Authentication authentication) throws UsernameExistsException {
		OAuth2AccessToken token = tokenStore.getAccessToken(authentication);
		if (token == null) {
			throw new BadClientCredentialsException();
		}
		tokenStore.removeAccessToken(token);
		if (token.getRefreshToken() != null) {
			tokenStore.removeRefreshToken(token.getRefreshToken());
		}
		if (token.isExpired()) {
			throw new BadClientCredentialsException();
		}
	}
}
