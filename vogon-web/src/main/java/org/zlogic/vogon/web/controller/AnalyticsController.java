/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.report.Report;
import org.zlogic.vogon.data.report.ReportFactory;
import org.zlogic.vogon.web.data.AccountRepository;
import org.zlogic.vogon.web.security.VogonSecurityUser;

/**
 * Spring MVC controller for analytics
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Controller
@RequestMapping(value = "/service/analytics")
public class AnalyticsController {

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
	 * Returns all tags
	 *
	 * @param user the authenticated user
	 * @return the set of all tags
	 */
	@RequestMapping(value = "/tags", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Set<String> getAllTags(@AuthenticationPrincipal VogonSecurityUser user) {
		ReportFactory reportFactory = new ReportFactory(user.getUser());
		return reportFactory.getAllTags(em);
	}

	/**
	 * Returns the report results
	 *
	 * @param reportFactory the requested report parameters
	 * @param user the authenticated user
	 * @return the report
	 */
	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Map<String, Report> getReport(@RequestBody ReportFactory reportFactory, @AuthenticationPrincipal VogonSecurityUser user) {
		reportFactory.setOwner(user.getUser());
		//Update accounts (needed to properly handle users)
		List<FinanceAccount> accounts = new ArrayList<>(reportFactory.getSelectedAccounts().size());
		for (FinanceAccount account : reportFactory.getSelectedAccounts()) {
			account = accountRepository.findByOwnerAndId(user.getUser(), account.getId());
			if (account != null)
				accounts.add(account);
		}
		reportFactory.setSelectedAccounts(accounts);
		//Build report
		return reportFactory.buildReport(em);
	}
}
