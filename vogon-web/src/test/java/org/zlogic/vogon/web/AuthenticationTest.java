/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.util.Collection;
import java.util.ResourceBundle;
import javax.annotation.Resource;
import org.json.JSONObject;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.JsonExpectationsHelper;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Tests for Authentication
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, properties = "spring.main.allow-bean-definition-overriding=true", classes = {Application.class, DatabaseConfiguration.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class AuthenticationTest {

	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/web/messages");

	private JsonExpectationsHelper jsonExpectationhelper = new JsonExpectationsHelper();

	@Autowired
	private RestTestClient restClient;

	@Autowired
	private Prepopupate prepopulate;

	@Autowired
	private TokenStore tokenStore;

	@Resource
	private ClientDetailsService clientDetailsService;

	@Before
	public void before() {
		prepopulate.clear();
	}

	@After
	public void after() {
		prepopulate.clear();
	}

	/**
	 * Test that authentication works with correct credentials
	 *
	 * @throws Exception
	 */
	@Test
	public void testAuthentication() throws Exception {
		prepopulate.prepopulate();

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("username", "user01");
		map.add("password", "mypassword");
		map.add("grant_type", "password");
		map.add("client_id", "vogonweb");
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/oauth/token", map, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		String token = new JSONObject(responseEntity.getBody()).getString("access_token");
		assertFalse(token.isEmpty());
	}

	/**
	 * Test that logout works for an authenticated user
	 *
	 * @throws Exception
	 */
	@Test
	public void testAuthenticationLogout() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientId("vogonweb");
		assertEquals(1, tokens.size());

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/oauth/logout", HttpMethod.POST, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertNull(responseEntity.getBody());
		tokens = tokenStore.findTokensByClientId("vogonweb");
		assertEquals(0, tokens.size());
	}

	/**
	 * Test that expired tokens are no longer accepted
	 *
	 * @throws Exception
	 */
	@Test
	public void testExpiredTokens() throws Exception {
		prepopulate.prepopulate();

		ClientDetails vogonwebClientDetails = clientDetailsService.loadClientByClientId("vogonweb");
		((BaseClientDetails) vogonwebClientDetails).setAccessTokenValiditySeconds(4);

		HttpHeaders headers = restClient.authenticate();
		Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientId("vogonweb");
		assertEquals(1, tokens.size());
		Thread.sleep(4000);

		HttpHeaders newHeaders = restClient.authenticate();
		tokens = tokenStore.findTokensByClientId("vogonweb");
		assertEquals(1, tokens.size());//Expired tokens are deleted once a new one is ussued
		Thread.sleep(4000);

		assertNotEquals(newHeaders.getFirst("Authorization"), headers.getFirst("Authorization"));

		HttpEntity<String> entity = new HttpEntity<>(newHeaders);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/oauth/logout", HttpMethod.POST, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpClientErrorException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			String token = newHeaders.getFirst("Authorization").substring("Bearer ".length());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"invalid_token\",\"error_description\":\"Access token expired: " + token + "\"}", ex.getResponseBodyAsString(), true);
		}
		tokens = tokenStore.findTokensByClientId("vogonweb");
		assertEquals(0, tokens.size());
	}

	/**
	 * Test that authentication with incorrect credentials is rejected
	 *
	 * @throws Exception
	 */
	@Test
	public void testAuthenticationIncorrectPassword() throws Exception {
		prepopulate.prepopulate();

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("username", "user01");
		map.add("password", "badpassword");
		map.add("grant_type", "password");
		map.add("client_id", "vogonweb");
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/oauth/token", map, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpClientErrorException ex) {
			assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"invalid_grant\",\"error_description\":\"Bad credentials\"}", ex.getResponseBodyAsString(), true);
		}
		Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientId("vogonweb");
		assertEquals(0, tokens.size());
	}

	/**
	 * Test that logout with an incorrect token is rejected
	 *
	 * @throws Exception
	 */
	@Test
	public void testAuthenticationInvalidTokenPassword() throws Exception {
		prepopulate.prepopulate();

		restClient.authenticate();

		Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientId("vogonweb");
		assertEquals(1, tokens.size());

		HttpHeaders headers = restClient.badAuthenticate();
		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/oauth/logout", HttpMethod.GET, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpClientErrorException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"invalid_token\",\"error_description\":\"Invalid access token: bad_token\"}", ex.getResponseBodyAsString(), true);
		}
		tokens = tokenStore.findTokensByClientId("vogonweb");
		assertEquals(1, tokens.size());
	}
}
