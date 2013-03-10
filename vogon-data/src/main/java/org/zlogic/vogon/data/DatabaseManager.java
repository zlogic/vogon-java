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
	 * EntityManager factory instance
	 */
	protected javax.persistence.EntityManagerFactory entityManagerFactory;
	/**
	 * Singleton instance of this class
	 */
	static DatabaseManager instance = new DatabaseManager();

	/**
	 * Default constructor for DatabaseManager
	 */
	protected DatabaseManager() {
		entityManagerFactory = javax.persistence.Persistence.createEntityManagerFactory("VogonPU"); //NOI18N
	}

	/**
	 * Returns the singleton instance of DatabaseManager
	 *
	 * @return the instance of DatabaseManager
	 */
	public static DatabaseManager getInstance() {
		return instance;
	}

	/**
	 * Returns the persistence unit for this package
	 *
	 * @return the persistence unit's EntityManagerFactory
	 */
	public javax.persistence.EntityManagerFactory getPersistenceUnit() {
		synchronized (this) {
			return terminated ? null : entityManagerFactory;
		}
	}

	/**
	 * Returns a new entity manager for this package
	 *
	 * @return a new EntityManager for the persistence unit
	 */
	public javax.persistence.EntityManager createEntityManager() {
		synchronized (this) {
			return terminated ? null : entityManagerFactory.createEntityManager();
		}
	}

	/**
	 * Shuts down the database. Recommended for embedded Derby since it uses a
	 * non-standard shutdown sequence.
	 */
	public void shutdown() {
		synchronized (this) {
			if (terminated)
				return;
			//Check if DB is Derby
			boolean shutdownDerbyManually = false;
			java.util.Map<String, Object> persistenceProperties = entityManagerFactory.getProperties();
			if (persistenceProperties.containsKey("javax.persistence.jdbc.driver")) { //NOI18N
				Object jdbcDriverValue = persistenceProperties.get("javax.persistence.jdbc.driver"); //NOI18N
				String jdbcDriverString = jdbcDriverValue.getClass() != String.class ? "" : (String) jdbcDriverValue; //NOI18N
				if (jdbcDriverString.contains("org.apache.derby.jdbc.EmbeddedDriver")) //NOI18N
					shutdownDerbyManually = true;
			}
			if (shutdownDerbyManually) {
				//Shutdown Derby
				try {
					java.sql.DriverManager.getConnection("jdbc:derby:;shutdown=true"); //NOI18N
				} catch (java.sql.SQLException ex) {
					if (!ex.getSQLState().equals("XJ015")) { //NOI18N
						Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
			getPersistenceUnit().close();
			terminated = true;
		}
	}
}
