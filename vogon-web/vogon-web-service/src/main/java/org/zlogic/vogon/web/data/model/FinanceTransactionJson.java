/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;

/**
 * JSON wrapper for FinanceTransaction class
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class FinanceTransactionJson extends FinanceTransaction {

	/**
	 * List of components mapped to TransactionComponentJson classes
	 */
	private List<TransactionComponentJson> componentsJson = new LinkedList<>();

	/**
	 * Default constructor
	 */
	protected FinanceTransactionJson() {
		super();
	}

	/**
	 * Creates a FinanceTransactionJson wrapper based on a FinanceTransaction
	 * instance
	 *
	 * @param transaction the wrapped FinanceTransaction instance
	 */
	public FinanceTransactionJson(FinanceTransaction transaction) {
		this.amount = transaction.getRawAmount();
		this.description = transaction.getDescription();
		this.id = transaction.getId();
		this.owner = transaction.getOwner();
		this.tags = Arrays.asList(transaction.getTags());
		this.transactionDate = transaction.getDate();
		this.type = transaction.getType();
		this.setVersion(transaction.getVersion());
		for (TransactionComponent component : transaction.getComponents())
			componentsJson.add(new TransactionComponentJson(component));
	}

	/**
	 * Sets the FinanceTransaction id
	 *
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Returns a list of all components
	 *
	 * @return the list of all transaction components
	 */
	@JsonProperty("components")
	public List<TransactionComponentJson> getComponentsJson() {
		return componentsJson;
	}

	/**
	 * Sets the list of all components
	 *
	 * @param componentsJson the transaction components list to set
	 */
	@JsonProperty("components")
	public void setComponentsJson(List<TransactionComponentJson> componentsJson) {
		this.componentsJson = componentsJson;
	}
}
