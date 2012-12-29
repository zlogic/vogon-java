package org.zlogic.att.data;

import javax.persistence.EntityManager;

/**
 * Interface for allowing to perform a custom modification in an EntityManager-managed transaction.
 * <p/>
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 29.12.12
 * Time: 23:21
 */
public interface TransactedChange {
	/**
	 * Perform the entity modification.
	 * EntityManager is already initialized and the transaction is started by PersistenceHelper.performTransactedChange().
	 * <p/>
	 * Make sure to update existing entities from database with merge() before performing changes.
	 *
	 * @param entityManager the entity manager used for this change
	 */
	public void performChange(EntityManager entityManager);
}
