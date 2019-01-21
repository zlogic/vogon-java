/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.util.List;
import java.util.ResourceBundle;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.JsonExpectationsHelper;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.web.data.UserRepository;

/**
 * Tests for Users Controller
 * {@link org.zlogic.vogon.web.controller.UsersController}
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, properties = "spring.main.allow-bean-definition-overriding=true", classes = {Application.class, DatabaseConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UsersControllerTest {

	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/web/messages");

	private JsonExpectationsHelper jsonExpectationhelper = new JsonExpectationsHelper();

	@Autowired
	private RestTestClient restClient;

	@Autowired
	private Prepopupate prepopulate;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Before
	public void before() {
		prepopulate.clear();
	}

	@After
	public void after() {
		prepopulate.clear();
	}

	private void validateDefaultUsers() {
		List<VogonUser> users = userRepository.findAll();
		assertEquals(2, users.size());
		VogonUser user01 = users.get(0);
		assertEquals("user01", user01.getUsername());
		assertTrue(passwordEncoder.matches("mypassword", user01.getPassword()));
		assertEquals(0, user01.getVersion());
		VogonUser user02 = users.get(1);
		assertEquals("user02", user02.getUsername());
		assertTrue(passwordEncoder.matches("mypassword2", user02.getPassword()));
		assertEquals(0, user02.getVersion());
	}

	/**
	 * Test that an authenticated user can get their user details
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetUserDetails() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/user", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("{username:\"user01\",version:0}", responseEntity.getBody(), true);

		validateDefaultUsers();
	}

	/**
	 * Test that an authenticated user can change their username
	 *
	 * @throws Exception
	 */
	@Test
	public void testChangeUsername() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>("{\"username\":\"user03\"}", headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/user", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("{username:\"user03\",version:1}", responseEntity.getBody(), true);

		List<VogonUser> users = userRepository.findAll();
		assertEquals(2, users.size());
		VogonUser user01 = users.get(0);
		assertEquals("user03", user01.getUsername());
		assertTrue(passwordEncoder.matches("mypassword", user01.getPassword()));
		VogonUser user02 = users.get(1);
		assertEquals("user02", user02.getUsername());
		assertTrue(passwordEncoder.matches("mypassword2", user02.getPassword()));
	}

	/**
	 * Test that an authenticated user can change their password
	 *
	 * @throws Exception
	 */
	@Test
	public void testChangePassword() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>("{\"password\":\"mypassword1\"}", headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/user", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("{username:\"user01\",version:1}", responseEntity.getBody(), true);

		List<VogonUser> users = userRepository.findAll();
		assertEquals(2, users.size());
		VogonUser user01 = users.get(0);
		assertEquals("user01", user01.getUsername());
		assertTrue(passwordEncoder.matches("mypassword1", user01.getPassword()));
		VogonUser user02 = users.get(1);
		assertEquals("user02", user02.getUsername());
		assertTrue(passwordEncoder.matches("mypassword2", user02.getPassword()));
	}

	/**
	 * Test that an authenticated user can change their username and password
	 *
	 * @throws Exception
	 */
	@Test
	public void testChangeUsernamePassword() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>("{\"username\":\"user03\",\"password\":\"mypassword1\"}", headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/user", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("{username:\"user03\",version:1}", responseEntity.getBody(), true);

		List<VogonUser> users = userRepository.findAll();
		assertEquals(2, users.size());
		VogonUser user01 = users.get(0);
		assertEquals("user03", user01.getUsername());
		assertTrue(passwordEncoder.matches("mypassword1", user01.getPassword()));
		VogonUser user02 = users.get(1);
		assertEquals("user02", user02.getUsername());
		assertTrue(passwordEncoder.matches("mypassword2", user02.getPassword()));
	}

	/**
	 * Test that an authenticated user cannot change their username if the
	 * desired username is already in use
	 *
	 * @throws Exception
	 */
	@Test
	public void testChangeUsernameAlreadyInUse() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>("{\"username\":\"user02\"}", headers);
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/service/user", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpServerErrorException ex) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{message:\"" + messages.getString("USER_ALREADY_EXISTS") + "\"}", ex.getResponseBodyAsString());
		}

		validateDefaultUsers();
	}

	/**
	 * Test that an authenticated user cannot change their username if the
	 * desired username is empty (empty username is ignored)
	 *
	 * @throws Exception
	 */
	@Test
	public void testChangeUsernameEmpty() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>("{\"username\":\"\"}", headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/user", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("{username:\"user01\",version:0}", responseEntity.getBody(), true);

		validateDefaultUsers();
	}

	/**
	 * Test that an authenticated user cannot change their username if the
	 * desired username is empty (empty password is ignored)
	 *
	 * @throws Exception
	 */
	@Test
	public void testChangePasswordEmpty() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>("{\"password\":\"\"}", headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/user", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("{username:\"user01\",version:0}", responseEntity.getBody(), true);

		validateDefaultUsers();
	}

	/**
	 * Test that an authenticated user cannot get another user's data by using
	 * their ID and only the authentication principal is used to identify the
	 * user
	 *
	 * @throws Exception
	 */
	@Test
	public void testIgnoreIdGet() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>("{\"id\":2}", headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/user", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("{username:\"user01\",version:0}", responseEntity.getBody(), true);

		validateDefaultUsers();
	}

	/**
	 * Test that an authenticated user cannot change another user's data by
	 * using their ID and only the authentication principal is used to identify
	 * the user
	 *
	 * @throws Exception
	 */
	@Test
	public void testIgnoreIdUpdate() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>("{\"username\":\"user03\",\"password\":\"mypassword1\",\"id\":2}", headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/user", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("{username:\"user03\",version:1}", responseEntity.getBody(), true);

		List<VogonUser> users = userRepository.findAll();
		assertEquals(2, users.size());
		VogonUser user01 = users.get(0);
		assertEquals("user03", user01.getUsername());
		assertTrue(passwordEncoder.matches("mypassword1", user01.getPassword()));
		VogonUser user02 = users.get(1);
		assertEquals("user02", user02.getUsername());
		assertTrue(passwordEncoder.matches("mypassword2", user02.getPassword()));
	}

	/**
	 * Test that an unauthenticated user (no token) is not allowed to get user
	 * details
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetUserDetailsNoToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.getDefaultHeaders();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/user", HttpMethod.GET, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (bad token) is not allowed to get user
	 * details
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetUserDetailsBadToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.badAuthenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/user", HttpMethod.GET, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"invalid_token\",\"error_description\":\"Invalid access token: bad_token\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (no token) is not allowed to change
	 * user details
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateUserDetailsNoToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.getDefaultHeaders();

		HttpEntity<String> entity = new HttpEntity<>("{\"username\":\"user03\",\"password\":\"mypassword1\",\"id\":2}", headers);
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/service/user", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}", ex.getResponseBodyAsString(), true);
		}

		validateDefaultUsers();
	}

	/**
	 * Test that an unauthenticated user (bad token) is not allowed to change
	 * user details
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateUserDetailsBadToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.badAuthenticate();

		HttpEntity<String> entity = new HttpEntity<>("{\"username\":\"user03\",\"password\":\"mypassword1\",\"id\":2}", headers);
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/service/user", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"invalid_token\",\"error_description\":\"Invalid access token: bad_token\"}", ex.getResponseBodyAsString(), true);
		}

		validateDefaultUsers();
	}
}
