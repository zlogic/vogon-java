/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
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
import javax.persistence.ManyToOne;
import javax.persistence.Version;

/**
 * Class for storing account data
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
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
	protected Long id;
	/**
	 * JPA version
	 */
	@Version
	private long version = 0L;
	/**
	 * The account owner
	 */
	@ManyToOne
	protected VogonUser owner;
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
	 * If this account should be included in the total
	 */
	protected Boolean includeInTotal;

	/**
	 * Creates an account
	 */
	protected FinanceAccount() {
		includeInTotal = true;
	}

	/**
	 * Creates an account
	 *
	 * @param owner the account owner
	 * @param name the account name
	 * @param currency the account currency
	 */
	public FinanceAccount(VogonUser owner, String name, Currency currency) {
		includeInTotal = true;
		this.name = name;
		this.balance = 0L;
		this.currency = (currency != null ? currency : Currency.getInstance(Locale.getDefault())).getCurrencyCode();
		FinanceAccount.this.setOwner(owner);
	}

	/**
	 * Returns the raw balance (should be divided by
	 * Constants.rawAmountMultiplier to get the real amount)
	 *
	 * @return the raw balance
	 */
	public long getRawBalance() {
		return balance;
	}

	/**
	 * Updates the raw balance by adding a value
	 *
	 * @param addAmount the amount to add (can be added)
	 */
	public void updateRawBalance(long addAmount) {
		synchronized (this) {
			balance += addAmount;
		}
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the account name
	 *
	 * @return the account name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the account name
	 *
	 * @param name the account name
	 */
	public void setName(String name) {
		if (name.isEmpty())
			return;
		this.name = name;
	}

	/**
	 * Returns the account currency
	 *
	 * @return the account currency
	 */
	public Currency getCurrency() {
		Currency currencyInstance = Currency.getInstance(this.currency);
		return currencyInstance != null ? currencyInstance : Currency.getInstance(Locale.getDefault());
	}

	/**
	 * Sets the account currency
	 *
	 * @param currency the account currency
	 */
	public void setCurrency(Currency currency) {
		if (currency == null)
			return;
		this.currency = currency.getCurrencyCode();
	}

	/**
	 * Returns if this account should be included in the total for all accounts
	 *
	 * @return true if this account should be included in the total for all
	 * accounts
	 */
	public boolean getIncludeInTotal() {
		return includeInTotal == null ? true : includeInTotal;
	}

	/**
	 * Sets if this account should be included in the total for all accounts
	 *
	 * @param includeInTotal true if this account should be included in the
	 * total for all accounts
	 */
	public void setIncludeInTotal(boolean includeInTotal) {
		this.includeInTotal = includeInTotal;
	}

	/**
	 * Returns the transaction owner
	 *
	 * @return the transaction owner
	 */
	public VogonUser getOwner() {
		return owner;
	}

	/**
	 * Sets the transaction owner
	 *
	 * @param owner the owner to set
	 */
	public void setOwner(VogonUser owner) {
		this.owner = owner;
	}

	/**
	 * Returns the balance as double
	 *
	 * @return the balance
	 */
	public double getBalance() {
		return balance / Constants.rawAmountMultiplier;
	}

	/**
	 * Returns the ID for this class instance
	 *
	 * @return the ID for this class instance
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Returns the version for this class instance
	 *
	 * @return the version for this class instance
	 */
	public long getVersion() {
		return version;
	}

	/**
	 * Sets the version of this class instance
	 *
	 * @param version the version of this class instance
	 */
	protected void setVersion(long version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FinanceAccount)
			return id != null ? id.equals(((FinanceAccount) obj).id) : null;
		else
			return this == obj;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + (int) (this.id ^ (this.id >>> 32));
		return hash;
	}
}
