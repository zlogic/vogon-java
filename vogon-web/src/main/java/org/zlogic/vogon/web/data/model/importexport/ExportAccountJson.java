/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.data.model.importexport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.zlogic.vogon.data.FinanceAccount;

/**
 * JSON wrapper for FinanceAccount class, used only for Data Import/Export
 * operations
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@JsonIgnoreProperties(value = {"rawBalance", "components", "owner", "version"})
public class ExportAccountJson extends FinanceAccount {

	/**
	 * Default constructor
	 */
	protected ExportAccountJson() {
		super();
	}

	/**
	 * Creates an ExportAccountJson wrapper based on a FinanceAccount instance
	 *
	 * @param account the wrapped FinanceAccount instance
	 * @param id the account id
	 */
	public ExportAccountJson(FinanceAccount account, long id) {
		super(null, account);
		this.id = id;
		this.balance = account.getRawBalance();
	}
}
