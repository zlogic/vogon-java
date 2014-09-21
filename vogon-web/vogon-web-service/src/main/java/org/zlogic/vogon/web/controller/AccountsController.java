/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.web.data.AccountRepository;
import org.zlogic.vogon.web.data.TransactionRepository;
import org.zlogic.vogon.web.security.VogonSecurityUser;

/**
 * Spring MVC controller for accounts
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Controller
@RequestMapping(value = "/service/accounts")
@Transactional(propagation = Propagation.REQUIRED)
public class AccountsController {

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
	 * Returns all accounts
	 *
	 * @param user the authenticated user
	 * @return the accounts
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Collection<FinanceAccount> getAllAccounts(@AuthenticationPrincipal VogonSecurityUser user) {
		return accountRepository.findByOwner(user.getUser());
	}

	/**
	 * Updates account list
	 *
	 * @param accounts the new updated accounts list
	 * @param user the authenticated user
	 * @return the accounts list from database after update
	 */
	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Collection<FinanceAccount> updateAccounts(@RequestBody Collection<FinanceAccount> accounts, @AuthenticationPrincipal VogonSecurityUser user) {
		List<FinanceAccount> existingAccounts = new ArrayList<>(accountRepository.findByOwner(user.getUser()));
		LinkedList<FinanceAccount> removedAccounts = new LinkedList<>(existingAccounts);
		//Merge with database
		for (FinanceAccount newAccount : accounts) {
			if (newAccount.getId() == null || !existingAccounts.contains(newAccount)) {
				//TODO: add functionality to initialise account to FinanceAccount class
				FinanceAccount createdAccount = new FinanceAccount(user.getUser(), newAccount.getName(), newAccount.getCurrency());
				accountRepository.save(createdAccount);
			} else {
				//TODO: add functionality to merge existing account component to FinanceAccount class
				FinanceAccount existingAccount = existingAccounts.get(existingAccounts.indexOf(newAccount));
				existingAccount.setOwner(user.getUser());
				existingAccount.setName(newAccount.getName());
				existingAccount.setCurrency(newAccount.getCurrency());
				existingAccount.setIncludeInTotal(newAccount.getIncludeInTotal());
				existingAccount.setShowInList(newAccount.getShowInList());
				removedAccounts.remove(newAccount);
			}
		}
		//Delete removed accounts
		for (FinanceAccount removedAccount : removedAccounts) {
			accountRepository.delete(removedAccount);
			//Delete all related transaction components
			for (FinanceTransaction transaction : transactionRepository.findByOwner(user.getUser())) {
				boolean save = false;
				for (TransactionComponent component : transaction.getComponentsForAccount(removedAccount)) {
					transaction.removeComponent(component);
					save = true;
				}
				if (save)
					transactionRepository.save(transaction);
			}
		}
		accountRepository.flush();
		transactionRepository.flush();
		return accountRepository.findByOwner(user.getUser());
	}
}
