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
 * Class for storing account data
 *
 * @author Dmitry Zolotukhin
 */
@Entity
public class FinanceAccount implements Serializable {
	/**
	 * Version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The account ID (only for persistence)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	protected long id;
	/**
	 * The account name
	 */
	protected String name;

	/**
	 * The account balance
	 */
	protected Long balance;

	/**
	 * The account currency
	 */
	protected String currency;

	/**
	 * Creates an account
	 */
	public FinanceAccount() {
	}

	/**
	 * Creates an account
	 *
	 * @param name The account name
	 * @param currency The account currency
	 */
	public FinanceAccount(String name,Currency currency) {
		this.name = name;
		this.balance = 0L;
		this.currency = (currency!=null?currency:Currency.getInstance(Locale.getDefault())).getCurrencyCode();
	}

	/**
	 * Returns the raw balance (should be divided by 100 to get the real amount)
	 * 
	 * @return the raw balance
	 */
	public long getRawBalance(){
		return balance;
	}

	/**
	 * Updates the raw balance by adding a value
	 * 
	 * @param addAmount the amount to add (can be added)
	 */
	public void updateRawBalance(long addAmount){
		synchronized(this){
			balance += addAmount;
		}
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the account name
	 *
	 * @return The account name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the account name
	 *
	 * @param name The account name
	 */
	public void setName(String name) {
		if(name.isEmpty())
			return;
		this.name = name;
	}

	/**
	 * Returns the account currency
	 *
	 * @return The account currency
	 */
	public Currency getCurrency() {
		Currency currency = Currency.getInstance(this.currency);
		return currency!=null?currency:Currency.getInstance(Locale.getDefault());
	}

	/**
	 * Sets the account currency
	 *
	 * @param currency The account currency
	 */
	public void setCurrency(Currency currency) {
		if(currency==null)
			return;
		this.currency = currency.getCurrencyCode();
	}

	/**
	 * Returns the balance as double
	 * 
	 * @return the balance
	 */
	public double getBalance(){
		return balance/100.0D;
	}
}
