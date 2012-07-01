/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

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
	 * Synchronized object for access to account balance
	 */
	@Transient
	private final Boolean balanceLock = true;
	/**
	 * Creates an account
	 */
	public FinanceAccount() {
	}

	/**
	 * Creates an account
	 *
	 * @param name The account name
	 */
	public FinanceAccount(String name) {
		this.name = name;
		this.balance = 0L;
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
		synchronized(balanceLock){
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
	 * Returns the balance as double
	 * 
	 * @return the balance
	 */
	public double getBalance(){
		return balance/100.0D;
	}
}
