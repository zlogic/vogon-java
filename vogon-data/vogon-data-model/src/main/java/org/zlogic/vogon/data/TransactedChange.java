/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import javax.persistence.EntityManager;

/**
 * Interface for allowing to perform a custom modification in an
 * EntityManager-managed transaction.
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public interface TransactedChange {

	/**
	 * Perform the entity modification. EntityManager is already initialized and
	 * the transaction is started by FInanceData.performTransactedChange(). Make
	 * sure to update existing entities from database with merge() before
	 * performing changes.
	 *
	 * @param entityManager the entity manager used for this change
	 */
	public void performChange(EntityManager entityManager);
}
