/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
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
	public ObjectProperty<ObjectWithStatus<String, Boolean>> nameProperty();

	/**
	 * The account currency property
	 *
	 * @return the account currency property
	 */
	public ObjectProperty<ObjectWithStatus<CurrencyModelAdapter, Boolean>> currencyProperty();

	/**
	 * The property indicating if account should be included in total (reporting
	 * accounts)
	 *
	 * @return the property indicating if account should be included in total
	 */
	public ObjectProperty<ObjectWithStatus<BooleanProperty, Boolean>> includeInTotalProperty();
}
