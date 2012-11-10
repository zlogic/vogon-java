/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zlogic.vogon.ui.adapter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Dmitry
 */
public interface AccountInterface {

	public StringProperty balanceProperty();

	public ObjectProperty<ObjectWithStatus<String, Boolean>> nameProperty();

	public ObjectProperty<ObjectWithStatus<CurrencyModelAdapter, Boolean>> currencyProperty();

	public ObjectProperty<ObjectWithStatus<BooleanProperty, Boolean>> includeInTotalProperty();
}
