/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import org.json.JSONArray;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.JsonExpectationsHelper;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Tests for Currencies Controller
 * {@link org.zlogic.vogon.web.controller.CurrenciesController}
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, properties = "spring.main.allow-bean-definition-overriding=true", classes = {Application.class, DatabaseConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CurrenciesControllerTest {

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
	 * Test that an authenticated user is allowed to get the list of all
	 * possible currencies
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetCurrencies() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/currencies", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		JSONArray currencies = new JSONArray(responseEntity.getBody());
		String rub = null, eur = null, usd = null;
		for (int i = 0; i < currencies.length(); i++) {
			JSONObject currency = currencies.getJSONObject(i);
			if (currency.getString("currencyCode").equals("RUB")) {
				rub = currency.toString();
			} else if (currency.getString("currencyCode").equals("EUR")) {
				eur = currency.toString();
			} else if (currency.getString("currencyCode").equals("USD")) {
				usd = currency.toString();
			}
		}
		jsonExpectationhelper.assertJsonEqual("{currencyCode:\"RUB\",displayName:\"Russian Ruble\"}", rub, true);
		jsonExpectationhelper.assertJsonEqual("{currencyCode:\"EUR\",displayName:\"Euro\"}", eur, true);
		jsonExpectationhelper.assertJsonEqual("{currencyCode:\"USD\",displayName:\"US Dollar\"}", usd, true);
	}

	/**
	 * Test that an unauthenticated user (no token) is not allowed to get the
	 * list of all possible currencies
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetCurrenciesNoToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.getDefaultHeaders();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/currencies", HttpMethod.GET, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (bad token) is not allowed to get the
	 * list of all possible currencies
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetCurrenciesBadToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.badAuthenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/currencies", HttpMethod.GET, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"invalid_token\",\"error_description\":\"Invalid access token: bad_token\"}", ex.getResponseBodyAsString(), true);
		}
	}
}
