/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.lang.Thread.UncaughtExceptionHandler;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Class for logging uncaught exceptions
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class ExceptionLogger implements UncaughtExceptionHandler {

	/**
	 * The console logger name
	 */
	public static final String UNCAUGHT_EXCEPTION_LOGGER = "UncaughtExceptionLogger"; //NOI18N
	/**
	 * The console logger
	 */
	private final static Logger logger = Logger.getLogger(UNCAUGHT_EXCEPTION_LOGGER);

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	/**
	 * Format for throwable cause message
	 */
	private MessageFormat format = new MessageFormat(messages.getString("CAUSED_BY"));

	/**
	 * The singleton instance
	 */
	private static ExceptionLogger instance = null;

	/**
	 * Hidden private constructor to enforce singleton usage
	 */
	private ExceptionLogger() {
	}

	/**
	 * Returns the ExceptionLogger singleton instance
	 *
	 * @return the ExceptionLogger singleton instance
	 */
	public static ExceptionLogger getInstance() {
		if (instance == null)
			instance = new ExceptionLogger();
		return instance;
	}

	/**
	 * Initializes the ExceptionLogger instance and assigns it to the Thread's
	 * DefaultUncaughtExceptionHandler
	 */
	public static void init() {
		Thread.setDefaultUncaughtExceptionHandler(getInstance());
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		StringBuilder exceptionStackTrace = new StringBuilder();
		exceptionStackTrace.append(messages.getString("RECEIVED_UNCAUGHT_EXCEPTION"));
		while (e != null) {
			exceptionStackTrace.append(format.format(new Object[]{e.getMessage()}));
			for (StackTraceElement ste : e.getStackTrace())
				exceptionStackTrace.append("\n\t").append(ste.toString()); //NOI18N
			e = e.getCause();
		}
		//TODO: also show the ExceptionDialogController here?
		logger.severe(exceptionStackTrace.toString());
	}
}
