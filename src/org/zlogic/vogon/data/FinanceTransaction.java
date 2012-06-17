/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

/**
 * Interface for storing a single finance transaction
 * @author Dmitry Zolotukhin
 */
public interface FinanceTransaction {
    /**
     * Returns a string representation of the transaction (e.g. for logging)
     * @return The string representation of the transaction
     */
    @Override
    java.lang.String toString();
}
