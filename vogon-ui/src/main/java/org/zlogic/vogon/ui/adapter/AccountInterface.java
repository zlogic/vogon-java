/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

/**
 * Interface for representing an account. This is used for reporting (fictional)
 * accounts.
 *
 * @author Dmitry Zolotukhin
 */
public interface AccountInterface {

	/**
	 * The account balance property (as string)
	 *
	 * @return the account balance property (as string)
	 */
	public StringProperty balanceProperty();

	/**
	 * The account name property
	 *
	 * @return the account balance property
	 */
	public StringProperty nameProperty();

	/**
	 * The account currency property
	 *
	 * @return the account currency property
	 */
	public ObjectProperty<CurrencyModelAdapter> currencyProperty();

	/**
	 * The property indicating if account should be included in total (reporting
	 * accounts)
	 *
	 * @return the property indicating if account should be included in total
	 */
	public BooleanProperty includeInTotalProperty();
}
