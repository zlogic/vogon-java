/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.data.model.importexport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Arrays;
import java.util.TreeSet;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.web.data.model.FinanceTransactionJson;

/**
 * JSON wrapper for FinanceTransaction class, used only for Data Import/Export
 * operations
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@JsonIgnoreProperties(value = {"id", "version", "owner", "amount"})
public class ExportFinanceTransactionJson extends FinanceTransactionJson {

	/**
	 * Default constructor
	 */
	protected ExportFinanceTransactionJson() {
		super();
	}

	/**
	 * Creates an ExportFinanceTransactionJson wrapper based on a
	 * FinanceTransaction instance
	 *
	 * @param transaction the wrapped FinanceTransaction instance
	 */
	public ExportFinanceTransactionJson(FinanceTransaction transaction) {
		this.description = transaction.getDescription();
		this.tags = new TreeSet<>(Arrays.asList(transaction.getTags()));
		this.transactionDate = transaction.getDate();
		this.type = transaction.getType();
	}
}
