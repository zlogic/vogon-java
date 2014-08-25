/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;

/**
 * Class for logging uncaught exceptions
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class ExceptionLogger implements UncaughtExceptionHandler, ExceptionHandler {

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
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * Format for throwable cause message
	 */
	private MessageFormat stackTraceFormat = new MessageFormat(messages.getString("CAUSED_BY"));
	/**
	 * Format for uncaught exception message
	 */
	private MessageFormat uncaughtExceptionFormat = new MessageFormat(messages.getString("RECEIVED_UNCAUGHT_EXCEPTION"));

	/**
	 * Java FX controller to display GUI error message
	 */
	private ExceptionDialogController exceptionDialogController;

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

	/**
	 * Loads the exception dialog FXML
	 */
	private void loadExceptionDialog() {
		//Load FXML
		FXMLLoader loader = new FXMLLoader(getClass().getResource("ExceptionDialog.fxml"), messages); //NOI18N
		loader.setLocation(getClass().getResource("ExceptionDialog.fxml")); //NOI18N
		try {
			loader.load();
		} catch (IOException ex) {
			logger.log(Level.SEVERE, messages.getString("ERROR_LOADING_FXML"), ex);
		}
		exceptionDialogController = loader.getController();
	}

	/**
	 * Sets the window icons
	 *
	 * @param icons the icons to be set
	 */
	public void setWindowIcons(ObservableList<Image> icons) {
		if (exceptionDialogController == null)
			loadExceptionDialog();
		exceptionDialogController.setWindowIcons(icons);
	}

	/**
	 * Returns a stack trace string for a Throwable
	 *
	 * @param thr the Throwable
	 * @return a stack trace string
	 */
	private String getExceptionStacktrace(Throwable thr) {
		StringBuilder exceptionStackTrace = new StringBuilder();
		while (thr != null) {
			exceptionStackTrace.append(stackTraceFormat.format(
					new Object[]{thr.getClass().getName() + (thr.getMessage() != null ? " (" + thr.getMessage() + ")" : "")}
			));
			for (StackTraceElement ste : thr.getStackTrace())
				exceptionStackTrace.append("\r\n\t").append(ste.toString()); //NOI18N
			thr = thr.getCause();
		}
		return exceptionStackTrace.toString();
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		logger.severe(uncaughtExceptionFormat.format(new Object[]{getExceptionStacktrace(e)}));
		if (exceptionDialogController == null)
			loadExceptionDialog();
		exceptionDialogController.showExceptionMessage(uncaughtExceptionFormat.format(
				new Object[]{e.getClass().getName() + (e.getMessage() != null ? " (" + e.getMessage() + ")" : "")}
		));
	}

	@Override
	public void showException(String explanation, Throwable ex) {
		String exceptionString = null;
		if (explanation != null)
			exceptionString = explanation;
		else if (ex != null)
			exceptionString = uncaughtExceptionFormat.format(
					new Object[]{ex.getClass().getName() + (ex.getMessage() != null ? " (" + ex.getMessage() + ")" : "")}
			);
		else
			exceptionString = messages.getString("UNKNOWN_ERROR");

		logger.severe(exceptionString);
		if (exceptionDialogController == null)
			loadExceptionDialog();
		exceptionDialogController.showExceptionMessage(exceptionString);
	}
}
