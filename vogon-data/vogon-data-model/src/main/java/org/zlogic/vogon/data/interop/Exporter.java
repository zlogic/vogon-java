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
 * Interface for exporting data to files
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public interface Exporter {

	/**
	 * Exports financial data into a file
	 *
	 * @param owner the user who owns the data to be exported
	 * @param accounts the accounts to export
	 * @param transactions the transactions to export
	 * @param currencyRates the currency rates to export
	 * @throws VogonExportException in case of any import errors (I/O, format
	 * etc.)
	 */
	public void exportData(VogonUser owner, Collection<FinanceAccount> accounts, Collection<FinanceTransaction> transactions, Collection<CurrencyRate> currencyRates) throws VogonExportException;
}
