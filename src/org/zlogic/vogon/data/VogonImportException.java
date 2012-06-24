/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

/**
 * File import exception class
 *
 * @author Dmitry Zolotukhin
 */
public class VogonImportException extends Exception {

    public VogonImportException() {
    }

    public VogonImportException(String message) {
	super(message);
    }

    public VogonImportException(Throwable cause) {
	super(cause);
    }

    public VogonImportException(String message, Throwable cause) {
	super(message, cause);
    }
}
