/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import java.util.Currency;
import java.util.Objects;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.zlogic.vogon.data.CurrencyRate;

/**
 * Class for storing a currency rate with property change detection.
 *
 * @author Dmitry Zolotukhin
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
	 * Default constructor
	 *
	 * @param rate the currency rate for this item
	 * @param dataManager the DataManager instance
	 */
	public CurrencyRateModelAdapter(CurrencyRate rate, DataManager dataManager) {
		this.rate = rate;
		updateProperties();

		//Add change listener
		value.addListener(new ChangeListener<Number>() {
			protected DataManager dataManager;
			protected CurrencyRate rate;

			public ChangeListener<Number> setData(CurrencyRate rate, DataManager dataManager) {
				this.rate = rate;
				this.dataManager = dataManager;
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				//FIXME URGENT
				/*
				 if (rate.getExchangeRate() != t1.doubleValue())
				 financeData.setExchangeRate(rate, t1.doubleValue());
				 */
			}
		}.setData(rate, dataManager));
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
	private void updateProperties() {
		value.set(rate.getExchangeRate());
	}
}
