/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

/**
 * Implements a transaction (amount associated with a specific account)
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Entity
public class TransactionComponent implements Serializable {

	/**
	 * Version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The transaction ID (only for persistence)
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
	 * The account
	 */
	@ManyToOne
	protected FinanceAccount account;
	/**
	 * The transaction
	 */
	@ManyToOne
	protected FinanceTransaction transaction;
	/**
	 * The amount this component changes the account's balance
	 */
	protected Long amount;

	/**
	 * Default constructor for a transaction component
	 */
	protected TransactionComponent() {
	}

	/**
	 * Constructor for a transaction component
	 *
	 * @param account the account
	 * @param transaction the transaction
	 * @param amount the amount which this component modifies the account, can
	 * be both negative and positive
	 */
	public TransactionComponent(FinanceAccount account, FinanceTransaction transaction, long amount) {
		this.account = account;
		this.transaction = transaction;
		this.amount = amount;
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the associated account
	 *
	 * @return the account
	 */
	public FinanceAccount getAccount() {
		return account;
	}

	/**
	 * Sets the associated account
	 *
	 * @param account the account to set
	 */
	public void setAccount(FinanceAccount account) {
		this.account = account;
	}

	/**
	 * Returns the associated transaction
	 *
	 * @return the transaction
	 */
	public FinanceTransaction getTransaction() {
		return transaction;
	}

	/**
	 * Sets the associated transaction
	 *
	 * @param transaction the account to set
	 */
	public void setTransaction(FinanceTransaction transaction) {
		this.transaction = transaction;
	}

	/**
	 * Returns the raw amount (should be divided by
	 * Constants.rawAmountMultiplier to get the real amount)
	 *
	 * @return the raw amount
	 */
	public Long getRawAmount() {
		return amount;
	}

	/**
	 * Sets a new raw amount (should be divided by Constants.rawAmountMultiplier
	 * to get the real amount) Also updates the account balance
	 *
	 * @param amount the new raw amount
	 */
	public void setRawAmount(long amount) {
		this.amount = amount;
	}

	/**
	 * Returns the real amount
	 *
	 * @return the raw amount
	 */
	public double getAmount() {
		return amount / Constants.RAW_AMOUNT_MULTIPLIER;
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
		if (obj instanceof TransactionComponent)
			return id != null ? id.equals(((TransactionComponent) obj).id) : false;
		else
			return this == obj;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + (int) (this.id ^ (this.id >>> 32));
		return hash;
	}
}
