/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for basic model operations
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class ModelBasicOperationsTest {

	private EntityManagerFactory emf;
	private EntityManager entityManager;

	@Before
	public void setUp() throws Exception {
		emf = Persistence.createEntityManagerFactory("VogonPU", TestUtils.getJpaProperties()); //NOI18N
		entityManager = emf.createEntityManager();
	}

	@After
	public void tearDown() throws Exception {
		entityManager.close();
		entityManager = null;
		emf.close();
		emf = null;
	}

	/**
	 * Create a new user, account and transaction with one component
	 */
	@Test
	public void createUserWithData() {
		Date date = TestUtils.parseJSONDate("2016-01-02"); //NOI18N
		VogonUser user = new VogonUser(" User01 ", "password"); //NOI18N
		FinanceAccount account = new FinanceAccount(user, "test account 1", Currency.getInstance("RUB")); //NOI18N
		FinanceTransaction transaction = new FinanceTransaction(user, "test transaction 1", new String[]{"hello", "world"}, date, FinanceTransaction.Type.EXPENSEINCOME); //NOI18N
		TransactionComponent component = new TransactionComponent(account, transaction, 3);
		transaction.addComponent(component);

		entityManager.getTransaction().begin();
		entityManager.persist(user);
		entityManager.persist(account);
		entityManager.persist(component);
		entityManager.persist(transaction);
		entityManager.getTransaction().commit();

		VogonUser foundUser = entityManager.find(VogonUser.class, user.getId());
		assertEquals("user01", foundUser.getUsername()); //NOI18N
		assertEquals("password", foundUser.getPassword()); //NOI18N
		FinanceAccount foundAccount = entityManager.find(FinanceAccount.class, account.getId());
		assertEquals("test account 1", foundAccount.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount.getCurrency()); //NOI18N
		assertEquals(3, foundAccount.getRawBalance());
		assertTrue(foundAccount.getIncludeInTotal());
		assertTrue(foundAccount.getShowInList());
		FinanceTransaction foundTransaction = entityManager.find(FinanceTransaction.class, transaction.getId());
		assertEquals("test transaction 1", foundTransaction.getDescription()); //NOI18N
		String[] tags = foundTransaction.getTags();
		Arrays.sort(tags);
		assertArrayEquals(new String[]{"hello", "world"}, tags); //NOI18N
		assertEquals(date, foundTransaction.getDate());
		assertEquals(1, foundTransaction.getComponents().size());
		assertEquals(account.getId(), foundTransaction.getComponents().get(0).getAccount().getId());
		assertEquals(Long.valueOf(3), foundTransaction.getComponents().get(0).getRawAmount());
	}

	/**
	 * Add a new transaction to an existing user account
	 */
	@Test
	public void addTransaction() {
		Date date = TestUtils.parseJSONDate("2016-01-02"); //NOI18N
		VogonUser user = new VogonUser("user01", "password"); //NOI18N
		FinanceAccount account1 = new FinanceAccount(user, "test account 1", Currency.getInstance("RUB")); //NOI18N
		FinanceAccount account2 = new FinanceAccount(user, "test account 2", Currency.getInstance("RUB")); //NOI18N

		entityManager.getTransaction().begin();
		entityManager.persist(user);
		entityManager.persist(account1);
		entityManager.persist(account2);
		entityManager.getTransaction().commit();

		FinanceTransaction transaction = new FinanceTransaction(user, "test transaction 1", null, date, FinanceTransaction.Type.EXPENSEINCOME); //NOI18N
		TransactionComponent component = new TransactionComponent(account1, transaction, 42);
		transaction.addComponent(component);

		entityManager.getTransaction().begin();
		entityManager.persist(component);
		entityManager.persist(transaction);
		entityManager.getTransaction().commit();

		FinanceAccount foundAccount1 = entityManager.find(FinanceAccount.class, account1.getId());
		FinanceAccount foundAccount2 = entityManager.find(FinanceAccount.class, account2.getId());
		assertEquals("test account 1", foundAccount1.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount1.getCurrency()); //NOI18N
		assertEquals(42, foundAccount1.getRawBalance());
		assertTrue(foundAccount1.getIncludeInTotal());
		assertTrue(foundAccount1.getShowInList());
		assertEquals("test account 2", foundAccount2.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount2.getCurrency()); //NOI18N
		assertEquals(0, foundAccount2.getRawBalance());
		assertTrue(foundAccount2.getIncludeInTotal());
		assertTrue(foundAccount2.getShowInList());
		FinanceTransaction foundTransaction = entityManager.find(FinanceTransaction.class, transaction.getId());
		assertEquals("test transaction 1", foundTransaction.getDescription()); //NOI18N
		assertArrayEquals(new String[0], foundTransaction.getTags());
		assertEquals(date, foundTransaction.getDate());
		assertEquals(1, foundTransaction.getComponents().size());
		assertEquals(foundAccount1.getId(), foundTransaction.getComponents().get(0).getAccount().getId());
		assertEquals(Long.valueOf(42), foundTransaction.getComponents().get(0).getRawAmount());
	}

	/**
	 * Update the amount for an existing transaction component
	 */
	@Test
	public void updateTransactionComponentAmount() {
		Date date = TestUtils.parseJSONDate("2016-01-02"); //NOI18N
		VogonUser user = new VogonUser("user01", "password"); //NOI18N
		FinanceAccount account1 = new FinanceAccount(user, "test account 1", Currency.getInstance("RUB")); //NOI18N
		FinanceAccount account2 = new FinanceAccount(user, "test account 2", Currency.getInstance("RUB")); //NOI18N
		FinanceTransaction transaction = new FinanceTransaction(user, "test transaction 1", null, date, FinanceTransaction.Type.EXPENSEINCOME); //NOI18N
		TransactionComponent component = new TransactionComponent(account1, transaction, 42);
		transaction.addComponent(component);

		entityManager.getTransaction().begin();
		entityManager.persist(user);
		entityManager.persist(account1);
		entityManager.persist(account2);
		entityManager.persist(component);
		entityManager.persist(transaction);
		entityManager.getTransaction().commit();

		entityManager.refresh(transaction);//This is a trick to update the components hashSet hashcode
		transaction.updateComponentRawAmount(component, 50L);

		entityManager.getTransaction().begin();
		entityManager.persist(transaction);
		entityManager.getTransaction().commit();

		FinanceAccount foundAccount1 = entityManager.find(FinanceAccount.class, account1.getId());
		FinanceAccount foundAccount2 = entityManager.find(FinanceAccount.class, account2.getId());
		assertEquals("test account 1", foundAccount1.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount1.getCurrency()); //NOI18N
		assertEquals(50, foundAccount1.getRawBalance());
		assertTrue(foundAccount1.getIncludeInTotal());
		assertTrue(foundAccount1.getShowInList());
		assertEquals("test account 2", foundAccount2.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount2.getCurrency()); //NOI18N
		assertEquals(0, foundAccount2.getRawBalance());
		assertTrue(foundAccount2.getIncludeInTotal());
		assertTrue(foundAccount2.getShowInList());
		FinanceTransaction foundTransaction = entityManager.find(FinanceTransaction.class, transaction.getId());
		assertEquals("test transaction 1", foundTransaction.getDescription()); //NOI18N
		assertArrayEquals(new String[0], foundTransaction.getTags());
		assertEquals(date, foundTransaction.getDate());
		assertEquals(1, foundTransaction.getComponents().size());
		assertEquals(foundAccount1.getId(), foundTransaction.getComponents().get(0).getAccount().getId());
		assertEquals(Long.valueOf(50), foundTransaction.getComponents().get(0).getRawAmount());
	}

	/**
	 * Update the amount for an existing transaction
	 */
	@Test
	public void updateTransactionComponentAccount() {
		Date date = TestUtils.parseJSONDate("2016-01-02"); //NOI18N
		VogonUser user = new VogonUser("user01", "password"); //NOI18N
		FinanceAccount account1 = new FinanceAccount(user, "test account 1", Currency.getInstance("RUB")); //NOI18N
		FinanceAccount account2 = new FinanceAccount(user, "test account 2", Currency.getInstance("RUB")); //NOI18N
		FinanceTransaction transaction = new FinanceTransaction(user, "test transaction 1", null, date, FinanceTransaction.Type.EXPENSEINCOME); //NOI18N
		TransactionComponent component1 = new TransactionComponent(account1, transaction, 42);
		TransactionComponent component2 = new TransactionComponent(account1, transaction, 160);
		transaction.addComponent(component1);
		transaction.addComponent(component2);

		entityManager.getTransaction().begin();
		entityManager.persist(user);
		entityManager.persist(account1);
		entityManager.persist(account2);
		entityManager.persist(component1);
		entityManager.persist(component2);
		entityManager.persist(transaction);
		entityManager.getTransaction().commit();

		FinanceAccount foundAccount1 = entityManager.find(FinanceAccount.class, account1.getId());
		FinanceAccount foundAccount2 = entityManager.find(FinanceAccount.class, account2.getId());
		assertEquals("test account 1", foundAccount1.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount1.getCurrency()); //NOI18N
		assertEquals(42 + 160, foundAccount1.getRawBalance());
		assertTrue(foundAccount1.getIncludeInTotal());
		assertTrue(foundAccount1.getShowInList());
		assertEquals("test account 2", foundAccount2.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount2.getCurrency()); //NOI18N
		assertEquals(0, foundAccount2.getRawBalance());
		assertTrue(foundAccount2.getIncludeInTotal());
		assertTrue(foundAccount2.getShowInList());
		FinanceTransaction foundTransaction = entityManager.find(FinanceTransaction.class, transaction.getId());
		assertEquals("test transaction 1", foundTransaction.getDescription()); //NOI18N
		assertArrayEquals(new String[0], foundTransaction.getTags());
		assertEquals(date, foundTransaction.getDate());
		assertEquals(2, foundTransaction.getComponents().size());
		assertEquals(foundAccount1.getId(), foundTransaction.getComponents().get(0).getAccount().getId());
		assertEquals(foundAccount1.getId(), foundTransaction.getComponents().get(1).getAccount().getId());
		assertEquals(Long.valueOf(42), foundTransaction.getComponents().get(0).getRawAmount());
		assertEquals(Long.valueOf(160), foundTransaction.getComponents().get(1).getRawAmount());

		entityManager.refresh(transaction);//This is a trick to update the components hashSet hashcode
		transaction.updateComponentAccount(component2, account2);

		entityManager.getTransaction().begin();
		entityManager.persist(transaction);
		entityManager.getTransaction().commit();

		foundAccount1 = entityManager.find(FinanceAccount.class, account1.getId());
		foundAccount2 = entityManager.find(FinanceAccount.class, account2.getId());
		assertEquals("test account 1", foundAccount1.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount1.getCurrency()); //NOI18N
		assertEquals(42, foundAccount1.getRawBalance());
		assertTrue(foundAccount1.getIncludeInTotal());
		assertTrue(foundAccount1.getShowInList());
		assertEquals("test account 2", foundAccount2.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount2.getCurrency()); //NOI18N
		assertEquals(160, foundAccount2.getRawBalance());
		assertTrue(foundAccount2.getIncludeInTotal());
		assertTrue(foundAccount2.getShowInList());
		foundTransaction = entityManager.find(FinanceTransaction.class, transaction.getId());
		assertEquals("test transaction 1", foundTransaction.getDescription()); //NOI18N
		assertArrayEquals(new String[0], foundTransaction.getTags());
		assertEquals(date, foundTransaction.getDate());
		assertEquals(2, foundTransaction.getComponents().size());
		assertEquals(foundAccount1.getId(), foundTransaction.getComponents().get(0).getAccount().getId());
		assertEquals(foundAccount2.getId(), foundTransaction.getComponents().get(1).getAccount().getId());
		assertEquals(Long.valueOf(42), foundTransaction.getComponents().get(0).getRawAmount());
		assertEquals(Long.valueOf(160), foundTransaction.getComponents().get(1).getRawAmount());
	}

	/**
	 * Add a transaction component to an existing transaction
	 */
	@Test
	public void addTransactionComponent() {
		Date date = TestUtils.parseJSONDate("2016-01-02"); //NOI18N
		VogonUser user = new VogonUser("user01", "password"); //NOI18N
		FinanceAccount account1 = new FinanceAccount(user, "test account 1", Currency.getInstance("RUB")); //NOI18N
		FinanceAccount account2 = new FinanceAccount(user, "test account 2", Currency.getInstance("RUB")); //NOI18N
		FinanceTransaction transaction = new FinanceTransaction(user, "test transaction 1", null, date, FinanceTransaction.Type.EXPENSEINCOME); //NOI18N
		TransactionComponent component1 = new TransactionComponent(account1, transaction, 42);
		transaction.addComponent(component1);

		entityManager.getTransaction().begin();
		entityManager.persist(user);
		entityManager.persist(account1);
		entityManager.persist(account2);
		entityManager.persist(component1);
		entityManager.persist(transaction);
		entityManager.getTransaction().commit();

		FinanceAccount foundAccount1 = entityManager.find(FinanceAccount.class, account1.getId());
		FinanceAccount foundAccount2 = entityManager.find(FinanceAccount.class, account2.getId());
		assertEquals("test account 1", foundAccount1.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount1.getCurrency()); //NOI18N
		assertEquals(42, foundAccount1.getRawBalance());
		assertTrue(foundAccount1.getIncludeInTotal());
		assertTrue(foundAccount1.getShowInList());
		assertEquals("test account 2", foundAccount2.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount2.getCurrency()); //NOI18N
		assertEquals(0, foundAccount2.getRawBalance());
		assertTrue(foundAccount2.getIncludeInTotal());
		assertTrue(foundAccount2.getShowInList());
		FinanceTransaction foundTransaction = entityManager.find(FinanceTransaction.class, transaction.getId());
		assertEquals("test transaction 1", foundTransaction.getDescription()); //NOI18N
		assertArrayEquals(new String[0], foundTransaction.getTags());
		assertEquals(date, foundTransaction.getDate());
		assertEquals(1, foundTransaction.getComponents().size());
		assertEquals(foundAccount1.getId(), foundTransaction.getComponents().get(0).getAccount().getId());
		assertEquals(Long.valueOf(42), foundTransaction.getComponents().get(0).getRawAmount());

		entityManager.refresh(transaction);//This is a trick to update the components hashSet hashcode
		TransactionComponent component2 = new TransactionComponent(account1, transaction, 160);
		transaction.addComponent(component2);

		entityManager.getTransaction().begin();
		entityManager.persist(transaction);
		entityManager.getTransaction().commit();

		foundAccount1 = entityManager.find(FinanceAccount.class, account1.getId());
		foundAccount2 = entityManager.find(FinanceAccount.class, account2.getId());
		assertEquals("test account 1", foundAccount1.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount1.getCurrency()); //NOI18N
		assertEquals(42 + 160, foundAccount1.getRawBalance());
		assertTrue(foundAccount1.getIncludeInTotal());
		assertTrue(foundAccount1.getShowInList());
		assertEquals("test account 2", foundAccount2.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount2.getCurrency()); //NOI18N
		assertEquals(0, foundAccount2.getRawBalance());
		assertTrue(foundAccount2.getIncludeInTotal());
		assertTrue(foundAccount2.getShowInList());
		foundTransaction = entityManager.find(FinanceTransaction.class, transaction.getId());
		assertEquals("test transaction 1", foundTransaction.getDescription()); //NOI18N
		assertArrayEquals(new String[0], foundTransaction.getTags());
		assertEquals(date, foundTransaction.getDate());
		assertEquals(2, foundTransaction.getComponents().size());
		assertEquals(foundAccount1.getId(), foundTransaction.getComponents().get(0).getAccount().getId());
		assertEquals(foundAccount1.getId(), foundTransaction.getComponents().get(1).getAccount().getId());
		assertEquals(Long.valueOf(42), foundTransaction.getComponents().get(0).getRawAmount());
		assertEquals(Long.valueOf(160), foundTransaction.getComponents().get(1).getRawAmount());
	}

	/**
	 * Remove a transaction component from an existing transaction
	 */
	@Test
	public void deleteTransactionComponent() {
		Date date = TestUtils.parseJSONDate("2016-01-02"); //NOI18N
		VogonUser user = new VogonUser("user01", "password"); //NOI18N
		FinanceAccount account1 = new FinanceAccount(user, "test account 1", Currency.getInstance("RUB")); //NOI18N
		FinanceAccount account2 = new FinanceAccount(user, "test account 2", Currency.getInstance("RUB")); //NOI18N
		FinanceTransaction transaction = new FinanceTransaction(user, "test transaction 1", null, date, FinanceTransaction.Type.EXPENSEINCOME); //NOI18N
		TransactionComponent component1 = new TransactionComponent(account1, transaction, 42);
		TransactionComponent component2 = new TransactionComponent(account1, transaction, 160);
		transaction.addComponent(component1);
		transaction.addComponent(component2);

		entityManager.getTransaction().begin();
		entityManager.persist(user);
		entityManager.persist(account1);
		entityManager.persist(account2);
		entityManager.persist(component1);
		entityManager.persist(component2);
		entityManager.persist(transaction);
		entityManager.getTransaction().commit();

		FinanceAccount foundAccount1 = entityManager.find(FinanceAccount.class, account1.getId());
		FinanceAccount foundAccount2 = entityManager.find(FinanceAccount.class, account2.getId());
		assertEquals("test account 1", foundAccount1.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount1.getCurrency()); //NOI18N
		assertEquals(42 + 160, foundAccount1.getRawBalance());
		assertTrue(foundAccount1.getIncludeInTotal());
		assertTrue(foundAccount1.getShowInList());
		assertEquals("test account 2", foundAccount2.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount2.getCurrency()); //NOI18N
		assertEquals(0, foundAccount2.getRawBalance());
		assertTrue(foundAccount2.getIncludeInTotal());
		assertTrue(foundAccount2.getShowInList());
		FinanceTransaction foundTransaction = entityManager.find(FinanceTransaction.class, transaction.getId());
		assertEquals("test transaction 1", foundTransaction.getDescription()); //NOI18N
		assertArrayEquals(new String[0], foundTransaction.getTags());
		assertEquals(date, foundTransaction.getDate());
		assertEquals(2, foundTransaction.getComponents().size());
		assertEquals(foundAccount1.getId(), foundTransaction.getComponents().get(0).getAccount().getId());
		assertEquals(foundAccount1.getId(), foundTransaction.getComponents().get(1).getAccount().getId());
		assertEquals(Long.valueOf(42), foundTransaction.getComponents().get(0).getRawAmount());
		assertEquals(Long.valueOf(160), foundTransaction.getComponents().get(1).getRawAmount());

		entityManager.refresh(transaction);//This is a trick to update the components hashSet hashcode
		transaction.removeComponent(component2);

		entityManager.getTransaction().begin();
		entityManager.persist(transaction);
		entityManager.remove(component2);
		entityManager.getTransaction().commit();

		foundAccount1 = entityManager.find(FinanceAccount.class, account1.getId());
		foundAccount2 = entityManager.find(FinanceAccount.class, account2.getId());
		TransactionComponent foundComponent2 = entityManager.find(TransactionComponent.class, component2.getId());
		assertEquals("test account 1", foundAccount1.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount1.getCurrency()); //NOI18N
		assertEquals(42, foundAccount1.getRawBalance());
		assertTrue(foundAccount1.getIncludeInTotal());
		assertTrue(foundAccount1.getShowInList());
		assertEquals("test account 2", foundAccount2.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount2.getCurrency()); //NOI18N
		assertEquals(0, foundAccount2.getRawBalance());
		assertTrue(foundAccount2.getIncludeInTotal());
		assertTrue(foundAccount2.getShowInList());
		foundTransaction = entityManager.find(FinanceTransaction.class, transaction.getId());
		assertEquals("test transaction 1", foundTransaction.getDescription()); //NOI18N
		assertArrayEquals(new String[0], foundTransaction.getTags());
		assertEquals(date, foundTransaction.getDate());
		assertEquals(1, foundTransaction.getComponents().size());
		assertEquals(foundAccount1.getId(), foundTransaction.getComponents().get(0).getAccount().getId());
		assertEquals(Long.valueOf(42), foundTransaction.getComponents().get(0).getRawAmount());
		assertNull(foundComponent2);
	}

	/**
	 * Remove a transaction
	 */
	@Test
	public void deleteTransaction() {
		Date date = TestUtils.parseJSONDate("2016-01-02"); //NOI18N
		VogonUser user = new VogonUser("user01", "password"); //NOI18N
		FinanceAccount account1 = new FinanceAccount(user, "test account 1", Currency.getInstance("RUB")); //NOI18N
		FinanceAccount account2 = new FinanceAccount(user, "test account 2", Currency.getInstance("RUB")); //NOI18N
		FinanceTransaction transaction1 = new FinanceTransaction(user, "test transaction 1", null, date, FinanceTransaction.Type.EXPENSEINCOME); //NOI18N
		FinanceTransaction transaction2 = new FinanceTransaction(user, "test transaction 2", null, date, FinanceTransaction.Type.EXPENSEINCOME); //NOI18N
		TransactionComponent component11 = new TransactionComponent(account1, transaction1, 42);
		TransactionComponent component12 = new TransactionComponent(account2, transaction1, 160);
		TransactionComponent component21 = new TransactionComponent(account1, transaction1, 7);
		TransactionComponent component22 = new TransactionComponent(account2, transaction1, 13);
		transaction1.addComponent(component11);
		transaction1.addComponent(component12);
		transaction2.addComponent(component21);
		transaction2.addComponent(component22);

		entityManager.getTransaction().begin();
		entityManager.persist(user);
		entityManager.persist(account1);
		entityManager.persist(account2);
		entityManager.persist(component11);
		entityManager.persist(component12);
		entityManager.persist(component21);
		entityManager.persist(component22);
		entityManager.persist(transaction1);
		entityManager.persist(transaction2);
		entityManager.getTransaction().commit();

		FinanceAccount foundAccount1 = entityManager.find(FinanceAccount.class, account1.getId());
		FinanceAccount foundAccount2 = entityManager.find(FinanceAccount.class, account2.getId());
		assertEquals("test account 1", foundAccount1.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount1.getCurrency()); //NOI18N
		assertEquals(42 + 7, foundAccount1.getRawBalance());
		assertTrue(foundAccount1.getIncludeInTotal());
		assertTrue(foundAccount1.getShowInList());
		assertEquals("test account 2", foundAccount2.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount2.getCurrency()); //NOI18N
		assertEquals(160 + 13, foundAccount2.getRawBalance());
		assertTrue(foundAccount2.getIncludeInTotal());
		assertTrue(foundAccount2.getShowInList());
		FinanceTransaction foundTransaction1 = entityManager.find(FinanceTransaction.class, transaction1.getId());
		FinanceTransaction foundTransaction2 = entityManager.find(FinanceTransaction.class, transaction2.getId());
		assertEquals("test transaction 1", foundTransaction1.getDescription()); //NOI18N
		assertArrayEquals(new String[0], foundTransaction1.getTags());
		assertEquals(date, foundTransaction1.getDate());
		assertEquals(2, transaction1.getComponents().size());
		assertEquals(foundAccount1.getId(), foundTransaction1.getComponents().get(0).getAccount().getId());
		assertEquals(foundAccount2.getId(), foundTransaction1.getComponents().get(1).getAccount().getId());
		assertEquals(Long.valueOf(42), foundTransaction1.getComponents().get(0).getRawAmount());
		assertEquals(Long.valueOf(160), foundTransaction1.getComponents().get(1).getRawAmount());
		assertEquals("test transaction 2", foundTransaction2.getDescription()); //NOI18N
		assertArrayEquals(new String[0], foundTransaction2.getTags());
		assertEquals(date, foundTransaction2.getDate());
		assertEquals(2, foundTransaction2.getComponents().size());
		assertEquals(foundAccount1.getId(), foundTransaction2.getComponents().get(0).getAccount().getId());
		assertEquals(foundAccount2.getId(), foundTransaction2.getComponents().get(1).getAccount().getId());
		assertEquals(Long.valueOf(7), foundTransaction2.getComponents().get(0).getRawAmount());
		assertEquals(Long.valueOf(13), foundTransaction2.getComponents().get(1).getRawAmount());

		transaction2.removeAllComponents();

		entityManager.getTransaction().begin();
		entityManager.persist(transaction2);
		entityManager.remove(transaction2);
		entityManager.getTransaction().commit();

		foundAccount1 = entityManager.find(FinanceAccount.class, account1.getId());
		foundAccount2 = entityManager.find(FinanceAccount.class, account2.getId());
		//TransactionComponent foundComponent2 = entityManager.find(TransactionComponent.class, component2.getId());
		assertEquals("test account 1", foundAccount1.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount1.getCurrency()); //NOI18N
		assertEquals(42, foundAccount1.getRawBalance());
		assertTrue(foundAccount1.getIncludeInTotal());
		assertTrue(foundAccount1.getShowInList());
		assertEquals("test account 2", foundAccount2.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount2.getCurrency()); //NOI18N
		assertEquals(160, foundAccount2.getRawBalance());
		assertTrue(foundAccount2.getIncludeInTotal());
		assertTrue(foundAccount2.getShowInList());
		foundTransaction1 = entityManager.find(FinanceTransaction.class, transaction1.getId());
		foundTransaction2 = entityManager.find(FinanceTransaction.class, transaction2.getId());
		assertEquals("test transaction 1", foundTransaction1.getDescription()); //NOI18N
		assertArrayEquals(new String[0], foundTransaction1.getTags());
		assertEquals(date, foundTransaction1.getDate());
		assertEquals(2, foundTransaction1.getComponents().size());
		assertEquals(foundAccount1.getId(), foundTransaction1.getComponents().get(0).getAccount().getId());
		assertEquals(foundAccount2.getId(), foundTransaction1.getComponents().get(1).getAccount().getId());
		assertEquals(Long.valueOf(42), foundTransaction1.getComponents().get(0).getRawAmount());
		assertNull(foundTransaction2);
	}

	/**
	 * Remove an account
	 */
	@Test
	public void deleteAccount() {
		Date date = TestUtils.parseJSONDate("2016-01-02"); //NOI18N
		VogonUser user = new VogonUser("user01", "password"); //NOI18N
		FinanceAccount account1 = new FinanceAccount(user, "test account 1", Currency.getInstance("RUB")); //NOI18N
		FinanceAccount account2 = new FinanceAccount(user, "test account 2", Currency.getInstance("RUB")); //NOI18N
		FinanceTransaction transaction1 = new FinanceTransaction(user, "test transaction 1", null, date, FinanceTransaction.Type.EXPENSEINCOME); //NOI18N
		FinanceTransaction transaction2 = new FinanceTransaction(user, "test transaction 2", null, date, FinanceTransaction.Type.EXPENSEINCOME); //NOI18N
		TransactionComponent component11 = new TransactionComponent(account1, transaction1, 42);
		TransactionComponent component12 = new TransactionComponent(account2, transaction1, 160);
		TransactionComponent component21 = new TransactionComponent(account1, transaction1, 7);
		TransactionComponent component22 = new TransactionComponent(account2, transaction1, 13);
		transaction1.addComponent(component11);
		transaction1.addComponent(component12);
		transaction2.addComponent(component21);
		transaction2.addComponent(component22);

		entityManager.getTransaction().begin();
		entityManager.persist(user);
		entityManager.persist(account1);
		entityManager.persist(account2);
		entityManager.persist(component11);
		entityManager.persist(component12);
		entityManager.persist(component21);
		entityManager.persist(component22);
		entityManager.persist(transaction1);
		entityManager.persist(transaction2);
		entityManager.getTransaction().commit();

		FinanceAccount foundAccount1 = entityManager.find(FinanceAccount.class, account1.getId());
		FinanceAccount foundAccount2 = entityManager.find(FinanceAccount.class, account2.getId());
		assertEquals("test account 1", foundAccount1.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount1.getCurrency()); //NOI18N
		assertEquals(42 + 7, foundAccount1.getRawBalance());
		assertTrue(foundAccount1.getIncludeInTotal());
		assertTrue(foundAccount1.getShowInList());
		assertEquals("test account 2", foundAccount2.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount2.getCurrency()); //NOI18N
		assertEquals(160 + 13, foundAccount2.getRawBalance());
		assertTrue(foundAccount2.getIncludeInTotal());
		assertTrue(foundAccount2.getShowInList());
		FinanceTransaction foundTransaction1 = entityManager.find(FinanceTransaction.class, transaction1.getId());
		FinanceTransaction foundTransaction2 = entityManager.find(FinanceTransaction.class, transaction2.getId());
		assertEquals("test transaction 1", foundTransaction1.getDescription()); //NOI18N
		assertArrayEquals(new String[0], foundTransaction1.getTags());
		assertEquals(date, foundTransaction1.getDate());
		assertEquals(2, transaction1.getComponents().size());
		assertEquals(foundAccount1.getId(), foundTransaction1.getComponents().get(0).getAccount().getId());
		assertEquals(foundAccount2.getId(), foundTransaction1.getComponents().get(1).getAccount().getId());
		assertEquals(Long.valueOf(42), foundTransaction1.getComponents().get(0).getRawAmount());
		assertEquals(Long.valueOf(160), foundTransaction1.getComponents().get(1).getRawAmount());
		assertEquals("test transaction 2", foundTransaction2.getDescription()); //NOI18N
		assertArrayEquals(new String[0], foundTransaction2.getTags());
		assertEquals(date, foundTransaction2.getDate());
		assertEquals(2, foundTransaction2.getComponents().size());
		assertEquals(foundAccount1.getId(), foundTransaction2.getComponents().get(0).getAccount().getId());
		assertEquals(foundAccount2.getId(), foundTransaction2.getComponents().get(1).getAccount().getId());
		assertEquals(Long.valueOf(7), foundTransaction2.getComponents().get(0).getRawAmount());
		assertEquals(Long.valueOf(13), foundTransaction2.getComponents().get(1).getRawAmount());

		//TODO: provide a more efficient way of doing this?
		entityManager.refresh(transaction1);//This is a trick to update the components hashSet hashcode
		entityManager.refresh(transaction2);//This is a trick to update the components hashSet hashcode
		transaction1.removeComponent(component12);
		transaction2.removeComponent(component22);

		entityManager.getTransaction().begin();
		entityManager.remove(component12);
		entityManager.remove(component22);
		entityManager.remove(account2);
		entityManager.getTransaction().commit();

		foundAccount1 = entityManager.find(FinanceAccount.class, account1.getId());
		foundAccount2 = entityManager.find(FinanceAccount.class, account2.getId());
		//TransactionComponent foundComponent2 = entityManager.find(TransactionComponent.class, component2.getId());
		assertEquals("test account 1", foundAccount1.getName()); //NOI18N
		assertEquals(Currency.getInstance("RUB"), foundAccount1.getCurrency()); //NOI18N
		assertEquals(42 + 7, foundAccount1.getRawBalance());
		assertTrue(foundAccount1.getIncludeInTotal());
		assertTrue(foundAccount1.getShowInList());
		foundTransaction1 = entityManager.find(FinanceTransaction.class, transaction1.getId());
		foundTransaction2 = entityManager.find(FinanceTransaction.class, transaction2.getId());
		assertEquals("test transaction 1", foundTransaction1.getDescription()); //NOI18N
		assertArrayEquals(new String[0], foundTransaction1.getTags());
		assertEquals(date, foundTransaction1.getDate());
		assertEquals(1, foundTransaction1.getComponents().size());
		assertEquals(foundAccount1.getId(), foundTransaction1.getComponents().get(0).getAccount().getId());
		assertEquals(Long.valueOf(42), foundTransaction1.getComponents().get(0).getRawAmount());
		assertEquals("test transaction 2", foundTransaction2.getDescription()); //NOI18N
		assertArrayEquals(new String[0], foundTransaction2.getTags());
		assertEquals(date, foundTransaction1.getDate());
		assertEquals(1, foundTransaction2.getComponents().size());
		assertEquals(foundAccount1.getId(), foundTransaction2.getComponents().get(0).getAccount().getId());
		assertEquals(Long.valueOf(7), foundTransaction2.getComponents().get(0).getRawAmount());
		assertNull(foundAccount2);
	}
}
