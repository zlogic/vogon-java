/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.interop;

/**
 * File import logical exception class (stack trace is useless)
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
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