/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.analytics;

import java.util.Date;
import org.zlogic.vogon.data.FinanceData;

/**
 * Central class for setting report parameters and generating various reports
 *
 * @author Dmitry Zolotukhin
 */
public class Report {

	/**
	 * The financeData used to generate the report
	 */
	protected FinanceData financeData;
	/**
	 * The low cutoff date for generating the report
	 */
	protected Date earliestDate;
	/**
	 * The high cutoff date for generating the report
	 */
	protected Date latestDate;

	/**
	 * Default constructor
	 *
	 * @param financeData the financeData instance to be used for generating the
	 * report
	 */
	public Report(FinanceData financeData) {
		this.financeData = financeData;
	}

	/*
	 * Set report parameters and filters
	 */
	/**
	 * Returns the low cutoff date for generating the report
	 *
	 * @return the low cutoff date for generating the report
	 */
	public Date getEarliestDate() {
		return earliestDate;
	}

	/**
	 * Sets the low cutoff date for generating the report
	 *
	 * @param earliestDate the low cutoff date for generating the report
	 */
	public void setEarliestDate(Date earliestDate) {
		this.earliestDate = earliestDate;
	}

	/**
	 * Returns the high cutoff date for generating the report
	 *
	 * @return the high cutoff date for generating the report
	 */
	public Date getLatestDate() {
		return latestDate;
	}

	/**
	 * Sets the high cutoff date for generating the report
	 *
	 * @param latestDate the high cutoff date for generating the report
	 */
	public void setLatestDate(Date latestDate) {
		this.latestDate = latestDate;
	}

	/**
	 * Generates a text-only report
	 *
	 * @return the text-based report
	 */
	public String getTextReport() {
		return earliestDate.toString() + "---" + latestDate.toString();
	}
}
