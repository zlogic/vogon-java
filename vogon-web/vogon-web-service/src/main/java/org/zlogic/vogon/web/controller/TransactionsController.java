/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller;

import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.web.data.InitializationHelper;
import org.zlogic.vogon.web.data.TransactionRepository;
import org.zlogic.vogon.web.data.UserRepository;

/**
 * Spring MVC controller for transactions
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Controller
@RequestMapping(value = "/service/transactions")
@Transactional
public class TransactionsController {

	/**
	 * The page size
	 */
	private static final int PAGE_SIZE = 100;//TODO: make this customizable
	/**
	 * The EntityManager instance
	 */
	@PersistenceContext
	private EntityManager em;

	/**
	 * The users repository
	 */
	@Autowired
	private UserRepository userRepository;
	/**
	 * The transactions repository
	 */
	@Autowired
	private TransactionRepository transactionRepository;

	/**
	 * InitializationHelper instance
	 */
	@Autowired
	private InitializationHelper initializationHelper;

	/**
	 * Returns all transactions in a specific range
	 *
	 * @param from the starting transaction index
	 * @param to the ending transaction index
	 * @return the transactions
	 */
	@RequestMapping(value = "/page_{page}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Collection<FinanceTransaction> getTransactions(@PathVariable int page) {
		PageRequest pageRequest = new PageRequest(page, PAGE_SIZE);
		return initializationHelper.initializeTransactions(transactionRepository.findAll(pageRequest).getContent());
	}

	/**
	 * Returns the number of transactions
	 *
	 * @return the number of transactions
	 */
	@RequestMapping(value = "/pages", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	long getTransactionsCount() {
		PageRequest pageRequest = new PageRequest(0, PAGE_SIZE);
		return transactionRepository.findAll(pageRequest).getTotalPages();
	}

	/**
	 * Returns all transactions
	 *
	 * @return the transactions
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Collection<FinanceTransaction> getAllTransactions() {
		return initializationHelper.initializeTransactions(transactionRepository.findAll());
	}
}
