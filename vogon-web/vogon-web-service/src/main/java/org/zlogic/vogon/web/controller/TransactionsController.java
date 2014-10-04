/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.FinanceTransaction_;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.web.data.AccountRepository;
import org.zlogic.vogon.web.data.InitializationHelper;
import org.zlogic.vogon.web.data.TransactionRepository;
import org.zlogic.vogon.web.data.model.FinanceTransactionJson;
import org.zlogic.vogon.web.data.model.TransactionComponentJson;
import org.zlogic.vogon.web.security.VogonSecurityUser;

/**
 * Spring MVC controller for transactions
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Controller
@RequestMapping(value = "/service/transactions")
@Transactional(propagation = Propagation.REQUIRED)
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
	 * The transactions repository
	 */
	@Autowired
	private TransactionRepository transactionRepository;
	/**
	 * The accounts repository
	 */
	@Autowired
	private AccountRepository accountRepository;
	/**
	 * InitializationHelper instance
	 */
	@Autowired
	private InitializationHelper initializationHelper;

	/**
	 * Default sort order
	 */
	private Sort sort = new JpaSort(Sort.Direction.DESC, FinanceTransaction_.transactionDate, FinanceTransaction_.id);

	/**
	 * Returns all transactions in a specific range, or all transactions if page
	 * parameter is missing
	 *
	 * @param page the page number
	 * @param user the authenticated user
	 * @return the transactions
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Collection<FinanceTransactionJson> getTransactions(@RequestParam("page") Integer page, @AuthenticationPrincipal VogonSecurityUser user) {
		if (page == null)
			return initializationHelper.initializeTransactions(transactionRepository.findByOwner(user.getUser()));
		PageRequest pageRequest = new PageRequest(page, PAGE_SIZE, sort);
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
	 * Returns a specific transaction
	 *
	 * @param id the transaction id
	 * @param user the authenticated user
	 * @return the number of transactions
	 */
	@RequestMapping(value = "/transaction/{id}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	FinanceTransactionJson getTransaction(@PathVariable long id, @AuthenticationPrincipal VogonSecurityUser user) {
		return initializationHelper.initializeTransaction(transactionRepository.findByOwnerAndId(user.getUser(), id));
	}

	/**
	 * Updates or creates a new transaction
	 *
	 * @param transaction the updated transaction
	 * @param user the authenticated user
	 * @return the transactions from database after update
	 */
	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	FinanceTransactionJson submitTransaction(@RequestBody FinanceTransactionJson transaction, @AuthenticationPrincipal VogonSecurityUser user) {
		FinanceTransaction existingTransaction = transactionRepository.findByOwnerAndId(user.getUser(), transaction.getId());
		//Merge with database
		if (existingTransaction == null)
			existingTransaction = new FinanceTransaction(user.getUser(), transaction);
		else
			existingTransaction.merge(transaction);
		List<TransactionComponent> removedComponents = new LinkedList<>(existingTransaction.getComponents());
		for (TransactionComponentJson newComponent : transaction.getComponentsJson()) {
			FinanceAccount existingAccount = accountRepository.findByOwnerAndId(user.getUser(), newComponent.getAccountId());
			if (!existingTransaction.getComponents().contains(newComponent)) {
				TransactionComponent createdComponent = new TransactionComponent(existingAccount, existingTransaction, newComponent.getRawAmount());
				em.persist(createdComponent);
				existingTransaction.addComponent(createdComponent);
			} else {
				TransactionComponent existingComponent = existingTransaction.getComponents().get(existingTransaction.getComponents().indexOf(newComponent));
				if (newComponent.getVersion() != existingComponent.getVersion())
					throw new ConcurrentModificationException("Transaction was already updated");
				existingTransaction.updateComponentAccount(existingComponent, existingAccount);
				existingTransaction.updateComponentRawAmount(existingComponent, newComponent.getAmount());
				removedComponents.remove(existingComponent);
			}
			existingTransaction = transactionRepository.save(existingTransaction);
			accountRepository.save(existingAccount);
		}
		//Remove deleted components
		for (TransactionComponent removedComponent : removedComponents)
			existingTransaction.removeComponent(removedComponent);
		existingTransaction = transactionRepository.saveAndFlush(existingTransaction);
		accountRepository.flush();
		return initializationHelper.initializeTransaction(existingTransaction);
	}

	/**
	 * Deletes a transaction
	 *
	 * @param id the transaction id
	 * @param user the authenticated user
	 * @return null
	 */
	@RequestMapping(value = "/transaction/{id}", method = RequestMethod.DELETE, produces = "application/json")
	public @ResponseBody
	FinanceTransactionJson deleteTransaction(@PathVariable long id, @AuthenticationPrincipal VogonSecurityUser user) {
		FinanceTransaction existingTransaction = transactionRepository.findByOwnerAndId(user.getUser(), id);
		if (existingTransaction != null) {
			existingTransaction.removeAllComponents();
			transactionRepository.save(existingTransaction);
			transactionRepository.delete(existingTransaction);
			return initializationHelper.initializeTransaction(existingTransaction);
		}
		return null;
	}
}
