/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.net.URI;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Persistence/JPA configuration class
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Configuration
@EnableTransactionManagement
public class PersistenceConfiguration {

	/**
	 * The logger
	 */
	private final static Logger log = LoggerFactory.getLogger(PersistenceConfiguration.class);

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/web/messages");

	/**
	 * The ServerTypeDetector instance
	 */
	@Autowired
	private ServerTypeDetector serverTypeDetector;

	/**
	 * Returns the path to the H2 database
	 *
	 * @return the path to the H2 database
	 */
	protected String getH2DatabasePath() {
		if (serverTypeDetector.getCloudType() == ServerTypeDetector.CloudType.OPENSHIFT)
			return System.getenv("OPENSHIFT_DATA_DIR"); //NOI18N
		if (System.getenv("VOGON_DATABASE_DIR") != null) //NOI18N
			return System.getenv("VOGON_DATABASE_DIR"); //NOI18N
		if (System.getProperty("vogon.database.dir") != null) //NOI18N
			return System.getProperty("vogon.database.dir"); //NOI18N
		if (null != serverTypeDetector.getServerType())
			switch (serverTypeDetector.getServerType()) {
				case TOMCAT:
					return System.getProperty("catalina.home"); //NOI18N
				case WILDFLY:
					return System.getProperty("jboss.server.data.dir"); //NOI18N
				case JETTY:
					return System.getProperty("jetty.base"); //NOI18N
			}
		return System.getProperty("user.dir"); //NOI18N 
	}

	/**
	 * Returns the JPA configuration overrides for database configuration
	 *
	 * @return the map of JPA configuration variables to override for the
	 * database connection
	 */
	protected Map<String, Object> getDatabaseConfiguration() {
		Map<String, Object> jpaProperties = new HashMap<>();
		boolean fallback = true;
		if (serverTypeDetector.getServerType() != ServerTypeDetector.ServerType.WILDFLY)
			jpaProperties.put("hibernate.connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider"); //NOI18N
		if (serverTypeDetector.getDatabaseType() == ServerTypeDetector.DatabaseType.POSTGRESQL) {
			String dbURL = null;
			if (serverTypeDetector.getCloudType() == ServerTypeDetector.CloudType.HEROKU)
				dbURL = System.getenv("DATABASE_URL"); //NOI18N
			else if (serverTypeDetector.getCloudType() == ServerTypeDetector.CloudType.OPENSHIFT)
				dbURL = System.getenv("OPENSHIFT_POSTGRESQL_DB_URL") + "/" + System.getenv("OPENSHIFT_APP_NAME"); //NOI18N
			try {
				URI dbUri = new URI(dbURL);
				String dbConnectionURL = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath(); //NOI18N //NOI18N
				String[] usernamePassword = dbUri.getUserInfo().split(":", 2); //NOI18N
				jpaProperties.put("javax.persistence.jdbc.url", dbConnectionURL); //NOI18N
				jpaProperties.put("javax.persistence.jdbc.user", usernamePassword[0]); //NOI18N
				jpaProperties.put("javax.persistence.jdbc.password", usernamePassword[1]); //NOI18N
				jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"); //NOI18N
				jpaProperties.put("hibernate.connection.driver_class", "org.postgresql.Driver"); //NOI18N
				fallback = false;
			} catch (Exception ex) {
				log.error(messages.getString("ERROR_EXTRACTING_DATABASE_CONFIGURATION"), ex);
			}
		}
		if (serverTypeDetector.getDatabaseType() == ServerTypeDetector.DatabaseType.H2 || fallback) {
			String dbConnectionURL = MessageFormat.format("jdbc:h2:{0}/Vogon", new Object[]{getH2DatabasePath()}); //NOI18N
			jpaProperties.put("javax.persistence.jdbc.url", dbConnectionURL); //NOI18N
			jpaProperties.put("javax.persistence.jdbc.user", ""); //NOI18N
			jpaProperties.put("javax.persistence.jdbc.password", ""); //NOI18N
			jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect"); //NOI18N
			jpaProperties.put("hibernate.connection.driver_class", "org.h2.Driver"); //NOI18N
		}
		return jpaProperties;
	}

	/**
	 * Creates the entityManagerFactory
	 *
	 * @return the entityManagerFactory
	 */
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactory.setPersistenceUnitName("VogonPU"); //NOI18N
		entityManagerFactory.getJpaPropertyMap().putAll(getDatabaseConfiguration());
		entityManagerFactory.setPersistenceXmlLocation("classpath:META-INF/persistence.xml"); //NOI18N
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

	/**
	 * Unloads the loaded JDBC driver(s) to prevent memory leaks
	 */
	@PreDestroy
	public void unloadJDBCDriver() {
		log.info(messages.getString("UNLOADING_JDBC_DRIVERS"));
		for (Enumeration<Driver> drivers = DriverManager.getDrivers(); drivers.hasMoreElements();) {
			try {
				Driver driver = drivers.nextElement();
				if (driver.getClass().getClassLoader() == PersistenceConfiguration.class.getClassLoader()) {
					log.info(MessageFormat.format(messages.getString("UNLOADING_DRIVER"), new Object[]{driver}));
					DriverManager.deregisterDriver(driver);
				} else {
					log.debug(MessageFormat.format(messages.getString("SKIPPING_DRIVER"), new Object[]{driver}));
				}
			} catch (SQLException ex) {
				log.error(messages.getString("ERROR_UNLOADING_DRIVER"), ex);
			}
		}
	}
}
