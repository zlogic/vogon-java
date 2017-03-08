/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.util.Currency;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.JsonExpectationsHelper;
import org.springframework.web.client.HttpStatusCodeException;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.web.data.AccountRepository;
import org.zlogic.vogon.web.data.UserRepository;

/**
 * Tests for Accounts Controller
 * {@link org.zlogic.vogon.web.controller.AccountsController}
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = {Application.class, DatabaseConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AccountsControllerTest {

	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/web/messages");

	private static final ResourceBundle accountMessages = ResourceBundle.getBundle("org/zlogic/vogon/data/messages");

	private JsonExpectationsHelper jsonExpectationhelper = new JsonExpectationsHelper();

	@Autowired
	private RestTestClient restClient;

	@Autowired
	private Prepopupate prepopulate;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private AccountRepository accountRepository;

	@Before
	public void before() {
		prepopulate.clear();
	}

	@After
	public void after() {
		prepopulate.clear();
	}

	private void validateDefaultAccounts() {
		List<VogonUser> users = userRepository.findAll();
		VogonUser user01 = users.get(0);
		VogonUser user02 = users.get(1);
		List<FinanceAccount> accounts = accountRepository.findAll();
		assertEquals(3, accounts.size());
		FinanceAccount account1 = accounts.get(0);
		assertEquals(user01, account1.getOwner());
		assertEquals(42 + 2.72, account1.getBalance(), 0);
		assertEquals("test account 1", account1.getName());
		assertEquals(Currency.getInstance("RUB"), account1.getCurrency());
		assertEquals(true, account1.getIncludeInTotal());
		assertEquals(true, account1.getShowInList());
		assertEquals(1, account1.getVersion());
		FinanceAccount account2 = accounts.get(1);
		assertEquals(user01, account2.getOwner());
		assertEquals(160 - 3.14, account2.getBalance(), 0);
		assertEquals("test account 2", account2.getName());
		assertEquals(Currency.getInstance("EUR"), account2.getCurrency());
		assertEquals(true, account2.getIncludeInTotal());
		assertEquals(true, account2.getShowInList());
		assertEquals(1, account2.getVersion());
		FinanceAccount account3 = accounts.get(2);
		assertEquals(user02, account3.getOwner());
		assertEquals(100, account3.getBalance(), 0);
		assertEquals("test account 3", account3.getName());
		assertEquals(Currency.getInstance("RUB"), account3.getCurrency());
		assertEquals(true, account3.getIncludeInTotal());
		assertEquals(true, account3.getShowInList());
		assertEquals(1, account2.getVersion());
	}

	/**
	 * Test that an authenticated user can get their accounts
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAccounts() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/accounts", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("["
				+ "{balance:44.72,id:3,name:\"test account 1\",currency:\"RUB\",includeInTotal:true,showInList:true,version:1},"
				+ "{balance:156.86,id:4,name:\"test account 2\",currency:\"EUR\",includeInTotal:true,showInList:true,version:1}"
				+ "]", responseEntity.getBody(), true);

		validateDefaultAccounts();
	}

	/**
	 * Test that an authenticated user can change their accounts
	 *
	 * @throws Exception
	 */
	@Test
	public void testChangeAccounts() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		String changeRequest = "["
				+ "{\"balance\":111,\"id\":3,\"name\":\"test account 1a\",\"currency\":\"RUB\",\"includeInTotal\":false,\"showInList\":false,\"version\":1},"
				+ "{\"balance\":222,\"name\":\"test account 3\",\"currency\":\"EUR\",\"includeInTotal\":true,\"showInList\":true,\"version\":1},"
				+ "{\"balance\":333,\"name\":\"test account 4\",\"currency\":\"USD\",\"includeInTotal\":true,\"showInList\":true,\"version\":1}"
				+ "]";
		HttpEntity<String> entity = new HttpEntity<>(changeRequest, headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/accounts", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("["
				+ "{balance:44.72,id:3,name:\"test account 1a\",currency:\"RUB\",includeInTotal:false,showInList:false,version:2},"
				+ "{balance:0,id:15,name:\"test account 3\",currency:\"EUR\",includeInTotal:true,showInList:true,version:0},"
				+ "{balance:0,id:16,name:\"test account 4\",currency:\"USD\",includeInTotal:true,showInList:true,version:0}"
				+ "]", responseEntity.getBody(), true);

		List<VogonUser> users = userRepository.findAll();
		VogonUser user01 = users.get(0);
		VogonUser user02 = users.get(1);

		List<FinanceAccount> accounts = accountRepository.findAll();
		assertEquals(4, accounts.size());
		FinanceAccount account1 = accounts.get(0);
		assertEquals(user01, account1.getOwner());
		assertEquals(42 + 2.72, account1.getBalance(), 0);
		assertEquals("test account 1a", account1.getName());
		assertEquals(Currency.getInstance("RUB"), account1.getCurrency());
		assertEquals(false, account1.getIncludeInTotal());
		assertEquals(false, account1.getShowInList());
		assertEquals(2, account1.getVersion());
		FinanceAccount account3 = accounts.get(1);
		assertEquals(user02, account3.getOwner());
		assertEquals(100, account3.getBalance(), 0);
		assertEquals("test account 3", account3.getName());
		assertEquals(Currency.getInstance("RUB"), account3.getCurrency());
		assertEquals(true, account3.getIncludeInTotal());
		assertEquals(true, account3.getShowInList());
		assertEquals(1, account3.getVersion());
		FinanceAccount account4 = accounts.get(2);
		assertEquals(user01, account4.getOwner());
		assertEquals(0, account4.getBalance(), 0);
		assertEquals("test account 3", account4.getName());
		assertEquals(Currency.getInstance("EUR"), account4.getCurrency());
		assertEquals(true, account4.getIncludeInTotal());
		assertEquals(true, account4.getShowInList());
		assertEquals(0, account4.getVersion());
		FinanceAccount account5 = accounts.get(3);
		assertEquals(user01, account5.getOwner());
		assertEquals(0, account5.getBalance(), 0);
		assertEquals("test account 4", account5.getName());
		assertEquals(Currency.getInstance("USD"), account5.getCurrency());
		assertEquals(true, account5.getIncludeInTotal());
		assertEquals(true, account5.getShowInList());
		assertEquals(0, account5.getVersion());
	}

	/**
	 * Test that an authenticated user can delete all of their accounts
	 *
	 * @throws Exception
	 */
	@Test
	public void testDeleteAllAccounts() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>("[]", headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/accounts", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("[]", responseEntity.getBody(), true);

		List<VogonUser> users = userRepository.findAll();
		VogonUser user02 = users.get(1);

		List<FinanceAccount> accounts = accountRepository.findAll();
		FinanceAccount account3 = accounts.get(0);
		assertEquals(user02, account3.getOwner());
		assertEquals(100, account3.getBalance(), 0);
		assertEquals("test account 3", account3.getName());
		assertEquals(Currency.getInstance("RUB"), account3.getCurrency());
		assertEquals(true, account3.getIncludeInTotal());
		assertEquals(true, account3.getShowInList());
		assertEquals(1, account3.getVersion());
	}

	/**
	 * Test that an authenticated user cannot update their accounts if the
	 * version numbers mismatch
	 *
	 * @throws Exception
	 */
	@Test
	public void testVersionConstraint() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		String changeRequest = "["
				+ "{\"balance\":111,\"id\":3,\"name\":\"test account 1a\",\"currency\":\"RUB\",\"includeInTotal\":false,\"showInList\":false,\"version\":0},"
				+ "{\"balance\":222,\"name\":\"test account 3\",\"currency\":\"EUR\",\"includeInTotal\":true,\"showInList\":true,\"version\":0}"
				+ "]";
		HttpEntity<String> entity = new HttpEntity<>(changeRequest, headers);
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/service/accounts", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{message:\"" + accountMessages.getString("ACCOUNT_WAS_ALREADY_UPDATED") + "\"}", ex.getResponseBodyAsString());
		}

		validateDefaultAccounts();
	}

	/**
	 * Test that an authenticated user cannot change another user's accounts
	 *
	 * @throws Exception
	 */
	@Test
	public void testOwnerConstraint() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		String changeRequest = "["
				+ "{\"balance\":111,\"id\":3,\"name\":\"test account 1a\",\"currency\":\"RUB\",\"includeInTotal\":false,\"showInList\":false,\"version\":1},"
				+ "{\"balance\":222,\"id\":5,\"name\":\"test account 5\",\"currency\":\"EUR\",\"includeInTotal\":true,\"showInList\":true,\"version\":1}"
				+ "]";
		HttpEntity<String> entity = new HttpEntity<>(changeRequest, headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/accounts", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("["
				+ "{balance:44.72,id:3,name:\"test account 1a\",currency:\"RUB\",includeInTotal:false,showInList:false,version:2},"
				+ "{balance:0,id:15,name:\"test account 5\",currency:\"EUR\",includeInTotal:true,showInList:true,version:0}"
				+ "]", responseEntity.getBody(), true);

		List<VogonUser> users = userRepository.findAll();
		VogonUser user01 = users.get(0);
		VogonUser user02 = users.get(1);

		List<FinanceAccount> accounts = accountRepository.findAll();
		assertEquals(3, accounts.size());
		FinanceAccount account1 = accounts.get(0);
		assertEquals(user01, account1.getOwner());
		assertEquals(42 + 2.72, account1.getBalance(), 0);
		assertEquals("test account 1a", account1.getName());
		assertEquals(Currency.getInstance("RUB"), account1.getCurrency());
		assertEquals(false, account1.getIncludeInTotal());
		assertEquals(false, account1.getShowInList());
		assertEquals(2, account1.getVersion());
		FinanceAccount account3 = accounts.get(1);
		assertEquals(user02, account3.getOwner());
		assertEquals(100, account3.getBalance(), 0);
		assertEquals("test account 3", account3.getName());
		assertEquals(Currency.getInstance("RUB"), account3.getCurrency());
		assertEquals(true, account3.getIncludeInTotal());
		assertEquals(true, account3.getShowInList());
		assertEquals(1, account3.getVersion());
		FinanceAccount account5 = accounts.get(2);
		assertEquals(user01, account5.getOwner());
		assertEquals(0, account5.getBalance(), 0);
		assertEquals("test account 5", account5.getName());
		assertEquals(Currency.getInstance("EUR"), account5.getCurrency());
		assertEquals(true, account5.getIncludeInTotal());
		assertEquals(true, account5.getShowInList());
		assertEquals(0, account5.getVersion());
	}

	/**
	 * Test that an unauthenticated user (no token) is not allowed to get
	 * accounts
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAccountsNoToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.getDefaultHeaders();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/accounts", HttpMethod.GET, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (bad token) is not allowed to get
	 * accounts
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAccountsBadToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.badAuthenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/accounts", HttpMethod.GET, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"invalid_token\",\"error_description\":\"Invalid access token: bad_token\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (no token) is not allowed to change
	 * accounts
	 *
	 * @throws Exception
	 */
	@Test
	public void testChangeAccountsNoToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.getDefaultHeaders();

		String changeRequest = "["
				+ "{\"balance\":111,\"id\":3,\"name\":\"test account 1a\",\"currency\":\"RUB\",\"includeInTotal\":false,\"showInList\":false,\"version\":1},"
				+ "{\"balance\":222,\"name\":\"test account 3\",\"currency\":\"EUR\",\"includeInTotal\":true,\"showInList\":true,\"version\":1},"
				+ "{\"balance\":333,\"name\":\"test account 4\",\"currency\":\"USD\",\"includeInTotal\":true,\"showInList\":true,\"version\":1}"
				+ "]";
		HttpEntity<String> entity = new HttpEntity<>(changeRequest, headers);
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/service/accounts", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (bad token) is not allowed to change
	 * accounts
	 *
	 * @throws Exception
	 */
	@Test
	public void testChangeAccountsBadToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.badAuthenticate();

		String changeRequest = "["
				+ "{\"balance\":111,\"id\":3,\"name\":\"test account 1a\",\"currency\":\"RUB\",\"includeInTotal\":false,\"showInList\":false,\"version\":1},"
				+ "{\"balance\":222,\"name\":\"test account 3\",\"currency\":\"EUR\",\"includeInTotal\":true,\"showInList\":true,\"version\":1},"
				+ "{\"balance\":333,\"name\":\"test account 4\",\"currency\":\"USD\",\"includeInTotal\":true,\"showInList\":true,\"version\":1}"
				+ "]";
		HttpEntity<String> entity = new HttpEntity<>(changeRequest, headers);
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/service/accounts", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"invalid_token\",\"error_description\":\"Invalid access token: bad_token\"}", ex.getResponseBodyAsString(), true);
		}
	}
}
