/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data.converters;

/**
 * File export exception class
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class ExportException extends RuntimeException {

	/**
	 * Default constructor
	 */
	public ExportException() {
	}

	/**
	 * Constructor based on a human-readable message
	 *
	 * @param message the message
	 */
	public ExportException(String message) {
		super(message);
	}

	/**
	 * Constructor based on a throwable object
	 *
	 * @param cause a throwable object
	 */
	public ExportException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor based on a human-readable and throwable object
	 *
	 * @param message the message
	 * @param cause a throwable object
	 */
	public ExportException(String message, Throwable cause) {
		super(message, cause);
	}
}
