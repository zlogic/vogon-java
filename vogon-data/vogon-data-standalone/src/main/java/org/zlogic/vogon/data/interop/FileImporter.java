/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.interop;

import javax.persistence.EntityManager;
import org.zlogic.vogon.data.standalone.FinanceData;

/**
 * Interface for importing data from files
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public interface FileImporter {

	/**
	 * Imports a file containing financial transactions
	 *
	 * @param financeData the FinanceData to be used for obtaining data
	 * @param entityManager the EntityManager to be used for storing new items
	 * and checking for duplicates
	 *
	 * @throws VogonImportException in case of any import errors (I/O, format
	 * etc.)
	 * @throws VogonImportLogicalException in case of any logical errors (such
	 * as an incorrect number of columns)
	 */
	public void importFile(FinanceData financeData, EntityManager entityManager) throws VogonImportException, VogonImportLogicalException;
}
