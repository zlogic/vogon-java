/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.events;

/**
 * Interface for accepting Transaction events. If several listeners are used,
 * the implementation is responsible for distributing the event. Event handling
 * calls should be as short as possible and should not make calls which result
 * in database transactions.
 *
 * @author Dmitry Zolotukhin
 */
public interface TransactionEventHandler {

	/**
	 * A transaction created callback
	 *
	 * @param transactionId the transaction that was created
	 */
	void transactionCreated(long transactionId);

	/**
	 * A transaction updated callback
	 *
	 * @param transactionId the transaction that was updated
	 */
	void transactionUpdated(long transactionId);

	/**
	 * A transaction updated handler (transaction list has been updated)
	 */
	void transactionsUpdated();

	/**
	 * A transaction deleted callback
	 *
	 * @param transactionId the deleted transaction
	 */
	void transactionDeleted(long transactionId);
}
