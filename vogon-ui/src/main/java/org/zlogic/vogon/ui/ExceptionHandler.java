/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

/**
 * UI interface for displaying an exception
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public interface ExceptionHandler {

	/**
	 * Shows an exception dialog
	 *
	 * @param explanation the exception human-readable explanation
	 * @param ex the actual exception
	 */
	public void showException(String explanation, Throwable ex);
}
