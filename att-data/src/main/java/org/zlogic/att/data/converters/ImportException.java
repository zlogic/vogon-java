/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data.converters;

/**
 * File import exception class
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class ImportException extends RuntimeException {

	/**
	 * Default constructor
	 */
	public ImportException() {
	}

	/**
	 * Constructor based on a human-readable message
	 *
	 * @param message the message
	 */
	public ImportException(String message) {
		super(message);
	}

	/**
	 * Constructor based on a throwable object
	 *
	 * @param cause a throwable object
	 */
	public ImportException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor based on a human-readable and throwable object
	 *
	 * @param message the message
	 * @param cause a throwable object
	 */
	public ImportException(String message, Throwable cause) {
		super(message, cause);
	}
}
