/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.util.Currency;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for model constraints
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class ModelConstraintsTest {

	private EntityManagerFactory emf;
	private EntityManager entityManager;
	@Rule
	public final ExpectedException exception = ExpectedException.none();

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
	 * Test constraint on creating duplicate usernames in bulk
	 */
	@Test
	public void testBulkCreateDuplicateUsernameConstraint() {
		VogonUser user1 = new VogonUser(" User01 ", "password"); //NOI18N
		VogonUser user2 = new VogonUser("user01", "password"); //NOI18N

		entityManager.getTransaction().begin();
		entityManager.persist(user1);
		entityManager.persist(user2);
		exception.expect(RollbackException.class);
		entityManager.getTransaction().commit();
	}

	/**
	 * Test constraint on creating duplicate usernames in separate transactions
	 */
	@Test
	public void testCreateDuplicateUsernameConstraint() {
		VogonUser user1 = new VogonUser(" User01 ", "password"); //NOI18N
		VogonUser user2 = new VogonUser("user01", "password"); //NOI18N

		entityManager.getTransaction().begin();
		entityManager.persist(user1);
		entityManager.getTransaction().commit();

		entityManager.getTransaction().begin();
		entityManager.persist(user2);
		exception.expect(RollbackException.class);
		entityManager.getTransaction().commit();
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

		EntityManager entityManagerOther = emf.createEntityManager();
		FinanceTransaction transactionOther = entityManagerOther.find(FinanceTransaction.class, transaction.getId());
		TransactionComponent component2Other = entityManagerOther.find(TransactionComponent.class, component2.getId());

		entityManager.refresh(transaction);//This is a trick to update the components hashSet hashcode
		transaction.updateComponentRawAmount(component1, 314);
		transactionOther.updateComponentRawAmount(component2Other, 314);

		entityManager.getTransaction().begin();
		entityManagerOther.getTransaction().begin();
		entityManager.persist(transaction);
		entityManagerOther.getTransaction().commit();
		exception.expect(RollbackException.class);
		entityManager.getTransaction().commit();
	}

}
