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

	/**
	 * Version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public VogonImportLogicalException() {
	}

	/**
	 * Constructor based on a human-readable message
	 *
	 * @param message the message
	 */
	public VogonImportLogicalException(String message) {
		super(message);
	}

	/**
	 * Constructor based on a throwable object
	 *
	 * @param cause a throwable object
	 */
	public VogonImportLogicalException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor based on a human-readable and throwable object
	 *
	 * @param message the message
	 * @param cause a throwable object
	 */
	public VogonImportLogicalException(String message, Throwable cause) {
		super(message, cause);
	}
}