/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.data;

import java.util.Collection;
import org.springframework.stereotype.Component;
import org.zlogic.vogon.data.FinanceTransaction;

/**
 * Bean to extract all data from beans to prevent lazy initialization
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Component
public class InitializationHelper {

	/**
	 * Initialize transactions
	 *
	 * @param transactions transactions to initialize
	 * @return the transactions list
	 */
	public Collection<FinanceTransaction> initializeTransactions(Collection<FinanceTransaction> transactions) {
		for (FinanceTransaction transaction : transactions) {
			transaction.getAccounts();
			transaction.getComponents();
			transaction.getTags();
		}
		return transactions;
	}
}
