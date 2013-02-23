/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

//FIXME: catch all Java FX exceptions here once this is supported by Java 8
/**
 * UI interface for displaying an exception
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public interface ExceptionHandler {

	/**
	 * Shows an exception dialog
	 *
	 * @param explanation the exception human-readable explanation
	 * @param ex the actual exception
	 * @param callerIsBackgroundThread true if the caller is a background thread
	 * (not a JavaFX event thread)
	 */
	public void showException(String explanation, Throwable ex, boolean callerIsBackgroundThread);
}
