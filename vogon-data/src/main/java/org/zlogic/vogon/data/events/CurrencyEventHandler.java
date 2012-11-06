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
public interface CurrencyEventHandler {
	/**
	 * A currencies updated handler (all currencies have been updated)
	 */
	void currenciesUpdated();
}
