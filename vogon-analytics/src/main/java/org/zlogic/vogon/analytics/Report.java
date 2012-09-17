/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.analytics;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import org.zlogic.vogon.data.DatabaseManager;
import org.zlogic.vogon.data.ExpenseTransaction;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.FinanceTransaction_;
import org.zlogic.vogon.data.TransferTransaction;

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
	 * Selected tags
	 */
	protected List<String> selectedTags;
	/**
	 * Show expense transactions
	 */
	protected boolean enabledExpenseTransactions;
	/**
	 * Show income transactions
	 */
	protected boolean enabledIncomeTransactions;
	/**
	 * Show transfer transactions
	 */
	protected boolean enabledTransferTransactions;

	/**
	 * Default constructor
	 *
	 * @param financeData the financeData instance to be used for generating the
	 * report
	 */
	public Report(FinanceData financeData) {
		this();
		this.financeData = financeData;
		entityManager = DatabaseManager.getInstance().createEntityManager();
	}

	/**
	 * Default constructor
	 *
	 */
	public Report() {
		//Prepare start/end dates
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		earliestDate = calendar.getTime();
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		latestDate = calendar.getTime();
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

	/**
	 * Returns the tags to be included in the report. Empty list means all tags
	 * will be used.
	 *
	 * @return the tags to be included in the report
	 */
	public List<String> getSelectedTags() {
		return selectedTags != null ? selectedTags : new LinkedList<String>();
	}

	/**
	 * Sets the tags to be included in the report. Null or empty means all tags
	 * will be used.
	 *
	 * @param selectedTags the tags to be included in the report
	 */
	public void setSelectedTags(List<String> selectedTags) {
		if (selectedTags == null || selectedTags.isEmpty())
			this.selectedTags = null;
		else
			this.selectedTags = selectedTags;
	}

	/**
	 * Sets the tags to be included in the report. Null or empty means all tags
	 * will be used.
	 *
	 * @param selectedTags the tags to be included in the report
	 */
	public void setSelectedTags(String[] selectedTags) {
		if (selectedTags == null || selectedTags.length == 0)
			this.selectedTags = null;
		else
			this.selectedTags = Arrays.asList(selectedTags);
	}

	/**
	 * Returns if expense transactions will be included in the report
	 *
	 * @return true if expense transactions will be included in the report
	 */
	public boolean isEnabledExpenseTransactions() {
		return enabledExpenseTransactions;
	}

	/**
	 * Sets if expense transactions will be included in the report
	 *
	 * @param enabledExpenseTransactions true if expense transactions should be
	 * included in the report
	 */
	public void setEnabledExpenseTransactions(boolean enabledExpenseTransactions) {
		this.enabledExpenseTransactions = enabledExpenseTransactions;
	}

	/**
	 * Returns if income transactions will be included in the report
	 *
	 * @return true if income transactions will be included in the report
	 */
	public boolean isEnabledIncomeTransactions() {
		return enabledIncomeTransactions;
	}

	/**
	 * Sets if income transactions will be included in the report
	 *
	 * @param enabledIncomeTransactions true if income transactions should be
	 * included in the report
	 */
	public void setEnabledIncomeTransactions(boolean enabledIncomeTransactions) {
		this.enabledIncomeTransactions = enabledIncomeTransactions;
	}

	/**
	 * Returns if transfer transactions will be included in the report
	 *
	 * @return true if transfer transactions will be included in the report
	 */
	public boolean isEnabledTransferTransactions() {
		return enabledTransferTransactions;
	}

	/**
	 * Sets if transfer transactions will be included in the report
	 *
	 * @param enabledTransferTransactions true if transfer transactions should
	 * be included in the report
	 */
	public void setEnabledTransferTransactions(boolean enabledTransferTransactions) {
		this.enabledTransferTransactions = enabledTransferTransactions;
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
	 * @param orderAbsolute true if order should be for absolute value (e.g.
	 * ABS(orderBy))
	 * @return list of all transactions matching the set filters
	 */
	public List<FinanceTransaction> getTransactions(SingularAttribute orderBy, boolean orderAsc, boolean orderAbsolute) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);

		Predicate datePredicate = criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(tr.get(FinanceTransaction_.transactionDate), earliestDate),
				criteriaBuilder.lessThanOrEqualTo(tr.get(FinanceTransaction_.transactionDate), latestDate));

		Predicate tagsPredicate = (selectedTags != null && !selectedTags.isEmpty()) ? tr.join(FinanceTransaction_.tags).in(criteriaBuilder.literal(selectedTags)) : null;

		Predicate transactionTypePredicate = null;
		if (enabledExpenseTransactions)
			transactionTypePredicate = transactionTypePredicate == null
					? criteriaBuilder.equal(tr.type(), ExpenseTransaction.class)
					: criteriaBuilder.or(transactionTypePredicate, criteriaBuilder.equal(tr.type(), ExpenseTransaction.class));
		if (enabledTransferTransactions)
			transactionTypePredicate = transactionTypePredicate == null
					? criteriaBuilder.equal(tr.type(), TransferTransaction.class)
					: criteriaBuilder.or(transactionTypePredicate, criteriaBuilder.equal(tr.type(), TransferTransaction.class));

		if (transactionTypePredicate == null || tagsPredicate == null)
			return new LinkedList<FinanceTransaction>();

		Predicate rootPredicate = datePredicate;
		rootPredicate = criteriaBuilder.and(rootPredicate, tagsPredicate);
		rootPredicate = criteriaBuilder.and(rootPredicate, transactionTypePredicate);

		transactionsCriteriaQuery.where(rootPredicate);

		Expression userOrderBy = tr.get(orderBy);

		if (orderAbsolute)
			userOrderBy = criteriaBuilder.abs(userOrderBy);
		Order userOrder = orderAsc ? criteriaBuilder.asc(userOrderBy) : criteriaBuilder.desc(userOrderBy);
		transactionsCriteriaQuery.orderBy(userOrder,
				criteriaBuilder.asc(tr.get(FinanceTransaction_.id)));

		return entityManager.createQuery(transactionsCriteriaQuery).getResultList();
	}

	/**
	 * Returns a list of all tags
	 *
	 * @return a list of all tags
	 */
	public List<String> getAllTags() {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<String> transactionsCriteriaQuery = criteriaBuilder.createQuery(String.class);
		Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);

		transactionsCriteriaQuery.select(tr.join(FinanceTransaction_.tags)).distinct(true);

		return entityManager.createQuery(transactionsCriteriaQuery).getResultList();
	}

	/**
	 * Retrieves all accounts from the database
	 *
	 * @return the list of all accounts stored in the database
	 */
	public List<FinanceAccount> getAllAccounts() {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceAccount> accountsCriteriaQuery = criteriaBuilder.createQuery(FinanceAccount.class);
		accountsCriteriaQuery.from(FinanceAccount.class);

		return entityManager.createQuery(accountsCriteriaQuery).getResultList();
	}

	/**
	 * Returns expenses grouped by tags
	 *
	 * @return expenses grouped by tags
	 */
	public Map<String, Double> getTagExpenses() {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> transactionsCriteriaQuery = criteriaBuilder.createTupleQuery();

		Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);

		Predicate datePredicate = criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(tr.get(FinanceTransaction_.transactionDate), earliestDate),
				criteriaBuilder.lessThanOrEqualTo(tr.get(FinanceTransaction_.transactionDate), latestDate));

		Predicate tagsPredicate = (selectedTags != null && !selectedTags.isEmpty()) ? tr.join(FinanceTransaction_.tags).in(criteriaBuilder.literal(selectedTags)) : null;

		Predicate transactionTypePredicate = null;
		if (enabledExpenseTransactions)
			transactionTypePredicate = transactionTypePredicate == null
					? criteriaBuilder.equal(tr.type(), ExpenseTransaction.class)
					: criteriaBuilder.or(transactionTypePredicate, criteriaBuilder.equal(tr.type(), ExpenseTransaction.class));
		if (enabledTransferTransactions)
			transactionTypePredicate = transactionTypePredicate == null
					? criteriaBuilder.equal(tr.type(), TransferTransaction.class)
					: criteriaBuilder.or(transactionTypePredicate, criteriaBuilder.equal(tr.type(), TransferTransaction.class));

		if (transactionTypePredicate == null || tagsPredicate == null)
			return new TreeMap<String, Double>();

		Predicate rootPredicate = datePredicate;
		rootPredicate = criteriaBuilder.and(rootPredicate, tagsPredicate);
		rootPredicate = criteriaBuilder.and(rootPredicate, transactionTypePredicate);

		Join tagsJoin = tr.join(FinanceTransaction_.tags);

		transactionsCriteriaQuery.where(rootPredicate);
		transactionsCriteriaQuery.multiselect(criteriaBuilder.sum(tr.get(FinanceTransaction_.amount)),
				tagsJoin).distinct(true);
		transactionsCriteriaQuery.groupBy(tagsJoin);

		Map<String, Double> result = new TreeMap<>();
		for (Tuple tuple : entityManager.createQuery(transactionsCriteriaQuery).getResultList())
			result.put(tuple.get(1, String.class), tuple.get(0, Long.class) / 100.0D);
		return result;
	}
}
