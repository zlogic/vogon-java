package org.zlogic.att.data;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.logging.Logger;

/**
 * Class with database-related helper tools.
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 29.12.12
 * Time: 21:51
 */
public class DatabaseTools {
	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(DatabaseTools.class.getName());
	/**
	 * The singleton class instance
	 */
	private static DatabaseTools instance;
	/**
	 * The entity manager factory
	 */
	protected EntityManagerFactory entityManagerFactory;

	/**
	 * Returns the class instance
	 *
	 * @return the class instance
	 */
	public static DatabaseTools getInstance() {
		if (instance == null)
			instance = new DatabaseTools();
		return instance;
	}

	/**
	 * Private constructor for the singleton instance
	 */
	private DatabaseTools() {
		log.fine("Creating entityManagerFactory");
		entityManagerFactory = javax.persistence.Persistence.createEntityManagerFactory("AwesomeTimeTrackerPersistenceUnit");
	}

	/**
	 * Creates an entity manager instance
	 *
	 * @return the created entity manager instance
	 */
	public EntityManager createEntityManager() {
		return entityManagerFactory.createEntityManager();
	}
}
