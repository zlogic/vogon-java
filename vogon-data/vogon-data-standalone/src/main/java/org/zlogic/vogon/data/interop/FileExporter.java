/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.interop;

import org.zlogic.vogon.data.standalone.FinanceData;

/**
 * Interface for exporting data to files
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public interface FileExporter {

	/**
	 * Exports financial data into a file
	 *
	 * @param financeData the data to be exported
	 * @throws VogonExportException in case of any import errors (I/O, format
	 * etc.)
	 */
	public void exportFile(FinanceData financeData) throws VogonExportException;
}
