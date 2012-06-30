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
import javax.persistence.ManyToOne;

/**
 * Implements a transaction (amount associated with a specific account)
 *
 * @author Dmitry Zolotukhin
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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected long id;
	/**
	 * The account
	 */
	@ManyToOne
	protected FinanceAccount account;
	/**
	 * The amount this component changes the account's balance
	 */
	protected Long amount;

	/**
	 * Default constructor for a transaction component
	 */
	public TransactionComponent() {
	}

	/**
	 * Constructor for a transaction component
	 *
	 * @param account The account
	 * @param amount The amount which this component modifies the account, can
	 * be both negative and positive
	 */
	public TransactionComponent(FinanceAccount account, long amount) {
		this.account = account;
		this.amount = amount;
	}

	/*
	 * Getters/setters
	 */
	/**
	 * @return the account
	 */
	public FinanceAccount getAccount() {
		return account;
	}

	/**
	 * Returns the raw amount (should be divided by 100 to get the real amount)
	 * 
	 * @return the raw amount
	 */
	public Long getRawAmount() {
		return amount;
	}

	/**
	 * Returns the real amount
	 * 
	 * @return the raw amount
	 */
	public double getAmount() {
		return amount/100.0D;
	}
}
