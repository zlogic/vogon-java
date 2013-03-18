/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

/**
 * Interface for allowing to perform a custom modification in an
 * EntityManager-managed transaction.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public interface TransactedQuery<ElementType,ResultType> {

	/**
	 * Return or build a query. CriteriaBuilder is already initialized and the
	 * transaction is started by PersistenceHelper.performTransactedQuery().
	 *
	 * @param criteriaBuilder the criteria builder used for this query
	 */
	public CriteriaQuery<ElementType> getQuery(CriteriaBuilder criteriaBuilder);
	public ResultType getQueryResult(TypedQuery<ElementType> preparedQuery);
}
