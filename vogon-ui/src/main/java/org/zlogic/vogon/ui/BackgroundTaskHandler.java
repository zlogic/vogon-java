/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

/**
 * Interface for processing background tasks
 *
 * @author Dmitry Zolotukhin
 */
public interface BackgroundTaskHandler {

	/**
	 * Runs a runnable task in the background. Wait for the previous task to
	 * complete.
	 *
	 * @param task the task to run
	 * @param taskDescription the task description to be displayed in the status
	 * bar
	 * @throws InterruptedException if unable to join the previous task
	 */
	public void runTask(final Runnable task, String taskDescription) throws InterruptedException;
}
