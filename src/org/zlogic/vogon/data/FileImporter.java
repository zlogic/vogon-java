/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

/**
 * Interface for importing data from files
 *
 * @author Dmitry Zolotukhin
 */
public interface FileImporter {

    /**
     * Imports a file containing financial transactions
     *
     * @param file The file to be imported
     * @return The financial transactions as a FinanceData class
     * @throws VogonImportException In case of any import errors (I/O, format
     * etc.)
     */
    public FinanceData importFile(java.io.File file) throws VogonImportException, VogonImportLogicalException;
}
