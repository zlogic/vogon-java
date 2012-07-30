/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

/**
 * Interface for exporting data to files
 *
 * @author Dmitry Zolotukhin
 */
public interface FileExporter {

	/**
	 * Exports financial data into a file
	 *
	 * @param financeData the data to be exported
	 * @throws VogonExportException In case of any import errors (I/O, format
	 * etc.)
	 */
	public void exportFile(FinanceData financeData) throws VogonExportException;
}
