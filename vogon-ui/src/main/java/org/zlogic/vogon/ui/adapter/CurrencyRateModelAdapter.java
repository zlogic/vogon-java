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
import org.zlogic.vogon.data.FinanceData;

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
	 * The FinanceData instance
	 */
	protected FinanceData financeData;
	/**
	 * The currency rate value property
	 */
	private final DoubleProperty value = new SimpleDoubleProperty();

	/**
	 * Default constructor
	 *
	 * @param rate the currency rate for this item
	 * @param financeData the FinanceData instance
	 */
	public CurrencyRateModelAdapter(CurrencyRate rate, FinanceData financeData) {
		this.rate = rate;
		updateProperties();

		//Add change listener
		value.addListener(new ChangeListener<Number>() {
			protected FinanceData financeData;
			protected CurrencyRate rate;

			public ChangeListener<Number> setData(CurrencyRate rate, FinanceData financeData) {
				this.rate = rate;
				this.financeData = financeData;
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				if (rate.getExchangeRate() != t1.doubleValue())
					financeData.setExchangeRate(rate, t1.doubleValue());
			}
		}.setData(rate, financeData));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CurrencyRateModelAdapter)
			return rate.equals(((CurrencyRateModelAdapter) obj).rate) && value.equals(((CurrencyRateModelAdapter) obj).value);
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
	 * @return
	 */
	public Currency getSourceCurrency() {
		return rate.getSource();
	}

	/**
	 * Returns the destination currency
	 *
	 * @return
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
