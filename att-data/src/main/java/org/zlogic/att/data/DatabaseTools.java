/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data;

import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * Class with database-related helper tools.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
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
