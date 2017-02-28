package org.zlogic.vogon.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ModelTest {

	private EntityManagerFactory emf;
	private EntityManager entityManager;

	private Map<String, Object> getJpaProperties() {
		Map<String, Object> jpaProperties = new HashMap<>();
		String dbConnectionURL = "jdbc:h2:mem:test"; //NOI18N
		jpaProperties.put("javax.persistence.jdbc.url", dbConnectionURL); //NOI18N
		jpaProperties.put("javax.persistence.jdbc.user", ""); //NOI18N
		jpaProperties.put("javax.persistence.jdbc.password", ""); //NOI18N
		jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect"); //NOI18N
		jpaProperties.put("hibernate.connection.driver_class", "org.h2.Driver"); //NOI18N
		return jpaProperties;
	}

	@Before
	public void setUp() throws Exception {
		emf = Persistence.createEntityManagerFactory("VogonPU", getJpaProperties()); //NOI18N
		entityManager = emf.createEntityManager();
	}

	@After
	public void tearDown() throws Exception {
		entityManager.close();
		entityManager = null;
		emf.close();
		emf = null;
	}

	@Test
	public void createUserWithData() throws Exception {
		Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2016-01-02");
		VogonUser user = new VogonUser(" User01 ", "password");
		FinanceAccount account = new FinanceAccount(user, "test account 1", Currency.getInstance("RUB"));
		FinanceTransaction transaction = new FinanceTransaction(user, "test transaction 1", new String[]{"hello", "world"}, date, FinanceTransaction.Type.EXPENSEINCOME);
		TransactionComponent component = new TransactionComponent(account, transaction, 3);
		transaction.addComponent(component);

		entityManager.getTransaction().begin();
		entityManager.persist(user);
		entityManager.persist(account);
		entityManager.persist(component);
		entityManager.persist(transaction);
		entityManager.getTransaction().commit();

		VogonUser foundUser = entityManager.find(VogonUser.class, user.getId());
		assertEquals("user01", foundUser.getUsername());
		assertEquals("password", foundUser.getPassword());
		FinanceAccount foundAccount = entityManager.find(FinanceAccount.class, account.getId());
		assertEquals("test account 1", foundAccount.getName());
		assertEquals(Currency.getInstance("RUB"), foundAccount.getCurrency());
		assertEquals(3, foundAccount.getRawBalance());
		assertTrue(account.getIncludeInTotal());
		assertTrue(account.getShowInList());
		FinanceTransaction foundTransaction = entityManager.find(FinanceTransaction.class, transaction.getId());
		assertEquals("test transaction 1", foundTransaction.getDescription());
		String[] tags = transaction.getTags();
		Arrays.sort(tags);
		assertArrayEquals(new String[]{"hello", "world"}, tags);
		assertEquals(date, transaction.getDate());
		assertEquals(1, transaction.getComponents().size());
		assertEquals(account.getId(), transaction.getComponents().get(0).getAccount().getId());
		assertEquals(Long.valueOf(3), transaction.getComponents().get(0).getRawAmount());
	}

}
