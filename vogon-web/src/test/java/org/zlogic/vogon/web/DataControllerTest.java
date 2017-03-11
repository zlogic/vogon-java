/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import javax.annotation.Resource;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.JsonExpectationsHelper;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.web.data.AccountRepository;
import org.zlogic.vogon.web.data.TransactionRepository;
import org.zlogic.vogon.web.data.UserRepository;

/**
 * Tests for Import/Export Data Controller
 * {@link org.zlogic.vogon.web.controller.DataController}
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = {Application.class, DatabaseConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DataControllerTest {

	private JsonExpectationsHelper jsonExpectationhelper = new JsonExpectationsHelper();

	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private RestTestClient restClient;

	@Autowired
	private Prepopupate prepopulate;

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
	 * Test that an authenticated user is allowed to export their data
	 *
	 * @throws Exception
	 */
	@Test
	public void testExportData() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));

		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<byte[]> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/export", HttpMethod.GET, entity, byte[].class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		String response = new String(responseEntity.getBody(), Charset.forName("utf-8"));

		jsonExpectationhelper.assertJsonEqual("{accounts:["
				+ "{id:1,name:\"test account 1\",currency:\"RUB\",balance:44.72,includeInTotal:true,showInList:true},"
				+ "{id:2,name:\"test account 2\",currency:\"EUR\",balance:156.86,includeInTotal:true,showInList:true}"
				+ "],transactions:["
				+ "{description:\"test transaction 1\",type:\"EXPENSEINCOME\",date:\"2014-02-17\",tags:[\"hello\",\"world\"],components:[{amount:42,accountId:1},{amount:160,accountId:2}]},"
				+ "{description:\"test transaction 3\",type:\"TRANSFER\",date:\"2014-02-17\",tags:[],components:[]},"
				+ "{description:\"test transaction 2\",type:\"EXPENSEINCOME\",date:\"2015-01-07\",tags:[\"hello\",\"magic\"],components:[{amount:-3.14,accountId:2},{amount:2.72,accountId:1}]}"
				+ "]}", response);
	}

	/**
	 * Test that an authenticated user with no accounts or transactions is
	 * allowed to export their data
	 *
	 * @throws Exception
	 */
	@Test
	public void testExportEmptyData() throws Exception {
		VogonUser user01 = new VogonUser("user01", passwordEncoder.encode("mypassword"));
		userRepository.save(user01);

		HttpHeaders headers = restClient.authenticate();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));

		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<byte[]> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/export", HttpMethod.GET, entity, byte[].class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		String response = new String(responseEntity.getBody(), Charset.forName("utf-8"));

		jsonExpectationhelper.assertJsonEqual("{accounts:[],transactions:[]}", response);
	}

	/**
	 * Test that an authenticated user with an empty account can import data
	 *
	 * @throws Exception
	 */
	@Test
	public void testImportData() throws Exception {
		prepopulate.prepopulate();

		userRepository.save(new VogonUser("user03", passwordEncoder.encode("mypassword3")));

		HttpHeaders headers = restClient.authenticate("user03", "mypassword3");
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
		String importData = "{\"accounts\":[ "
				+ "{\"id\":2,\"version\":1,\"name\":\"Orange Bank\",\"balance\":990.0,\"currency\":\"PLN\",\"includeInTotal\":true,\"showInList\":true},"
				+ "{\"id\":3,\"version\":1,\"name\":\"Green Bank\",\"balance\":900.0,\"currency\":\"ALL\",\"includeInTotal\":true,\"showInList\":false},"
				+ "{\"id\":4,\"version\":1,\"name\":\"Purple Bank\",\"balance\":800.0,\"currency\":\"ZWL\",\"includeInTotal\":false,\"showInList\":true},"
				+ "{\"id\":5,\"version\":1,\"name\":\"Magical Credit Card\",\"balance\":-80.0,\"currency\":\"PLN\",\"includeInTotal\":false,\"showInList\":false}"
				+ "],\"transactions\":["
				+ "{\"id\":6,\"version\":1,\"type\":\"EXPENSEINCOME\",\"description\":\"Widgets\",\"tags\":[\"Widgets\"],\"amount\":-100.0,\"date\":\"2015-11-02\",\"components\":[{\"id\":7,\"version\":0,\"amount\":-100.0,\"accountId\":3 }]},"
				+ "{\"id\":8,\"version\":1,\"type\":\"EXPENSEINCOME\",\"description\":\"Salary\",\"tags\":[\"Salary\"],\"amount\":3000.0,\"date\":\"2015-11-01\",\"components\":[{\"id\":9,\"version\":0,\"amount\":1000.0,\"accountId\":2},{\"id\":10,\"version\":0,\"amount\":1000.0,\"accountId\":3},{\"id\":11,\"version\":0,\"amount\":1000.0,\"accountId\":4}]},"
				+ "{\"id\":12,\"version\":1,\"type\":\"EXPENSEINCOME\",\"description\":\"Gadgets\",\"tags\":[\"Gadgets\"],\"amount\":-100.0,\"date\":\"2015-11-03\",\"components\":[{\"id\":13,\"version\":0,\"amount\":-100.0,\"accountId\":5}]},"
				+ "{\"id\":14,\"version\":1,\"type\":\"TRANSFER\",\"description\":\"Credit card payment\",\"tags\":[\"Credit\"],\"amount\":100.0,\"date\":\"2015-11-09\",\"components\":[{\"id\":15,\"version\":0,\"amount\":-100.0,\"accountId\":4},{\"id\":16,\"version\":0,\"amount\":20.0,\"accountId\":5}]},"
				+ "{\"id\":17,\"version\":1,\"type\":\"EXPENSEINCOME\",\"description\":\"Stuff\",\"tags\":[\"Widgets\",\"Gadgets\"],\"amount\":-110.0,\"date\":\"2015-11-07\",\"components\":[{\"id\":18,\"version\":0,\"amount\":-10.0,\"accountId\":2},{\"id\":19,\"version\":0,\"amount\":-100.0,\"accountId\":4}]}"
				+ "]}";
		bodyMap.add("file", new ByteArrayResource(importData.getBytes("utf-8")) {
			@Override
			public String getFilename() {
				return "vogon-export.json";
			}
		});
		HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(bodyMap, headers);

		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/import", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("true", responseEntity.getBody());

		transactionTemplate.execute((ts) -> {
			List<VogonUser> users = userRepository.findAll();
			assertEquals(3, users.size());
			VogonUser user01 = users.get(0);
			VogonUser user02 = users.get(1);
			VogonUser user03 = users.get(2);

			List<FinanceAccount> accounts = accountRepository.findAll();
			assertEquals(7, accounts.size());
			FinanceAccount account1 = accounts.get(0);
			FinanceAccount account2 = accounts.get(1);
			FinanceAccount account3 = accounts.get(2);
			FinanceAccount account4 = accounts.get(3);
			FinanceAccount account5 = accounts.get(4);
			FinanceAccount account6 = accounts.get(5);
			FinanceAccount account7 = accounts.get(6);
			assertEquals("test account 1", account1.getName());
			assertEquals(Currency.getInstance("RUB"), account1.getCurrency());
			assertEquals(true, account1.getIncludeInTotal());
			assertEquals(true, account1.getShowInList());
			assertEquals(44.72, account1.getBalance(), 0);
			assertEquals("test account 2", account2.getName());
			assertEquals(Currency.getInstance("EUR"), account2.getCurrency());
			assertEquals(true, account2.getIncludeInTotal());
			assertEquals(true, account2.getShowInList());
			assertEquals(156.86, account2.getBalance(), 0);
			assertEquals("test account 3", account3.getName());
			assertEquals(Currency.getInstance("RUB"), account3.getCurrency());
			assertEquals(true, account3.getIncludeInTotal());
			assertEquals(true, account3.getShowInList());
			assertEquals(100, account3.getBalance(), 0);
			assertEquals("Orange Bank", account4.getName());
			assertEquals(Currency.getInstance("PLN"), account4.getCurrency());
			assertEquals(true, account4.getIncludeInTotal());
			assertEquals(true, account4.getShowInList());
			assertEquals(990.0, account4.getBalance(), 0);
			assertEquals("Green Bank", account5.getName());
			assertEquals(Currency.getInstance("ALL"), account5.getCurrency());
			assertEquals(true, account5.getIncludeInTotal());
			assertEquals(false, account5.getShowInList());
			assertEquals(900.0, account5.getBalance(), 0);
			assertEquals("Purple Bank", account6.getName());
			assertEquals(Currency.getInstance("ZWL"), account6.getCurrency());
			assertEquals(false, account6.getIncludeInTotal());
			assertEquals(true, account6.getShowInList());
			assertEquals(800.0, account6.getBalance(), 0);
			assertEquals("Magical Credit Card", account7.getName());
			assertEquals(Currency.getInstance("PLN"), account7.getCurrency());
			assertEquals(false, account7.getIncludeInTotal());
			assertEquals(false, account7.getShowInList());
			assertEquals(-80, account7.getBalance(), 0);
			List<FinanceTransaction> transactions = transactionRepository.findAll();
			assertEquals(9, transactions.size());
			FinanceTransaction transaction1 = transactions.get(0);
			assertEquals(user01, transaction1.getOwner());
			assertEquals(Sets.newSet("hello", "world"), Sets.newSet(transaction1.getTags()));
			assertEquals(FinanceTransaction.Type.EXPENSEINCOME, transaction1.getType());
			assertEquals("test transaction 1", transaction1.getDescription());
			assertEquals(prepopulate.parseJSONDate("2014-02-17"), transaction1.getDate());
			assertEquals(0, transaction1.getVersion());
			assertEquals(2, transaction1.getComponents().size());
			TransactionComponent component11 = transaction1.getComponents().get(0);
			TransactionComponent component12 = transaction1.getComponents().get(1);
			assertEquals(account1, component11.getAccount());
			assertEquals(42, component11.getAmount(), 0);
			assertEquals(0, component11.getVersion());
			assertEquals(account2, component12.getAccount());
			assertEquals(160, component12.getAmount(), 0);
			assertEquals(0, component12.getVersion());
			FinanceTransaction transaction3 = transactions.get(1);
			assertEquals(user01, transaction3.getOwner());
			assertArrayEquals(new String[]{}, transaction3.getTags());
			assertEquals(FinanceTransaction.Type.TRANSFER, transaction3.getType());
			assertEquals("test transaction 3", transaction3.getDescription());
			assertEquals(prepopulate.parseJSONDate("2014-02-17"), transaction3.getDate());
			assertEquals(0, transaction3.getVersion());
			assertEquals(0, transaction3.getComponents().size());
			FinanceTransaction transaction2 = transactions.get(2);
			assertEquals(user01, transaction2.getOwner());
			assertEquals(Sets.newSet("hello", "magic"), Sets.newSet(transaction2.getTags()));
			assertEquals(FinanceTransaction.Type.EXPENSEINCOME, transaction2.getType());
			assertEquals("test transaction 2", transaction2.getDescription());
			assertEquals(prepopulate.parseJSONDate("2015-01-07"), transaction2.getDate());
			assertEquals(0, transaction2.getVersion());
			assertEquals(2, transaction2.getComponents().size());
			TransactionComponent component21 = transaction2.getComponents().get(0);
			TransactionComponent component22 = transaction2.getComponents().get(1);
			assertEquals(account2, component21.getAccount());
			assertEquals(-3.14, component21.getAmount(), 0);
			assertEquals(0, component21.getVersion());
			assertEquals(account1, component22.getAccount());
			assertEquals(2.72, component22.getAmount(), 0);
			assertEquals(0, component22.getVersion());
			FinanceTransaction transaction4 = transactions.get(3);
			assertEquals(user02, transaction4.getOwner());
			assertArrayEquals(new String[]{}, transaction4.getTags());
			assertEquals(FinanceTransaction.Type.EXPENSEINCOME, transaction4.getType());
			assertEquals("test transaction 3", transaction4.getDescription());
			assertEquals(prepopulate.parseJSONDate("2014-05-17"), transaction4.getDate());
			assertEquals(0, transaction4.getVersion());
			assertEquals(1, transaction4.getComponents().size());
			TransactionComponent component3 = transaction4.getComponents().get(0);
			assertEquals(account3, component3.getAccount());
			assertEquals(100, component3.getAmount(), 0);
			assertEquals(0, component3.getVersion());
			FinanceTransaction transaction5 = transactions.get(4);
			assertEquals(user03, transaction5.getOwner());
			assertEquals(Sets.newSet("Widgets"), Sets.newSet(transaction5.getTags()));
			assertEquals(FinanceTransaction.Type.EXPENSEINCOME, transaction5.getType());
			assertEquals("Widgets", transaction5.getDescription());
			assertEquals(prepopulate.parseJSONDate("2015-11-02"), transaction5.getDate());
			assertEquals(1, transaction5.getVersion());
			assertEquals(1, transaction5.getComponents().size());
			TransactionComponent component5 = transaction5.getComponents().get(0);
			assertEquals(account5, component5.getAccount());
			assertEquals(-100, component5.getAmount(), 0);
			assertEquals(0, component5.getVersion());
			FinanceTransaction transaction6 = transactions.get(5);
			assertEquals(user03, transaction6.getOwner());
			assertEquals(Sets.newSet("Salary"), Sets.newSet(transaction6.getTags()));
			assertEquals(FinanceTransaction.Type.EXPENSEINCOME, transaction6.getType());
			assertEquals("Salary", transaction6.getDescription());
			assertEquals(prepopulate.parseJSONDate("2015-11-01"), transaction6.getDate());
			assertEquals(1, transaction6.getVersion());
			assertEquals(3, transaction6.getComponents().size());
			TransactionComponent component61 = transaction6.getComponents().get(0);
			assertEquals(account4, component61.getAccount());
			assertEquals(1000, component61.getAmount(), 0);
			assertEquals(0, component61.getVersion());
			TransactionComponent component62 = transaction6.getComponents().get(1);
			assertEquals(account5, component62.getAccount());
			assertEquals(1000, component62.getAmount(), 0);
			assertEquals(0, component62.getVersion());
			TransactionComponent component63 = transaction6.getComponents().get(2);
			assertEquals(account6, component63.getAccount());
			assertEquals(1000, component63.getAmount(), 0);
			assertEquals(0, component63.getVersion());
			FinanceTransaction transaction7 = transactions.get(6);
			assertEquals(user03, transaction7.getOwner());
			assertEquals(Sets.newSet("Gadgets"), Sets.newSet(transaction7.getTags()));
			assertEquals(FinanceTransaction.Type.EXPENSEINCOME, transaction7.getType());
			assertEquals("Gadgets", transaction7.getDescription());
			assertEquals(prepopulate.parseJSONDate("2015-11-03"), transaction7.getDate());
			assertEquals(1, transaction7.getVersion());
			assertEquals(1, transaction7.getComponents().size());
			TransactionComponent component7 = transaction7.getComponents().get(0);
			assertEquals(account7, component7.getAccount());
			assertEquals(-100.0, component7.getAmount(), 0);
			assertEquals(0, component7.getVersion());
			FinanceTransaction transaction8 = transactions.get(7);
			assertEquals(user03, transaction8.getOwner());
			assertEquals(Sets.newSet("Credit"), Sets.newSet(transaction8.getTags()));
			assertEquals(FinanceTransaction.Type.TRANSFER, transaction8.getType());
			assertEquals("Credit card payment", transaction8.getDescription());
			assertEquals(prepopulate.parseJSONDate("2015-11-09"), transaction8.getDate());
			assertEquals(1, transaction8.getVersion());
			assertEquals(2, transaction8.getComponents().size());
			TransactionComponent component81 = transaction8.getComponents().get(0);
			assertEquals(account6, component81.getAccount());
			assertEquals(-100.0, component81.getAmount(), 0);
			assertEquals(0, component81.getVersion());
			TransactionComponent component82 = transaction8.getComponents().get(1);
			assertEquals(account7, component82.getAccount());
			assertEquals(20.0, component82.getAmount(), 0);
			assertEquals(0, component82.getVersion());
			FinanceTransaction transaction9 = transactions.get(8);
			assertEquals(user03, transaction9.getOwner());
			assertEquals(Sets.newSet("Widgets", "Gadgets"), Sets.newSet(transaction9.getTags()));
			assertEquals(FinanceTransaction.Type.EXPENSEINCOME, transaction9.getType());
			assertEquals("Stuff", transaction9.getDescription());
			assertEquals(prepopulate.parseJSONDate("2015-11-07"), transaction9.getDate());
			assertEquals(1, transaction9.getVersion());
			assertEquals(2, transaction9.getComponents().size());
			TransactionComponent component91 = transaction9.getComponents().get(0);
			assertEquals(account4, component91.getAccount());
			assertEquals(-10.0, component91.getAmount(), 0);
			assertEquals(0, component91.getVersion());
			TransactionComponent component92 = transaction9.getComponents().get(1);
			assertEquals(account6, component92.getAccount());
			assertEquals(-100.0, component92.getAmount(), 0);
			assertEquals(0, component92.getVersion());
			return null;
		});
	}

	/**
	 * Test that an unauthenticated user (no token) is not allowed to export
	 * data
	 *
	 * @throws Exception
	 */
	@Test
	public void testExportNoToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.getDefaultHeaders();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/export", HttpMethod.GET, entity, byte[].class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (bad token) is not allowed to export
	 * data
	 *
	 * @throws Exception
	 */
	@Test
	public void testExportBadToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.badAuthenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/export", HttpMethod.GET, entity, byte[].class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"invalid_token\",\"error_description\":\"Invalid access token: bad_token\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (bad access_token) is not allowed to
	 * export data
	 *
	 * @throws Exception
	 */
	@Test
	public void testExportBadAccessToken() throws Exception {
		prepopulate.prepopulate();

		HttpEntity<String> entity = new HttpEntity<>("access_token=aaaa");
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/export", HttpMethod.POST, entity, byte[].class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (no token) is not allowed to import
	 * data
	 *
	 * @throws Exception
	 */
	@Test
	public void testImportNoToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.getDefaultHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
		String importData = "{\"accounts\":[],\"transactions\":[]}";
		bodyMap.add("file", new ByteArrayResource(importData.getBytes("utf-8")) {
			@Override
			public String getFilename() {
				return "vogon-export.json";
			}
		});
		HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(bodyMap, headers);
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/service/import", entity, byte[].class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (bad token) is not allowed to import
	 * data
	 *
	 * @throws Exception
	 */
	@Test
	public void testImportBadToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.badAuthenticate();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
		String importData = "{\"accounts\":[],\"transactions\":[]}";
		bodyMap.add("file", new ByteArrayResource(importData.getBytes("utf-8")) {
			@Override
			public String getFilename() {
				return "vogon-export.json";
			}
		});
		HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(bodyMap, headers);
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/service/import", entity, byte[].class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"invalid_token\",\"error_description\":\"Invalid access token: bad_token\"}", ex.getResponseBodyAsString(), true);
		}
	}
}
