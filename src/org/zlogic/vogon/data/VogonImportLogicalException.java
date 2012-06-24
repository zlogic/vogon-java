/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

/**
 * File import logical exception class (stack trace is useless)
 *
 * @author Dmitry Zolotukhin
 */
public class VogonImportLogicalException extends Exception {

    public VogonImportLogicalException() {
    }

    public VogonImportLogicalException(String message) {
	super(message);
    }

    public VogonImportLogicalException(Throwable cause) {
	super(cause);
    }

    public VogonImportLogicalException(String message, Throwable cause) {
	super(message, cause);
    }
}