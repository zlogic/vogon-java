/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import java.util.Currency;
import java.util.Locale;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Class for storing a currency exchange rate
 * 
 * @author Dmitry Zolotukhin
 */
@Entity
public class CurrencyRate implements Serializable {
	/**
	 * Version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The currency rate ID (only for persistence)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	protected long id;

	/**
	 * The source currency
	 */
	String source;
	/**
	 * The converted/destination currency
	 */
	String destination;
	/**
	 * The source=>destination exchange rate
	 */
	double exchangeRate;
	/**
	 * Creates a currency exchange rate
	 */
	public CurrencyRate() {
	}
	/**
	 * Creates a currency exchange rate
	 * @param source the source currency
	 * @param destination the converted currency
	 * @param exchangeRate the from=>to conversion rate
	 */
	public CurrencyRate(Currency source, Currency destination,double exchangeRate) {
		this.source = source.getCurrencyCode();
		this.destination = destination.getCurrencyCode();
		this.exchangeRate = exchangeRate;
	}

	/**
	 * Converts an amount
	 * @param amount the amount to convert in source currency
	 * @return the amount, converted to destination currency
	 */
	double convert(long amount){
		return Math.round(amount*exchangeRate)/100.0;
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the source conversion currency
	 * 
	 * @return the source currency
	 */
	public Currency getSource() {
		Currency currency = Currency.getInstance(source);
		return currency!=null?currency:Currency.getInstance(Locale.getDefault());
	}
	/**
	 * Sets the source currency
	 * 
	 * @param source the source currency
	 */
	public void setSource(Currency source) {
		this.source = source.getCurrencyCode();
	}

	/**
	 * Returns the destination conversion currency
	 * 
	 * @return the destination currency
	 */
	public Currency getDestination() {
		Currency currency = Currency.getInstance(destination);
		return currency!=null?currency:Currency.getInstance(Locale.getDefault());
	}
	/**
	 * Sets the destination currency
	 * 
	 * @param destination the destination currency
	 */
	public void setDestination(Currency destination) {
		this.destination = destination.getCurrencyCode();
	}
	/**
	 * Returns the source=>destination conversion rate
	 * 
	 * @return the conversion rate
	 */
	public double getExchangeRate() {
		return exchangeRate;
	}
	/**
	 * Sets the source=>destination conversion rate
	 * 
	 * @param exchangeRate the conversion rate
	 */
	public void setExchangeRate(double exchangeRate) {
		this.exchangeRate = exchangeRate;
	}
}
