/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/**
 * Test-specific database configuration to use an in-memory database and replace {@link org.zlogic.vogon.web.DatabaseConfiguration#entityManagerFactory()}
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Configuration
public class DatabaseConfiguration {
	/**
	 * Returns the JPA configuration properties map
	 *
	 * @return the JPA configuration properties map
	 */
	private static Map<String, Object> getJpaProperties() {
		Map<String, Object> jpaProperties = new HashMap<>();
		String dbConnectionURL = "jdbc:h2:mem:test"; //NOI18N
		jpaProperties.put("javax.persistence.jdbc.url", dbConnectionURL); //NOI18N
		jpaProperties.put("javax.persistence.jdbc.user", ""); //NOI18N
		jpaProperties.put("javax.persistence.jdbc.password", ""); //NOI18N
		jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect"); //NOI18N
		jpaProperties.put("hibernate.connection.driver_class", "org.h2.Driver"); //NOI18N
		return jpaProperties;
	}
	/**
	 * Returns the test-specific LocalContainerEntityManagerFactoryBean
	 * @return the test-specific LocalContainerEntityManagerFactoryBean
	 */
	@Bean
	@Primary
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(){
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactory.setPersistenceUnitName("VogonPU"); //NOI18N
		entityManagerFactory.getJpaPropertyMap().putAll(getJpaProperties());
		entityManagerFactory.setPersistenceXmlLocation("classpath:META-INF/persistence.xml"); //NOI18N
		entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		return entityManagerFactory;
	}
}
