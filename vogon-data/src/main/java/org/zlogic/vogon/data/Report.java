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
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
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
	 * Filter enablement
	 */
	protected enum FilterType {

		/**
		 * Filter transaction date (earliestDate+latestDate)
		 */
		DATE,
		/**
		 * Filter by transaction tags (selectedTags)
		 */
		TAGS,
		/**
		 * Filter by transaction accounts (selectedAccounts)
		 */
		ACCOUNTS,
		/**
		 * Filter by transaction type (Expense-Income or Transfer)
		 */
		TRANSACTION_TYPE,
		/**
		 * Filter by expense transaction type (enabledIncomeTransactions and
		 * enabledExpenseTransactions)
		 */
		EXPENSE_TYPE
	};

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
		return getTransactions(FinanceTransaction_.amount, false, true, EnumSet.allOf(FilterType.class), -1, -1);
	}

	/**
	 * Class for storing a generated predicate and joins (should be used in
	 * groupBy to avoid duplicate results)
	 */
	protected class ConstructedPredicate {

		/**
		 * The generated predicate
		 */
		private Predicate predicate;
		/**
		 * The components join
		 */
		private Join<FinanceTransaction, TransactionComponent> componentsJoin;
		/**
		 * The tags join
		 */
		private Join<FinanceTransaction, String> tagsJoin;

		/**
		 * Default constructor
		 *
		 * @param predicate the generated predicate
		 * @param componentsJoin the transaction components join
		 * @param tagsJoin the transaction tags join
		 */
		public ConstructedPredicate(Predicate predicate, Join<FinanceTransaction, TransactionComponent> componentsJoin, Join<FinanceTransaction, String> tagsJoin) {
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
		public Join<FinanceTransaction, TransactionComponent> getComponentsJoin() {
			return componentsJoin;
		}

		/**
		 * Returns the transaction tags join
		 *
		 * @return the transaction tags join
		 */
		public Join<FinanceTransaction, String> getTagsJoin() {
			return tagsJoin;
		}
	}

	/**
	 * Returns a predicate for filtering transactions
	 *
	 * @param criteriaBuilder the CriteriaBuilder
	 * @param tr the FinanceTransaction Root
	 * @param appliedFilters the filters which should be applied
	 * @return the predicate for filtering transactions and joins which should
	 * be used in groupBy if result of selection is not a unique FinanceAccount
	 */
	protected ConstructedPredicate getFilteredTransactionsPredicate(CriteriaBuilder criteriaBuilder, Root<FinanceTransaction> tr, EnumSet<FilterType> appliedFilters) {
		//Date filter
		Predicate datePredicate = criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(tr.get(FinanceTransaction_.transactionDate), earliestDate),
				criteriaBuilder.lessThanOrEqualTo(tr.get(FinanceTransaction_.transactionDate), latestDate));

		//Transaction type filter
		Predicate transactionTypePredicate = criteriaBuilder.disjunction();
		if (enabledExpenseTransactions || enabledIncomeTransactions)
			transactionTypePredicate = criteriaBuilder.or(transactionTypePredicate, criteriaBuilder.equal(tr.get(FinanceTransaction_.type), FinanceTransaction.Type.EXPENSEINCOME));
		if (enabledTransferTransactions)
			transactionTypePredicate = criteriaBuilder.or(transactionTypePredicate, criteriaBuilder.equal(tr.get(FinanceTransaction_.type), FinanceTransaction.Type.TRANSFER));


		//Expense/income filter
		Predicate expenseTypePredicate = criteriaBuilder.disjunction();
		if (enabledExpenseTransactions)
			expenseTypePredicate = criteriaBuilder.or(expenseTypePredicate, criteriaBuilder.lessThanOrEqualTo(tr.get(FinanceTransaction_.amount), new Long(0)));
		if (enabledIncomeTransactions)
			expenseTypePredicate = criteriaBuilder.or(expenseTypePredicate, criteriaBuilder.greaterThanOrEqualTo(tr.get(FinanceTransaction_.amount), new Long(0)));

		//Tags jon
		Join<FinanceTransaction, String> tagsJoin = tr.join(FinanceTransaction_.tags);
		Predicate tagsPredicate = (selectedTags != null && !selectedTags.isEmpty()) ? tagsJoin.in(criteriaBuilder.literal(selectedTags)) : criteriaBuilder.disjunction();

		//Transaction components join
		Join<FinanceTransaction, TransactionComponent> componentsJoin = tr.join(FinanceTransaction_.components);
		Predicate accountsPredicate = (selectedAccounts != null && !selectedAccounts.isEmpty()) ? componentsJoin.get(TransactionComponent_.account).in(criteriaBuilder.literal(selectedAccounts)) : criteriaBuilder.disjunction();

		//Combine all filters
		Predicate rootPredicate = criteriaBuilder.conjunction();
		if (appliedFilters.contains(FilterType.DATE))
			rootPredicate = criteriaBuilder.and(rootPredicate, datePredicate);
		if (appliedFilters.contains(FilterType.ACCOUNTS))
			rootPredicate = criteriaBuilder.and(rootPredicate, accountsPredicate);
		if (appliedFilters.contains(FilterType.TAGS))
			rootPredicate = criteriaBuilder.and(rootPredicate, tagsPredicate);
		if (appliedFilters.contains(FilterType.TRANSACTION_TYPE))
			rootPredicate = criteriaBuilder.and(rootPredicate, transactionTypePredicate);
		if (appliedFilters.contains(FilterType.EXPENSE_TYPE))
			rootPredicate = criteriaBuilder.and(rootPredicate, expenseTypePredicate);
		return new ConstructedPredicate(rootPredicate, componentsJoin, tagsJoin);
	}

	/**
	 * Returns all transactions matching the set filters
	 *
	 * @param <OrderByClass> type of ordering field
	 * @param orderBy field for ordering the result
	 * @param orderAsc true if results should be ordered ascending, false if
	 * descending
	 * @param orderAbsolute true if order should be for absolute value (e.g.
	 * ABS(orderBy))
	 * @param appliedFilters the filters which should be applied
	 * @param firstTransaction the first transaction number to be selected
	 * @param lastTransaction the last transaction number to be selected
	 * @return list of all transactions matching the set filters
	 */
	public <OrderByClass> List<FinanceTransaction> getTransactions(SingularAttribute<FinanceTransaction, OrderByClass> orderBy, boolean orderAsc, boolean orderAbsolute, EnumSet<FilterType> appliedFilters, int firstTransaction, int lastTransaction) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> transactionsCriteriaQuery = criteriaBuilder.createTupleQuery();
		Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);

		//Build general filter
		ConstructedPredicate predicate = getFilteredTransactionsPredicate(criteriaBuilder, tr, appliedFilters);
		transactionsCriteriaQuery.where(predicate.getPredicate());

		//Configure the query
		Expression<?> userOrderBy = tr.get(orderBy);

		if (orderAbsolute) {
			if (orderBy.getType().getJavaType().isInstance(Number.class))
				userOrderBy = criteriaBuilder.abs(userOrderBy.as(Number.class));
			else if (orderBy.getType().getJavaType() == Short.TYPE)
				userOrderBy = criteriaBuilder.abs(userOrderBy.as(Short.TYPE));
			else if (orderBy.getType().getJavaType() == Integer.TYPE)
				userOrderBy = criteriaBuilder.abs(userOrderBy.as(Integer.TYPE));
			else if (orderBy.getType().getJavaType() == Long.TYPE)
				userOrderBy = criteriaBuilder.abs(userOrderBy.as(Long.TYPE));
			else if (orderBy.getType().getJavaType() == Float.TYPE)
				userOrderBy = criteriaBuilder.abs(userOrderBy.as(Float.TYPE));
			else if (orderBy.getType().getJavaType() == Double.TYPE)
				userOrderBy = criteriaBuilder.abs(userOrderBy.as(Double.TYPE));
		}
		Order userOrder = orderAsc ? criteriaBuilder.asc(userOrderBy) : criteriaBuilder.desc(userOrderBy);
		Order idOrder = orderAsc ? criteriaBuilder.asc(tr.get(FinanceTransaction_.id)) : criteriaBuilder.desc(tr.get(FinanceTransaction_.id));

		transactionsCriteriaQuery.multiselect(tr, userOrderBy).distinct(true);
		transactionsCriteriaQuery.orderBy(userOrder, idOrder);
		transactionsCriteriaQuery.groupBy(tr, userOrderBy, predicate.getComponentsJoin(), predicate.getTagsJoin());

		//Fetch data
		TypedQuery<Tuple> query = entityManager.createQuery(transactionsCriteriaQuery);
		if (firstTransaction >= 0)
			query = query.setFirstResult(firstTransaction);
		if (lastTransaction >= 0 && firstTransaction >= 0)
			query = query.setMaxResults(lastTransaction - firstTransaction + 1);

		List<FinanceTransaction> transactions = new LinkedList<>();
		for (Tuple tuple : query.getResultList())
			transactions.add(tuple.get(tr));

		//Post-fetch components
		CriteriaQuery<FinanceTransaction> transactionsComponentsFetchCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		Root<FinanceTransaction> trComponentsFetch = transactionsComponentsFetchCriteriaQuery.from(FinanceTransaction.class);
		transactionsComponentsFetchCriteriaQuery.where(tr.in(transactions));
		trComponentsFetch.fetch(FinanceTransaction_.components, JoinType.LEFT).fetch(TransactionComponent_.account, JoinType.LEFT);
		entityManager.createQuery(transactionsComponentsFetchCriteriaQuery).getResultList();

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

		Join<FinanceTransaction, TransactionComponent> componentsJoin = tr.join(FinanceTransaction_.components);
		Path<FinanceAccount> joinedAccount = componentsJoin.get(TransactionComponent_.account);

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
		Map<String, Long> sumBalance = new TreeMap<>();

		for (Currency currency : financeData.getCurrencies())
			sumBalance.put(currency.getCurrencyCode(), 0L);

		for (FinanceAccount account : selectedAccounts)
			sumBalance.put(account.getCurrency().getCurrencyCode(), sumBalance.get(account.getCurrency().getCurrencyCode()) + getRawAccountBalanceByDate(account, earliestDate));

		//Process transactions in batches
		Map<Date, Long> currentBalance = new TreeMap<>();
		boolean done = false;
		while (!done) {
			//Fetch next batch
			List<FinanceTransaction> transactions = getTransactions(
					FinanceTransaction_.transactionDate, true, false,
					EnumSet.of(FilterType.DATE, FilterType.ACCOUNTS),
					//currentTransaction, currentTransaction + Constants.batchFetchSize-1);
					-1, -1);
			transactions.size();
			//done = transactions.isEmpty();
			done = true;

			//Calculate sum for accounts/currencies for each transaction
			for (FinanceTransaction transaction : transactions) {
				for (FinanceAccount account : selectedAccounts)
					for (TransactionComponent component : transaction.getComponentsForAccount(account))
						sumBalance.put(account.getCurrency().getCurrencyCode(), sumBalance.get(account.getCurrency().getCurrencyCode()) + component.getRawAmount());
				currentBalance.put(transaction.getDate(), convertRawAmountToCommonCurrency(sumBalance));
			}
		}

		Map<Date, Double> result = new TreeMap<>();
		for (Map.Entry<Date, Long> entry : currentBalance.entrySet())
			result.put(entry.getKey(), entry.getValue() / Constants.rawAmountMultiplier);

		return result;
	}

	/**
	 * Class for storing results of a getTagExpenses() call
	 */
	public class TagExpense {

		/**
		 * The tag name
		 */
		protected String tag;
		/**
		 * The currency for amount
		 */
		protected Currency currency;
		/**
		 * True if the currency was converted
		 */
		protected boolean currencyConverted;
		/**
		 * The tag's amount
		 */
		protected double amount;

		/**
		 * Default constructor
		 *
		 * @param tag the tag name
		 * @param currency the currency
		 * @param currencyConverted true if the currency was converted
		 * @param amount the total amount
		 */
		protected TagExpense(String tag, Currency currency, boolean currencyConverted, double amount) {
			this.tag = tag;
			this.currency = currency;
			this.currencyConverted = currencyConverted;
			this.amount = amount;
		}

		/**
		 * Returns the set tag
		 *
		 * @return the tag
		 */
		public String getTag() {
			return tag;
		}

		/**
		 * Returns the currency
		 *
		 * @return the currency
		 */
		public Currency getCurrency() {
			return currency;
		}

		/**
		 * Returns the tag amount
		 *
		 * @return the tag amount
		 */
		public double getAmount() {
			return amount;
		}

		/**
		 * Returns true if the currency was converted
		 *
		 * @return true if the currency was converted
		 */
		public boolean isCurrencyConverted() {
			return currencyConverted;
		}
	}

	/**
	 * Returns expenses grouped by tags
	 *
	 * @return expenses grouped by tags
	 */
	public List<TagExpense> getTagExpenses() {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		Map<String, TagExpense> result = new TreeMap<>();
		//Process currencies separately
		for (Currency currency : financeData.getCurrencies()) {
			//Obtain the tag-total sum table via a query
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Tuple> transactionsCriteriaQuery = criteriaBuilder.createTupleQuery();
			Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);

			ConstructedPredicate predicate = getFilteredTransactionsPredicate(criteriaBuilder, tr, EnumSet.allOf(FilterType.class));

			Predicate currencyPredicate = criteriaBuilder.equal(predicate.getComponentsJoin().get(TransactionComponent_.account).get(FinanceAccount_.currency), currency.getCurrencyCode());

			transactionsCriteriaQuery.multiselect(criteriaBuilder.sum(predicate.getComponentsJoin().get(TransactionComponent_.amount)),
					predicate.getTagsJoin()).distinct(true);
			transactionsCriteriaQuery.where(criteriaBuilder.and(predicate.getPredicate(), currencyPredicate));
			transactionsCriteriaQuery.groupBy(predicate.getComponentsJoin(), predicate.getTagsJoin(), predicate.getComponentsJoin().get(TransactionComponent_.account).get(FinanceAccount_.currency));

			double exchangeRate = financeData.getExchangeRate(currency, financeData.getDefaultCurrency());

			//Convert results to a common currency if tag contains transactions in different currencies
			for (Tuple tuple : entityManager.createQuery(transactionsCriteriaQuery).getResultList()) {
				String tag = tuple.get(1, String.class);
				double amount = (tuple.get(0, Long.class) / Constants.rawAmountMultiplier);
				if (!result.containsKey(tag))
					result.put(tag, new TagExpense(tag, currency, false, 0));
				TagExpense tagExpense = result.get(tag);

				if (tagExpense.currency == currency)
					tagExpense.amount += amount;
				else if (tagExpense.currencyConverted)
					tagExpense.amount += exchangeRate * amount;
				else {
					tagExpense.currencyConverted = true;
					tagExpense.amount *= financeData.getExchangeRate(tagExpense.currency, financeData.getDefaultCurrency());
					tagExpense.currency = financeData.getDefaultCurrency();
				}
			}
		}
		entityManager.close();
		return new LinkedList<>(result.values());
	}
}
