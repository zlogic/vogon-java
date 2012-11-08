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

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The rate
	 */
	protected CurrencyRate rate;
	protected FinanceData financeData;
	private final DoubleProperty value = new SimpleDoubleProperty();

	/**
	 * Default constructor
	 *
	 * @param rate the currency rate for this item
	 */
	public CurrencyRateModelAdapter(CurrencyRate rate, FinanceData financeData) {
		this.rate = rate;
		updateProperties();

		value.addListener(new ChangeListener<Double>() {
			protected FinanceData financeData;
			protected CurrencyRate rate;

			public ChangeListener setData(CurrencyRate rate, FinanceData financeData) {
				this.rate = rate;
				this.financeData = financeData;
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends Double> ov, Double t, Double t1) {
				if (rate.getExchangeRate() != t1)
					financeData.setExchangeRate(rate, t1);
			}
		}.setData(rate, financeData));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CurrencyRateModelAdapter)
			return rate.equals(((CurrencyRateModelAdapter) obj).rate);
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

	public Currency getSourceCurrency() {
		return rate.getSource();
	}

	public Currency getDestinationCurrency() {
		return rate.getDestination();
	}

	public DoubleProperty exchangeRateProperty() {
		return value;
	}

	private void updateProperties() {
		value.set(rate.getExchangeRate());
	}
}
