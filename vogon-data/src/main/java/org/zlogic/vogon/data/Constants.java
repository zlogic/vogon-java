/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

/**
 * Class for storing constants
 *
 * @author Dmitry Zolotukhin
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
