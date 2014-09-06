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
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
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
import org.zlogic.vogon.web.security.VogonSecurityUser;

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
	 * @param page the page number
	 * @param user the authenticated user
	 * @return the transactions
	 */
	@RequestMapping(value = "/page_{page}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Collection<FinanceTransaction> getTransactions(@PathVariable int page, @AuthenticationPrincipal VogonSecurityUser user) {
		PageRequest pageRequest = new PageRequest(page, PAGE_SIZE);
		return initializationHelper.initializeTransactions(transactionRepository.findByOwner(user.getUser(), pageRequest).getContent());
	}

	/**
	 * Returns the number of transactions
	 *
	 * @param user the authenticated user
	 * @return the number of transactions
	 */
	@RequestMapping(value = "/pages", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	long getTransactionsCount(@AuthenticationPrincipal VogonSecurityUser user) {
		PageRequest pageRequest = new PageRequest(0, PAGE_SIZE);
		return transactionRepository.findByOwner(user.getUser(), pageRequest).getTotalPages();
	}

	/**
	 * Returns all transactions
	 *
	 * @param user the authenticated user
	 * @return the transactions
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Collection<FinanceTransaction> getAllTransactions(@AuthenticationPrincipal VogonSecurityUser user) {
		return initializationHelper.initializeTransactions(transactionRepository.findByOwner(user.getUser()));
	}
}
