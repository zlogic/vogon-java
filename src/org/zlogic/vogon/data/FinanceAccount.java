/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

/**
 * Class for storing account data
 *
 * @author Dmitry Zolotukhin
 */
public class FinanceAccount {

    /**
     * The account name
     */
    protected String name;
    /**
     * Finance data reference for recalculating balance
     */
    protected FinanceData financeData;

    /**
     * Creates an account
     *
     * @param name The account name
     * @param financeData
     */
    public FinanceAccount(String name) {
	this.name = name;
    }

    /**
     * Returns the account name
     *
     * @return The account name
     */
    public String getName() {
	return name;
    }

    /**
     * Calculates and returns the actual (latest) account balance
     *
     * @return The actual account balance
     */
    public double getActualBalance() {
	if (financeData == null)
	    return Double.NaN;

	return financeData.getActualBalance(this);
    }

    /**
     * Sets the finance data reference for account operations
     *
     * @param financeData The finance data reference
     */
    public void setFinanceData(FinanceData financeData) {
	this.financeData = financeData;
    }
}
