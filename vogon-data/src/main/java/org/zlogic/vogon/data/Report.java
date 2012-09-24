/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Central class for setting report parameters and generating various reports.
 * Interacts with DB directly.
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
	 * Selected tags
	 */
	protected List<String> selectedTags;
	/**
	 * Selected accounts
	 */
	protected List<FinanceAccount> selectedAccounts;
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
	 * Returns the tags to be included in the report.
	 *
	 * @return the tags to be included in the report
	 */
	public List<String> getSelectedTags() {
		return selectedTags != null ? selectedTags : new LinkedList<String>();
	}

	/**
	 * Sets the tags to be included in the report.
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
	 * Sets the tags to be included in the report.
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
	 * Returns the accounts to be included in the report.
	 *
	 * @return the accounts to be included in the report
	 */
	public List<FinanceAccount> getSelectedAccounts() {
		return selectedAccounts != null ? selectedAccounts : new LinkedList<FinanceAccount>();
	}

	/**
	 * Sets the accounts to be included in the report.
	 *
	 * @param selectedAccounts the accounts to be included in the report
	 */
	public void setSelectedAccounts(List<FinanceAccount> selectedAccounts) {
		if (selectedAccounts == null || selectedAccounts.isEmpty())
			this.selectedAccounts = null;
		else
			this.selectedAccounts = selectedAccounts;
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
	 * Returns all transactions matching the set filters, ordered by amount
	 * descending
	 *
	 * @return list of all transactions matching the set filters, ordered by
	 * amount descending
	 */
	public List<FinanceTransaction> getTransactions() {
		return getTransactions(FinanceTransaction_.amount, false, true);
	}

	/**
	 * Class for storing a generated predicate and joins (should be used in
	 * groupBy to avoid duplicate results)
	 */
	protected class ConstructedPredicate {

		private Predicate predicate;
		private Join componentsJoin;
		private Join tagsJoin;

		/**
		 * Default constructor
		 *
		 * @param predicate the generated predicate
		 * @param componentsJoin the transaction components join
		 * @param tagsJoin the transaction tags join
		 */
		public ConstructedPredicate(Predicate predicate, Join componentsJoin, Join tagsJoin) {
			this.predicate = predicate;
			this.componentsJoin = componentsJoin;
			this.tagsJoin = tagsJoin;
		}

		/**
		 * Returns the generated predicate
		 *
		 * @return the predicate
		 */
		public Predicate getPredicate() {
			return predicate;
		}

		/**
		 * Returns the transaction components join
		 *
		 * @return the transaction components join
		 */
		public Join getComponentsJoin() {
			return componentsJoin;
		}

		/**
		 * Returns the transaction tags join
		 *
		 * @return the transaction tags join
		 */
		public Join getTagsJoin() {
			return tagsJoin;
		}
	}

	/**
	 * Returns a predicate for filtering transactions
	 *
	 * @param criteriaBuilder the CriteriaBuilder
	 * @param tr the FinanceTransaction Root
	 * @return the predicate for filtering transactions and joins which should
	 * be used in groupBy if result of selection is not a unique FinanceAccount
	 */
	protected ConstructedPredicate getFilteredTransactionsPredicate(CriteriaBuilder criteriaBuilder, Root<FinanceTransaction> tr) {
		Predicate datePredicate = criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(tr.get(FinanceTransaction_.transactionDate), earliestDate),
				criteriaBuilder.lessThanOrEqualTo(tr.get(FinanceTransaction_.transactionDate), latestDate));

		Predicate transactionTypePredicate = criteriaBuilder.disjunction();
		if (enabledExpenseTransactions || enabledIncomeTransactions)
			transactionTypePredicate = criteriaBuilder.or(transactionTypePredicate, criteriaBuilder.equal(tr.type(), ExpenseTransaction.class));
		if (enabledTransferTransactions)
			transactionTypePredicate = criteriaBuilder.or(transactionTypePredicate, criteriaBuilder.equal(tr.type(), TransferTransaction.class));

		Predicate expenseTypePredicate = criteriaBuilder.disjunction();
		if (enabledExpenseTransactions)
			expenseTypePredicate = criteriaBuilder.or(expenseTypePredicate, criteriaBuilder.lessThanOrEqualTo(tr.get(FinanceTransaction_.amount), new Long(0)));

		if (enabledIncomeTransactions)
			expenseTypePredicate = criteriaBuilder.or(expenseTypePredicate, criteriaBuilder.greaterThanOrEqualTo(tr.get(FinanceTransaction_.amount), new Long(0)));

		Join tagsJoin = tr.join(FinanceTransaction_.tags);

		Predicate tagsPredicate = (selectedTags != null && !selectedTags.isEmpty()) ? tagsJoin.in(criteriaBuilder.literal(selectedTags)) : criteriaBuilder.disjunction();

		Join componentsJoin = tr.join(FinanceTransaction_.components);

		Predicate accountsPredicate = (selectedAccounts != null && !selectedAccounts.isEmpty()) ? componentsJoin.get(TransactionComponent_.account).in(criteriaBuilder.literal(selectedAccounts)) : criteriaBuilder.disjunction();

		Predicate rootPredicate = datePredicate;
		rootPredicate = criteriaBuilder.and(rootPredicate, accountsPredicate);
		rootPredicate = criteriaBuilder.and(rootPredicate, tagsPredicate);
		rootPredicate = criteriaBuilder.and(rootPredicate, transactionTypePredicate);
		rootPredicate = criteriaBuilder.and(rootPredicate, expenseTypePredicate);
		return new ConstructedPredicate(rootPredicate, componentsJoin, tagsJoin);
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
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);

		ConstructedPredicate predicate = getFilteredTransactionsPredicate(criteriaBuilder, tr);

		transactionsCriteriaQuery.where(predicate.getPredicate());

		Expression userOrderBy = tr.get(orderBy);

		if (orderAbsolute)
			userOrderBy = criteriaBuilder.abs(userOrderBy);
		Order userOrder = orderAsc ? criteriaBuilder.asc(userOrderBy) : criteriaBuilder.desc(userOrderBy);
		Order idOrder = orderAsc ? criteriaBuilder.asc(tr.get(FinanceTransaction_.id)) : criteriaBuilder.desc(tr.get(FinanceTransaction_.id));
		tr.fetch(FinanceTransaction_.components).fetch(TransactionComponent_.account);

		transactionsCriteriaQuery.orderBy(userOrder, idOrder);
		transactionsCriteriaQuery.select(tr).distinct(true);

		List<FinanceTransaction> transactions = entityManager.createQuery(transactionsCriteriaQuery).getResultList();
		entityManager.close();
		return transactions;
	}

	/**
	 * Returns a list of all tags
	 *
	 * @return a list of all tags
	 */
	public List<String> getAllTags() {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<String> tagsCriteriaQuery = criteriaBuilder.createQuery(String.class);
		Root<FinanceTransaction> tr = tagsCriteriaQuery.from(FinanceTransaction.class);

		tagsCriteriaQuery.select(tr.join(FinanceTransaction_.tags)).distinct(true);

		List<String> tags = entityManager.createQuery(tagsCriteriaQuery).getResultList();
		entityManager.close();
		return tags;
	}

	/**
	 * Retrieves all accounts from the database
	 *
	 * @return the list of all accounts stored in the database
	 */
	public List<FinanceAccount> getAllAccounts() {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceAccount> accountsCriteriaQuery = criteriaBuilder.createQuery(FinanceAccount.class);
		accountsCriteriaQuery.from(FinanceAccount.class);
		List<FinanceAccount> accounts = entityManager.createQuery(accountsCriteriaQuery).getResultList();
		entityManager.close();
		return accounts;
	}

	/**
	 * Returns a raw account balance by a specific date
	 *
	 * @param account the account
	 * @param byDate the date
	 * @return the raw balance
	 */
	public long getRawAccountBalanceByDate(FinanceAccount account, Date byDate) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> transactionsCriteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);

		Join componentsJoin = tr.join(FinanceTransaction_.components);
		Path joinedAccount = componentsJoin.get(TransactionComponent_.account);

		Predicate accountsPredicate = criteriaBuilder.equal(componentsJoin.get(TransactionComponent_.account), account);
		Predicate datePredicate = criteriaBuilder.lessThan(tr.get(FinanceTransaction_.transactionDate), byDate);
		transactionsCriteriaQuery.select(criteriaBuilder.sum(componentsJoin.get(TransactionComponent_.amount)));
		transactionsCriteriaQuery.where(criteriaBuilder.and(accountsPredicate, datePredicate));
		transactionsCriteriaQuery.groupBy(joinedAccount);

		long result = 0;
		try {
			result = entityManager.createQuery(transactionsCriteriaQuery).getSingleResult();
		} catch (NoResultException ex) {
		}
		entityManager.close();
		return result;
	}

	/**
	 * Converts a multi-currency amount map into the default currency
	 *
	 * @param amounts the amounts in different currencies
	 * @return sum of all amounts converted to the default currency
	 */
	protected long convertRawAmountToCommonCurrency(Map<String, Long> amounts) {
		long result = 0;
		for (Map.Entry<String, Long> amountInCurrency : amounts.entrySet())
			result += Currency.getInstance(amountInCurrency.getKey()) == financeData.getDefaultCurrency()
					? amountInCurrency.getValue()
					: (long) (Math.round(financeData.getExchangeRate(Currency.getInstance(amountInCurrency.getKey()), financeData.getDefaultCurrency()) * (double) amountInCurrency.getValue()));
		return result;
	}

	/**
	 * Returns a graph for the total balance of accounts, sorted by date
	 *
	 * @return a graph for the total balance of accounts, sorted by date
	 */
	public Map<Date, Double> getAccountsBalanceGraph() {
		List<FinanceTransaction> transactions = getTransactions(FinanceTransaction_.transactionDate, true, false);
		Map<Date, Long> currentBalance = new TreeMap<>();
		Map<Date, Double> result = new TreeMap<>();
		Map<String, Long> sumBalance = new TreeMap<>();

		for (Currency currency : financeData.getCurrencies())
			sumBalance.put(currency.getCurrencyCode(), 0L);

		for (FinanceAccount account : selectedAccounts) {
			sumBalance.put(account.getCurrency().getCurrencyCode(), sumBalance.get(account.getCurrency().getCurrencyCode()) + getRawAccountBalanceByDate(account, earliestDate));
		}
		for (FinanceTransaction transaction : transactions) {
			for (FinanceAccount account : selectedAccounts)
				for (TransactionComponent component : transaction.getComponentsForAccount(account))
					sumBalance.put(account.getCurrency().getCurrencyCode(), sumBalance.get(account.getCurrency().getCurrencyCode()) + component.getRawAmount());
			currentBalance.put(transaction.getDate(), convertRawAmountToCommonCurrency(sumBalance));
		}

		for (Map.Entry<Date, Long> entry : currentBalance.entrySet())
			result.put(entry.getKey(), entry.getValue() / 100.0D);

		return result;
	}

	/**
	 * Returns expenses grouped by tags
	 *
	 * @return expenses grouped by tags
	 */
	public Map<String, Double> getTagExpenses() {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		Map<String, Double> result = new TreeMap<>();
		for (Currency currency : financeData.getCurrencies()) {
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Tuple> transactionsCriteriaQuery = criteriaBuilder.createTupleQuery();
			Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);

			ConstructedPredicate predicate = getFilteredTransactionsPredicate(criteriaBuilder, tr);

			Predicate currencyPredicate = criteriaBuilder.equal(predicate.getComponentsJoin().get(TransactionComponent_.account).get(FinanceAccount_.currency), currency.getCurrencyCode());

			transactionsCriteriaQuery.multiselect(criteriaBuilder.sum(tr.get(FinanceTransaction_.amount)),
					predicate.getTagsJoin()).distinct(true);
			transactionsCriteriaQuery.where(criteriaBuilder.and(predicate.getPredicate(), currencyPredicate));
			transactionsCriteriaQuery.groupBy(predicate.getComponentsJoin(), predicate.getTagsJoin());

			double exchangeRate = financeData.getExchangeRate(currency, financeData.getDefaultCurrency());

			for (Tuple tuple : entityManager.createQuery(transactionsCriteriaQuery).getResultList()) {
				String tag = tuple.get(1, String.class);
				double accumulatedBalance = result.containsKey(tag) ? result.get(tag) : 0.0D;
				result.put(tag, accumulatedBalance + exchangeRate * (tuple.get(0, Long.class) / 100.0D));
			}
		}
		entityManager.close();
		return result;
	}
}
