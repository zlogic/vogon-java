/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.util.Arrays;
import java.util.List;

/**
 * Various helper utilities
 *
 * @author Dmitry Zolotukhin
 */
public class Utils {

	/**
	 * Generates a full stack trace from an exception (e.g. to be used in error
	 * messages or for logging). Can be used instead of e.printStackTrance()
	 *
	 * @param t The exception
	 * @return The string representation of the exception, identical to
	 * e.printStackTrance().
	 */
	static public String getStackTrace(Throwable t) {
		java.io.StringWriter sw = new java.io.StringWriter();
		java.io.PrintWriter pw = new java.io.PrintWriter(sw, true);
		t.printStackTrace(pw);
		pw.flush();
		sw.flush();
		return sw.toString();
	}

	/**
	 * Joins an array of strings with the specified separator
	 *
	 * @param parts The string array
	 * @param separator The separator inserted during joining
	 * @return the joined array
	 */
	static public String join(String[] parts, String separator) {
		return join(Arrays.asList(parts), separator);
	}

	/**
	 * Joins an array of strings with the specified separator
	 *
	 * @param parts The string array
	 * @param separator The separator inserted during joining
	 * @return the joined array
	 */
	static public String join(List<String> parts, String separator) {
		if (parts.isEmpty())
			return ""; //NOI18N
		StringBuilder builder = new StringBuilder();
		String sep = ""; //NOI18N
		for (String part : parts) {
			builder.append(sep);
			builder.append(part);
			sep = separator;
		}
		return builder.toString();
	}
}
