/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
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
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.web.data.TransactionRepository;
import org.zlogic.vogon.web.data.UserRepository;

/**
 * Tests for Transactions Controller
 * {@link org.zlogic.vogon.web.controller.AccountsController} read/get
 * operations
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = {Application.class, DatabaseConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TransactionsControllerReadTest {

	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/web/messages");

	private static final ResourceBundle transactionMessages = ResourceBundle.getBundle("org/zlogic/vogon/data/messages");

	private JsonExpectationsHelper jsonExpectationhelper = new JsonExpectationsHelper();

	@Autowired
	private RestTestClient restClient;

	@Autowired
	private Prepopupate prepopulate;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Before
	public void before() {
		prepopulate.clear();
	}

	@After
	public void after() {
		prepopulate.clear();
	}

	/**
	 * Test that an authenticated user can get their transactions with default
	 * sort parameters
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTransactionsDefaultSort() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("["
				+ "{tags:[\"hello\",\"world\"],id:6,type:\"EXPENSEINCOME\",description:\"test transaction 1\",date:\"2014-02-17\",version:0,components:[{accountId:3,amount:42,id:7,version:0},{accountId:4,amount:160,id:8,version:0}]},"
				+ "{tags:[],id:9,type:\"TRANSFER\",description:\"test transaction 3\",date:\"2014-02-17\",version:0,components:[]},"
				+ "{tags:[\"hello\",\"magic\"],id:10,type:\"EXPENSEINCOME\",description:\"test transaction 2\",date:\"2015-01-07\",version:0,components:[{accountId:4,amount:-3.14,id:11,version:0},{accountId:3,amount:2.72,id:12,version:0}]}"
				+ "]", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user can get their transactions, sorted by
	 * date descending
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTransactionsDateDesc() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions?sortColumn=DATE&sortDirection=DESC", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("["
				+ "{tags:[\"hello\",\"magic\"],id:10,type:\"EXPENSEINCOME\",description:\"test transaction 2\",date:\"2015-01-07\",version:0,components:[{accountId:4,amount:-3.14,id:11,version:0},{accountId:3,amount:2.72,id:12,version:0}]},"
				+ "{tags:[],id:9,type:\"TRANSFER\",description:\"test transaction 3\",date:\"2014-02-17\",version:0,components:[]},"
				+ "{tags:[\"hello\",\"world\"],id:6,type:\"EXPENSEINCOME\",description:\"test transaction 1\",date:\"2014-02-17\",version:0,components:[{accountId:3,amount:42,id:7,version:0},{accountId:4,amount:160,id:8,version:0}]}"
				+ "]", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user can get a specific transaction
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetSpecificTransaction() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions/transaction/6", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual(
				"{tags:[\"hello\",\"world\"],id:6,type:\"EXPENSEINCOME\",description:\"test transaction 1\",date:\"2014-02-17\",version:0,components:[{accountId:3,amount:42,id:7,version:0},{accountId:4,amount:160,id:8,version:0}]}",
				responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user will get an error when trying to get a
	 * specific non-existing transaction
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetSpecificNonExistingTransaction() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions/transaction/160", HttpMethod.GET, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{message:\"" + MessageFormat.format(messages.getString("TRANSACTION_DOES_NOT_EXIST"), 160) + "\"}", ex.getResponseBodyAsString());
		}
	}

	/**
	 * Test that an authenticated user will get an error when trying to get a
	 * specific transaction belonging to another user
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetSpecificAnotherUserTransaction() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate("user02", "mypassword2");

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions/transaction/6", HttpMethod.GET, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{message:\"" + MessageFormat.format(messages.getString("TRANSACTION_DOES_NOT_EXIST"), 6) + "\"}", ex.getResponseBodyAsString());
		}
	}

	/**
	 * Test that an authenticated user can get their transactions, sorted by
	 * date ascending
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTransactionsDateAsc() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions?sortColumn=DATE&sortDirection=ASC", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("["
				+ "{tags:[\"hello\",\"world\"],id:6,type:\"EXPENSEINCOME\",description:\"test transaction 1\",date:\"2014-02-17\",version:0,components:[{accountId:3,amount:42,id:7,version:0},{accountId:4,amount:160,id:8,version:0}]},"
				+ "{tags:[],id:9,type:\"TRANSFER\",description:\"test transaction 3\",date:\"2014-02-17\",version:0,components:[]},"
				+ "{tags:[\"hello\",\"magic\"],id:10,type:\"EXPENSEINCOME\",description:\"test transaction 2\",date:\"2015-01-07\",version:0,components:[{accountId:4,amount:-3.14,id:11,version:0},{accountId:3,amount:2.72,id:12,version:0}]}"
				+ "]", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user can get their transactions, sorted by
	 * description descending
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTransactionsDescriptionDesc() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions?sortColumn=DESCRIPTION&sortDirection=DESC", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("["
				+ "{tags:[],id:9,type:\"TRANSFER\",description:\"test transaction 3\",date:\"2014-02-17\",version:0,components:[]},"
				+ "{tags:[\"hello\",\"magic\"],id:10,type:\"EXPENSEINCOME\",description:\"test transaction 2\",date:\"2015-01-07\",version:0,components:[{accountId:4,amount:-3.14,id:11,version:0},{accountId:3,amount:2.72,id:12,version:0}]},"
				+ "{tags:[\"hello\",\"world\"],id:6,type:\"EXPENSEINCOME\",description:\"test transaction 1\",date:\"2014-02-17\",version:0,components:[{accountId:3,amount:42,id:7,version:0},{accountId:4,amount:160,id:8,version:0}]}"
				+ "]", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user can get their transactions, sorted by
	 * description ascending
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTransactionsDescriptionAsc() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions?sortColumn=DESCRIPTION&sortDirection=ASC", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("["
				+ "{tags:[\"hello\",\"world\"],id:6,type:\"EXPENSEINCOME\",description:\"test transaction 1\",date:\"2014-02-17\",version:0,components:[{accountId:3,amount:42,id:7,version:0},{accountId:4,amount:160,id:8,version:0}]},"
				+ "{tags:[\"hello\",\"magic\"],id:10,type:\"EXPENSEINCOME\",description:\"test transaction 2\",date:\"2015-01-07\",version:0,components:[{accountId:4,amount:-3.14,id:11,version:0},{accountId:3,amount:2.72,id:12,version:0}]},"
				+ "{tags:[],id:9,type:\"TRANSFER\",description:\"test transaction 3\",date:\"2014-02-17\",version:0,components:[]}"
				+ "]", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user can get transactions for an existing date
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTransactionsExistingDate() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions?filterDate=2014-02-17", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("["
				+ "{tags:[\"hello\",\"world\"],id:6,type:\"EXPENSEINCOME\",description:\"test transaction 1\",date:\"2014-02-17\",version:0,components:[{accountId:3,amount:42,id:7,version:0},{accountId:4,amount:160,id:8,version:0}]},"
				+ "{tags:[],id:9,type:\"TRANSFER\",description:\"test transaction 3\",date:\"2014-02-17\",version:0,components:[]}"
				+ "]", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user will get an empty transaction list for a
	 * non-existing date
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTransactionsNonExistingDate() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions?filterDate=1970-01-01", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("[]", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user can get transactions for an existing tag
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTransactionsExistingTag() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions?filterTags=hello,world", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("["
				+ "{tags:[\"hello\",\"world\"],id:6,type:\"EXPENSEINCOME\",description:\"test transaction 1\",date:\"2014-02-17\",version:0,components:[{accountId:3,amount:42,id:7,version:0},{accountId:4,amount:160,id:8,version:0}]},"
				+ "{tags:[\"hello\",\"magic\"],id:10,type:\"EXPENSEINCOME\",description:\"test transaction 2\",date:\"2015-01-07\",version:0,components:[{accountId:4,amount:-3.14,id:11,version:0},{accountId:3,amount:2.72,id:12,version:0}]}"
				+ "]", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user will get an empty transaction list for a
	 * non-existing tag
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTransactionsNonExistingTag() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions?filterTags=unknown", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("[]", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user can get transactions for an existing
	 * description filter
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTransactionsExistingDescription() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions?filterDescription=%transaction 2%", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("["
				+ "{tags:[\"hello\",\"magic\"],id:10,type:\"EXPENSEINCOME\",description:\"test transaction 2\",date:\"2015-01-07\",version:0,components:[{accountId:4,amount:-3.14,id:11,version:0},{accountId:3,amount:2.72,id:12,version:0}]}"
				+ "]", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user will get an empty transaction list for a
	 * non-existing description filter
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTransactionsNonExistingDescription() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions?filterDescription=%transaction 4%", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("[]", responseEntity.getBody(), true);
	}

	/**
	 * Test that an authenticated user can get transactions with paging
	 *
	 * @throws Exception
	 */
	@Test
	public void testAllPages() throws Exception {
		prepopulate.prepopulate();

		VogonUser user01 = userRepository.findByUsernameIgnoreCase("user01");

		List<FinanceTransaction> addTransactions = new ArrayList<>();
		for (int i = 1; i < 250; i++) {
			FinanceTransaction generatedTransaction = new FinanceTransaction(user01, "page transaction " + i, null, new Date(), FinanceTransaction.Type.EXPENSEINCOME);
			addTransactions.add(generatedTransaction);
		}
		transactionRepository.saveAll(addTransactions);

		List<JSONObject> receivedTransactions = new ArrayList<>();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions?page=0", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		JSONArray responseObject = new JSONArray(responseEntity.getBody());
		assertEquals(100, responseObject.length());
		for (int i = 0; i < responseObject.length(); i++) {
			receivedTransactions.add(responseObject.getJSONObject(i));
		}

		responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions?page=1", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		responseObject = new JSONArray(responseEntity.getBody());
		assertEquals(100, responseObject.length());
		for (int i = 0; i < responseObject.length(); i++) {
			receivedTransactions.add(responseObject.getJSONObject(i));
		}

		responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions?page=2", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		responseObject = new JSONArray(responseEntity.getBody());
		assertEquals(52, responseObject.length());
		for (int i = 0; i < responseObject.length(); i++) {
			receivedTransactions.add(responseObject.getJSONObject(i));
		}

		assertEquals("test transaction 1", receivedTransactions.get(0).getString("description"));
		assertEquals("test transaction 3", receivedTransactions.get(1).getString("description"));
		assertEquals("test transaction 2", receivedTransactions.get(2).getString("description"));
		for (int i = 3; i < receivedTransactions.size(); i++) {
			assertEquals("page transaction " + (i - 2), receivedTransactions.get(i).getString("description"));
		}
	}

	/**
	 * Test that an authenticated user will get an empty transaction list for a
	 * non-existing page
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetNonExistingPage() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions?page=2", HttpMethod.GET, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		jsonExpectationhelper.assertJsonEqual("[]", responseEntity.getBody(), true);
	}

	/**
	 * Test that an unauthenticated user (no token) is not allowed to get
	 * transactions
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTransactionsNoToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.getDefaultHeaders();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions", HttpMethod.GET, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (bad token) is not allowed to get
	 * transactions
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTransactionsBadToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.badAuthenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions", HttpMethod.GET, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"invalid_token\",\"error_description\":\"Invalid access token: bad_token\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (no token) is not allowed to get a
	 * specific transaction
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTransactionNoToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.getDefaultHeaders();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions/transaction/6", HttpMethod.GET, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (bad token) is not allowed to get a
	 * specific transaction
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetTransactionBadToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.badAuthenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions/transaction/6", HttpMethod.GET, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"invalid_token\",\"error_description\":\"Invalid access token: bad_token\"}", ex.getResponseBodyAsString(), true);
		}
	}
}
