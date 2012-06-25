/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import javax.persistence.*;

/**
 * Class for storing account data
 *
 * @author Dmitry Zolotukhin
 */
@Entity
public class FinanceAccount implements Serializable {

    /**
     * The account ID (only for persistence)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;
    /**
     * The account name
     */
    protected String name;

    /**
     * Creates an account
     */
    public FinanceAccount() {
    }

    /**
     * Creates an account
     *
     * @param name The account name
     */
    public FinanceAccount(String name) {
	this.name = name;
    }
    /**
     * Finance data reference for recalculating balance
     */
    @Transient
    protected FinanceData financeData;

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
