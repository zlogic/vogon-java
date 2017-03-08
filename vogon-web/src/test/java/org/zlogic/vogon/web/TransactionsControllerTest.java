/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.util.List;
import java.util.ResourceBundle;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.JsonExpectationsHelper;
import org.springframework.transaction.support.TransactionTemplate;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.web.data.AccountRepository;
import org.zlogic.vogon.web.data.TransactionRepository;
import org.zlogic.vogon.web.data.UserRepository;

/**
 * Tests for Transactions Controller
 * {@link org.zlogic.vogon.web.controller.AccountsController}
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = {Application.class, DatabaseConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TransactionsControllerTest {

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
}
