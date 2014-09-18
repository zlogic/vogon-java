/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import java.util.Currency;
import java.util.Objects;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javax.persistence.EntityManager;
import org.zlogic.vogon.data.CurrencyRate;
import org.zlogic.vogon.data.standalone.TransactedChange;

/**
 * Class for storing a currency rate with property change detection.
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class CurrencyRateModelAdapter {

	/**
	 * The rate
	 */
	protected CurrencyRate rate;
	/**
	 * The DataManager instance
	 */
	protected DataManager dataManager;
	/**
	 * The currency rate value property
	 */
	private final DoubleProperty value = new SimpleDoubleProperty();
	/**
	 * Listener for changes of value (saves to database)
	 */
	private ChangeListener<Number> valueListener = new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
			if (oldValue.equals(newValue))
				return;
			dataManager.getFinanceData().performTransactedChange(new TransactedChange() {
				private double value;

				public TransactedChange setValue(double value) {
					this.value = value;
					return this;
				}

				@Override
				public void performChange(EntityManager entityManager) {
					rate.setExchangeRate(value);
					entityManager.merge(rate);
				}
			}.setValue(newValue.doubleValue()));
			updateFxProperties();
			dataManager.refreshAccounts();
			dataManager.updateTransactionsFxProperties();
		}
	};

	/**
	 * Default constructor
	 *
	 * @param rate the currency rate for this item
	 * @param dataManager the DataManager instance
	 */
	public CurrencyRateModelAdapter(CurrencyRate rate, DataManager dataManager) {
		this.rate = rate;
		this.dataManager = dataManager;
		updateFxProperties();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CurrencyRateModelAdapter)
			return rate.equals(((CurrencyRateModelAdapter) obj).rate) && value.get() == ((CurrencyRateModelAdapter) obj).value.get();
		if (obj instanceof CurrencyRate)
			return rate.equals((CurrencyRate) obj);
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 71 * hash + Objects.hashCode(this.rate);
		return hash;
	}

	/**
	 * Returns the source currency
	 *
	 * @return the source currency
	 */
	public Currency getSourceCurrency() {
		return rate.getSource();
	}

	/**
	 * Returns the destination currency
	 *
	 * @return the destination currency
	 */
	public Currency getDestinationCurrency() {
		return rate.getDestination();
	}

	/**
	 * Returns the rate property (changes are applied to FinanceData
	 * automatically)
	 *
	 * @return the rate property
	 */
	public DoubleProperty exchangeRateProperty() {
		return value;
	}

	/**
	 * Updates the properties from the current currency rate, causing
	 * ChangeListeners to trigger.
	 */
	private void updateFxProperties() {
		//Remove property change listeners
		value.removeListener(valueListener);
		value.set(rate.getExchangeRate());
		//Restore property change listeners
		value.addListener(valueListener);
	}
}
