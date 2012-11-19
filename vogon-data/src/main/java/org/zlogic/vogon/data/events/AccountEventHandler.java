/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.events;

/**
 * Interface for accepting Account events. If several listeners are used, the
 * implementation is responsible for distributing the event. Event handling
 * calls should be as short as possible and should not make calls which result
 * in database transactions.
 *
 * @author Dmitry Zolotukhin
 */
public interface AccountEventHandler {

	/**
	 * An account created callback
	 *
	 * @param accountId the account that was created
	 */
	void accountCreated(long accountId);

	/**
	 * An account updated callback
	 *
	 * @param accountId the account that was updated
	 */
	void accountUpdated(long accountId);

	/**
	 * An account updated handler (account list has been updated)
	 */
	void accountsUpdated();

	/**
	 * An account deleted callback
	 *
	 * @param accountId the deleted account
	 */
	void accountDeleted(long accountId);
}
