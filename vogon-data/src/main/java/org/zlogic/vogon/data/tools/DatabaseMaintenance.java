/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.tools;

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
			for(TransactionComponent orphanedComponent : orphanedAccount.getComponents())
				orphanedComponent.setAccount(null);
			entityManager.remove(orphanedAccount);
		}

		CriteriaQuery<FinanceTransaction> transactionCriteriaQuery = cb.createQuery(FinanceTransaction.class);
		Root<FinanceTransaction> transaction = transactionCriteriaQuery.from(FinanceTransaction.class);
		transaction.fetch(FinanceTransaction_.components);
		transactionCriteriaQuery.where(transaction.get(FinanceTransaction_.owner).isNull());
		transactionCriteriaQuery.distinct(true);
		for(FinanceTransaction orphanedTransaction : entityManager.createQuery(transactionCriteriaQuery).getResultList()){
			for(TransactionComponent orphanedComponent : orphanedTransaction.getComponents())
				orphanedComponent.setTransaction(null);
			entityManager.remove(orphanedTransaction);
		}
		
		CriteriaQuery<TransactionComponent> componentsCriteriaQuery = cb.createQuery(TransactionComponent.class);
		Root<TransactionComponent> component = componentsCriteriaQuery.from(TransactionComponent.class);
		componentsCriteriaQuery.where(component.get(TransactionComponent_.transaction).isNull());
		for(TransactionComponent orphanedComponent : entityManager.createQuery(componentsCriteriaQuery).getResultList())
			entityManager.remove(orphanedComponent);
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
		//Request all transactions from database
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		transactionsCriteriaQuery.from(FinanceTransaction.class);
		FinanceAccount tempAccount = entityManager.find(FinanceAccount.class, account.getId());

		//Recalculate balance from related transactions
		tempAccount.updateRawBalance(-tempAccount.getRawBalance());

		TypedQuery<FinanceTransaction> transactionsBatchQuery = entityManager.createQuery(transactionsCriteriaQuery);

		List<FinanceTransaction> transactions = transactionsBatchQuery.getResultList();
		for (FinanceTransaction transaction : transactions)
			for (TransactionComponent component : transaction.getComponentsForAccount(tempAccount))
				tempAccount.updateRawBalance(component.getRawAmount());
	}
}
