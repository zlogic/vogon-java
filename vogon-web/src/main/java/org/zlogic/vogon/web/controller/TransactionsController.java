/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import org.zlogic.vogon.web.configuration.VogonConfiguration;
import org.zlogic.vogon.web.data.AccountRepository;
import org.zlogic.vogon.web.data.InitializationHelper;
import org.zlogic.vogon.web.data.TransactionFilterSpecification;
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
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/web/messages");
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
	 * The configuration handler
	 */
	@Autowired
	private VogonConfiguration configuration;

	/**
	 * Sort column options
	 */
	private enum SortColumn {

		/**
		 * FinanceTransaction_.transactionDate
		 */
		DATE,
		/**
		 * FinanceTransaction_.description
		 */
		DESCRIPTION
	};

	/**
	 * Returns all transactions in a specific range, or all transactions if page
	 * parameter is missing
	 *
	 * @param page the page number
	 * @param sortColumn the column used for sorting
	 * @param sortDirection the sort direction
	 * @param filterDescription
	 * @param filterTags the tags to be filtered
	 * @param filterDate the date to be filtered
	 * @param user the authenticated user
	 * @return the transactions
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Collection<FinanceTransactionJson> getTransactions(
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "sortColumn", required = false) SortColumn sortColumn,
			@RequestParam(value = "sortDirection", required = false) Sort.Direction sortDirection,
			@RequestParam(value = "filterDescription", required = false) String filterDescription,
			@RequestParam(value = "filterDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date filterDate,
			@RequestParam(value = "filterTags", required = false) Collection<String> filterTags,
			@AuthenticationPrincipal VogonSecurityUser user) {
		Attribute sortAttribute = FinanceTransaction_.transactionDate;
		if (sortColumn != null)
			switch (sortColumn) {
				case DATE:
					sortAttribute = FinanceTransaction_.transactionDate;
					break;
				case DESCRIPTION:
					sortAttribute = FinanceTransaction_.description;
					break;
			}
		if (sortDirection == null)
			sortDirection = Sort.Direction.fromOptionalString(null).orElse(null);
		Sort sort = new JpaSort(sortDirection, sortAttribute, FinanceTransaction_.id);
		//TODO: Optimize if https://jira.spring.io/browse/DATAJPA-209 gets implemented?
		TransactionFilterSpecification filter = new TransactionFilterSpecification(user.getUser());
		filter.setFilterDescription(filterDescription);
		filter.setFilterDate(filterDate);
		if (filterTags != null)
			filter.setFilterTags(new HashSet<>(filterTags));
		if (page == null)
			return initializationHelper.initializeTransactions(transactionRepository.findAll(filter, sort));
		PageRequest pageRequest = PageRequest.of(page, configuration.getTransactionsPageSize(), sort);
		return initializationHelper.initializeTransactions(transactionRepository.findAll(filter, pageRequest).getContent());
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
		PageRequest pageRequest = PageRequest.of(0, configuration.getTransactionsPageSize());
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
		FinanceTransaction transaction = transactionRepository.findByOwnerAndId(user.getUser(), id);
		if (transaction == null)
			throw new EntityNotFoundException(MessageFormat.format(messages.getString("TRANSACTION_DOES_NOT_EXIST"), id));
		return initializationHelper.initializeTransaction(transaction);
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
			if(existingAccount == null)
				throw new EntityNotFoundException(MessageFormat.format(messages.getString("CANNOT_SET_AN_INVALID_ACCOUNT_ID"), newComponent.getAccountId()));
			if (!existingTransaction.getComponents().contains(newComponent)) {
				TransactionComponent createdComponent = new TransactionComponent(existingAccount, existingTransaction, newComponent.getRawAmount());
				em.persist(createdComponent);
			} else {
				TransactionComponent existingComponent = existingTransaction.getComponents().get(existingTransaction.getComponents().indexOf(newComponent));
				if (newComponent.getVersion() != existingComponent.getVersion())
					throw new ConcurrentModificationException(messages.getString("TRANSACTION_WAS_ALREADY_UPDATED"));
				existingComponent.setAccount(existingAccount);
				existingComponent.setRawAmount(newComponent.getRawAmount());
				removedComponents.remove(existingComponent);
			}
			existingTransaction = transactionRepository.save(existingTransaction);
			accountRepository.save(existingAccount);
		}
		//Remove deleted components
		for (TransactionComponent removedComponent : removedComponents) {
			removedComponent.setAccount(null);
			removedComponent.setTransaction(null);
		}
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
		if (existingTransaction == null) {
			throw new EntityNotFoundException(MessageFormat.format(messages.getString("CANNOT_DELETE_A_NON_EXISTING_TRANSACTION"), id));
		}
		FinanceTransactionJson deletedTransactionJson = initializationHelper.initializeTransaction(existingTransaction);
		for (TransactionComponent component : existingTransaction.getComponents()) {
			component.setAccount(null);
			component.setTransaction(null);
		}
		transactionRepository.save(existingTransaction);
		transactionRepository.delete(existingTransaction);
		return deletedTransactionJson;
	}
}
