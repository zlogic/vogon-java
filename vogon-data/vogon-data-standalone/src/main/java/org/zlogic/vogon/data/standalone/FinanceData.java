/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.standalone;

import java.util.Currency;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import org.zlogic.vogon.data.CurrencyRate;
import org.zlogic.vogon.data.CurrencyRate_;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.FinanceTransaction_;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.data.TransactionComponent_;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.data.interop.Exporter;
import org.zlogic.vogon.data.interop.Importer;
import org.zlogic.vogon.data.interop.VogonExportException;
import org.zlogic.vogon.data.interop.VogonImportException;
import org.zlogic.vogon.data.interop.VogonImportLogicalException;
import org.zlogic.vogon.data.report.Report;
import org.zlogic.vogon.data.report.ReportFactory;
import org.zlogic.vogon.data.tools.DatabaseMaintenance;

/**
 * Class for storing the finance data, performing database operations and
 * generating events
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class FinanceData {

	/**
	 * Contains exchange rates
	 */
	protected java.util.List<CurrencyRate> exchangeRates;
	/**
	 * Entity manager factory
	 */
	private EntityManagerFactory entityManagerFactory;
	/**
	 * True if shutdown is started. Disables any transactions.
	 */
	private boolean shuttingDown = false;
	/**
	 * Lock for shuttingDown
	 */
	private ReentrantReadWriteLock shuttingDownLock = new ReentrantReadWriteLock();
	/**
	 * Number of transactions in the database
	 */
	protected long transactionsCount = 0;

	/**
	 * Default constructor
	 */
	public FinanceData() {
		entityManagerFactory = Persistence.createEntityManagerFactory("VogonPU"); //NOI18N
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		restoreFromDatabase(entityManager);
		entityManager.close();
	}

	/**
	 * Starts the shutdown and blocks any future requests to the database.
	 */
	public void shutdown() {
		try {
			shuttingDownLock.writeLock().lock();
			shuttingDown = true;
			entityManagerFactory.close();
		} finally {
			shuttingDownLock.writeLock().unlock();
		}
	}

	/**
	 * Returns true if the application is shutting down and database requests
	 * will be ignored.
	 *
	 * @return true if the applications is shutting down
	 */
	public boolean isShuttingDown() {
		return shuttingDown;
	}

	/**
	 * Performs a requested change with a supplied TransactedChange. If process
	 * is shutting down, the change is ignored.
	 *
	 * @param requestedChange a TransactedChange implementation
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public void performTransactedChange(TransactedChange requestedChange) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();
			requestedChange.performChange(entityManager);
			entityManager.getTransaction().commit();
			entityManager.close();
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Performs a requested query with a supplied TransactedQuery. If process is
	 * shutting down, the change is ignored.
	 *
	 * @param <ElementType> element type for query
	 * @param <ResultType> return result type
	 * @param requestedQuery a TransactedQuery implementation
	 * @return the query result
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public <ElementType, ResultType> ResultType performTransactedQuery(TransactedQuery<ElementType, ResultType> requestedQuery) throws ApplicationShuttingDownException {
		ResultType result = null;
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();
			result = requestedQuery.getQueryResult(entityManager.createQuery(requestedQuery.getQuery(entityManager.getCriteriaBuilder())));
			entityManager.getTransaction().commit();
			entityManager.close();
		} finally {
			shuttingDownLock.readLock().unlock();
		}
		return result;
	}

	/**
	 * Imports and persists data into this instance by using the output of the
	 * specified Importer. If process is shutting down, the change is ignored.
	 *
	 * @param importer a configured Importer instance
	 * @throws VogonImportException in case of import errors (I/O, format,
	 * indexing etc.)
	 * @throws VogonImportLogicalException in case of logical errors (without
	 * meaningful stack trace, just to show an error message)
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public void importData(Importer importer) throws VogonImportException, VogonImportLogicalException, ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			VogonUser user = getUserFromDatabase(entityManager);
			entityManager.getTransaction().begin();
			importer.importData(user, entityManager);
			entityManager.getTransaction().commit();

			restoreFromDatabase(entityManager);

			populateCurrencies(entityManager);

			user = getUserFromDatabase(entityManager);
			Currency defaultCurrency = user.getDefaultCurrency();

			if (!getCurrencies().contains(defaultCurrency)) {
				entityManager.getTransaction().begin();
				user = entityManager.find(VogonUser.class, user.getId());
				if (getCurrencies().size() > 0)
					user.setDefaultCurrency(getCurrencies().contains(Currency.getInstance(Locale.getDefault())) ? Currency.getInstance(Locale.getDefault()) : getCurrencies().get(0));
				else
					user.setDefaultCurrency(Currency.getInstance(Locale.getDefault()));
				entityManager.getTransaction().commit();
			}
			entityManager.close();
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Exports data by using the specified Exporter. If process is shutting
	 * down, the request is ignored.
	 *
	 * @param exporter a configured Exporter instance
	 * @throws VogonExportException in case of export errors (I/O, format,
	 * indexing etc.)
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public void exportData(Exporter exporter) throws VogonExportException, ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();

			EntityManager entityManager = entityManagerFactory.createEntityManager();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			exporter.exportData(getUserFromDatabase(entityManager), getAccounts(), getTransactions(), getCurrencyRates());
			entityManager.close();
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Restores all data from the persistence database
	 *
	 * @param entityManager the entity manager
	 */
	private void restoreFromDatabase(EntityManager entityManager) {
		exchangeRates = getCurrencyRatesFromDatabase(entityManager);
		transactionsCount = getTransactionsCountFromDatabase(entityManager);
	}

	/**
	 * Returns the latest copy of a transaction from the database. If process is
	 * shutting down, the request is ignored.
	 *
	 * @param entityManager the entity manager
	 * @param transaction the transaction to be searched (only the id is used)
	 * @return the transaction
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public FinanceTransaction getUpdatedTransactionFromDatabase(EntityManager entityManager, FinanceTransaction transaction) throws ApplicationShuttingDownException {
		if (transaction == null)
			return null;

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		//Retreive the transactions
		CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);
		tr.fetch(FinanceTransaction_.tags, JoinType.LEFT);
		transactionsCriteriaQuery.where(criteriaBuilder.equal(tr.get(FinanceTransaction_.id), transaction.getId()));

		FinanceTransaction result;
		try {
			result = entityManager.createQuery(transactionsCriteriaQuery).getSingleResult();
		} catch (NoResultException ex) {
			entityManager.close();
			return null;
		}

		//Post-fetch components
		CriteriaQuery<FinanceTransaction> transactionsComponentsFetchCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		Root<FinanceTransaction> trComponentsFetch = transactionsComponentsFetchCriteriaQuery.from(FinanceTransaction.class);
		transactionsComponentsFetchCriteriaQuery.where(criteriaBuilder.equal(tr.get(FinanceTransaction_.id), transaction.getId()));
		trComponentsFetch.fetch(FinanceTransaction_.components, JoinType.LEFT).fetch(TransactionComponent_.account, JoinType.LEFT);
		entityManager.createQuery(transactionsComponentsFetchCriteriaQuery).getSingleResult();
		return result;
	}

	/**
	 * Returns the latest copy of a transaction component from the database. If
	 * process is shutting down, the request is ignored.
	 *
	 * @param entityManager the entity manager
	 * @param component the transaction component to be searched (only the id is
	 * used)
	 * @return the transaction component
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public TransactionComponent getUpdatedTransactionComponentFromDatabase(EntityManager entityManager, TransactionComponent component) throws ApplicationShuttingDownException {
		if (component == null)
			return null;

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		//Retreive the transactions
		CriteriaQuery<TransactionComponent> transactionComponentCriteriaQuery = criteriaBuilder.createQuery(TransactionComponent.class);
		Root<TransactionComponent> trc = transactionComponentCriteriaQuery.from(TransactionComponent.class);
		transactionComponentCriteriaQuery.where(criteriaBuilder.equal(trc.get(TransactionComponent_.id), component.getId()));

		//Post-fetch transaction and account
		trc.fetch(TransactionComponent_.transaction, JoinType.LEFT);
		trc.fetch(TransactionComponent_.account, JoinType.LEFT);

		TransactionComponent result;
		try {
			result = entityManager.createQuery(transactionComponentCriteriaQuery).getSingleResult();
		} catch (NoResultException ex) {
			entityManager.close();
			return null;
		}

		return result;
	}

	/**
	 * Retrieves all transactions from the database (from firstTransaction to
	 * lastTransaction). If process is shutting down, the request is ignored.
	 *
	 * @param firstTransaction the first transaction number to be selected
	 * @param lastTransaction the last transaction number to be selected
	 * @return the list of all transactions stored in the database
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	protected List<FinanceTransaction> getTransactionsFromDatabase(int firstTransaction, int lastTransaction) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

			//Retreive the transactions
			CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
			Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);
			tr.fetch(FinanceTransaction_.tags, JoinType.LEFT);

			transactionsCriteriaQuery.orderBy(
					criteriaBuilder.asc(tr.get(FinanceTransaction_.transactionDate)),
					criteriaBuilder.asc(tr.get(FinanceTransaction_.id)));
			transactionsCriteriaQuery.select(tr).distinct(true);

			//Limit the number of transactions retreived
			TypedQuery<FinanceTransaction> query = entityManager.createQuery(transactionsCriteriaQuery);
			if (firstTransaction >= 0)
				query = query.setFirstResult(firstTransaction);
			if (lastTransaction >= 0 && firstTransaction >= 0)
				query = query.setMaxResults(lastTransaction - firstTransaction + 1);

			List<FinanceTransaction> result = query.getResultList();

			//Post-fetch components
			if (!result.isEmpty()) {
				CriteriaQuery<FinanceTransaction> transactionsComponentsFetchCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
				Root<FinanceTransaction> trComponentsFetch = transactionsComponentsFetchCriteriaQuery.from(FinanceTransaction.class);
				transactionsComponentsFetchCriteriaQuery.where(tr.in(result));
				trComponentsFetch.fetch(FinanceTransaction_.components, JoinType.LEFT).fetch(TransactionComponent_.account, JoinType.LEFT);
				entityManager.createQuery(transactionsComponentsFetchCriteriaQuery).getResultList();
			}
			entityManager.close();
			return result;
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Returns the number of transactions stored in the database
	 *
	 * @param entityManager the entity manager
	 * @return the number of transactions stored in the database
	 */
	protected long getTransactionsCountFromDatabase(EntityManager entityManager) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		CriteriaQuery<Long> transactionsCriteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);

		transactionsCriteriaQuery.select(criteriaBuilder.countDistinct(tr));

		Long result = entityManager.createQuery(transactionsCriteriaQuery).getSingleResult();
		transactionsCount = result;
		return result;
	}

	/**
	 * Retrieves all accounts from the database
	 *
	 * @param entityManager the entity manager
	 * @return the list of all accounts stored in the database
	 */
	protected List<FinanceAccount> getAccountsFromDatabase(EntityManager entityManager) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceAccount> accountsCriteriaQuery = criteriaBuilder.createQuery(FinanceAccount.class);
		accountsCriteriaQuery.from(FinanceAccount.class);

		List<FinanceAccount> result = entityManager.createQuery(accountsCriteriaQuery).getResultList();
		return result;
	}

	/**
	 * Retrieves all currency exchange rates from the database
	 *
	 * @param entityManager the entity manager
	 * @return the list of all currency exchange rates stored in the database
	 */
	protected List<CurrencyRate> getCurrencyRatesFromDatabase(EntityManager entityManager) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<CurrencyRate> exchangeRatesCriteriaQuery = criteriaBuilder.createQuery(CurrencyRate.class);
		Root<CurrencyRate> er = exchangeRatesCriteriaQuery.from(CurrencyRate.class);
		exchangeRatesCriteriaQuery.orderBy(
				criteriaBuilder.asc(er.get(CurrencyRate_.source)),
				criteriaBuilder.asc(er.get(CurrencyRate_.destination)));
		return entityManager.createQuery(exchangeRatesCriteriaQuery).getResultList();
	}

	/**
	 * Retrieves the Preferences class instance from the database
	 *
	 * @param entityManager the entity manager
	 * @return the Preferences class instance, or a new persisted instance if
	 * the database doesn't contain any
	 */
	public VogonUser getUserFromDatabase(EntityManager entityManager) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<VogonUser> userCriteriaQuery = criteriaBuilder.createQuery(VogonUser.class);
		userCriteriaQuery.from(VogonUser.class);
		VogonUser user = null;
		try {
			user = entityManager.createQuery(userCriteriaQuery).getSingleResult();
		} catch (javax.persistence.NoResultException ex) {
		}
		if (user == null) {
			user = new VogonUser(org.zlogic.vogon.data.Constants.DEFAULT_USERNAME, org.zlogic.vogon.data.Constants.DEFAULT_PASSWORD);
			entityManager.getTransaction().begin();
			entityManager.persist(user);
			entityManager.getTransaction().commit();
		}
		return user;
	}

	/**
	 * Retrieves the default currency from the database
	 *
	 * @param entityManager the entity manager
	 * @return the default currency stored in the database, or the system locale
	 * currency
	 */
	protected Currency getDefaultCurrencyFromDatabase(EntityManager entityManager) {
		VogonUser user = getUserFromDatabase(entityManager);
		Currency currency = user.getDefaultCurrency();
		if (currency == null) {
			currency = Currency.getInstance(Locale.getDefault());

			entityManager.merge(user);
		}
		return currency;
	}

	/**
	 * Returns the list of all currencies used in this instance
	 *
	 * @return the list of used currencies
	 */
	public List<Currency> getCurrencies() {
		List<Currency> currencies = new LinkedList<>();
		for (CurrencyRate rate : exchangeRates) {
			if (!currencies.contains(rate.getSource()))
				currencies.add(rate.getSource());
			if (!currencies.contains(rate.getDestination()))
				currencies.add(rate.getDestination());
		}
		return currencies;
	}

	/**
	 * Automatically creates missing currency exchange rates. If process is
	 * shutting down, the change is ignored.
	 *
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public void populateCurrencies() throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			populateCurrencies(entityManager);
			entityManager.close();
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Automatically creates missing currency exchange rates. Should only be
	 * called from an started transaction
	 *
	 * @param entityManager the entity manager
	 */
	protected void populateCurrencies(EntityManager entityManager) {
		entityManager.getTransaction().begin();

		//Search for missing currencies
		List<CurrencyRate> usedRates = new LinkedList<>();
		List<FinanceAccount> accounts = getAccountsFromDatabase(entityManager);
		for (FinanceAccount account1 : accounts) {
			for (FinanceAccount account2 : accounts) {
				if (account1.getCurrency() != account2.getCurrency()) {
					CurrencyRate rateFrom = null, rateTo = null;
					//Check that currencies between account1 and account2 can be converted
					for (CurrencyRate rate : exchangeRates) {
						if (rate.getSource() == account1.getCurrency() && rate.getDestination() == account2.getCurrency())
							rateFrom = rate;
						if (rate.getDestination() == account1.getCurrency() && rate.getSource() == account2.getCurrency())
							rateTo = rate;
					}
					//Add missing currency rates
					if (rateFrom == null) {
						CurrencyRate rate = new CurrencyRate(account1.getCurrency(), account2.getCurrency(), 1.0);
						entityManager.persist(rate);
						exchangeRates.add(rate);
						usedRates.add(rate);
					} else if (!usedRates.contains(rateFrom))
						usedRates.add(rateFrom);
					if (rateTo == null) {
						CurrencyRate rate = new CurrencyRate(account2.getCurrency(), account1.getCurrency(), 1.0);
						entityManager.persist(rate);
						exchangeRates.add(rate);
						usedRates.add(rate);
					} else if (!usedRates.contains(rateTo))
						usedRates.add(rateTo);
				}
			}
		}

		//Remove orphaned currencies
		for (CurrencyRate rate : exchangeRates) {
			if (!usedRates.contains(rate)) {
				CurrencyRate foundRate = entityManager.find(CurrencyRate.class, rate.getId());
				if (foundRate != null)
					entityManager.remove(foundRate);
			}
		}

		entityManager.getTransaction().commit();

		exchangeRates.clear();
		exchangeRates.addAll(usedRates);
	}

	/**
	 * Creates a new account and persists it in the database. If process is
	 * shutting down, the change is ignored.
	 *
	 * @param name the account name
	 * @param currency the account currency
	 * @return the created account
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public FinanceAccount createAccount(String name, Currency currency) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();

			VogonUser owner = getUserFromDatabase(entityManager);
			FinanceAccount account = new FinanceAccount(owner, name, currency);
			entityManager.persist(account);

			entityManager.getTransaction().commit();

			entityManager.close();
			return account;
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Creates a new transaction and persists it in the database. If process is
	 * shutting down, the change is ignored.
	 *
	 * @param description the transaction description
	 * @param tags the transaction tags
	 * @param date the transaction date
	 * @param type the transaction type
	 * @return the created transaction
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public FinanceTransaction createTransaction(String description, String[] tags, Date date, FinanceTransaction.Type type) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();

			VogonUser owner = getUserFromDatabase(entityManager);
			FinanceTransaction transaction = new FinanceTransaction(owner, description, tags, date, type);
			entityManager.persist(transaction);

			entityManager.getTransaction().commit();
			transactionsCount++;

			entityManager.close();
			return transaction;
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Clones a transaction. If process is shutting down, the change is ignored.
	 *
	 * @param transaction the transaction to be cloned
	 * @return the transaction clone
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public FinanceTransaction cloneTransaction(FinanceTransaction transaction) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();

			transaction = entityManager.find(FinanceTransaction.class, transaction.getId());
			FinanceTransaction newTransaction = transaction.clone();
			for (TransactionComponent component : newTransaction.getComponents())
				entityManager.persist(component);
			entityManager.persist(newTransaction);

			entityManager.getTransaction().commit();
			transactionsCount++;

			entityManager.close();
			return newTransaction;
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Creates a new transaction component and persists it in the database. If
	 * process is shutting down, the change is ignored.
	 *
	 * @param account the account
	 * @param transaction the transaction
	 * @param amount the amount which this component modifies the account, can
	 * be both negative and positive
	 * @return the created transaction component
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public TransactionComponent createTransactionComponent(FinanceAccount account, FinanceTransaction transaction, long amount) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();

			account = account != null ? entityManager.find(FinanceAccount.class, account.getId()) : null;
			transaction = entityManager.find(FinanceTransaction.class, transaction.getId());
			TransactionComponent component = new TransactionComponent(account, transaction, amount);
			entityManager.persist(component);

			if (transaction != null)
				transaction.addComponent(component);

			entityManager.getTransaction().commit();

			entityManager.close();
			return component;
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Returns an exchange rate for a pair of currencies
	 *
	 * @param source the source currency
	 * @param destination the target currency
	 * @return the source=>target exchange rate
	 */
	public double getExchangeRate(Currency source, Currency destination) {
		if (source == destination)
			return 1.0;
		for (CurrencyRate rate : exchangeRates) {
			if (rate.getSource() == source && rate.getDestination() == destination)
				return rate.getExchangeRate();
		}
		return Double.NaN;
	}

	/**
	 * Returns the transaction amount converted to a specific currency
	 *
	 * @param transaction the transaction
	 * @param currency the target currency
	 * @return the transaction amount, converted to the target currency
	 */
	public double getAmountInCurrency(FinanceTransaction transaction, Currency currency) {
		double amount = 0;
		for (TransactionComponent component : transaction.getComponents()) {
			double rate = getExchangeRate(component.getAccount() != null ? component.getAccount().getCurrency() : null, currency);
			amount += rate * component.getAmount();
		}
		return amount;
	}

	/**
	 * Deletes a transaction component (with all dependencies). If process is
	 * shutting down, the change is ignored.
	 *
	 * @param component the transaction component to delete
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public void deleteTransactionComponent(TransactionComponent component) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();

			component = entityManager.find(TransactionComponent.class, component.getId());

			FinanceTransaction transaction = component.getTransaction();
			FinanceAccount account = component.getAccount();
			if (transaction != null) {
				component.getTransaction().removeComponent(component);
				entityManager.merge(transaction);
			}

			entityManager.remove(entityManager.find(TransactionComponent.class, component.getId()));

			if (account != null)
				entityManager.merge(account);

			entityManager.getTransaction().commit();

			entityManager.close();
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Deletes a transaction (with all dependencies). If process is shutting
	 * down, the change is ignored.
	 *
	 * @param transaction the transaction to delete
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public void deleteTransaction(FinanceTransaction transaction) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();

			transaction = entityManager.find(FinanceTransaction.class, transaction.getId());

			if (transaction == null)
				return;

			List<FinanceAccount> affectedAccounts = transaction.getAccounts();

			//Remove all components
			for (TransactionComponent component : transaction.getComponents())
				entityManager.remove(entityManager.find(TransactionComponent.class, component.getId()));
			transaction.removeAllComponents();

			//Remove transaction
			FinanceTransaction foundTransaction = entityManager.find(FinanceTransaction.class, transaction.getId());
			entityManager.remove(foundTransaction);
			for (FinanceAccount account : affectedAccounts)
				entityManager.merge(account);
			entityManager.getTransaction().commit();

			if (foundTransaction != null)
				transactionsCount--;

			entityManager.close();
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Deletes an account (with all dependencies). If process is shutting down,
	 * the change is ignored.
	 *
	 * @param account the account to delete
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public void deleteAccount(FinanceAccount account) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();

			//Delete all related transaction components
			for (FinanceTransaction transaction : getTransactions()) {
				List<TransactionComponent> components = transaction.getComponentsForAccount(account);
				transaction.removeComponents(components);
				if (!components.isEmpty())
					entityManager.merge(transaction);
				for (TransactionComponent component : components)
					entityManager.remove(entityManager.find(TransactionComponent.class, component.getId()));
			}

			//Remove account
			entityManager.remove(entityManager.find(FinanceAccount.class, account.getId()));

			entityManager.getTransaction().commit();

			populateCurrencies(entityManager);

			entityManager.close();
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Recalculates an account's balance based on its transactions. If process
	 * is shutting down, the change is ignored.
	 *
	 * @param account the account to be updated
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public void refreshAccountBalance(FinanceAccount account) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			//Request all transactions from database
			EntityManager tempEntityManager = entityManagerFactory.createEntityManager();
			tempEntityManager.getTransaction().begin();

			new DatabaseMaintenance().refreshAccountBalance(account, tempEntityManager);

			tempEntityManager.getTransaction().commit();

			tempEntityManager.close();
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Deletes all orphaned transaction components. If process is shutting down,
	 * the change is ignored.
	 *
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public void cleanup() throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager tempEntityManager = entityManagerFactory.createEntityManager();

			tempEntityManager.getTransaction().begin();
			new DatabaseMaintenance().cleanup(tempEntityManager);

			populateCurrencies(tempEntityManager);
			restoreFromDatabase(tempEntityManager);

			tempEntityManager.getTransaction().commit();
			tempEntityManager.close();
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the list of accounts. If process is shutting down, the request is
	 * ignored.
	 *
	 * @return the list of accounts
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public List<FinanceAccount> getAccounts() throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			List<FinanceAccount> accounts = getAccountsFromDatabase(entityManager);
			entityManager.close();
			return accounts;
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Returns the list of transactions (from firstTransaction to
	 * lastTransaction). If process is shutting down, the request is ignored.
	 *
	 *
	 * @param firstTransaction the first transaction number to be selected
	 * @param lastTransaction the last transaction number to be selected
	 * @return the list of transactions
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public List<FinanceTransaction> getTransactions(int firstTransaction, int lastTransaction) throws ApplicationShuttingDownException {
		return getTransactionsFromDatabase(firstTransaction, lastTransaction);
	}

	/**
	 * Returns a specific transaction from database. If process is shutting
	 * down, the request is ignored.
	 *
	 * @param index the transaction's index
	 * @return all transactions in the database
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public FinanceTransaction getTransaction(int index) throws ApplicationShuttingDownException {
		List<FinanceTransaction> transactions = getTransactionsFromDatabase(index, index);
		return transactions.isEmpty() ? null : transactions.get(0);
	}

	/**
	 * Returns all transactions from database. If process is shutting down, the
	 * request is ignored.
	 *
	 * @return all transactions in the database
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public List<FinanceTransaction> getTransactions() throws ApplicationShuttingDownException {
		return getTransactionsFromDatabase(-1, -1);
	}

	/**
	 * Returns number of transactions
	 *
	 * @return the number of transactions
	 */
	public int getTransactionCount() {
		return (int) transactionsCount;
	}

	/**
	 * Returns the list of currency rates
	 *
	 * @return the list of currency rates
	 */
	public List<CurrencyRate> getCurrencyRates() {
		return exchangeRates;
	}

	/**
	 * Returns the default currency from database
	 *
	 * @return the default currency
	 */
	public Currency getDefaultCurrency() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		Currency defaultCurrency = getDefaultCurrencyFromDatabase(entityManager);
		entityManager.close();
		return defaultCurrency;
	}

	/**
	 * Returns the report factory for the default user
	 *
	 * @return the report factory for the default user
	 */
	public ReportFactory getReportFactory() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		ReportFactory reportFactory = new ReportFactory(getUserFromDatabase(entityManager));
		entityManager.close();
		return reportFactory;
	}

	/**
	 * Builds a report
	 *
	 * @param reportFactory the report configuration to use
	 * @return the generated report
	 */
	public Report buildReport(ReportFactory reportFactory) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		Report report = reportFactory.buildReport(entityManager);
		entityManager.close();
		return report;
	}

	/**
	 * Returns the list of all tags
	 *
	 * @return the list of all tags
	 */
	public Set<String> getAllTags() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		Set<String> tags = new ReportFactory(getUserFromDatabase(entityManager)).getAllTags(entityManager);
		entityManager.close();
		return tags;
	}
}
