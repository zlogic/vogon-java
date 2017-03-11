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
import javax.persistence.EntityManager;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
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
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.web.data.AccountRepository;
import org.zlogic.vogon.web.data.TransactionRepository;
import org.zlogic.vogon.web.data.UserRepository;

/**
 * Tests for Transactions Controller
 * {@link org.zlogic.vogon.web.controller.AccountsController} write/update
 * operations
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = {Application.class, DatabaseConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TransactionsControllerWriteTest {

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

	@Autowired
	private AccountRepository accountRepository;

	@Resource
	private TransactionTemplate transactionTemplate;

	@Resource
	private EntityManager entityManager;

	@Before
	public void before() {
		prepopulate.clear();
	}

	@After
	public void after() {
		prepopulate.clear();
	}

	private void validateDefaultTransactions() {
		transactionTemplate.execute((ts) -> {
			List<VogonUser> users = userRepository.findAll();
			VogonUser user01 = users.get(0);
			VogonUser user02 = users.get(1);
			List<FinanceAccount> accounts = accountRepository.findAll();
			FinanceAccount account1 = accounts.get(0);
			FinanceAccount account2 = accounts.get(1);
			FinanceAccount account3 = accounts.get(2);
			assertEquals(44.72, account1.getBalance(), 0);
			assertEquals(156.86, account2.getBalance(), 0);
			assertEquals(100, account3.getBalance(), 0);
			List<FinanceTransaction> transactions = transactionRepository.findAll();
			assertEquals(4, transactions.size());
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
			return null;
		});
	}

	/**
	 * Test that an authenticated user can change their transaction
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateTransaction() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		String changeRequest = "{\"tags\":[\"hello\",\"world\"],\"id\":6,\"type\":\"EXPENSEINCOME\",\"description\":\"test transaction 1a\",\"date\":\"2014-02-17\",\"version\":0,\"components\":[{\"accountId\":4,\"amount\":42,\"id\":7,\"version\":0},{\"accountId\":4,\"amount\":15}]}";
		HttpEntity<String> entity = new HttpEntity<>(changeRequest, headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/transactions", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

		String expectedResponse = "{tags:[\"hello\",\"world\"],id:6,type:\"EXPENSEINCOME\",description:\"test transaction 1a\",date:\"2014-02-17\",version:2,components:[{accountId:4,amount:42,id:7,version:1},{accountId:4,amount:15,id:15,version:0}]}";
		jsonExpectationhelper.assertJsonEqual(expectedResponse, responseEntity.getBody(), true);

		transactionTemplate.execute((ts) -> {
			List<VogonUser> users = userRepository.findAll();
			VogonUser user01 = users.get(0);
			VogonUser user02 = users.get(1);
			List<FinanceAccount> accounts = accountRepository.findAll();
			FinanceAccount account1 = accounts.get(0);
			FinanceAccount account2 = accounts.get(1);
			FinanceAccount account3 = accounts.get(2);
			assertEquals(2.72, account1.getBalance(), 0);
			assertEquals(53.86, account2.getBalance(), 0);
			assertEquals(100, account3.getBalance(), 0);
			List<FinanceTransaction> transactions = transactionRepository.findAll();
			assertEquals(4, transactions.size());
			FinanceTransaction transaction1 = transactions.get(0);
			assertEquals(user01, transaction1.getOwner());
			assertEquals(Sets.newSet("hello", "world"), Sets.newSet(transaction1.getTags()));
			assertEquals(FinanceTransaction.Type.EXPENSEINCOME, transaction1.getType());
			assertEquals("test transaction 1a", transaction1.getDescription());
			assertEquals(prepopulate.parseJSONDate("2014-02-17"), transaction1.getDate());
			assertEquals(2, transaction1.getVersion());
			assertEquals(2, transaction1.getComponents().size());
			TransactionComponent component11 = transaction1.getComponents().get(0);
			TransactionComponent component12 = transaction1.getComponents().get(1);
			assertEquals(account2, component11.getAccount());
			assertEquals(42, component11.getAmount(), 0);
			assertEquals(1, component11.getVersion());
			assertEquals(account2, component12.getAccount());
			assertEquals(15, component12.getAmount(), 0);
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
			return null;
		});
	}

	/**
	 * Test that an authenticated user can create a new transaction
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateTransaction() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		String changeRequest = "{\"tags\":[\"hello\",\"super\"],\"type\":\"EXPENSEINCOME\",\"description\":\"test transaction 4\",\"date\":\"2016-02-07\",\"components\":[{\"accountId\":3,\"amount\":100}]}";
		HttpEntity<String> entity = new HttpEntity<>(changeRequest, headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/transactions", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

		String expectedResponse = "{tags:[\"hello\",\"super\"],id:16,type:\"EXPENSEINCOME\",description:\"test transaction 4\",date:\"2016-02-07\",version:0,components:[{accountId:3,amount:100,id:15,version:1}]}";
		jsonExpectationhelper.assertJsonEqual(expectedResponse, responseEntity.getBody(), true);

		transactionTemplate.execute((ts) -> {
			List<VogonUser> users = userRepository.findAll();
			VogonUser user01 = users.get(0);
			VogonUser user02 = users.get(1);
			List<FinanceAccount> accounts = accountRepository.findAll();
			FinanceAccount account1 = accounts.get(0);
			FinanceAccount account2 = accounts.get(1);
			FinanceAccount account3 = accounts.get(2);
			assertEquals(144.72, account1.getBalance(), 0);
			assertEquals(156.86, account2.getBalance(), 0);
			assertEquals(100, account3.getBalance(), 0);
			List<FinanceTransaction> transactions = transactionRepository.findAll();
			assertEquals(5, transactions.size());
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
			assertEquals(user01, transaction5.getOwner());
			assertEquals(Sets.newSet("hello", "super"), Sets.newSet(transaction5.getTags()));
			assertEquals(FinanceTransaction.Type.EXPENSEINCOME, transaction5.getType());
			assertEquals("test transaction 4", transaction5.getDescription());
			assertEquals(prepopulate.parseJSONDate("2016-02-07"), transaction5.getDate());
			assertEquals(0, transaction5.getVersion());
			assertEquals(1, transaction5.getComponents().size());
			TransactionComponent component4 = transaction5.getComponents().get(0);
			assertEquals(account1, component4.getAccount());
			assertEquals(100, component4.getAmount(), 0);
			assertEquals(1, component4.getVersion());
			return null;
		});
	}

	/**
	 * Test that an authenticated user can delete all transaction components of
	 * a transaction
	 *
	 * @throws Exception
	 */
	@Test
	public void testDeleteTransactionComponents() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		String changeRequest = "{\"tags\":[\"hello\",\"world\"],\"id\":6,\"type\":\"EXPENSEINCOME\",\"description\":\"test transaction 1a\",\"date\":\"2014-02-17\",\"version\":0,\"components\":[]}";
		HttpEntity<String> entity = new HttpEntity<>(changeRequest, headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().postForEntity("https://localhost:8443/service/transactions", entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

		String expectedResponse = "{tags:[\"hello\",\"world\"],id:6,type:\"EXPENSEINCOME\",description:\"test transaction 1a\",date:\"2014-02-17\",version:1,components:[]}";
		jsonExpectationhelper.assertJsonEqual(expectedResponse, responseEntity.getBody(), true);

		transactionTemplate.execute((ts) -> {
			List<VogonUser> users = userRepository.findAll();
			VogonUser user01 = users.get(0);
			VogonUser user02 = users.get(1);
			List<FinanceAccount> accounts = accountRepository.findAll();
			FinanceAccount account1 = accounts.get(0);
			FinanceAccount account2 = accounts.get(1);
			FinanceAccount account3 = accounts.get(2);
			assertEquals(2.72, account1.getBalance(), 0);
			assertEquals(-3.14, account2.getBalance(), 0);
			assertEquals(100, account3.getBalance(), 0);
			List<FinanceTransaction> transactions = transactionRepository.findAll();
			assertEquals(4, transactions.size());
			FinanceTransaction transaction1 = transactions.get(0);
			assertEquals(user01, transaction1.getOwner());
			assertEquals(Sets.newSet("hello", "world"), Sets.newSet(transaction1.getTags()));
			assertEquals(FinanceTransaction.Type.EXPENSEINCOME, transaction1.getType());
			assertEquals("test transaction 1a", transaction1.getDescription());
			assertEquals(prepopulate.parseJSONDate("2014-02-17"), transaction1.getDate());
			assertEquals(1, transaction1.getVersion());
			assertEquals(0, transaction1.getComponents().size());
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
			return null;
		});
	}

	/**
	 * Test that an authenticated user can delete all transaction components of
	 * a transaction
	 *
	 * @throws Exception
	 */
	@Test
	public void testDeleteTransaction() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> responseEntity = restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions/transaction/6", HttpMethod.DELETE, entity, String.class);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

		String expectedResponse = "{tags:[\"hello\",\"world\"],id:6,type:\"EXPENSEINCOME\",description:\"test transaction 1\",date:\"2014-02-17\",version:0,components:[{accountId:3,amount:42,id:7,version:0},{accountId:4,amount:160,id:8,version:0}]}";
		jsonExpectationhelper.assertJsonEqual(expectedResponse, responseEntity.getBody(), true);

		transactionTemplate.execute((ts) -> {
			List<VogonUser> users = userRepository.findAll();
			VogonUser user01 = users.get(0);
			VogonUser user02 = users.get(1);
			List<FinanceAccount> accounts = accountRepository.findAll();
			FinanceAccount account1 = accounts.get(0);
			FinanceAccount account2 = accounts.get(1);
			FinanceAccount account3 = accounts.get(2);
			assertEquals(2.72, account1.getBalance(), 0);
			assertEquals(-3.14, account2.getBalance(), 0);
			assertEquals(100, account3.getBalance(), 0);
			List<FinanceTransaction> transactions = transactionRepository.findAll();
			assertEquals(3, transactions.size());
			FinanceTransaction transaction3 = transactions.get(0);
			assertEquals(user01, transaction3.getOwner());
			assertArrayEquals(new String[]{}, transaction3.getTags());
			assertEquals(FinanceTransaction.Type.TRANSFER, transaction3.getType());
			assertEquals("test transaction 3", transaction3.getDescription());
			assertEquals(prepopulate.parseJSONDate("2014-02-17"), transaction3.getDate());
			assertEquals(0, transaction3.getVersion());
			assertEquals(0, transaction3.getComponents().size());
			FinanceTransaction transaction2 = transactions.get(1);
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
			FinanceTransaction transaction4 = transactions.get(2);
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
			return null;
		});
	}

	/**
	 * Test that an authenticated user cannot change their transaction if the
	 * version numbers mismatch
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateTransactionVersionConstraint() throws Exception {
		prepopulate.prepopulate();

		for (FinanceTransaction transaction : transactionRepository.findAll()) {
			if (transaction.getId() == 6) {
				transaction.setTags(new String[]{"hello", "world"});
				transactionRepository.save(transaction);
			}
		}

		HttpHeaders headers = restClient.authenticate();

		String changeRequest = "{\"tags\":[\"hello\",\"world\"],\"id\":6,\"type\":\"EXPENSEINCOME\",\"description\":\"test transaction 1a\",\"date\":\"2014-02-17\",\"version\":0,\"components\":[{\"accountId\":4,\"amount\":42,\"id\":7,\"version\":0},{\"accountId\":4,\"amount\":15}]}";
		HttpEntity<String> entity = new HttpEntity<>(changeRequest, headers);
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/service/transactions", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpServerErrorException ex) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{message:\"" + transactionMessages.getString("TRANSACTION_WAS_ALREADY_UPDATED") + "\"}", ex.getResponseBodyAsString());
		}

		transactionTemplate.execute((ts) -> {
			List<VogonUser> users = userRepository.findAll();
			VogonUser user01 = users.get(0);
			VogonUser user02 = users.get(1);
			List<FinanceAccount> accounts = accountRepository.findAll();
			FinanceAccount account1 = accounts.get(0);
			FinanceAccount account2 = accounts.get(1);
			FinanceAccount account3 = accounts.get(2);
			assertEquals(44.72, account1.getBalance(), 0);
			assertEquals(156.86, account2.getBalance(), 0);
			assertEquals(100, account3.getBalance(), 0);
			List<FinanceTransaction> transactions = transactionRepository.findAll();
			assertEquals(4, transactions.size());
			FinanceTransaction transaction1 = transactions.get(0);
			assertEquals(user01, transaction1.getOwner());
			assertEquals(Sets.newSet("hello", "world"), Sets.newSet(transaction1.getTags()));
			assertEquals(FinanceTransaction.Type.EXPENSEINCOME, transaction1.getType());
			assertEquals("test transaction 1", transaction1.getDescription());
			assertEquals(prepopulate.parseJSONDate("2014-02-17"), transaction1.getDate());
			assertEquals(1, transaction1.getVersion());
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
			return null;
		});
	}

	/**
	 * Test that an authenticated user cannot change their transaction component
	 * if the version numbers mismatch
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateTransactionComponentVersionConstraint() throws Exception {
		prepopulate.prepopulate();

		transactionTemplate.execute((ts) -> {
			TransactionComponent component = entityManager.find(TransactionComponent.class, Long.valueOf(7));
			component.setRawAmount(component.getRawAmount() - 1);
			entityManager.persist(component);
			entityManager.flush();
			component.setRawAmount(component.getRawAmount() + 1);
			entityManager.persist(component);
			entityManager.flush();
			return null;
		});

		HttpHeaders headers = restClient.authenticate();

		String changeRequest = "{\"tags\":[\"hello\",\"world\"],\"id\":6,\"type\":\"EXPENSEINCOME\",\"description\":\"test transaction 1a\",\"date\":\"2014-02-17\",\"version\":0,\"components\":[{\"accountId\":4,\"amount\":42,\"id\":7,\"version\":0},{\"accountId\":4,\"amount\":15}]}";
		HttpEntity<String> entity = new HttpEntity<>(changeRequest, headers);
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/service/transactions", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpServerErrorException ex) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{message:\"" + transactionMessages.getString("TRANSACTION_WAS_ALREADY_UPDATED") + "\"}", ex.getResponseBodyAsString());
		}

		transactionTemplate.execute((ts) -> {
			List<VogonUser> users = userRepository.findAll();
			VogonUser user01 = users.get(0);
			VogonUser user02 = users.get(1);
			List<FinanceAccount> accounts = accountRepository.findAll();
			FinanceAccount account1 = accounts.get(0);
			FinanceAccount account2 = accounts.get(1);
			FinanceAccount account3 = accounts.get(2);
			assertEquals(44.72, account1.getBalance(), 0);
			assertEquals(156.86, account2.getBalance(), 0);
			assertEquals(100, account3.getBalance(), 0);
			List<FinanceTransaction> transactions = transactionRepository.findAll();
			assertEquals(4, transactions.size());
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
			assertEquals(2, component11.getVersion());
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
			return null;
		});
	}

	/**
	 * Test that an authenticated user cannot use an Account belonging to
	 * another user when updating a transaction
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateAccountOwnerConstraint() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		String changeRequest = "{\"tags\":[\"hello\",\"world\"],\"id\":6,\"type\":\"EXPENSEINCOME\",\"description\":\"test transaction 1a\",\"date\":\"2014-02-17\",\"version\":0,\"components\":[{\"accountId\":5,\"amount\":42,\"id\":7,\"version\":0},{\"accountId\":5,\"amount\":15}]}";
		HttpEntity<String> entity = new HttpEntity<>(changeRequest, headers);
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/service/transactions", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpServerErrorException ex) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{message:\"" + MessageFormat.format(messages.getString("CANNOT_SET_AN_INVALID_ACCOUNT_ID"), 5) + "\"}", ex.getResponseBodyAsString());
		}

		validateDefaultTransactions();
	}

	/**
	 * Test that an authenticated user cannot delete a transaction belonging to
	 * another user
	 *
	 * @throws Exception
	 */
	@Test
	public void testDeleteTransactionOwnerConstraint() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions/transaction/13", HttpMethod.DELETE, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpServerErrorException ex) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{message:\"" + MessageFormat.format(messages.getString("CANNOT_DELETE_A_NON_EXISTING_TRANSACTION"), 13) + "\"}", ex.getResponseBodyAsString());
		} catch (HttpClientErrorException ex) {
			System.out.println(ex.getResponseBodyAsString());
		}

		validateDefaultTransactions();
	}

	/**
	 * Test that an authenticated user cannot use a non-existing Account when
	 * updating a transaction
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateAccountExistsConstraint() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		String changeRequest = "{\"tags\":[\"hello\",\"world\"],\"id\":6,\"type\":\"EXPENSEINCOME\",\"description\":\"test transaction 1a\",\"date\":\"2014-02-17\",\"version\":0,\"components\":[{\"accountId\":160,\"amount\":42,\"id\":7,\"version\":0},{\"accountId\":160,\"amount\":15}]}";
		HttpEntity<String> entity = new HttpEntity<>(changeRequest, headers);
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/service/transactions", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpServerErrorException ex) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{message:\"" + MessageFormat.format(messages.getString("CANNOT_SET_AN_INVALID_ACCOUNT_ID"), 160) + "\"}", ex.getResponseBodyAsString());
		}

		validateDefaultTransactions();
	}

	/**
	 * Test that an authenticated user cannot delete a non-existing transaction
	 *
	 * @throws Exception
	 */
	@Test
	public void testDeleteTransactionExistsConstraint() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.authenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions/transaction/160", HttpMethod.DELETE, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpServerErrorException ex) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{message:\"" + MessageFormat.format(messages.getString("CANNOT_DELETE_A_NON_EXISTING_TRANSACTION"), 160) + "\"}", ex.getResponseBodyAsString());
		} catch (HttpClientErrorException ex) {
			System.out.println(ex.getResponseBodyAsString());
		}

		validateDefaultTransactions();
	}

	/**
	 * Test that an unauthenticated user (no token) is not allowed to change
	 * transactions
	 *
	 * @throws Exception
	 */
	@Test
	public void testChangeAccountsNoToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.getDefaultHeaders();

		String changeRequest = "{\"tags\":[\"hello\",\"world\"],\"id\":6,\"type\":\"EXPENSEINCOME\",\"description\":\"test transaction 1a\",\"date\":\"2014-02-17\",\"version\":0,\"components\":[{\"accountId\":4,\"amount\":42,\"id\":7,\"version\":0},{\"accountId\":4,\"amount\":15}]}";
		HttpEntity<String> entity = new HttpEntity<>(changeRequest, headers);
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/service/transactions", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}", ex.getResponseBodyAsString(), true);
		}

		validateDefaultTransactions();
	}

	/**
	 * Test that an unauthenticated user (bad token) is not allowed to change
	 * transactions
	 *
	 * @throws Exception
	 */
	@Test
	public void testChangeAccountsBadToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.badAuthenticate();

		String changeRequest = "{\"tags\":[\"hello\",\"world\"],\"id\":6,\"type\":\"EXPENSEINCOME\",\"description\":\"test transaction 1a\",\"date\":\"2014-02-17\",\"version\":0,\"components\":[{\"accountId\":4,\"amount\":42,\"id\":7,\"version\":0},{\"accountId\":4,\"amount\":15}]}";
		HttpEntity<String> entity = new HttpEntity<>(changeRequest, headers);
		try {
			restClient.getRestTemplate().postForEntity("https://localhost:8443/service/transactions", entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"invalid_token\",\"error_description\":\"Invalid access token: bad_token\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (no token) is not allowed to delete a
	 * specific transaction
	 *
	 * @throws Exception
	 */
	@Test
	public void testDeleteTransactionNoToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.getDefaultHeaders();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions/transaction/6", HttpMethod.DELETE, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}", ex.getResponseBodyAsString(), true);
		}
	}

	/**
	 * Test that an unauthenticated user (bad token) is not allowed to delete a
	 * specific transaction
	 *
	 * @throws Exception
	 */
	@Test
	public void testDeleteTransactionBadToken() throws Exception {
		prepopulate.prepopulate();

		HttpHeaders headers = restClient.badAuthenticate();

		HttpEntity<String> entity = new HttpEntity<>(headers);
		try {
			restClient.getRestTemplate().exchange("https://localhost:8443/service/transactions/transaction/6", HttpMethod.DELETE, entity, String.class);
			fail("Expected an HttpServerErrorException to be thrown");
		} catch (HttpStatusCodeException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			jsonExpectationhelper.assertJsonEqual("{\"error\":\"invalid_token\",\"error_description\":\"Invalid access token: bad_token\"}", ex.getResponseBodyAsString(), true);
		}
	}
}
