/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for managing database connections and other database-related functions
 *
 * @author Dmitry Zolotukhin
 */
public class DatabaseManager {

	/**
	 * True if this database manager is terminated
	 */
	private Boolean terminated = false;
	/**
	 * Locker object for terminated field
	 */
	final private Boolean terminatedMutex = true;
	
	/**
	 * EntityManager factory instance
	 */
	protected javax.persistence.EntityManagerFactory entityManagerFactory;
	
	/**
	 * EntityManager instance
	 */
	protected javax.persistence.EntityManager entityManager;
	
	/**
	 * Singleton instance of this class
	 */
	static DatabaseManager instance = new DatabaseManager();

	/**
	 * Default constructor for DatabaseManager
	 */
	protected DatabaseManager() {
		entityManagerFactory = javax.persistence.Persistence.createEntityManagerFactory("VogonPU");
		entityManager = entityManagerFactory.createEntityManager();
	}

	/**
	 * Returns the singleton instance of DatabaseManager
	 * 
	 * @return The instance of DatabaseManager
	 */
	public static DatabaseManager getInstance(){
		return instance;
	}
	
	/**
	 * Returns the persistence unit for this package
	 *
	 * @return The persistence unit's EntityManagerFactory
	 */
	public javax.persistence.EntityManagerFactory getPersistenceUnit() {
		synchronized (terminatedMutex) {
			return terminated ? null : entityManagerFactory;
		}
	}
	
	/**
	 * Returns the entity manager for this package
	 *
	 * @return The persistence unit's EntityManager instance
	 */
	public javax.persistence.EntityManager getEntityManager() {
		synchronized (terminatedMutex) {
			return terminated ? null : entityManager;
		}
	}

	/**
	 * Shuts down the database. Recommended for embedded Derby since it uses a
	 * non-standard shutdown sequence.
	 */
	public void shutdown() {
		synchronized (terminatedMutex) {
			if (terminated)
				return;
			//Check if DB is Derby
			boolean shutdownDerbyManually = false;
			java.util.Map<String, Object> persistenceProperties = entityManager.getProperties();
			if (persistenceProperties.containsKey("javax.persistence.jdbc.driver")) {
				Object jdbcDriverValue = persistenceProperties.get("javax.persistence.jdbc.driver");
				String jdbcDriverString = jdbcDriverValue.getClass() != String.class ? "" : (String) jdbcDriverValue;
				if (jdbcDriverString.contains("org.apache.derby.jdbc.EmbeddedDriver"))
					shutdownDerbyManually = true;
			}
			if (shutdownDerbyManually) {
				//Shutdown Derby
				try {
					java.sql.DriverManager.getConnection("jdbc:derby:;shutdown=true");
				} catch (java.sql.SQLException ex) {
					if (!ex.getSQLState().equals("XJ015")) {
						Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
			getPersistenceUnit().close();
			terminated = true;
		}
	}
}