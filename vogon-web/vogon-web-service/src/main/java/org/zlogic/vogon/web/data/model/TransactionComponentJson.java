/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.zlogic.vogon.data.Constants;
import org.zlogic.vogon.data.TransactionComponent;

/**
 * JSON wrapper for FinanceTransaction class
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@JsonIgnoreProperties(value = {"transaction", "account", "rawAmount"})
public class TransactionComponentJson extends TransactionComponent {

	/**
	 * The associated account id
	 */
	private Long accountId;

	/**
	 * Sets the associated account id
	 *
	 * @param accountId the associated account id to set
	 */
	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	/**
	 * Default constructor
	 */
	protected TransactionComponentJson() {
		super();
	}

	/**
	 * Creates a TransactionComponentJson wrapper based on a
	 * TransactionComponent instance
	 *
	 * @param component the wrapped TransactionComponent instance
	 */
	public TransactionComponentJson(TransactionComponent component) {
		this.id = component.getId();
		this.amount = component.getRawAmount();
		this.transaction = component.getTransaction();
		this.account = component.getAccount();
		this.accountId = component.getAccount().getId();
		this.setVersion(component.getVersion());
	}

	/**
	 * Returns the associated account class id
	 *
	 * @return the associated account class id (or null if no account is set)
	 */
	public Long getAccountId() {
		if (account != null)
			return account.getId();
		else
			return accountId;
	}

	/**
	 * Sets the TransactionComponent id
	 *
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Sets the component's amount
	 *
	 * @param amount the amount to set
	 */
	public void setAmount(double amount) {
		this.setRawAmount(Math.round(amount * Constants.RAW_AMOUNT_MULTIPLIER));
	}
}
