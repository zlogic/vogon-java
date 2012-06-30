/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.util.List;

import javax.persistence.EntityManager;
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

		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		CriteriaQuery<FinanceAccount> accountsCriteriaQuery = criteriaBuilder.createQuery(FinanceAccount.class);
		
		transactions = entityManager.createQuery(transactionsCriteriaQuery).getResultList();
		accounts = entityManager.createQuery(accountsCriteriaQuery).getResultList();
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
	}


	/**
	 * Sets a new account name. Adds the account to the persistence if needed.
	 * 
	 * @param account The account to be updated
	 * @param name The new account name
	 */
	public void setAccountName(FinanceAccount account,String name){
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		entityManager.getTransaction().begin();
		account.setName(name);

		if(!entityManager.contains(account))
			entityManager.persist(account);
		entityManager.getTransaction().commit();
	}
	
	/**
	 * Deletes an account (with all dependencies)
	 * 
	 * @param account The account to delete
	 */
	public void deleteAccount(FinanceAccount account){
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		entityManager.getTransaction().begin();
		for(FinanceTransaction transaction : transactions){
			List<TransactionComponent> components = transaction.getComponentsForAccount(account);
			transaction.removeComponents(components);
			for(TransactionComponent component:components)
				entityManager.remove(component);
		}
		accounts.remove(account);
		entityManager.remove(account);
		entityManager.getTransaction().commit();
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
