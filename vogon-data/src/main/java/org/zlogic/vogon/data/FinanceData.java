/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.util.Currency;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import org.zlogic.vogon.data.interop.FileExporter;
import org.zlogic.vogon.data.interop.FileImporter;
import org.zlogic.vogon.data.interop.VogonExportException;
import org.zlogic.vogon.data.interop.VogonImportException;
import org.zlogic.vogon.data.interop.VogonImportLogicalException;

/**
 * Class for storing the finance data, performing database operations and
 * generating events
 *
 * @author Dmitry Zolotukhin
 */
public class FinanceData {

	/**
	 * Contains exchange rates
	 */
	protected java.util.List<CurrencyRate> exchangeRates;
	/**
	 * Entity manager factory
	 */
	private EntityManagerFactory entityManagerFactory = javax.persistence.Persistence.createEntityManagerFactory("VogonPU"); //NOI18N
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
	 * @param requestedQuery a TransactedQuery implementation
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
	 * specified FileImporter
	 *
	 * @param importer a configured FileImporter instance
	 * @throws VogonImportException in case of import errors (I/O, format,
	 * indexing etc.)
	 * @throws VogonImportLogicalException in case of logical errors (without
	 * meaningful stack trace, just to show an error message)
	 */
	public void importData(FileImporter importer) throws VogonImportException, VogonImportLogicalException, ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			importer.importFile(this, entityManager);

			restoreFromDatabase(entityManager);

			populateCurrencies(entityManager);

			Preferences preferences = getPreferencesFromDatabase(entityManager);
			Currency defaultCurrency = preferences.getDefaultCurrency();

			if (!getCurrencies().contains(defaultCurrency)) {
				entityManager.getTransaction().begin();
				preferences = entityManager.find(Preferences.class, preferences.id);
				if (getCurrencies().size() > 0)
					preferences.setDefaultCurrency(getCurrencies().contains(Currency.getInstance(Locale.getDefault())) ? Currency.getInstance(Locale.getDefault()) : getCurrencies().get(0));
				else
					preferences.setDefaultCurrency(Currency.getInstance(Locale.getDefault()));
				entityManager.getTransaction().commit();
			}
			entityManager.close();
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Exports data by using the specified FileExporter
	 *
	 * @param exporter a configured FileExporter instance
	 * @throws VogonExportException in case of export errors (I/O, format,
	 * indexing etc.)
	 */
	public void exportData(FileExporter exporter) throws VogonExportException, ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			exporter.exportFile(this);
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Restores all data from the persistence database
	 */
	private void restoreFromDatabase(EntityManager entityManager) {
		exchangeRates = getCurrencyRatesFromDatabase(entityManager);
		transactionsCount = getTransactionsCountFromDatabase(entityManager);
	}

	/**
	 * Returns the latest copy of a transaction from the database
	 *
	 * @param transaction the transaction to be searcher (only the id is used)
	 * @return the transaction
	 */
	public FinanceTransaction getUpdatedTransactionFromDatabase(EntityManager entityManager, FinanceTransaction transaction) throws ApplicationShuttingDownException {
		if (transaction == null)
			return null;

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		//Retreive the transactions
		CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);
		tr.fetch(FinanceTransaction_.tags, JoinType.LEFT);
		transactionsCriteriaQuery.where(criteriaBuilder.equal(tr.get(FinanceTransaction_.id), transaction.id));

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
		transactionsComponentsFetchCriteriaQuery.where(criteriaBuilder.equal(tr.get(FinanceTransaction_.id), transaction.id));
		trComponentsFetch.fetch(FinanceTransaction_.components, JoinType.LEFT).fetch(TransactionComponent_.account, JoinType.LEFT);
		entityManager.createQuery(transactionsComponentsFetchCriteriaQuery).getSingleResult();
		return result;
	}

	/**
	 * Returns the latest copy of a transaction from the database
	 *
	 * @param transaction the transaction to be searcher (only the id is used)
	 * @return the transaction
	 */
	public TransactionComponent getUpdatedTransactionComponentFromDatabase(EntityManager entityManager, TransactionComponent component) throws ApplicationShuttingDownException {
		if (component == null)
			return null;

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		//Retreive the transactions
		CriteriaQuery<TransactionComponent> transactionComponentCriteriaQuery = criteriaBuilder.createQuery(TransactionComponent.class);
		Root<TransactionComponent> trc = transactionComponentCriteriaQuery.from(TransactionComponent.class);
		transactionComponentCriteriaQuery.where(criteriaBuilder.equal(trc.get(TransactionComponent_.id), component.id));

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
	 * lastTransaction)
	 *
	 * @param firstTransaction the first transaction number to be selected
	 * @param lastTransaction the last transaction number to be selected
	 * @return the list of all transactions stored in the database
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
	 * @param entityManager the entity manager (used for obtaining the same
	 * classes from DB)
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
	 * @param entityManager the entity manager (used for obtaining the same
	 * classes from DB)
	 * @return the Preferences class instance, or a new persisted instance if
	 * the database doesn't contain any
	 */
	public Preferences getPreferencesFromDatabase(EntityManager entityManager) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Preferences> preferencesCriteriaQuery = criteriaBuilder.createQuery(Preferences.class);
		preferencesCriteriaQuery.from(Preferences.class);
		Preferences preferences = null;
		try {
			preferences = entityManager.createQuery(preferencesCriteriaQuery).getSingleResult();
		} catch (javax.persistence.NoResultException ex) {
		}
		if (preferences == null) {
			preferences = new Preferences();
			entityManager.getTransaction().begin();
			entityManager.persist(preferences);
			entityManager.getTransaction().commit();
		}
		return preferences;
	}

	/**
	 * Retrieves the default currency from the database
	 *
	 * @param entityManager the entity manager (used for obtaining the same
	 * classes from DB)
	 * @return the default currency stored in the database, or the system locale
	 * currency
	 */
	protected Currency getDefaultCurrencyFromDatabase(EntityManager entityManager) {
		Preferences preferences = getPreferencesFromDatabase(entityManager);
		Currency currency = preferences.getDefaultCurrency();
		if (currency == null) {
			currency = Currency.getInstance(Locale.getDefault());

			entityManager.merge(preferences);
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
	 * Automatically creates missing currency exchange rates.
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
				CurrencyRate foundRate = entityManager.find(CurrencyRate.class, rate.id);
				if (foundRate != null)
					entityManager.remove(foundRate);
			}
		}

		entityManager.getTransaction().commit();

		exchangeRates.clear();
		exchangeRates.addAll(usedRates);
	}

	/**
	 * Adds a new account
	 *
	 * @param account the account to be added
	 */
	public FinanceAccount createAccount(String name, Currency currency) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();

			FinanceAccount account = new FinanceAccount(name, currency);
			entityManager.persist(account);

			entityManager.getTransaction().commit();

			entityManager.close();
			return account;
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Adds a new transaction
	 *
	 * @param transaction the transaction to be added
	 */
	public FinanceTransaction createTransaction(String description, String[] tags, Date date, FinanceTransaction.Type type) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();

			FinanceTransaction transaction = new FinanceTransaction(description, tags, date, type);
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
	 * Clones a transaction
	 *
	 * @param transaction the transaction to be cloned
	 */
	public FinanceTransaction cloneTransaction(FinanceTransaction transaction) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();

			transaction = entityManager.find(FinanceTransaction.class, transaction.id);
			FinanceTransaction newTransaction = transaction.clone();
			entityManager.persist(newTransaction);

			entityManager.getTransaction().commit();
			transactionsCount++;

			entityManager.close();
			return transaction;
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Adds a new transaction
	 *
	 * @param component the component to be added
	 */
	public TransactionComponent createTransactionComponent(FinanceAccount account, FinanceTransaction transaction, long amount) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();

			account = account != null ? entityManager.find(FinanceAccount.class, account.id) : null;
			transaction = entityManager.find(FinanceTransaction.class, transaction.id);
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
	 * Deletes a transaction component (with all dependencies)
	 *
	 * @param component the transaction component to delete
	 */
	public void deleteTransactionComponent(TransactionComponent component) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();

			component = entityManager.find(TransactionComponent.class, component.id);

			FinanceTransaction transaction = component.getTransaction();
			FinanceAccount account = component.getAccount();
			if (transaction != null) {
				component.getTransaction().removeComponent(component);
				entityManager.merge(transaction);
			}

			entityManager.remove(entityManager.find(TransactionComponent.class, component.id));

			if (account != null)
				entityManager.merge(account);

			entityManager.getTransaction().commit();

			entityManager.close();
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Deletes a transaction (with all dependencies)
	 *
	 * @param transaction the transaction to delete
	 */
	public void deleteTransaction(FinanceTransaction transaction) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();

			transaction = entityManager.find(FinanceTransaction.class, transaction.id);

			if (transaction == null)
				return;

			List<FinanceAccount> affectedAccounts = transaction.getAccounts();

			//Remove all components
			for (TransactionComponent component : transaction.getComponents())
				entityManager.remove(entityManager.find(TransactionComponent.class, component.id));
			transaction.removeAllComponents();

			//Remove transaction
			FinanceTransaction foundTransaction = entityManager.find(FinanceTransaction.class, transaction.id);
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
	 * Deletes an account (with all dependencies)
	 *
	 * @param account the account to delete
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
					entityManager.remove(entityManager.find(TransactionComponent.class, component.id));
			}

			//Remove account
			entityManager.remove(entityManager.find(FinanceAccount.class, account.id));

			entityManager.getTransaction().commit();

			populateCurrencies(entityManager);

			entityManager.close();
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Recalculates an account's balance based on its transactions
	 *
	 * @param account the account to be updated
	 */
	public void refreshAccountBalance(FinanceAccount account) throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			//Request all transactions from database
			EntityManager tempEntityManager = entityManagerFactory.createEntityManager();
			CriteriaBuilder criteriaBuilder = tempEntityManager.getCriteriaBuilder();
			CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
			transactionsCriteriaQuery.from(FinanceTransaction.class);
			FinanceAccount tempAccount = tempEntityManager.find(FinanceAccount.class, account.id);

			//Recalculate balance from related transactions
			tempEntityManager.getTransaction().begin();
			tempAccount.updateRawBalance(-tempAccount.getRawBalance());

			TypedQuery<FinanceTransaction> transactionsBatchQuery = tempEntityManager.createQuery(transactionsCriteriaQuery);

			int currentTransaction = 0;
			boolean done = false;
			while (!done) {
				List<FinanceTransaction> transactions = transactionsBatchQuery.setFirstResult(currentTransaction).setMaxResults(Constants.batchFetchSize).getResultList();
				currentTransaction += transactions.size();
				done = transactions.isEmpty();
				for (FinanceTransaction transaction : transactions)
					for (TransactionComponent component : transaction.getComponentsForAccount(tempAccount))
						tempAccount.updateRawBalance(component.getRawAmount());
			}
			tempEntityManager.getTransaction().commit();

			//Update real account balance from temporary account
			account.updateRawBalance(-account.getRawBalance() + tempAccount.getRawBalance());

			tempEntityManager.close();
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/**
	 * Deletes all orphaned transactions, accounts and transaction components
	 */
	public void cleanup() throws ApplicationShuttingDownException {
		try {
			shuttingDownLock.readLock().lock();
			if (shuttingDown)
				throw new ApplicationShuttingDownException();
			EntityManager tempEntityManager = entityManagerFactory.createEntityManager();
			CriteriaBuilder componentCriteriaBuilder = tempEntityManager.getCriteriaBuilder();
			CriteriaQuery<TransactionComponent> componentsCriteriaQuery = componentCriteriaBuilder.createQuery(TransactionComponent.class);
			componentsCriteriaQuery.from(TransactionComponent.class);

			CriteriaBuilder transactionCriteriaBuilder = tempEntityManager.getCriteriaBuilder();
			CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = transactionCriteriaBuilder.createQuery(FinanceTransaction.class);
			transactionsCriteriaQuery.from(FinanceTransaction.class);

			tempEntityManager.getTransaction().begin();

			//Get all data from DB
			List<TransactionComponent> componentsDB = tempEntityManager.createQuery(componentsCriteriaQuery).getResultList();
			List<FinanceTransaction> transactionsDB = tempEntityManager.createQuery(transactionsCriteriaQuery).getResultList();

			//Remove OK items from list
			for (FinanceTransaction transaction : transactionsDB)
				componentsDB.removeAll(transaction.getComponents());

			//Remove anything that still exists
			for (TransactionComponent component : componentsDB) {
				if (component.getTransaction() != null)
					component.getTransaction().removeComponent(component);
				component.setTransaction(null);
				tempEntityManager.remove(component);
			}

			tempEntityManager.getTransaction().commit();

			populateCurrencies(tempEntityManager);
			restoreFromDatabase(tempEntityManager);

			tempEntityManager.close();
		} finally {
			shuttingDownLock.readLock().unlock();
		}
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the list of accounts
	 *
	 * @return the list of accounts
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
	 * lastTransaction)
	 *
	 * @param firstTransaction the first transaction number to be selected
	 * @param lastTransaction the last transaction number to be selected
	 * @return the list of transactions
	 */
	public List<FinanceTransaction> getTransactions(int firstTransaction, int lastTransaction) throws ApplicationShuttingDownException {
		return getTransactionsFromDatabase(firstTransaction, lastTransaction);
	}

	/**
	 * Returns a specific transaction from database
	 *
	 * @param index the transaction's index
	 * @return all transactions in the database
	 */
	public FinanceTransaction getTransaction(int index) throws ApplicationShuttingDownException {
		List<FinanceTransaction> transactions = getTransactionsFromDatabase(index, index);
		return transactions.isEmpty() ? null : transactions.get(0);
	}

	/**
	 * Returns all transactions from database
	 *
	 * @return all transactions in the database
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
		Preferences preferences = getPreferencesFromDatabase(entityManager);
		Currency defaultCurrency = preferences.getDefaultCurrency();
		entityManager.close();
		return defaultCurrency;
	}
}
