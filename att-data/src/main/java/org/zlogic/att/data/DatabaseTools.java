package org.zlogic.att.data;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.logging.Logger;

/**
 * Class with database-related helper tools.
 * <p/>
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 29.12.12
 * Time: 21:51
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseTools {
	private final static Logger log = Logger.getLogger(DatabaseTools.class.getName());
	private static DatabaseTools instance;
	protected EntityManagerFactory entityManagerFactory;

	public static DatabaseTools getInstance() {
		if (instance == null)
			instance = new DatabaseTools();
		return instance;
	}

	private DatabaseTools() {
		log.fine("Creating entityManagerFactory");
		entityManagerFactory = javax.persistence.Persistence.createEntityManagerFactory("AwesomeTimeTrackerPersistenceUnit");
	}

	public EntityManager createEntityManager() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		return entityManager;
	}
}
