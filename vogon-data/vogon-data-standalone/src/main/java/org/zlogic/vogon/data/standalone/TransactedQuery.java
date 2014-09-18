/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.standalone;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

/**
 * Interface for allowing to perform a custom modification in an
 * EntityManager-managed transaction.
 *
 * @param <ElementType> element type for query
 * @param <ResultType> return result type
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public interface TransactedQuery<ElementType, ResultType> {

	/**
	 * Return or build a query. CriteriaBuilder is already initialized and the
	 * transaction is started by PersistenceHelper.performTransactedQuery().
	 *
	 * @param criteriaBuilder the criteria builder used for this query
	 * @return the created criteria query
	 */
	public CriteriaQuery<ElementType> getQuery(CriteriaBuilder criteriaBuilder);

	/**
	 * Performs the query and returns the result.
	 *
	 * @param preparedQuery the previously prepared query (made by getQuery)
	 * @return thew query result
	 */
	public ResultType getQueryResult(TypedQuery<ElementType> preparedQuery);
}
