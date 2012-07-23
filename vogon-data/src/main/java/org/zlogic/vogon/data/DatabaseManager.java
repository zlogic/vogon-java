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
		entityManagerFactory = javax.persistence.Persistence.createEntityManagerFactory("VogonPU"); //$NON-NLS-1$
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
		synchronized (this) {
			return terminated ? null : entityManagerFactory;
		}
	}

	/**
	 * Returns a new entity manager for this package
	 *
	 * @return A new EntityManager for the persistence unit
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
			java.util.Map<String, Object> persistenceProperties = entityManager.getProperties();
			if (persistenceProperties.containsKey("javax.persistence.jdbc.driver")) { //$NON-NLS-1$
				Object jdbcDriverValue = persistenceProperties.get("javax.persistence.jdbc.driver"); //$NON-NLS-1$
				String jdbcDriverString = jdbcDriverValue.getClass() != String.class ? "" : (String) jdbcDriverValue; //$NON-NLS-1$
				if (jdbcDriverString.contains("org.apache.derby.jdbc.EmbeddedDriver")) //$NON-NLS-1$
					shutdownDerbyManually = true;
			}
			if (shutdownDerbyManually) {
				//Shutdown Derby
				try {
					java.sql.DriverManager.getConnection("jdbc:derby:;shutdown=true"); //$NON-NLS-1$
				} catch (java.sql.SQLException ex) {
					if (!ex.getSQLState().equals("XJ015")) { //$NON-NLS-1$
						Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
			getPersistenceUnit().close();
			terminated = true;
		}
	}
}
