/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

/**
 * Class for storing constants
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class Constants {

	/**
	 * Multiplier for converting from raw amount to double (10E[decimal points])
	 */
	public static final double rawAmountMultiplier = 100.0D;
	
	/**
	 * Default username if only one user is supported
	 */
	public static final String defaultUserUsername = "Default";
	/**
	 * Default password if only one user is supported
	 */
	public static final String defaultUserPassword = "Default";
}
