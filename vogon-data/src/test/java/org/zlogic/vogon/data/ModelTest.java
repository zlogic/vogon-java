package org.zlogic.vogon.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.text.SimpleDateFormat;
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
	public void createUser() throws Exception {
		Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2016-01-02");
		VogonUser user = new VogonUser("user01", "password");
		FinanceAccount account = new FinanceAccount(user, "Account 1", Currency.getInstance("EUR"));
		FinanceTransaction transaction = new FinanceTransaction(user,"Transaction 1", new String[]{"Tag1", "Tag2"}, date, FinanceTransaction.Type.EXPENSEINCOME);
		TransactionComponent component = new TransactionComponent(account, transaction, 1000);
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
		assertEquals("Account 1", foundAccount.getName());
		assertEquals(Currency.getInstance("EUR"), foundAccount.getCurrency());
		FinanceTransaction foundTransaction = entityManager.find(FinanceTransaction.class, transaction.getId());
		assertEquals("Transaction 1", foundTransaction.getDescription());
		assertArrayEquals(new String[]{"Tag1", "Tag2"}, transaction.getTags());
		assertEquals(date, transaction.getDate());
		assertEquals(1, transaction.getComponents().size());
		assertEquals(account.getId(), transaction.getComponents().get(0).getAccount().getId());
		assertEquals(Long.valueOf(1000), transaction.getComponents().get(0).getRawAmount());
	}

}