/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.interop;

/**
 * File export exception class
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class VogonExportException extends Exception {

	/**
	 * Version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public VogonExportException() {
	}

	/**
	 * Constructor based on a human-readable message
	 *
	 * @param message the message
	 */
	public VogonExportException(String message) {
		super(message);
	}

	/**
	 * Constructor based on a throwable object
	 *
	 * @param cause a throwable object
	 */
	public VogonExportException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor based on a human-readable and throwable object
	 *
	 * @param message the message
	 * @param cause a throwable object
	 */
	public VogonExportException(String message, Throwable cause) {
		super(message, cause);
	}
}
