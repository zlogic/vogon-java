/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

/**
 * Class for storing the complete finance data
 *
 * @author Dmitry Zolotukhin
 */
public class FinanceData {

	/**
	 * Contains all finance transactions
	 */
	protected java.util.List<FinanceTransaction> transactions;
	/**
	 * Contains all accounts
	 */
	protected java.util.List<FinanceAccount> accounts;
	/**
	 * Contains a list of all tags (cached)
	 */
	protected java.util.Set<String> tags;

	/**
	 * Default constructor
	 */
	public FinanceData() {
		transactions = new java.util.ArrayList<>();
		accounts = new java.util.ArrayList<>();

		EntityManagerFactory entityManagerFactory = new DatabaseManager().getPersistenceUnit();
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		CriteriaQuery<FinanceAccount> accountsCriteriaQuery = criteriaBuilder.createQuery(FinanceAccount.class);
		
		transactions = entityManager.createQuery(transactionsCriteriaQuery).getResultList();
		accounts = entityManager.createQuery(accountsCriteriaQuery).getResultList();

		setClassReferences();
	}

	/**
	 * Constructs FinanceData from pre-populated arrays (e.g. when importing
	 * data)
	 *
	 * @param transactions Array of financial transactions
	 * @param accounts Array of accounts
	 */
	public FinanceData(java.util.ArrayList<FinanceTransaction> transactions, java.util.ArrayList<FinanceAccount> accounts) {
		this.transactions = new java.util.ArrayList<>();
		this.transactions.addAll(transactions);
		this.accounts = new java.util.ArrayList<>();
		this.accounts.addAll(accounts);
		//Set class references
		setClassReferences();
		//Extract tags
	}

	/**
	 * Sets the references (e.g. to this for accounts) for classes
	 */
	private void setClassReferences() {
		for (FinanceAccount account : this.accounts)
			account.setFinanceData(this);
	}

	/**
	 * Updates tag list
	 */
	protected void updateTags() {
		for (FinanceTransaction transaction : this.transactions)
			tags.addAll(Arrays.asList(transaction.getTags()));
	}

	/**
	 * Calculates the account balance from its transactions
	 *
	 * @param account The account to check balanse
	 * @return The account's balance
	 */
	public double getActualBalance(FinanceAccount account) {
		double balance = 0;
		for (FinanceTransaction transaction : transactions)
			balance += transaction.getAccountAction(account);
		return balance;
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns a transaction at position i
	 *
	 * @param i The index
	 * @return A transaction
	 */
	public FinanceTransaction getTransaction(int i) {
		return transactions.get(i);
	}

	/**
	 * Returns the number of transactions
	 *
	 * @return The number of transactions
	 */
	public int getNumTransactions() {
		return transactions.size();
	}

	/**
	 * Returns an account at position i
	 *
	 * @param i The index
	 * @return An account
	 */
	public FinanceAccount getAccount(int i) {
		return accounts.get(i);
	}

	/**
	 * Returns the number of accounts
	 *
	 * @return The number of accounts
	 */
	public int getNumAccounts() {
		return accounts.size();
	}
}
