/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.events;

import org.zlogic.vogon.data.FinanceAccount;

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
	 * @param newAccount the account that was created
	 */
	void accountCreated(FinanceAccount newAccount);

	/**
	 * An account updated callback
	 *
	 * @param updatedAccount the account that was updated
	 */
	void accountUpdated(FinanceAccount updatedAccount);

	/**
	 * An account updated handler (account list has been updated)
	 */
	void accountsUpdated();

	/**
	 * An account deleted callback
	 *
	 * @param deletedAccount the deleted account
	 */
	void accountDeleted(FinanceAccount deletedAccount);
}
