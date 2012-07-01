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
	 * Sets a new account name. Adds the account to the persistence if needed.
	 * 
	 * @param account The account to be updated
	 * @param name The new account name
	 */
	public void setAccountName(FinanceAccount account,String name){
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		entityManager.getTransaction().begin();
		account.setName(name);

		if(!accounts.contains(account))
			accounts.add(account);

		if(!entityManager.contains(account))
			entityManager.persist(account);
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

		if(!transactions.contains(transaction)){
			transactions.add(transaction);
			for(TransactionComponent component : transaction.getComponents())
				if(!entityManager.contains(component))
					entityManager.persist(component);
		}

		if(!entityManager.contains(transaction))
			entityManager.persist(transaction);
		entityManager.getTransaction().commit();
	}

	/**
	 * Sets a new date for a transaction
	 * 
	 * @param transaction The transaction to be updated
	 * @param tags The new date
	 */
	public void setTransactionDate(FinanceTransaction transaction,Date date){
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		entityManager.getTransaction().begin();
		transaction.setDate(date);

		if(!transactions.contains(transaction)){
			transactions.add(transaction);
			for(TransactionComponent component : transaction.getComponents())
				if(!entityManager.contains(component))
					entityManager.persist(component);
		}

		if(!entityManager.contains(transaction))
			entityManager.persist(transaction);
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

		if(!transactions.contains(transaction)){
			transactions.add(transaction);
			for(TransactionComponent component : transaction.getComponents())
				if(!entityManager.contains(component))
					entityManager.persist(component);
		}

		if(!entityManager.contains(transaction))
			entityManager.persist(transaction);
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

		transaction.updateComponentRawAmount(component,(long)(newAmount*100));

		if(!transactions.contains(transaction)){
			transactions.add(transaction);
			for(TransactionComponent component_ : transaction.getComponents())
				if(!entityManager.contains(component_))
					entityManager.persist(component_);
		}

		if(!entityManager.contains(transaction))
			entityManager.persist(transaction);

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

		if(!transactions.contains(transaction)){
			transactions.add(transaction);
			for(TransactionComponent component_ : transaction.getComponents())
				if(!entityManager.contains(component_))
					entityManager.persist(component_);
		}

		if(!entityManager.contains(transaction))
			entityManager.persist(transaction);

		if(!entityManager.contains(component))
			entityManager.persist(component);

		if(!entityManager.contains(newAccount))
			entityManager.persist(newAccount);
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
			component.getTransaction().updateComponentRawAmount(component,(long)(newAmount*100));
		else{
			component.setRawAmount((long)(newAmount*100));
			component.getTransaction().addComponent(component);
		}

		if(!transactions.contains(component.getTransaction())){
			transactions.add(component.getTransaction());
			for(TransactionComponent component_ : component.getTransaction().getComponents())
				if(!entityManager.contains(component_))
					entityManager.persist(component_);
		}

		if(!entityManager.contains(component.getTransaction()))
			entityManager.persist(component.getTransaction());

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

		if(!transactions.contains(component.getTransaction())){
			transactions.add(component.getTransaction());
			for(TransactionComponent component_ : component.getTransaction().getComponents())
				if(!entityManager.contains(component_))
					entityManager.persist(component_);
		}

		if(!entityManager.contains(component.getTransaction()))
			entityManager.persist(component.getTransaction());

		if(!entityManager.contains(component))
			entityManager.persist(component);

		if(!entityManager.contains(newAccount))
			entityManager.persist(newAccount);
		entityManager.getTransaction().commit();
	}

	/**
	 * Deletes a transaction component (with all dependencies)
	 * 
	 * @param account The transaction to delete
	 */
	public void deleteTransactionComponent(TransactionComponent component){
		EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
		entityManager.getTransaction().begin();

		if(!transactions.contains(component.getTransaction())){
			transactions.add(component.getTransaction());
			for(TransactionComponent component_ : component.getTransaction().getComponents())
				if(!entityManager.contains(component_))
					entityManager.persist(component_);
		}

		component.getTransaction().removeComponent(component);

		entityManager.remove(component);
		entityManager.getTransaction().commit();
	}

	/**
	 * Deletes a transaction (with all dependencies)
	 * 
	 * @param account The transaction to delete
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
