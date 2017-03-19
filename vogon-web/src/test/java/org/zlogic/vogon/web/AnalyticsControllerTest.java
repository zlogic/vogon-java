/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

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

/**
 * Tests for Analytics Controller
 * {@link org.zlogic.vogon.web.controller.AnalyticsController}
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = {Application.class, DatabaseConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AnalyticsControllerTest {

	private JsonExpectationsHelper jsonExpectationhelper = new JsonExpectationsHelper();

	@Autowired
	private RestTestClient restClient;

	@Autowired
	private Prepopupate prepopulate;

	@Before
	public void before() {
		prepopulate.clear();
	}

	@After
	public void after() {
		prepopulate.clear();
	}

	/**
	 * Test that an authenticated user is allowed to get the list of all of
	 * their tags
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTags() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/analytics/tags", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

		jsonExpectationhelper.assertJsonEqual("[\"\",\"hello\",\"world\",\"magic\"]", responseEntity.getBody());
	}

	/**
	 * Test that an authenticated user is allowed to get an empty list of all of
	 * their tags
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetEmptyTags() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate("user02", "mypassword2");

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/analytics/tags", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

		jsonExpectationhelper.assertJsonEqual("[\"\"]", responseEntity.getBody());
	}

	/**
	 * Test that an authenticated user is allowed to get analytics data for all
	 * of their transactions
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAnalyticsAllTranctions() throws Exception {
		prepopulate.prepopulateExtra();

		HttpHeaders headers = restClient.authenticate();

		String request = "{\"earliestDate\":\"2010-01-01\",\"latestDate\":\"2020-01-01\",\"enabledTransferTransactions\":true,\"enabledIncomeTransactions\":true,\"enabledExpenseTransactions\":true,\"selectedTags\":[\"\",\"hello\",\"world\",\"magic\"],\"selectedAccounts\":[{\"id\":3},{\"id\":4}]}";
		HttpEntity<String> entity = new HttpEntity<>(request, headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/analytics", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

		jsonExpectationhelper.assertJsonEqual("{"
				+ "RUB:{"
				+ "transactions:[{description:\"test transaction 4\",date:\"2014-06-07\",type:\"TRANSFER\",amount:144},{description:\"test transaction 1\",date:\"2014-02-17\",type:\"EXPENSEINCOME\",amount:42},{description:\"test transaction 2\",date:\"2015-01-07\",type:\"EXPENSEINCOME\",amount:2.72}],"
				+ "tagExpenses:[{tag:\"\",amount:144},{tag:\"hello\",amount:44.72},{tag:\"world\",amount:42},{tag:\"magic\",amount:2.72}],"
				+ "accountsBalanceGraph:{\"2014-02-17\":42,\"2014-06-07\":-102,\"2015-01-07\":-99.28}"
				+ "},EUR:{"
				+ "transactions:[{description:\"test transaction 1\",date:\"2014-02-17\",type:\"EXPENSEINCOME\",amount:160},{description:\"test transaction 4\",date:\"2014-06-07\",type:\"TRANSFER\",amount:144},{description:\"test transaction 2\",date:\"2015-01-07\",type:\"EXPENSEINCOME\",amount:-3.14}],"
				+ "tagExpenses:[{tag:\"world\",amount:160},{tag:\"hello\",amount:156.86},{tag:\"\",amount:144},{tag:\"magic\",amount:-3.14}],"
				+ "accountsBalanceGraph:{\"2014-02-17\":160,\"2014-06-07\":304,\"2015-01-07\":300.86}"
				+ "}"
				+ "}", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user is allowed to get analytics data for only
	 * income transactions
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAnalyticsIncomeTranctions() throws Exception {
		prepopulate.prepopulateExtra();

		HttpHeaders headers = restClient.authenticate();

		String request = "{\"earliestDate\":\"2010-01-01\",\"latestDate\":\"2020-01-01\",\"enabledTransferTransactions\":false,\"enabledIncomeTransactions\":true,\"enabledExpenseTransactions\":false,\"selectedTags\":[\"\",\"hello\",\"world\",\"magic\"],\"selectedAccounts\":[{\"id\":3},{\"id\":4}]}";
		HttpEntity<String> entity = new HttpEntity<>(request, headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/analytics", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

		jsonExpectationhelper.assertJsonEqual("{"
				+ "RUB:{"
				+ "transactions:[{description:\"test transaction 1\",date:\"2014-02-17\",type:\"EXPENSEINCOME\",amount:42},{description:\"test transaction 2\",date:\"2015-01-07\",type:\"EXPENSEINCOME\",amount:2.72}],"
				+ "tagExpenses:[{tag:\"hello\",amount:44.72},{tag:\"world\",amount:42},{tag:\"magic\",amount:2.72}],"
				+ "accountsBalanceGraph:{\"2014-02-17\":42,\"2015-01-07\":44.72}"
				+ "},EUR:{"
				+ "transactions:[{description:\"test transaction 1\",date:\"2014-02-17\",type:\"EXPENSEINCOME\",amount:160}],"
				+ "tagExpenses:[{tag:\"hello\",amount:160},{tag:\"world\",amount:160}],"
				+ "accountsBalanceGraph:{\"2014-02-17\":160}"
				+ "}"
				+ "}", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user is allowed to get analytics data for only
	 * expense transactions
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAnalyticsExpenseTranctions() throws Exception {
		prepopulate.prepopulateExtra();

		HttpHeaders headers = restClient.authenticate();

		String request = "{\"earliestDate\":\"2010-01-01\",\"latestDate\":\"2020-01-01\",\"enabledTransferTransactions\":false,\"enabledIncomeTransactions\":false,\"enabledExpenseTransactions\":true,\"selectedTags\":[\"\",\"hello\",\"world\",\"magic\"],\"selectedAccounts\":[{\"id\":3},{\"id\":4}]}";
		HttpEntity<String> entity = new HttpEntity<>(request, headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/analytics", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

		jsonExpectationhelper.assertJsonEqual("{"
				+ "RUB:{transactions:[],tagExpenses:[],accountsBalanceGraph:{}},"
				+ "EUR:{"
				+ "transactions:[{description:\"test transaction 2\",date:\"2015-01-07\",type:\"EXPENSEINCOME\",amount:-3.14}],"
				+ "tagExpenses:[{tag:\"hello\",amount:-3.14},{tag:\"magic\",amount:-3.14}],"
				+ "accountsBalanceGraph:{\"2015-01-07\":-3.14}"
				+ "}"
				+ "}", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user is allowed to get analytics data for only
	 * transfer transactions
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAnalyticsTransferTranctions() throws Exception {
		prepopulate.prepopulateExtra();

		HttpHeaders headers = restClient.authenticate();

		String request = "{\"earliestDate\":\"2010-01-01\",\"latestDate\":\"2020-01-01\",\"enabledTransferTransactions\":true,\"enabledIncomeTransactions\":false,\"enabledExpenseTransactions\":false,\"selectedTags\":[\"\",\"hello\",\"world\",\"magic\"],\"selectedAccounts\":[{\"id\":3},{\"id\":4}]}";
		HttpEntity<String> entity = new HttpEntity<>(request, headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/analytics", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

		jsonExpectationhelper.assertJsonEqual("{"
				+ "RUB:{"
				+ "transactions:[{description:\"test transaction 4\",date:\"2014-06-07\",type:\"TRANSFER\",amount:144}],"
				+ "tagExpenses:[{tag:\"\",amount:144}],"
				+ "accountsBalanceGraph:{\"2014-06-07\":-144}"
				+ "},EUR:{"
				+ "transactions:[{description:\"test transaction 4\",date:\"2014-06-07\",type:\"TRANSFER\",amount:144}],"
				+ "tagExpenses:[{tag:\"\",amount:144}],"
				+ "accountsBalanceGraph:{\"2014-06-07\":144}"
				+ "}"
				+ "}", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user is allowed to get analytics data for only
	 * transactions from a specific date
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAnalyticsDayTranctions() throws Exception {
		prepopulate.prepopulateExtra();

		HttpHeaders headers = restClient.authenticate();

		String request = "{\"earliestDate\":\"2014-02-17\",\"latestDate\":\"2014-02-17\",\"enabledTransferTransactions\":true,\"enabledIncomeTransactions\":true,\"enabledExpenseTransactions\":true,\"selectedTags\":[\"\",\"hello\",\"world\",\"magic\"],\"selectedAccounts\":[{\"id\":3},{\"id\":4}]}";
		HttpEntity<String> entity = new HttpEntity<>(request, headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/analytics", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

		jsonExpectationhelper.assertJsonEqual("{"
				+ "RUB:{"
				+ "transactions:[{description:\"test transaction 1\",date:\"2014-02-17\",type:\"EXPENSEINCOME\",amount:42}],"
				+ "tagExpenses:[{tag:\"hello\",amount:42},{tag:\"world\",amount:42}],"
				+ "accountsBalanceGraph:{\"2014-02-17\":42}"
				+ "},EUR:{"
				+ "transactions:[{description:\"test transaction 1\",date:\"2014-02-17\",type:\"EXPENSEINCOME\",amount:160}],"
				+ "tagExpenses:[{tag:\"hello\",amount:160},{tag:\"world\",amount:160}],"
				+ "accountsBalanceGraph:{\"2014-02-17\":160}"
				+ "}"
				+ "}", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user is allowed to get analytics data for only
	 * transactions with a specific account
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAnalyticsAccountTranctions() throws Exception {
		prepopulate.prepopulateExtra();

		HttpHeaders headers = restClient.authenticate();

		String request = "{\"earliestDate\":\"2010-01-01\",\"latestDate\":\"2020-01-01\",\"enabledTransferTransactions\":true,\"enabledIncomeTransactions\":true,\"enabledExpenseTransactions\":true,\"selectedTags\":[\"\",\"hello\",\"world\",\"magic\"],\"selectedAccounts\":[{\"id\":3}]}";
		HttpEntity<String> entity = new HttpEntity<>(request, headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/analytics", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

		jsonExpectationhelper.assertJsonEqual("{"
				+ "RUB:{"
				+ "transactions:[{description:\"test transaction 4\",date:\"2014-06-07\",type:\"TRANSFER\",amount:144},{description:\"test transaction 1\",date:\"2014-02-17\",type:\"EXPENSEINCOME\",amount:42},{description:\"test transaction 2\",date:\"2015-01-07\",type:\"EXPENSEINCOME\",amount:2.72}],"
				+ "tagExpenses:[{tag:\"\",amount:144},{tag:\"hello\",amount:44.72},{tag:\"world\",amount:42},{tag:\"magic\",amount:2.72}],"
				+ "accountsBalanceGraph:{\"2014-02-17\":42,\"2014-06-07\":-102,\"2015-01-07\":-99.28}"
				+ "}"
				+ "}", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user is allowed to get analytics data for only
	 * transactions with a specific tag
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAnalyticsTagTranctions() throws Exception {
		prepopulate.prepopulateExtra();

		HttpHeaders headers = restClient.authenticate();

		String request = "{\"earliestDate\":\"2010-01-01\",\"latestDate\":\"2020-01-01\",\"enabledTransferTransactions\":true,\"enabledIncomeTransactions\":true,\"enabledExpenseTransactions\":true,\"selectedTags\":[\"magic\"],\"selectedAccounts\":[{\"id\":3},{\"id\":4}]}";
		HttpEntity<String> entity = new HttpEntity<>(request, headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/analytics", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

		jsonExpectationhelper.assertJsonEqual("{"
				+ "RUB:{"
				+ "transactions:[{description:\"test transaction 2\",date:\"2015-01-07\",type:\"EXPENSEINCOME\",amount:2.72}],"
				+ "tagExpenses:[{tag:\"hello\",amount:2.72},{tag:\"magic\",amount:2.72}],"
				+ "accountsBalanceGraph:{\"2015-01-07\":2.72}"
				+ "},EUR:{"
				+ "transactions: [{description:\"test transaction 2\",date:\"2015-01-07\",type:\"EXPENSEINCOME\",amount:-3.14}],"
				+ "tagExpenses:[{tag:\"hello\",amount:-3.14},{tag:\"magic\",amount:-3.14}],"
				+ "accountsBalanceGraph:{\"2015-01-07\":-3.14}"
				+ "}"
				+ "}", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user can get a response for an empty analytics
	 * request
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAnalyticsEmptyRequest() throws Exception {
		prepopulate.prepopulateExtra();

		HttpHeaders headers = restClient.authenticate();

		String request = "{}";
		HttpEntity<String> entity = new HttpEntity<>(request, headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/analytics", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

		jsonExpectationhelper.assertJsonEqual("{}", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user will not get details from an account
	 * belonging to another user in an analytics request
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAnalyticsAnotherUserAccount() throws Exception {
		prepopulate.prepopulateExtra();

		HttpHeaders headers = restClient.authenticate();

		String request = "{\"earliestDate\":\"2010-01-01\",\"latestDate\":\"2020-01-01\",\"enabledTransferTransactions\":true,\"enabledIncomeTransactions\":true,\"enabledExpenseTransactions\":true,\"selectedTags\":[\"\",\"hello\",\"world\",\"magic\"],\"selectedAccounts\":[{\"id\":3},{\"id\":4},{\"id\":5}]}";
		HttpEntity<String> entity = new HttpEntity<>(request, headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/analytics", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

		jsonExpectationhelper.assertJsonEqual("{"
				+ "RUB:{"
				+ "transactions:[{description:\"test transaction 4\",date:\"2014-06-07\",type:\"TRANSFER\",amount:144},{description:\"test transaction 1\",date:\"2014-02-17\",type:\"EXPENSEINCOME\",amount:42},{description:\"test transaction 2\",date:\"2015-01-07\",type:\"EXPENSEINCOME\",amount:2.72}],"
				+ "tagExpenses:[{tag:\"\",amount:144},{tag:\"hello\",amount:44.72},{tag:\"world\",amount:42},{tag:\"magic\",amount:2.72}],"
				+ "accountsBalanceGraph:{\"2014-02-17\":42,\"2014-06-07\":-102,\"2015-01-07\":-99.28}"
				+ "},EUR:{"
				+ "transactions:[{description:\"test transaction 1\",date:\"2014-02-17\",type:\"EXPENSEINCOME\",amount:160},{description:\"test transaction 4\",date:\"2014-06-07\",type:\"TRANSFER\",amount:144},{description:\"test transaction 2\",date:\"2015-01-07\",type:\"EXPENSEINCOME\",amount:-3.14}],"
				+ "tagExpenses:[{tag:\"world\",amount:160},{tag:\"hello\",amount:156.86},{tag:\"\",amount:144},{tag:\"magic\",amount:-3.14}],"
				+ "accountsBalanceGraph:{\"2014-02-17\":160,\"2014-06-07\":304,\"2015-01-07\":300.86}"
				+ "}"
				+ "}", responseEntity.getBody(), true);
	}

	/**
	 * Test that an unauthenticated user (no token) is not allowed to get the
	 * list of all of their tags
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTagsNoToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.getDefaultHeaders();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/analytics/tags", HttpMethod.GET, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (bad token) is not allowed to get the
	 * list of all of their tags
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTagsBadToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.badAuthenticate();

		String request = "{\"earliestDate\":\"2010-01-01\",\"latestDate\":\"2020-01-01\",\"enabledTransferTransactions\":true,\"enabledIncomeTransactions\":true,\"enabledExpenseTransactions\":true,\"selectedTags\":[\"\",\"hello\",\"world\",\"magic\"],\"selectedAccounts\":[{\"id\":3},{\"id\":4}]}";
		HttpEntity<String> entity = new HttpEntity<>(request, headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/analytics/tags", HttpMethod.GET, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"invalid_token\",\"error_description\":\"Invalid access token: bad_token\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (no token) is not allowed to get
	 * analytics
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAnalyticsNoToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.getDefaultHeaders();

		String request = "{\"earliestDate\":\"2010-01-01\",\"latestDate\":\"2020-01-01\",\"enabledTransferTransactions\":true,\"enabledIncomeTransactions\":true,\"enabledExpenseTransactions\":true,\"selectedTags\":[\"\",\"hello\",\"world\",\"magic\"],\"selectedAccounts\":[{\"id\":3},{\"id\":4}]}";
		HttpEntity<String> entity = new HttpEntity<>(request, headers);
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/service/analytics", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (bad token) is not allowed to get
	 * analytics
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAnalyticsBadToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.badAuthenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/service/analytics", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"invalid_token\",\"error_description\":\"Invalid access token: bad_token\"}", ex.getResponseBodyAsString(), true);
		}
	}
}
