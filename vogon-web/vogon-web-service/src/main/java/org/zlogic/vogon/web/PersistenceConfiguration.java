/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import javax.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Persitence/JPA configuration class
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Configuration
@EnableTransactionManagement
public class PersistenceConfiguration {

	/**
	 * Returns the path to the database
	 *
	 * @return the path to the database
	 */
	public String getDatabasePath() {
		return System.getProperty("jboss.server.data.dir", System.getProperty("catalina.home", System.getProperty("user.dir")));
	}

	/**
	 * Creates the entityManagerFactory
	 *
	 * @return the entityManagerFactory
	 */
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactory.setPersistenceUnitName("VogonPU");
		entityManagerFactory.getJpaPropertyMap().put("javax.persistence.jdbc.url", "jdbc:h2:" + getDatabasePath() + "/Vogon");
		entityManagerFactory.setPersistenceXmlLocation("classpath:META-INF/persistence.xml");
		return entityManagerFactory;
	}

	/**
	 * Creates the TransactionManager
	 *
	 * @param emf the EntityManagerFactory
	 * @return the TransactionManager
	 */
	@Bean
	public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf);
		return transactionManager;
	}
}
