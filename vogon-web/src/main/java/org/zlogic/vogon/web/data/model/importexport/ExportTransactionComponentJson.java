/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.data.model.importexport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.web.data.model.TransactionComponentJson;

/**
 * JSON wrapper for TransactionComponent class, used only for Data Import/Export
 * operations
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@JsonIgnoreProperties(value = {"id", "version", "transaction", "account", "rawAmount"})
public class ExportTransactionComponentJson extends TransactionComponentJson {

	/**
	 * Default constructor
	 */
	protected ExportTransactionComponentJson() {
		super();
	}

	/**
	 * Creates an ExportTransactionComponentJson wrapper based on a
	 * TransactionComponent instance
	 *
	 * @param component the wrapped TransactionComponent instance
	 * @param accountId the associated account ID
	 */
	public ExportTransactionComponentJson(TransactionComponent component, long accountId) {
		this.amount = component.getRawAmount();
		this.transaction = component.getTransaction();
		ExportTransactionComponentJson.this.setAccountId(accountId);
	}
}
