/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.Resource;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.JsonExpectationsHelper;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.web.configuration.VogonConfiguration;
import org.zlogic.vogon.web.data.UserRepository;

/**
 * Tests for Registration Controller
 * {@link org.zlogic.vogon.web.controller.RegistrationController}
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = {Application.class, DatabaseConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RegistrationTest {

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

	@MockBean
	private VogonConfiguration vogonConfiguration;

	@Resource
	private TransactionTemplate transactionTemplate;

	@Before
	public void before() {
		prepopulate.clear();
	}

	@After
	public void after() {
		prepopulate.clear();
	}

	/**
	 * Test that a user is allowed to register if registration is allowed
	 *
	 * @throws Exception
	 */
	@Test
	public void testRegistration() throws Exception {
		given(vogonConfiguration.isAllowRegistration()).willReturn(true);

		HttpEntity<String> entity = new HttpEntity<>("{\"username\":\"user01\",\"password\":\"password\"}", restClient.getDefaultHeaders());
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/register", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("{username:\"user01\"}", responseEntity.getBody());

		transactionTemplate.execute((ts) -> {
			List<VogonUser> users = userRepository.findAll();
			assertEquals(1, users.size());
			VogonUser user = users.get(0);
			assertEquals("user01", user.getUsername());
			assertTrue(passwordEncoder.matches("password", user.getPassword()));
			assertTrue(user.getAccounts().isEmpty());
			assertTrue(user.getTransactions().isEmpty());
			return null;
		});
	}

	/**
	 * Test that a user is allowed to register if registration is not allowed
	 *
	 * @throws Exception
	 */
	@Test
	public void testRegistrationNotAllowed() throws Exception {
		given(vogonConfiguration.isAllowRegistration()).willReturn(false);

		HttpEntity<String> entity = new HttpEntity<>("{\"username\":\"user01\",\"password\":\"password\"}", restClient.getDefaultHeaders());
		try {
			restClient.getRestTemplate().postForObject("https://localhost:8443/register", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{message:\"" + messages.getString("REGISTRATION_IS_NOT_ALLOWED") + "\"}", ex.getResponseBodyAsString());
		}
		assertEquals(0, userRepository.count());
	}

	/**
	 * Test that a user is not allowed to register if another user with the same
	 * username already exists
	 *
	 * @throws Exception
	 */
	@Test
	public void testDuplicateRegistrationNotAllowed() throws Exception {
		given(vogonConfiguration.isAllowRegistration()).willReturn(true);

		prepopulate.prepopulate();

		HttpEntity<String> entity = new HttpEntity<>("{\"username\":\"user01\",\"password\":\"anotherpassword\"}", restClient.getDefaultHeaders());
		try {
			restClient.getRestTemplate().postForObject("https://localhost:8443/register", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{message:\"" + messages.getString("USER_ALREADY_EXISTS") + "\"}", ex.getResponseBodyAsString());
		}
		transactionTemplate.execute((ts) -> {
			List<VogonUser> users = userRepository.findAll();
			assertEquals(2, users.size());
			VogonUser user01 = users.get(0);
			assertEquals("user01", user01.getUsername());
			assertTrue(passwordEncoder.matches("mypassword", user01.getPassword()));
			assertTrue(user01.getAccounts().isEmpty());
			assertTrue(user01.getTransactions().isEmpty());
			VogonUser user02 = users.get(1);
			assertEquals("user02", user02.getUsername());
			assertTrue(passwordEncoder.matches("mypassword2", user02.getPassword()));
			assertTrue(user02.getAccounts().isEmpty());
			assertTrue(user02.getTransactions().isEmpty());
			return null;
		});
	}

	/**
	 * Test that a user is not allowed to register if the username is empty
	 *
	 * @throws Exception
	 */
	@Test
	public void testEmptyUsername() throws Exception {
		given(vogonConfiguration.isAllowRegistration()).willReturn(true);

		HttpEntity<String> entity = new HttpEntity<>("{\"username\":\"\",\"password\":\"password\"}", restClient.getDefaultHeaders());
		try {
			restClient.getRestTemplate().postForObject("https://localhost:8443/register", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{message:\"" + MessageFormat.format(messages.getString("CANNOT_REGISTER_USER_BECAUSE_OF_ERROR"), "javax.persistence.PersistenceException: org.hibernate.PropertyValueException: not-null property references a null or transient value : org.zlogic.vogon.data.VogonUser.username") + "\"}", ex.getResponseBodyAsString());
		}
		assertEquals(0, userRepository.count());
	}

	/**
	 * Test that a user is not allowed to register if the password is empty
	 *
	 * @throws Exception
	 */
	@Test
	public void testEmptyPassword() throws Exception {
		given(vogonConfiguration.isAllowRegistration()).willReturn(true);

		HttpEntity<String> entity = new HttpEntity<>("{\"username\":\"user01\",\"password\":\"\"}", restClient.getDefaultHeaders());
		try {
			restClient.getRestTemplate().postForObject("https://localhost:8443/register", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{message:\"" + MessageFormat.format(messages.getString("CANNOT_REGISTER_USER_BECAUSE_OF_ERROR"), "javax.persistence.PersistenceException: org.hibernate.PropertyValueException: not-null property references a null or transient value : org.zlogic.vogon.data.VogonUser.password") + "\"}", ex.getResponseBodyAsString());
		}
		assertEquals(0, userRepository.count());
	}
}
