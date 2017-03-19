/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import org.zlogic.vogon.data.Constants;
import org.zlogic.vogon.data.FinanceAccount;
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
	protected ReportFactory() {
	}

	/**
	 * Constructs ReportFactory for user
	 *
	 * @param user the reporting user
	 */
	public ReportFactory(VogonUser user) {
		ReportFactory.this.setOwner(user);
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
		return selectedTags != null ? selectedTags : new ArrayList<>();
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
		return selectedAccounts != null ? selectedAccounts : new ArrayList<>();
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
	public Map<String, Report> buildReport(EntityManager entityManager) throws SecurityException {
		if (owner == null)
			throw new SecurityException(messages.getString("NOT_ALLOWED_TO_GET_DATA_FOR_UNKNOWN_USER"));
		
		Collection<Currency> currencies = new HashSet<>();
		if(selectedAccounts != null)
			for (FinanceAccount account : selectedAccounts) {
				currencies.add(account.getCurrency());
			}

		Map<String, Report> reportsByCurrency = new TreeMap<>();

		for (Currency currency : currencies) {
			Collection<FinanceAccount> currencyAccounts = new HashSet<>();
			for (FinanceAccount account : selectedAccounts)
				if (account.getCurrency() == currency)
					currencyAccounts.add(account);

			Map<String, TagExpense> tagExpenses = new TreeMap<>();
			
			List<ReportTransaction> reportTransactions = new ArrayList<>();
			for (FinanceTransaction transaction : getTransactions(entityManager, currencyAccounts)){
				ReportTransaction reportTransaction = new ReportTransaction(transaction, currencyAccounts);
				reportTransactions.add(reportTransaction);
				for (String tag : transaction.getTags())
					addTagExpense(tag, reportTransaction.getRawAmount(), tagExpenses);
				if (transaction.getTags().length == 0)
					addTagExpense("", reportTransaction.getRawAmount(), tagExpenses);
			}
			reportTransactions.sort((tr1, tr2) -> -Double.compare(Math.abs(tr1.getAmount()), Math.abs(tr2.getAmount())));

			List<TagExpense> tagExpenseList = new ArrayList<>(tagExpenses.values());
			tagExpenseList.sort((tag1, tag2) -> -Double.compare(Math.abs(tag1.getAmount()), Math.abs(tag2.getAmount())));

			Report report = new Report();
			report.setTransactions(reportTransactions);
			report.setTagExpenses(tagExpenseList);
			report.setAccountsBalanceGraph(getAccountsBalanceGraph(entityManager, currencyAccounts));

			reportsByCurrency.put(currency.getCurrencyCode(), report);
		}
		return reportsByCurrency;
	}

	/**
	 * Returns all transactions matching the set filters, ordered by date
	 * descending
	 *
	 * @param entityManager the EntityManager to be used for making queries
	 * @param accounts the accounts for which transaction components should be included
	 * @return list of all transactions matching the set filters, ordered by
	 * amount descending
	 */
	private List<FinanceTransaction> getTransactions(EntityManager entityManager, Collection<FinanceAccount> accounts) {
		return getTransactions(entityManager, accounts, FinanceTransaction_.transactionDate, true, true, EnumSet.allOf(FilterType.class));
	}

	/**
	 * Class for storing a generated predicate and joins (should be used in
	 * groupBy to avoid duplicate results)
	 */
	private class ConstructedPredicate {

		/**
		 * The generated predicate
		 */
		private final Predicate predicate;
		/**
		 * The components join
		 */
		private final Join<FinanceTransaction, TransactionComponent> componentsJoin;
		/**
		 * The tags join
		 */
		private final Join<FinanceTransaction, String> tagsJoin;

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
	 * @param accounts the accounts for which transaction components should be included
	 * @param appliedFilters the filters which should be applied
	 * @return the predicate for filtering transactions and joins which should
	 * be used in groupBy if result of selection is not a unique FinanceAccount
	 */
	private ConstructedPredicate getFilteredTransactionsPredicate(CriteriaBuilder criteriaBuilder, Root<FinanceTransaction> tr, Collection<FinanceAccount> accounts , EnumSet<FilterType> appliedFilters) {
		//User filter
		Predicate userPredicate = criteriaBuilder.equal(tr.get(FinanceTransaction_.owner), owner.getId());

		//Date filter
		Predicate datePredicate = criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(tr.<Date>get(FinanceTransaction_.transactionDate), earliestDate),
				criteriaBuilder.lessThanOrEqualTo(tr.<Date>get(FinanceTransaction_.transactionDate), latestDate));

		//Transaction type filter
		Predicate transactionTypePredicate = criteriaBuilder.disjunction();
		if (enabledExpenseTransactions || enabledIncomeTransactions)
			transactionTypePredicate = criteriaBuilder.or(transactionTypePredicate, criteriaBuilder.equal(tr.get(FinanceTransaction_.type), FinanceTransaction.Type.EXPENSEINCOME));
		if (enabledTransferTransactions)
			transactionTypePredicate = criteriaBuilder.or(transactionTypePredicate, criteriaBuilder.equal(tr.get(FinanceTransaction_.type), FinanceTransaction.Type.TRANSFER));

		//Tags join
		Join<FinanceTransaction, String> tagsJoin = tr.join(FinanceTransaction_.tags);
		Predicate tagsPredicate = (selectedTags != null && !selectedTags.isEmpty()) ? tagsJoin.in(criteriaBuilder.literal(selectedTags)) : criteriaBuilder.disjunction();

		//Transaction components join
		Join<FinanceTransaction, TransactionComponent> componentsJoin = tr.join(FinanceTransaction_.components);
		Predicate accountsPredicate = componentsJoin.get(TransactionComponent_.account).in(criteriaBuilder.literal(accounts));

		//Combine all filters
		Predicate rootPredicate = criteriaBuilder.conjunction();
		rootPredicate = criteriaBuilder.and(rootPredicate, userPredicate);
		if (appliedFilters.contains(FilterType.DATE))
			rootPredicate = criteriaBuilder.and(rootPredicate, datePredicate);
		if (appliedFilters.contains(FilterType.ACCOUNTS))
			rootPredicate = criteriaBuilder.and(rootPredicate, accountsPredicate);
		if (appliedFilters.contains(FilterType.TAGS))
			rootPredicate = criteriaBuilder.and(rootPredicate, tagsPredicate);
		if (appliedFilters.contains(FilterType.TRANSACTION_TYPE))
			rootPredicate = criteriaBuilder.and(rootPredicate, transactionTypePredicate);
		return new ConstructedPredicate(rootPredicate, componentsJoin, tagsJoin);
	}

	/**
	 * Returns all transactions matching the set filters
	 *
	 * @param <OrderByClass> type of ordering field
	 * @param entityManager the EntityManager to be used for making queries
	 * @param accounts the accounts for which transaction components should be included
	 * @param orderBy field for ordering the result
	 * @param orderAsc true if results should be ordered ascending, false if
	 * descending
	 * @param orderAbsolute true if order should be for absolute value (e.g.
	 * ABS(orderBy))
	 * @param appliedFilters the filters which should be applied
	 * @return list of all transactions matching the set filters
	 */
	private <OrderByClass> List<FinanceTransaction> getTransactions(EntityManager entityManager, Collection<FinanceAccount> accounts, SingularAttribute<FinanceTransaction, OrderByClass> orderBy, boolean orderAsc, boolean orderAbsolute, EnumSet<FilterType> appliedFilters) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);

		//Build general filter
		ConstructedPredicate predicate = getFilteredTransactionsPredicate(criteriaBuilder, tr, accounts, appliedFilters);
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

		transactionsCriteriaQuery.select(tr).distinct(true);
		transactionsCriteriaQuery.orderBy(userOrder, idOrder);
		transactionsCriteriaQuery.groupBy(tr, userOrderBy, predicate.getComponentsJoin(), predicate.getTagsJoin());

		List<FinanceTransaction> transactions = new ArrayList<>();
		for (FinanceTransaction transaction : entityManager.createQuery(transactionsCriteriaQuery).getResultList()) {
			if (!appliedFilters.contains(FilterType.TRANSACTION_TYPE)) {
				transactions.add(transaction);
				continue;
			}
			//Additional expense/income filter
			if (transaction.getType() == FinanceTransaction.Type.EXPENSEINCOME) {
				long amount = 0;
				for (TransactionComponent component : transaction.getComponents()) {
					amount += accounts.contains(component.getAccount()) ? component.getRawAmount() : 0;
				}
				if ((enabledIncomeTransactions && amount >= 0) || (enabledExpenseTransactions && amount <= 0)) {
					transactions.add(transaction);
				}
			} else {
				transactions.add(transaction);
			}
		}
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
		HashSet<String> result = new HashSet<>(entityManager.createQuery(tagsCriteriaQuery).getResultList());
		result.add("");
		return result;
	}


	/**
	 * Returns a graph for the total balance of accounts, sorted by date
	 *
	 * @param entityManager the EntityManager to be used for making queries
	 * @return a graph for the total balance of accounts, sorted by date
	 */
	private Map<Date, Double> getAccountsBalanceGraph(EntityManager entityManager, Collection<FinanceAccount> accounts) {
		//Process transactions
		List<FinanceTransaction> transactions = getTransactions(
				entityManager,
				accounts,
				FinanceTransaction_.transactionDate, true, false,
				EnumSet.of(FilterType.ACCOUNTS, FilterType.TRANSACTION_TYPE, FilterType.TAGS));

		//Calculate sum for accounts/currencies for each transaction
		long sumBalance = 0;
		DateBalance<Long> currentBalance = new DateBalance<>(Long.class);
		for (FinanceTransaction transaction : transactions) {
			//Compute balance change for transaction
			for (FinanceAccount account : accounts)
				for (TransactionComponent component : transaction.getComponentsForAccount(account))
					sumBalance += component.getRawAmount();

			//Update balance map
			currentBalance.setBalance(transaction.getDate(), sumBalance);
		}

		//Convert from long to double
		Map<Date, Double> result = new TreeMap<>();
		for (Map.Entry<Date, Long> dateBalance : currentBalance.getData().entrySet())
			if ((dateBalance.getKey().after(earliestDate) || dateBalance.getKey().equals(earliestDate))
					&& (dateBalance.getKey().before(latestDate) || dateBalance.getKey().equals(latestDate)))
				result.put(dateBalance.getKey(), dateBalance.getValue() / Constants.RAW_AMOUNT_MULTIPLIER);
		return result;
	}
	
	/**
	 * Adds an amount to the appropriate TagExpense
	 *
	 * @param tag the tag
	 * @param amount the amount to add
	 * @param tagExpenses all stored TagExpense instances
	 */
	private void addTagExpense(String tag, long amount, Map<String, TagExpense> tagExpenses) {
		if (!tagExpenses.containsKey(tag)) {
			tagExpenses.put(tag, new TagExpense(tag));
		}
		tagExpenses.get(tag).addRawAmount(amount);
	}
}
