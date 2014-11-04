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
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;

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
		CriteriaBuilder componentCriteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<TransactionComponent> componentsCriteriaQuery = componentCriteriaBuilder.createQuery(TransactionComponent.class);
		componentsCriteriaQuery.from(TransactionComponent.class);

		CriteriaBuilder transactionCriteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = transactionCriteriaBuilder.createQuery(FinanceTransaction.class);
		transactionsCriteriaQuery.from(FinanceTransaction.class);

		//Get all data from DB
		List<TransactionComponent> componentsDB = entityManager.createQuery(componentsCriteriaQuery).getResultList();
		List<FinanceTransaction> transactionsDB = entityManager.createQuery(transactionsCriteriaQuery).getResultList();

		//Remove OK items from list
		for (FinanceTransaction transaction : transactionsDB)
			componentsDB.removeAll(transaction.getComponents());

		//Remove anything that still exists
		for (TransactionComponent component : componentsDB) {
			if (component.getTransaction() != null)
				component.getTransaction().removeComponent(component);
			component.setTransaction(null);
			entityManager.remove(component);
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
