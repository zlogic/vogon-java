/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

/**
 * Class for storing constants
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
 */
public class Constants {

	/**
	 * Batch fetch size for processing large amounts of data
	 */
	public static final int batchFetchSize = 200;
	/**
	 * Multiplier for converting from raw amount to double (10E[decimal points])
	 */
	public static final double rawAmountMultiplier = 100.0D;
}
