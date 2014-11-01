/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.data;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.web.data.model.FinanceTransactionJson;

/**
 * Bean to extract all data from beans to prevent lazy initialization
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Component
public class InitializationHelper {

	/**
	 * Initialize transaction
	 *
	 * @param transaction transaction to initialize
	 * @return the transaction
	 */
	public FinanceTransactionJson initializeTransaction(FinanceTransaction transaction) {
		return new FinanceTransactionJson(transaction);
	}

	/**
	 * Initialize transactions
	 *
	 * @param transactions transactions to initialize
	 * @return the transactions list
	 */
	public List<FinanceTransactionJson> initializeTransactions(Collection<FinanceTransaction> transactions) {
		List<FinanceTransactionJson> newTransactions = new LinkedList<>();
		for (FinanceTransaction transaction : transactions)
			newTransactions.add(initializeTransaction(transaction));
		return newTransactions;
	}
}
