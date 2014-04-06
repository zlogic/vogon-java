/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.adapters;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.ResourceBundle;

/**
 * Class for formatting dates
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class DurationFormatter {

	private static final MessageFormat format = new MessageFormat(ResourceBundle.getBundle("org/zlogic/att/ui/adapters/messages").getString("{0,NUMBER,0}:{1,NUMBER,00}:{2,NUMBER,00}"));

	/**
	 * Returns a formatted duration
	 *
	 * @param duration the duration to format
	 * @return the formatted duration
	 */
	public static String formatDuration(Duration duration) {
		long hours = duration.toHours();
		long minutes = duration.toMinutes() - duration.toHours() * 60;
		long seconds = duration.getSeconds() - duration.toMinutes() * 60;
		return format.format(new Object[]{hours, minutes, seconds});
	}
}
