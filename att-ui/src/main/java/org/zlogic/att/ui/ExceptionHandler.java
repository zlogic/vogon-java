/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

/**
 * UI interface for displaying an exception
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
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
