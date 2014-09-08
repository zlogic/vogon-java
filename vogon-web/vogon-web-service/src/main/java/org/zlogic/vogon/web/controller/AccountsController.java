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
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.web.data.AccountRepository;
import org.zlogic.vogon.web.security.VogonSecurityUser;

/**
 * Spring MVC controller for accounts
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Controller
@RequestMapping(value = "/service/accounts")
@Transactional
public class AccountsController {

	/**
	 * The EntityManager instance
	 */
	@PersistenceContext
	private EntityManager em;

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
}
