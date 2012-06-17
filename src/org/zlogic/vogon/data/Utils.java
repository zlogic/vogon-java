/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

/**
 * Various helper utilities
 * @author Zlogic
 */
public class Utils {

    /**
     * Generates a full stack trace from an exception (e.g. to be used in error messages or for logging).
     * Can be used instead of e.printStackTrance()
     * @param t The exception
     * @return The string representation of the exception, identical to e.printStackTrance().
     */
    static public String getStackTrace(Throwable t) {
	java.io.StringWriter sw = new java.io.StringWriter();
	java.io.PrintWriter pw = new java.io.PrintWriter(sw, true);
	t.printStackTrace(pw);
	pw.flush();
	sw.flush();
	return sw.toString();
    }
}
