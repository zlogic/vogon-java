/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.analytics;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import org.zlogic.vogon.data.DatabaseManager;
import org.zlogic.vogon.data.ExpenseTransaction;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.FinanceTransaction_;

/**
 * Central class for setting report parameters and generating various reports
 *
 * @author Dmitry Zolotukhin
 */
public class Report {

	/**
	 * The financeData used to generate the report
	 */
	protected FinanceData financeData;
	/**
	 * The low cutoff date for generating the report
	 */
	protected Date earliestDate;
	/**
	 * The high cutoff date for generating the report
	 */
	protected Date latestDate;
	/**
	 * Entity manager
	 */
	protected EntityManager entityManager;

	/**
	 * Default constructor
	 *
	 * @param financeData the financeData instance to be used for generating the
	 * report
	 */
	public Report(FinanceData financeData) {
		this.financeData = financeData;
		entityManager = DatabaseManager.getInstance().createEntityManager();
	}

	/*
	 * Set report parameters and filters
	 */
	/**
	 * Returns the low cutoff date for generating the report
	 *
	 * @return the low cutoff date for generating the report
	 */
	public Date getEarliestDate() {
		return earliestDate;
	}

	/**
	 * Sets the low cutoff date for generating the report
	 *
	 * @param earliestDate the low cutoff date for generating the report
	 */
	public void setEarliestDate(Date earliestDate) {
		this.earliestDate = earliestDate;
	}

	/**
	 * Returns the high cutoff date for generating the report
	 *
	 * @return the high cutoff date for generating the report
	 */
	public Date getLatestDate() {
		return latestDate;
	}

	/**
	 * Sets the high cutoff date for generating the report
	 *
	 * @param latestDate the high cutoff date for generating the report
	 */
	public void setLatestDate(Date latestDate) {
		this.latestDate = latestDate;
	}

	/*
	 * Obtain report data
	 */
	/**
	 * Generates a text-only report
	 *
	 * @return the text-based report
	 */
	public String getTextReport() {
		String report = "";
		report += new MessageFormat("{0,date,yyyy-MM-dd}---{1,date,yyyy-MM-dd}\n").format(new Object[]{earliestDate, latestDate});
		report += "Transactions:\n";
		for (FinanceTransaction transaction : getTransactions(FinanceTransaction_.amount, false, true))
			report += new MessageFormat("{0,date,yyyy-MM-dd}: {1} {2,number,0.00}\n").format(new Object[]{transaction.getDate(), transaction.getDescription(), transaction.getAmount()});
		report += "Top tags:\n";
		List<Map.Entry<String, Double>> tagsSorted = new LinkedList(getTagExpenses().entrySet());
		Collections.sort(tagsSorted, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry<String, Double>) (o1)).getValue()).compareTo(((Map.Entry<String, Double>) (o2)).getValue());
			}
		});
		for (Map.Entry<String, Double> tag : tagsSorted)
			report += new MessageFormat("{0} {1,number,0.00}\n").format(new Object[]{tag.getKey(), tag.getValue()});
		return report;
	}

	/**
	 * Returns all transactions matching the set filters, ordered by date
	 * ascending
	 *
	 * @return list of all transactions matching the set filters, ordered by
	 * date ascending
	 */
	public List<FinanceTransaction> getTransactions() {
		return getTransactions(FinanceTransaction_.transactionDate, true, false);
	}

	/**
	 * Returns all transactions matching the set filters
	 *
	 * @param orderBy field for ordering the result
	 * @param orderAsc true if results should be ordered ascending, false if
	 * descending
	 * @param orderAbsolute true if order should be for absolute value (e.g. ABS(orderBy))
	 * @return list of all transactions matching the set filters
	 */
	public List<FinanceTransaction> getTransactions(SingularAttribute orderBy, boolean orderAsc, boolean orderAbsolute) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);
		transactionsCriteriaQuery.where(
				criteriaBuilder.and(
				criteriaBuilder.and(
				criteriaBuilder.greaterThanOrEqualTo(tr.get(FinanceTransaction_.transactionDate), earliestDate),
				criteriaBuilder.lessThanOrEqualTo(tr.get(FinanceTransaction_.transactionDate), latestDate)),
				criteriaBuilder.equal(tr.type(), ExpenseTransaction.class)));
		Expression userOrderBy = tr.get(orderBy);
		if (orderAbsolute)
			userOrderBy = criteriaBuilder.abs(userOrderBy);
		Order userOrder = orderAsc ? criteriaBuilder.asc(userOrderBy) : criteriaBuilder.desc(userOrderBy);
		transactionsCriteriaQuery.orderBy(userOrder,
				criteriaBuilder.asc(tr.get(FinanceTransaction_.id)));

		return entityManager.createQuery(transactionsCriteriaQuery).getResultList();
	}

	/**
	 * Returns expenses grouped by tags
	 *
	 * @return expenses grouped by tags
	 */
	public Map<String, Double> getTagExpenses() {
		Map<String, Double> result = new TreeMap<>();
		for (FinanceTransaction transaction : getTransactions())
			for (String tag : transaction.getTags())
				if (result.containsKey(tag))
					result.put(tag, result.get(tag) + transaction.getAmount());
				else if (!tag.isEmpty())
					result.put(tag, transaction.getAmount());
		return result;
	}
}
