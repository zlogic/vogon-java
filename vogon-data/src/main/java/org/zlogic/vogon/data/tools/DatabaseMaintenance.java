/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.tools;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceAccount_;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.FinanceTransaction_;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.data.TransactionComponent_;

/**
 * Class for performing database maintenance operations
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class DatabaseMaintenance {

	/**
	 * Deletes all orphaned transaction components.
	 *
	 * @param entityManager the EntityManager to be used for making queries;
	 * should be opened/closed outside of this function before calling this
	 * function
	 */
	public void cleanup(EntityManager entityManager) {	
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		
		CriteriaQuery<FinanceAccount> accountsCriteriaQuery = cb.createQuery(FinanceAccount.class);
		Root<FinanceAccount> account = accountsCriteriaQuery.from(FinanceAccount.class);
		account.fetch(FinanceAccount_.transactionComponents);
		accountsCriteriaQuery.where(account.get(FinanceAccount_.owner).isNull());
		accountsCriteriaQuery.distinct(true);
		for(FinanceAccount orphanedAccount : entityManager.createQuery(accountsCriteriaQuery).getResultList()){
			for(TransactionComponent orphanedComponent : new ArrayList<>(orphanedAccount.getComponents()))
				orphanedComponent.setAccount(null);
			entityManager.remove(orphanedAccount);
		}

		CriteriaQuery<FinanceTransaction> transactionCriteriaQuery = cb.createQuery(FinanceTransaction.class);
		Root<FinanceTransaction> transaction = transactionCriteriaQuery.from(FinanceTransaction.class);
		transaction.fetch(FinanceTransaction_.components);
		transactionCriteriaQuery.where(transaction.get(FinanceTransaction_.owner).isNull());
		transactionCriteriaQuery.distinct(true);
		for(FinanceTransaction orphanedTransaction : entityManager.createQuery(transactionCriteriaQuery).getResultList()){
			for(TransactionComponent orphanedComponent : new ArrayList<>(orphanedTransaction.getComponents())){
				orphanedComponent.setTransaction(null);
			}
			entityManager.remove(orphanedTransaction);
		}
		
		CriteriaQuery<TransactionComponent> componentsCriteriaQuery = cb.createQuery(TransactionComponent.class);
		Root<TransactionComponent> component = componentsCriteriaQuery.from(TransactionComponent.class);
		componentsCriteriaQuery.where(component.get(TransactionComponent_.transaction).isNull());
		for(TransactionComponent orphanedComponent : entityManager.createQuery(componentsCriteriaQuery).getResultList()){
			if (orphanedComponent.getAccount() != null)
				orphanedComponent.setAccount(null);
			entityManager.remove(orphanedComponent);
		}
	}

	/**
	 * Recalculates an account's balance based on its transactions.
	 *
	 * @param account the account to be updated
	 * @param entityManager the EntityManager to be used for making queries;
	 * should be opened/closed outside of this function before calling this
	 * function
	 */
	public void refreshAccountBalance(FinanceAccount account, EntityManager entityManager) {
		FinanceAccount tempAccount = entityManager.find(FinanceAccount.class, account.getId());
		List<TransactionComponent> accountComponents = new ArrayList(tempAccount.getComponents());
		//Remove all components to force reset balance
		for (TransactionComponent component : accountComponents)
			component.setAccount(null);
		//Restore all components
		for (TransactionComponent component : accountComponents)
			component.setAccount(tempAccount);
	}
}
