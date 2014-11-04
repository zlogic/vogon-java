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
	public static final double RAW_AMOUNT_MULTIPLIER = 100.0D;

	/**
	 * Default username if only one user is supported
	 */
	public static final String DEFAULT_USERNAME = "Default"; //NOI18N
	/**
	 * Default password if only one user is supported
	 */
	public static final String DEFAULT_PASSWORD = "Default"; //NOI18N
}
