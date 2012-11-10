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
 *
 * @author Dmitry Zolotukhin
 */
public interface AccountInterface {

	public StringProperty balanceProperty();

	public ObjectProperty<ObjectWithStatus<String, Boolean>> nameProperty();

	public ObjectProperty<ObjectWithStatus<CurrencyModelAdapter, Boolean>> currencyProperty();

	public ObjectProperty<ObjectWithStatus<BooleanProperty, Boolean>> includeInTotalProperty();
}
