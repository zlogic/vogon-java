/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.report;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
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
import org.zlogic.vogon.data.Constants;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceAccount_;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.FinanceTransaction_;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.data.TransactionComponent_;
import org.zlogic.vogon.data.VogonUser;

/**
 * Central class for setting report parameters and generating various reports.
 * Interacts with DB directly.
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class ReportFactory {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/data/messages");
	/**
	 * The report owner user
	 */
	private VogonUser owner;
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
	 * Constructs ReportFactory with no user
	 */
	public ReportFactory() {
		//Prepare start/end dates
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		earliestDate = calendar.getTime();
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		latestDate = calendar.getTime();
	}

	/**
	 * Constructs ReportFactory for user
	 *
	 * @param user the reporting user
	 */
	public ReportFactory(VogonUser user) {
		this();
		setOwner(user);
	}

	/*
	 * Set report parameters and filters
	 */
	/**
	 * Returns the report owner user
	 *
	 * @return the report owner user
	 */
	public VogonUser getOwner() {
		return owner;
	}

	/**
	 * Sets the report owner user
	 *
	 * @param owner the report owner user
	 */
	public void setOwner(VogonUser owner) {
		this.owner = owner;
	}

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

	/**
	 * Prepares the report
	 *
	 * @param entityManager the EntityManager to be used for making queries;
	 * should be opened/closed outside of this function before calling this
	 * function
	 * @return the report
	 */
	public Report buildReport(EntityManager entityManager) throws SecurityException {
		if (owner == null)
			throw new SecurityException(messages.getString("NOT_ALLOWED_TO_GET_DATA_FOR_UNKNOWN_USER"));
		Report report = new Report();
		report.setTransactions(getTransactions(entityManager));
		report.setTagExpenses(getTagExpenses(entityManager));
		report.setAccountsBalanceGraph(getAccountsBalanceGraph(entityManager));
		return report;
	}

	/**
	 * Returns all transactions matching the set filters, ordered by amount
	 * descending
	 *
	 * @param entityManager the EntityManager to be used for making queries
	 * @return list of all transactions matching the set filters, ordered by
	 * amount descending
	 */
	protected List<FinanceTransaction> getTransactions(EntityManager entityManager) {
		return getTransactions(entityManager, FinanceTransaction_.amount, false, true, EnumSet.allOf(FilterType.class), -1, -1);
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
		//User filter
		Predicate userPredicate = criteriaBuilder.equal(tr.get(FinanceTransaction_.owner), owner.getId());

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
		rootPredicate = criteriaBuilder.and(userPredicate);
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
	 * @param entityManager the EntityManager to be used for making queries
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
	protected <OrderByClass> List<FinanceTransaction> getTransactions(EntityManager entityManager, SingularAttribute<FinanceTransaction, OrderByClass> orderBy, boolean orderAsc, boolean orderAbsolute, EnumSet<FilterType> appliedFilters, int firstTransaction, int lastTransaction) {
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
		return transactions;
	}

	/**
	 * Returns a list of all tags
	 *
	 * @param entityManager the EntityManager to be used for making queries
	 * @return a list of all tags
	 */
	public Set<String> getAllTags(EntityManager entityManager) throws SecurityException {
		if (owner == null)
			throw new SecurityException(messages.getString("NOT_ALLOWED_TO_GET_DATA_FOR_UNKNOWN_USER"));
		CriteriaQuery<String> tagsCriteriaQuery = entityManager.getCriteriaBuilder().createQuery(String.class);
		Root<FinanceTransaction> tr = tagsCriteriaQuery.from(FinanceTransaction.class);

		Predicate userPredicate = entityManager.getCriteriaBuilder().equal(tr.get(FinanceTransaction_.owner), owner);
		tagsCriteriaQuery.where(userPredicate);

		tagsCriteriaQuery.select(tr.join(FinanceTransaction_.tags)).distinct(true);
		return new HashSet<>(entityManager.createQuery(tagsCriteriaQuery).getResultList());
	}

	/**
	 * Returns a raw account balance by a specific date
	 *
	 * @param entityManager the EntityManager to be used for making queries
	 * @param account the account
	 * @param byDate the date
	 * @return the raw balance
	 */
	protected long getRawAccountBalanceByDate(EntityManager entityManager, FinanceAccount account, Date byDate) throws SecurityException {
		if (!account.getOwner().equals(owner))
			throw new SecurityException(MessageFormat.format(messages.getString("NOT_ALLOWED_TO_GET_DATA_FOR_ANOTHER_USER"), new Object[]{account.getOwner().getUsername()}));
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> transactionsCriteriaQuery = entityManager.getCriteriaBuilder().createQuery(Long.class);
		Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);

		Join<FinanceTransaction, TransactionComponent> componentsJoin = tr.join(FinanceTransaction_.components);
		Path<FinanceAccount> joinedAccount = componentsJoin.get(TransactionComponent_.account);

		Predicate accountsPredicate = criteriaBuilder.equal(componentsJoin.get(TransactionComponent_.account), account);
		Predicate datePredicate = criteriaBuilder.lessThan(tr.get(FinanceTransaction_.transactionDate), byDate);
		transactionsCriteriaQuery.select(criteriaBuilder.sum(componentsJoin.get(TransactionComponent_.amount)));
		transactionsCriteriaQuery.where(criteriaBuilder.and(accountsPredicate, datePredicate));
		transactionsCriteriaQuery.groupBy(joinedAccount);

		try {
			return entityManager.createQuery(transactionsCriteriaQuery).getSingleResult();
		} catch (NoResultException ex) {
			return 0;
		}
	}

	/**
	 * Returns a graph for the total balance of accounts, sorted by date
	 *
	 * @param entityManager the EntityManager to be used for making queries
	 * @return a graph for the total balance of accounts, sorted by date
	 */
	protected Map<String, DateBalance<Double>> getAccountsBalanceGraph(EntityManager entityManager) {
		Map<String, Long> sumBalance = new HashMap<>();

		if (selectedAccounts != null)
			for (FinanceAccount account : selectedAccounts) {
				if (!sumBalance.containsKey(account.getCurrency().getCurrencyCode()))
					sumBalance.put(account.getCurrency().getCurrencyCode(), 0L);
				sumBalance.put(account.getCurrency().getCurrencyCode(), sumBalance.get(account.getCurrency().getCurrencyCode()) + getRawAccountBalanceByDate(entityManager, account, earliestDate));
			}

		//Process transactions
		Map<String, DateBalance<Long>> currentBalance = new HashMap<>();

		List<FinanceTransaction> transactions = getTransactions(
				entityManager,
				FinanceTransaction_.transactionDate, true, false,
				EnumSet.of(FilterType.DATE, FilterType.ACCOUNTS),
				-1, -1);

		//Calculate sum for accounts/currencies for each transaction
		for (FinanceTransaction transaction : transactions) {
			//Compute balance change for transaction
			if (selectedAccounts != null)
				for (FinanceAccount account : selectedAccounts)
					for (TransactionComponent component : transaction.getComponentsForAccount(account))
						sumBalance.put(account.getCurrency().getCurrencyCode(), sumBalance.get(account.getCurrency().getCurrencyCode()) + component.getRawAmount());

			//Update balance map
			for (Map.Entry<String, Long> sumBalanceCurrency : sumBalance.entrySet()) {
				String currency = sumBalanceCurrency.getKey();
				if (!currentBalance.containsKey(currency))
					currentBalance.put(currency, new DateBalance(Long.class));
				currentBalance.get(currency).setBalance(transaction.getDate(), sumBalanceCurrency.getValue());
			}
		}

		//Convert from long to double
		Map<String, DateBalance<Double>> result = new HashMap<>();
		for (Map.Entry<String, DateBalance<Long>> dateBalanceCurrency : currentBalance.entrySet())
			for (Map.Entry<Date, Long> dateBalance : dateBalanceCurrency.getValue().getData().entrySet()) {
				String currency = dateBalanceCurrency.getKey();
				if (!result.containsKey(currency))
					result.put(currency, new DateBalance<>(Double.class));
				result.get(dateBalanceCurrency.getKey()).setBalance(dateBalance.getKey(), dateBalance.getValue() / Constants.RAW_AMOUNT_MULTIPLIER);
			}
		return result;
	}

	/**
	 * Returns expenses grouped by tags
	 *
	 * @param entityManager the EntityManager to be used for making queries
	 * @return expenses grouped by tags
	 */
	protected List<TagExpense> getTagExpenses(EntityManager entityManager) {
		Map<String, TagExpense> result = new TreeMap<>();
		//Process currencies separately
		Set<Currency> currencies = new HashSet<>();
		if (selectedAccounts != null)
			for (FinanceAccount account : selectedAccounts)
				currencies.add(account.getCurrency());
		for (Currency currency : currencies) {
			//Obtain the tag-total sum table via a query
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Tuple> transactionsCriteriaQuery = criteriaBuilder.createTupleQuery();
			Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);

			ConstructedPredicate predicate = getFilteredTransactionsPredicate(criteriaBuilder, tr, EnumSet.allOf(FilterType.class));

			Predicate currencyPredicate = criteriaBuilder.equal(predicate.getComponentsJoin().get(TransactionComponent_.account).get(FinanceAccount_.currency), currency.getCurrencyCode());

			transactionsCriteriaQuery.multiselect(criteriaBuilder.sum(predicate.getComponentsJoin().get(TransactionComponent_.amount)),
					predicate.getTagsJoin()).distinct(true);
			transactionsCriteriaQuery.where(criteriaBuilder.and(predicate.getPredicate(), currencyPredicate));
			transactionsCriteriaQuery.groupBy(predicate.getTagsJoin(), predicate.getComponentsJoin().get(TransactionComponent_.account).get(FinanceAccount_.currency));

			List<Tuple> resultForCurrency = entityManager.createQuery(transactionsCriteriaQuery).getResultList();

			//Convert results to a common currency if tag contains transactions in different currencies
			for (Tuple tuple : resultForCurrency) {
				String tag = tuple.get(1, String.class);
				double amount = (tuple.get(0, Long.class) / Constants.RAW_AMOUNT_MULTIPLIER);
				if (!result.containsKey(tag))
					result.put(tag, new TagExpense(tag));
				TagExpense tagExpense = result.get(tag);

				tagExpense.addAmount(currency, amount);
			}
		}
		return new LinkedList<>(result.values());
	}
}
