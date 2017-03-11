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
import org.springframework.web.client.HttpClientErrorException;
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
	 * Test that an authenticated user is allowed to get analytics data for all of their transactions
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAnalyticsAllTranctions() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		String request = "{\"earliestDate\":\"2010-01-01\",\"latestDate\":\"2020-01-01\",\"enabledTransferTransactions\":true,\"enabledIncomeTransactions\":true,\"enabledExpenseTransactions\":true,\"selectedTags\":[\"\",\"hello\",\"world\",\"magic\"],\"selectedAccounts\":[{\"id\":3},{\"id\":4}]}";
		HttpEntity<String> entity = new HttpEntity<>(request, headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/analytics", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

		System.out.println(responseEntity.getBody());
		
		//jsonExpectationhelper.assertJsonEqual("{}", responseEntity.getBody(), true);
	}
}
