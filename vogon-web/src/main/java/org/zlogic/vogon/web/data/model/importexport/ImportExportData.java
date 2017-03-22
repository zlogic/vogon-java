/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.data.model.importexport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.web.data.model.TransactionComponentJson;

/**
 * JSON wrapper for user data, used only for Data Import/Export operations
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class ImportExportData {

	/**
	 * Accounts to be exported/imported
	 */
	private List<ExportAccountJson> accounts = new ArrayList<>();
	/**
	 * Transactions to be exported/imported
	 */
	private List<ExportFinanceTransactionJson> transactions = new ArrayList<>();

	/**
	 * Default constructor
	 */
	protected ImportExportData() {
		super();
	}

	/**
	 * Wraps the FinanceAccount and FinanceTransaction lists into an
	 * ImportExportData class
	 *
	 * @param accounts the accounts to use
	 * @param transactions the transactions to use
	 */
	public ImportExportData(Collection<FinanceAccount> accounts, Collection<FinanceTransaction> transactions) {
		convert(accounts, transactions);
	}

	/**
	 * Converts FinanceAccount and FinanceTransaction into a format used by
	 * ImportExportData
	 *
	 * @param sourceAccounts the accounts to convert
	 * @param sourceTransactions the transactions to convert
	 */
	private void convert(Collection<FinanceAccount> sourceAccounts, Collection<FinanceTransaction> sourceTransactions) {
		Map<Long, Long> accountRemapping = new HashMap<>();

		int accountIndex = 1;
		for (FinanceAccount account : sourceAccounts) {
			accountRemapping.put(account.getId(), Long.valueOf(accountIndex));
			accounts.add(new ExportAccountJson(account, accountIndex));
			accountIndex++;
		}

		for (FinanceTransaction sourceTransaction : sourceTransactions) {
			List<TransactionComponentJson> components = new ArrayList<>();
			for (TransactionComponent sourceComponent : sourceTransaction.getComponents()) {
				ExportTransactionComponentJson component = new ExportTransactionComponentJson(sourceComponent, accountRemapping.get(sourceComponent.getAccount().getId()));
				components.add(component);
			}
			ExportFinanceTransactionJson transaction = new ExportFinanceTransactionJson(sourceTransaction);
			transaction.setComponentsJson(components);
			transactions.add(transaction);
		}
	}

	/**
	 * Converts all Accounts and Transactions into Entity classes and assigns
	 * them to the specified user
	 *
	 * @param user user to whom Accounts and Transactions should be assigned
	 * @param em EntityManager instance to use when persisting Entity classes
	 */
	public void persist(VogonUser user, EntityManager em) {

		Map<Long, FinanceAccount> accountRemapping = new HashMap<>();
		for (ExportAccountJson account : accounts) {
			FinanceAccount convertedAccount = new FinanceAccount(user, account);
			accountRemapping.put(account.getId(), convertedAccount);
			em.persist(convertedAccount);
		}

		for (ExportFinanceTransactionJson transaction : transactions) {
			FinanceTransaction convertedTransaction = new FinanceTransaction(user, transaction);
			em.persist(convertedTransaction);

			for (TransactionComponent component : transaction.getComponentsJson()) {
				TransactionComponent convertedComponent = new TransactionComponent(accountRemapping.get(((TransactionComponentJson) component).getAccountId()), convertedTransaction, component.getRawAmount());
				em.persist(convertedComponent);
			}
			em.merge(convertedTransaction);
		}
		for (FinanceAccount account : accountRemapping.values()) {
			em.merge(account);
		}
	}

	/**
	 * Returns the accounts to be exported/imported
	 *
	 * @return the accounts to be exported/imported
	 */
	public List<ExportAccountJson> getAccounts() {
		return accounts;
	}

	/**
	 * Sets the accounts to be exported/imported
	 *
	 * @param accounts the accounts to be exported/imported
	 */
	public void setAccounts(List<ExportAccountJson> accounts) {
		this.accounts = accounts;
	}

	/**
	 * Returns the transactions to be exported/imported
	 *
	 * @return the transactions to be exported/imported
	 */
	public List<ExportFinanceTransactionJson> getTransactions() {
		return transactions;
	}

	/**
	 * Sets the transactions to be exported/imported
	 *
	 * @param transactions the transactions to be exported/imported
	 */
	public void setTransactions(List<ExportFinanceTransactionJson> transactions) {
		this.transactions = transactions;
	}

}
