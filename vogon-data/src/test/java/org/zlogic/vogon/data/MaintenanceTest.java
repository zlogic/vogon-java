/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.util.Currency;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaQuery;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.zlogic.vogon.data.tools.DatabaseMaintenance;

/**
 * Tests for database maintenance features
 * {@link org.zlogic.vogon.data.tools.DatabaseMaintenance}
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class MaintenanceTest {

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
	 * Test that maintenance deletes orphaned objects
	 */
	@Test
	public void deleteOrphanedItemsTest() {
		Date date = TestUtils.parseJSONDate("2016-01-02"); //NOI18N
		VogonUser user = new VogonUser("user01", "password"); //NOI18N
		FinanceAccount account = new FinanceAccount(user, "test account 1", Currency.getInstance("RUB")); //NOI18N
		FinanceTransaction transaction = new FinanceTransaction(user, "test transaction 1", null, date, FinanceTransaction.Type.EXPENSEINCOME); //NOI18N
		TransactionComponent component1 = new TransactionComponent(account, transaction, 42);
		TransactionComponent component2 = new TransactionComponent(account, transaction, 160);
		transaction.addComponent(component1);
		transaction.addComponent(component2);

		entityManager.getTransaction().begin();
		entityManager.persist(user);
		entityManager.persist(account);
		entityManager.persist(component1);
		entityManager.persist(component2);
		entityManager.persist(transaction);
		entityManager.getTransaction().commit();

		FinanceAccount orphanedAccount = new FinanceAccount(null, "orphaned account 1", Currency.getInstance("RUB")); //NOI18N
		FinanceTransaction orphanedTransaction = new FinanceTransaction(null, "orphaned transaction 1", null, date, FinanceTransaction.Type.EXPENSEINCOME); //NOI18N
		TransactionComponent orphanedComponent1 = new TransactionComponent(orphanedAccount, orphanedTransaction, 9);
		TransactionComponent orphanedComponent2 = new TransactionComponent(orphanedAccount, orphanedTransaction, 27);
		TransactionComponent orphanedComponent3 = new TransactionComponent(null, null, 314);
		TransactionComponent orphanedComponent4 = new TransactionComponent(account, null, 7);
		orphanedTransaction.addComponent(orphanedComponent1);
		orphanedTransaction.addComponent(orphanedComponent2);

		entityManager.getTransaction().begin();
		entityManager.persist(orphanedAccount);
		entityManager.persist(orphanedComponent1);
		entityManager.persist(orphanedComponent2);
		entityManager.persist(orphanedComponent3);
		entityManager.persist(orphanedComponent4);
		entityManager.persist(orphanedTransaction);
		entityManager.getTransaction().commit();

		CriteriaQuery<FinanceAccount> accountQuery = entityManager.getCriteriaBuilder().createQuery(FinanceAccount.class);
		CriteriaQuery<FinanceTransaction> transactionQuery = entityManager.getCriteriaBuilder().createQuery(FinanceTransaction.class);
		CriteriaQuery<TransactionComponent> transactionComponentQuery = entityManager.getCriteriaBuilder().createQuery(TransactionComponent.class);

		accountQuery.from(FinanceAccount.class);
		transactionQuery.from(FinanceTransaction.class);
		transactionComponentQuery.from(TransactionComponent.class);
		List<FinanceAccount> accounts = entityManager.createQuery(accountQuery).getResultList();
		List<FinanceTransaction> transactions = entityManager.createQuery(transactionQuery).getResultList();
		List<TransactionComponent> transactionComponents = entityManager.createQuery(transactionComponentQuery).getResultList();

		assertEquals(2, accounts.size());
		assertEquals(2, transactions.size());
		assertEquals(6, transactionComponents.size());
		assertThat(accounts, IsCollectionContaining.hasItem(orphanedAccount));
		assertThat(transactions, IsCollectionContaining.hasItem(orphanedTransaction));
		assertThat(transactionComponents, IsCollectionContaining.hasItems(orphanedComponent1, orphanedComponent2, orphanedComponent3, orphanedComponent4));

		entityManager.getTransaction().begin();
		new DatabaseMaintenance().cleanup(entityManager);
		entityManager.getTransaction().commit();

		accounts = entityManager.createQuery(accountQuery).getResultList();
		transactions = entityManager.createQuery(transactionQuery).getResultList();
		transactionComponents = entityManager.createQuery(transactionComponentQuery).getResultList();

		assertEquals(2, accounts.size());
		assertEquals(1, transactions.size());
		assertEquals(2, transactionComponents.size());
		assertThat(accounts, IsCollectionContaining.hasItem(account));
		assertThat(transactions, IsCollectionContaining.hasItem(transaction));
		assertThat(transactionComponents, IsCollectionContaining.hasItems(component1, component2));
	}
	
	/**
	 * Test that maintenance recalculates account balance
	 */
	@Test
	public void recalculateBalanceTest() {
		Date date = TestUtils.parseJSONDate("2016-01-02"); //NOI18N
		VogonUser user = new VogonUser("user01", "password"); //NOI18N
		FinanceAccount account = new FinanceAccount(user, "test account 1", Currency.getInstance("RUB")); //NOI18N
		FinanceTransaction transaction = new FinanceTransaction(user, "test transaction 1", null, date, FinanceTransaction.Type.EXPENSEINCOME); //NOI18N
		TransactionComponent component1 = new TransactionComponent(account, transaction, 42);
		TransactionComponent component2 = new TransactionComponent(account, transaction, 160);
		transaction.addComponent(component1);
		transaction.addComponent(component2);

		entityManager.getTransaction().begin();
		entityManager.persist(user);
		entityManager.persist(account);
		entityManager.persist(component1);
		entityManager.persist(component2);
		entityManager.persist(transaction);
		entityManager.getTransaction().commit();

		account.balance = 11L;

		entityManager.getTransaction().begin();
		entityManager.persist(account);
		entityManager.getTransaction().commit();

		FinanceAccount foundAccount = entityManager.find(FinanceAccount.class, account.getId());

		assertEquals(11, foundAccount.getRawBalance());
		
		entityManager.getTransaction().begin();
		new DatabaseMaintenance().refreshAccountBalance(account, entityManager);
		entityManager.getTransaction().commit();
		
		entityManager.refresh(foundAccount);
		
		assertEquals(42 + 160, foundAccount.getRawBalance());
	}
}
