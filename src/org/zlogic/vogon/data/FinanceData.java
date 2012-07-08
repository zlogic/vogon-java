/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;


/**
 * Class for storing the complete finance data
 *
 * @author Dmitry Zolotukhin
 */
@Entity
public class FinanceData {
	/**
	 * The transaction ID (only for persistence)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected long id;
	/**
	 * Contains all finance transactions
	 */
	@OneToMany
	@OrderBy("transactionDate ASC,id ASC")
	protected java.util.List<FinanceTransaction> transactions;
	/**
	 * Contains all accounts
	 */
	@OneToMany
	protected java.util.List<FinanceAccount> accounts;
	/**
	 * Contains a list of all tags (cached)
	 */
	protected java.util.Set<String> tags;

	/**
	 * Default constructor
	 */
	public FinanceData() {
	}

	/**
	 * Imports and persists data into this instance by using the output of the specified FileImporter
	 * 
	 * @param importer A configured FileImported instance
	 * @throws VogonImportLogicalException 
	 * @throws VogonImportException 
	 */
	public void importData(FileImporter importer) throws VogonImportException, VogonImportLogicalException{
		FinanceData newFinanceData = importer.importFile();

		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		
		entityManager.getTransaction().begin();
		
		for(FinanceTransaction transaction : newFinanceData.transactions){
			entityManager.persist(transaction);
			for(TransactionComponent component : transaction.getComponents())
				entityManager.persist(component);
		}
		
		for(FinanceAccount account : newFinanceData.accounts)
			if(!entityManager.contains(account)){
				entityManager.persist(account);
				accounts.add(account);
			}
		
		transactions.addAll(newFinanceData.transactions);
		
		entityManager.getTransaction().commit();
	}
	
	/**
	 * Restores all data from the persistence database
	 * 
	 * @return The restored FinanceData instance
	 */
	public static FinanceData restoreFromDatabase(){
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceData> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceData.class);

		List <FinanceData> data = entityManager.createQuery(transactionsCriteriaQuery).getResultList();

		if(data.isEmpty()){
			FinanceData newFinanceData = new FinanceData();
			newFinanceData.transactions = new java.util.LinkedList<>();
			newFinanceData.accounts = new java.util.LinkedList<>();
			entityManager.getTransaction().begin();
			entityManager.persist(newFinanceData);
			entityManager.getTransaction().commit();
			return newFinanceData;
		}else
			return data.get(0);
	}

	/**
	 * Constructs FinanceData from pre-populated arrays (e.g. when importing
	 * data)
	 *
	 * @param transactions Array of financial transactions
	 * @param accounts Array of accounts
	 */
	public FinanceData(java.util.ArrayList<FinanceTransaction> transactions, java.util.ArrayList<FinanceAccount> accounts) {
		this.transactions = new java.util.LinkedList<>();
		this.transactions.addAll(transactions);
		this.accounts = new java.util.LinkedList<>();
		this.accounts.addAll(accounts);
	}

	/**
	 * Internal helper function
	 * Adds an account to the list & persists it (if necessary)
	 * Safe to call even if the account already exists
	 * Should only be called from an started transaction
	 * 
	 * @param account the account to be added
	 * @param entityManager the entity manager
	 */
	protected void persistenceAddAccount(FinanceAccount account,EntityManager entityManager){
		if(account==null)
			return;
		
		if(!accounts.contains(account))
			accounts.add(account);

		if(!entityManager.contains(account))
			entityManager.persist(account);
	}
	
	/**
	 * Internal helper function
	 * Adds a transaction to the list & persists it and its components (if necessary)
	 * Safe to call even if the transaction already exists
	 * Should only be called from an started transaction
	 * 
	 * @param transaction the transaction to be added
	 * @param entityManager the entity manager
	 */
	protected void persistenceAddTransaction(FinanceTransaction transaction,EntityManager entityManager){
		if(transaction==null)
			return;
		
		if(!transactions.contains(transaction)){
			transactions.add(transaction);
			for(TransactionComponent component : transaction.getComponents())
				if(!entityManager.contains(component))
					entityManager.persist(component);
		}

		if(!entityManager.contains(transaction))
			entityManager.persist(transaction);
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

		persistenceAddAccount(account,entityManager);
		
		entityManager.getTransaction().commit();
	}

	/**
	 * Sets new tags for a transaction
	 * 
	 * @param transaction The transaction to be updated
	 * @param tags The new tags
	 */
	public void setTransactionTags(FinanceTransaction transaction,String[] tags){
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		entityManager.getTransaction().begin();
		transaction.setTags(tags);

		persistenceAddTransaction(transaction,entityManager);

		entityManager.getTransaction().commit();
	}

	/**
	 * Sets a new date for a transaction
	 * 
	 * @param transaction The transaction to be updated
	 * @param date The new date
	 */
	public void setTransactionDate(FinanceTransaction transaction,Date date){
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		entityManager.getTransaction().begin();
		transaction.setDate(date);

		persistenceAddTransaction(transaction,entityManager);

		entityManager.getTransaction().commit();
	}

	/**
	 * Sets a new description for a transaction
	 * 
	 * @param transaction The transaction to be updated
	 * @param description The new description
	 */
	public void setTransactionDescription(FinanceTransaction transaction,String description){
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		entityManager.getTransaction().begin();
		transaction.setDescription(description);

		persistenceAddTransaction(transaction,entityManager);

		entityManager.getTransaction().commit();
	}

	/**
	 * Sets an expense transaction amount, works only for single-component expense transactions
	 * 
	 * @param transaction The transaction to be updated
	 * @param newAmount The new amount
	 */
	public void setTransactionAmount(ExpenseTransaction transaction,double newAmount){
		if(transaction.getComponentsCount()!=1)
			return;

		TransactionComponent component = transaction.getComponents().get(0);
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		entityManager.getTransaction().begin();

		transaction.updateComponentRawAmount(component,(long)Math.round(newAmount*100));

		persistenceAddTransaction(transaction,entityManager);

		if(!entityManager.contains(component))
			entityManager.persist(component);
		entityManager.getTransaction().commit();
	}

	/**
	 * Sets an expense transaction account, works only for single-component expense transactions
	 * 
	 * @param transaction The transaction to be updated
	 * @param newAccount The new account
	 */
	public void setTransactionAccount(ExpenseTransaction transaction,FinanceAccount newAccount){
		if(transaction.getComponentsCount()!=1)
			return;

		TransactionComponent component = transaction.getComponents().get(0);
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		entityManager.getTransaction().begin();

		transaction.updateComponentAccount(component,newAccount);

		persistenceAddTransaction(transaction,entityManager);

		if(!entityManager.contains(component))
			entityManager.persist(component);

		persistenceAddAccount(newAccount,entityManager);
		
		entityManager.getTransaction().commit();
	}


	/**
	 * Sets an transaction component amount
	 * 
	 * @param component The component to be updated
	 * @param newAmount The new amount
	 */
	public void setTransactionComponentAmount(TransactionComponent component,double newAmount){
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		entityManager.getTransaction().begin();

		if(component.getTransaction().getComponents().contains(component))
			component.getTransaction().updateComponentRawAmount(component,(long)Math.round(newAmount*100));
		else{
			component.setRawAmount((long)Math.round(newAmount*100));
			component.getTransaction().addComponent(component);
		}

		persistenceAddTransaction(component.getTransaction(),entityManager);

		if(!entityManager.contains(component))
			entityManager.persist(component);
		entityManager.getTransaction().commit();
	}

	/**
	 * Sets an transaction component account
	 * 
	 * @param component The component to be updated
	 * @param newAccount The new account
	 */
	public void setTransactionComponentAccount(TransactionComponent component,FinanceAccount newAccount){
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		entityManager.getTransaction().begin();

		if(component.getTransaction().getComponents().contains(component))
			component.getTransaction().updateComponentAccount(component,newAccount);
		else{
			component.setAccount(newAccount);
			component.getTransaction().addComponent(component);
		}


		persistenceAddTransaction(component.getTransaction(),entityManager);

		if(!entityManager.contains(component))
			entityManager.persist(component);

		persistenceAddAccount(newAccount,entityManager);
		entityManager.getTransaction().commit();
	}

	/**
	 * Deletes a transaction component (with all dependencies)
	 * 
	 * @param component The transaction component to delete
	 */
	public void deleteTransactionComponent(TransactionComponent component){
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		entityManager.getTransaction().begin();

		persistenceAddTransaction(component.getTransaction(),entityManager);

		component.getTransaction().removeComponent(component);

		entityManager.remove(component);
		entityManager.getTransaction().commit();
	}

	/**
	 * Deletes a transaction (with all dependencies)
	 * 
	 * @param transaction The transaction to delete
	 */
	public void deleteTransaction(FinanceTransaction transaction){
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		entityManager.getTransaction().begin();

		transactions.remove(transaction);

		for(TransactionComponent component : transaction.getComponents())
			entityManager.remove(component);
		transaction.removeAllComponents();

		entityManager.remove(transaction);
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
	
	/**
	 * Recalculates an account's balance based on its transactions
	 * 
	 * @param account the account to be updated
	 */
	public void refreshAccountBalance(FinanceAccount account){
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		entityManager.getTransaction().begin();
		account.updateRawBalance(-account.getRawBalance());
		for(FinanceTransaction transaction:transactions){
			for(TransactionComponent component:transaction.getComponentsForAccount(account)){
				account.updateRawBalance(component.getRawAmount());
			}
		}
		entityManager.getTransaction().commit();
	}
	
	/**
	 * Deletes all orphaned transactions, accounts and transaction components
	 */
	public void cleanup(){
		//TODO: test this!!
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		CriteriaQuery<FinanceAccount> accountsCriteriaQuery = criteriaBuilder.createQuery(FinanceAccount.class);
		CriteriaQuery<TransactionComponent> componentsCriteriaQuery = criteriaBuilder.createQuery(TransactionComponent.class);
		
		//Get all data from DB
		List<FinanceTransaction> transactionsDB = entityManager.createQuery(transactionsCriteriaQuery).getResultList();
		List<FinanceAccount> accountsDB = entityManager.createQuery(accountsCriteriaQuery).getResultList();
		List<TransactionComponent> componentsDB = entityManager.createQuery(componentsCriteriaQuery).getResultList();

		//Remove OK items from list
		transactionsDB.removeAll(transactions);
		accountsDB.removeAll(accounts);
		for(FinanceTransaction transaction : transactions)
			componentsDB.removeAll(transaction.getComponents());

		transactions.removeAll(transactionsDB);
		accounts.removeAll(accountsDB);
		
		entityManager.getTransaction().begin();
		for(FinanceTransaction transaction : transactionsDB){
			for(TransactionComponent component : transaction.getComponents())
				entityManager.remove(component);
			componentsDB.removeAll(transaction.getComponents());
			transaction.removeAllComponents();
			entityManager.remove(transaction);
		}
		
		for(FinanceAccount account : accountsDB)
			entityManager.remove(account);
		
		for(TransactionComponent component : componentsDB){
			if(component.getTransaction()!=null)
				component.getTransaction().removeComponent(component);
			component.setTransaction(null);
			entityManager.remove(component);
		}
		
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
