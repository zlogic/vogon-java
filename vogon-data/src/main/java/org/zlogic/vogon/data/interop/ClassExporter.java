/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.interop;

import java.util.Collection;
import org.zlogic.vogon.data.CurrencyRate;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.VogonUser;

/**
 * Implementation for exporting data to Java POJOs
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class ClassExporter implements Exporter {

	/**
	 * The export result
	 */
	private ExportedData export;

	@Override
	public void exportData(VogonUser owner, Collection<FinanceAccount> accounts, Collection<FinanceTransaction> transactions, Collection<CurrencyRate> currencyRates) throws VogonExportException {
		export = new ExportedData();
		export.setUser(owner);
		export.setAccounts(accounts);
		export.setTransactions(transactions);
		export.setCurrencyRates(currencyRates);
	}

	/**
	 * Returns the export result
	 *
	 * @return the export result
	 */
	public ExportedData getExportedData() {
		return export;
	}
}
