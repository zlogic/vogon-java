/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.interop;

import javax.persistence.EntityManager;
import org.zlogic.vogon.data.VogonUser;

/**
 * Interface for importing data from files
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public interface Importer {

	/**
	 * Imports a file containing financial transactions
	 *
	 * @param owner the user who will be the owner of imported data
	 * @param entityManager the EntityManager to be used for storing new items
	 * and checking for duplicates; should have a transaction opened and closed
	 * before calling this function
	 *
	 * @throws VogonImportException in case of any import errors (I/O, format
	 * etc.)
	 * @throws VogonImportLogicalException in case of any logical errors (such
	 * as an incorrect number of columns)
	 */
	public void importData(VogonUser owner, EntityManager entityManager) throws VogonImportException, VogonImportLogicalException;
}
