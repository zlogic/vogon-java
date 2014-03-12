package org.zlogic.att.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Class for redirecting stdout to log
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class ConsoleLogger {

	/**
	 * The console logger name
	 */
	public static final String CONSOLE_LOGGER = "Console"; //NOI18N
	/**
	 * The console logger
	 */
	private final static Logger logger = Logger.getLogger(CONSOLE_LOGGER);

	/**
	 * Initializes console logging if level is not OFF
	 */
	public void init() {
		if (logger.getLevel() != null && logger.getLevel() != Level.OFF) {
			boolean needToDisableConsoleLogging = needToDisableConsoleLogging();
			System.setErr(new PrintStream(new LoggerStream(Level.SEVERE, needToDisableConsoleLogging)));
			System.setOut(new PrintStream(new LoggerStream(Level.FINE, needToDisableConsoleLogging)));
		}
	}

	/**
	 * Stream to redirect output to log
	 */
	public class LoggerStream extends OutputStream {

		/**
		 * Log level for this stream
		 */
		private final Level logLevel;

		/**
		 * True if stack trace should be checked for write() recursions
		 */
		private boolean checkRecursion;

		/**
		 * Initializes the logger stream
		 *
		 * @param logLevel the log level for this stream
		 */
		public LoggerStream(Level logLevel, boolean checkRecursion) {
			super();

			this.logLevel = logLevel;
			this.checkRecursion = checkRecursion;
		}

		/**
		 * Returns true if write() has entered a recursion
		 *
		 * @return true if write() has entered a recursion
		 */
		private boolean isRecursion() {
			if (!checkRecursion)
				return false;
			int writeCallCount = 0;
			for (StackTraceElement ste : Thread.currentThread().getStackTrace())
				if (ste.getClassName().equals(LoggerStream.class.getName()) && ste.getMethodName().equals("write")) //NOI18N
					writeCallCount++;
			return writeCallCount >= 2;
		}

		@Override
		public void write(byte[] b) throws IOException {
			if (isRecursion())
				return;
			String string = new String(b);
			if (!string.trim().isEmpty())
				logger.log(logLevel, string);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			if (isRecursion())
				return;
			String string = new String(b, off, len);
			if (!string.trim().isEmpty())
				logger.log(logLevel, string);
		}

		@Override
		public void write(int b) throws IOException {
			if (isRecursion())
				return;
			String string = String.valueOf((char) b);
			if (!string.trim().isEmpty())
				logger.log(logLevel, string);
		}
	}

	/**
	 * Returns true if console logging is enabled and so should be disabled
	 *
	 * @return true if console logging is enabled and so should be disabled
	 */
	private boolean needToDisableConsoleLogging() {
		Logger rootLogger = LogManager.getLogManager().getLogger("");//NOI18N
		for (Handler handler : rootLogger.getHandlers())
			if (handler instanceof ConsoleHandler)
				return true;
		return false;
	}
}
