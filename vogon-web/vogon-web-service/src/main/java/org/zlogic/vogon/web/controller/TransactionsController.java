/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller;

import java.util.Collection;
import javax.inject.Inject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.web.Initializer;

/**
 * Spring MVC controller for transactions
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Controller
@RequestMapping(value = "/service/transactions")
public class TransactionsController {

	/**
	 * The initializer (FinanceData holder) instance
	 */
	@Inject
	private Initializer initializer;

	/**
	 * Returns all transactions in a specific range
	 *
	 * @param from the starting transaction index
	 * @param to the ending transaction index
	 * @return the transactions
	 */
	@RequestMapping(value = "/{from}-{to}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Collection<FinanceTransaction> getTransactions(@PathVariable Integer from, @PathVariable int to) {
		return initializer.getFinanceData().getTransactions(from, to);
	}

	/**
	 * Returns the number of transactions
	 *
	 * @return the number of transactions
	 */
	@RequestMapping(value = "/count", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	int getTransactionsCount() {
		return initializer.getFinanceData().getTransactionCount();
	}

	/**
	 * Returns all transactions
	 *
	 * @return the transactions
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Collection<FinanceTransaction> getAllTransactions() {
		return initializer.getFinanceData().getTransactions();
	}
}
