/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.util.Currency;
import java.util.Date;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.persistence.EntityManager;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.swing.event.EventListenerList;

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
	 * Preferred currency
	 */
	protected Currency defaultCurrency;
	/**
	 * Number of transactions in the database
	 */
	protected long transactionsCount = 0;

	/**
	 * Default constructor
	 */
	public FinanceData() {
		restoreFromDatabase();
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
	public void importData(FileImporter importer) throws VogonImportException, VogonImportLogicalException {
		importer.importFile();

		restoreFromDatabase();

		populateCurrencies();
		if (!getCurrencies().contains(defaultCurrency))
			if (getCurrencies().size() > 0)
				setDefaultCurrency(getCurrencies().contains(Currency.getInstance(Locale.getDefault())) ? Currency.getInstance(Locale.getDefault()) : getCurrencies().get(0));
			else
				setDefaultCurrency(Currency.getInstance(Locale.getDefault()));

		fireTransactionsUpdated();
		fireAccountsUpdated();
		fireCurrenciesUpdated();
	}

	/**
	 * Exports data by using the specified FileExporter
	 *
	 * @param exporter a configured FileExporter instance
	 * @throws VogonExportException in case of export errors (I/O, format,
	 * indexing etc.)
	 */
	public void exportData(FileExporter exporter) throws VogonExportException {
		exporter.exportFile(this);
	}

	/**
	 * Restores all data from the persistence database
	 */
	private void restoreFromDatabase() {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		exchangeRates = getCurrencyRatesFromDatabase(entityManager);
		defaultCurrency = getDefaultCurrencyFromDatabase(entityManager);
		entityManager.close();

		transactionsCount = getTransactionsCountFromDatabase();

		fireTransactionsUpdated();
		fireAccountsUpdated();
		fireCurrenciesUpdated();
	}

	/**
	 * Retrieves all transactions from the database (from firstTransaction to
	 * lastTransaction)
	 *
	 * @param firstTransaction the first transaction number to be selected
	 * @param lastTransaction the last transaction number to be selected
	 * @return the list of all transactions stored in the database
	 */
	protected List<FinanceTransaction> getTransactionsFromDatabase(int firstTransaction, int lastTransaction) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		//Prefetch components
		CriteriaQuery<FinanceTransaction> transactionsComponentsFetchCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		Root<FinanceTransaction> trComponentsFetch = transactionsComponentsFetchCriteriaQuery.from(FinanceTransaction.class);
		trComponentsFetch.fetch(FinanceTransaction_.components, JoinType.LEFT).fetch(TransactionComponent_.account, JoinType.LEFT);
		entityManager.createQuery(transactionsComponentsFetchCriteriaQuery).getResultList();

		//Retreive the transactions
		CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);
		tr.fetch(FinanceTransaction_.tags, JoinType.LEFT);

		transactionsCriteriaQuery.orderBy(
				criteriaBuilder.asc(tr.get(FinanceTransaction_.transactionDate)),
				criteriaBuilder.asc(tr.get(FinanceTransaction_.id)));
		transactionsCriteriaQuery.select(tr).distinct(true);

		TypedQuery query = entityManager.createQuery(transactionsCriteriaQuery);
		if (firstTransaction >= 0)
			query = query.setFirstResult(firstTransaction);
		if (lastTransaction >= 0)
			query = query.setMaxResults(lastTransaction - firstTransaction + 1);

		List<FinanceTransaction> result = query.getResultList();
		entityManager.close();
		return result;
	}

	/**
	 * Returns the number of transactions stored in the database
	 *
	 * @return the number of transactions stored in the database
	 */
	protected long getTransactionsCountFromDatabase() {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		CriteriaQuery<Long> transactionsCriteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);

		transactionsCriteriaQuery.select(criteriaBuilder.countDistinct(tr));

		Long result = entityManager.createQuery(transactionsCriteriaQuery).getSingleResult();
		entityManager.close();
		return result;
	}

	/**
	 * Retrieves all accounts from the database
	 *
	 * @return the list of all accounts stored in the database
	 */
	protected List<FinanceAccount> getAccountsFromDatabase() {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceAccount> accountsCriteriaQuery = criteriaBuilder.createQuery(FinanceAccount.class);
		Root<FinanceAccount> acc = accountsCriteriaQuery.from(FinanceAccount.class);

		List<FinanceAccount> result = entityManager.createQuery(accountsCriteriaQuery).getResultList();
		entityManager.close();
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
	protected Preferences getPreferencesFromDatabase(EntityManager entityManager) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Preferences> preferencesCriteriaQuery = criteriaBuilder.createQuery(Preferences.class);
		Root<Preferences> prf = preferencesCriteriaQuery.from(Preferences.class);
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
	 * Internal helper function Adds an account to the list & persists it (if
	 * necessary) Safe to call even if the account already exists Should only be
	 * called from an started transaction
	 *
	 * @param account the account to be added
	 * @param entityManager the entity manager with an initiated transaction
	 * @return true if account was added, false if it's already present in the
	 * accounts list
	 */
	protected boolean persistenceAdd(FinanceAccount account, EntityManager entityManager) {
		if (account == null)
			return false;

		boolean result = false;

		if (entityManager.find(FinanceAccount.class, account.id) == null) {
			entityManager.persist(account);
			result = true;
		}

		populateCurrencies();

		return result;
	}

	/**
	 * Internal helper function Adds a transaction to the list & persists it and
	 * its components (if necessary) Safe to call even if the transaction
	 * already exists Should only be called from an started transaction
	 *
	 * @param transaction the transaction to be added
	 * @param entityManager the entity manager
	 * @return true if transaction was added, false if it's already present in
	 * the transactions list
	 */
	protected boolean persistenceAdd(FinanceTransaction transaction, EntityManager entityManager) {
		if (transaction == null)
			return false;

		boolean result = false;

		for (TransactionComponent component : transaction.getComponents())
			if (entityManager.find(TransactionComponent.class, component.id) == null)
				entityManager.persist(component);

		if (entityManager.find(FinanceTransaction.class, transaction.id) == null) {
			entityManager.persist(transaction);
			result = true;
		}

		return result;
	}

	/**
	 * Returns the total balance for all accounts with a specific currency
	 *
	 * @param currency the currency (or null if the balance should be calculated
	 * for all currencies)
	 * @return the total balance
	 */
	public double getTotalBalance(Currency currency) {
		long totalBalance = 0;
		for (FinanceAccount account : getAccountsFromDatabase()) {
			if (!account.getIncludeInTotal())
				continue;
			if (account.getCurrency() == currency)
				totalBalance += account.getRawBalance();
			else if (currency == null)
				totalBalance += Math.round(account.getBalance() * getExchangeRate(account.getCurrency(), getDefaultCurrency()) * 100);
		}
		return totalBalance / 100.0;
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
	 * Automatically creates missing currency exchange rates Should only be
	 * called from an started transaction
	 */
	protected void populateCurrencies() {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();

		entityManager.getTransaction().begin();

		//Search for missing currencies
		List<CurrencyRate> usedRates = new LinkedList<>();
		List<FinanceAccount> accounts = getAccountsFromDatabase();
		for (FinanceAccount account1 : accounts) {
			for (FinanceAccount account2 : accounts) {
				if (account1.getCurrency() != account2.getCurrency()) {
					CurrencyRate rateFrom = null, rateTo = null;
					for (CurrencyRate rate : exchangeRates) {
						if (rate.getSource() == account1.getCurrency() && rate.getDestination() == account2.getCurrency())
							rateFrom = rate;
						if (rate.getDestination() == account1.getCurrency() && rate.getSource() == account2.getCurrency())
							rateTo = rate;
					}
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
			if (!usedRates.contains(rate))
				entityManager.remove(entityManager.find(CurrencyRate.class, rate.id));
		}

		entityManager.getTransaction().commit();

		entityManager.close();
	}

	/**
	 * Adds a new account
	 *
	 * @param account the account to be added
	 */
	public void createAccount(FinanceAccount account) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		boolean accountAdded = persistenceAdd(account, entityManager);

		entityManager.getTransaction().commit();
		entityManager.close();

		if (accountAdded) {
			fireAccountCreated(account);
			fireCurrenciesUpdated();
		}
	}

	/**
	 * Sets a new account name. Adds the account to the persistence if needed.
	 *
	 * @param account the account to be updated
	 * @param name the new account name
	 */
	public void setAccountName(FinanceAccount account, String name) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		boolean accountAdded = persistenceAdd(account, entityManager);

		account.setName(name);
		entityManager.merge(account);

		entityManager.getTransaction().commit();
		entityManager.close();

		if (accountAdded) {
			fireAccountCreated(account);
			fireCurrenciesUpdated();
		}
		fireAccountUpdated(account);
	}

	/**
	 * Sets a new account currency. Adds the account to the persistence if
	 * needed.
	 *
	 * @param account the account to be updated
	 * @param currency the new account currency
	 */
	public void setAccountCurrency(FinanceAccount account, Currency currency) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		boolean accountAdded = persistenceAdd(account, entityManager);

		account.setCurrency(currency);
		entityManager.merge(account);

		entityManager.getTransaction().commit();
		entityManager.close();

		populateCurrencies();

		if (accountAdded)
			fireAccountsUpdated();
		fireCurrenciesUpdated();
	}

	/**
	 * Sets if this account should be included in the total for all accounts.
	 *
	 * @param account the account to be updated
	 * @param includeInTotal true if the account should be included in the total
	 */
	public void setAccountIncludeInTotal(FinanceAccount account, boolean includeInTotal) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		persistenceAdd(account, entityManager);

		account.setIncludeInTotal(includeInTotal);
		entityManager.merge(account);

		populateCurrencies();

		entityManager.getTransaction().commit();
		entityManager.close();

		fireAccountsUpdated();
	}

	/**
	 * Adds a new transaction
	 *
	 * @param transaction the transaction to be added
	 */
	public void createTransaction(FinanceTransaction transaction) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		boolean transactionAdded = persistenceAdd(transaction, entityManager);

		entityManager.getTransaction().commit();
		entityManager.close();

		if (transactionAdded) {
			fireTransactionCreated(transaction);
			fireTransactionsUpdated();
		}
		for (FinanceAccount account : transaction.getAccounts())
			fireAccountUpdated(account);
	}

	/**
	 * Sets new tags for a transaction
	 *
	 * @param transaction the transaction to be updated
	 * @param tags the new tags
	 */
	public void setTransactionTags(FinanceTransaction transaction, String[] tags) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		boolean transactionAdded = persistenceAdd(transaction, entityManager);

		transaction.setTags(tags);
		entityManager.merge(transaction);

		entityManager.getTransaction().commit();
		entityManager.close();

		if (transactionAdded) {
			fireTransactionCreated(transaction);
			fireTransactionsUpdated();
		}
		fireTransactionUpdated(transaction);
		for (FinanceAccount account : transaction.getAccounts())
			fireAccountUpdated(account);
	}

	/**
	 * Sets a new date for a transaction
	 *
	 * @param transaction the transaction to be updated
	 * @param date the new date
	 */
	public void setTransactionDate(FinanceTransaction transaction, Date date) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		boolean transactionAdded = persistenceAdd(transaction, entityManager);

		transaction.setDate(date);
		entityManager.merge(transaction);

		entityManager.getTransaction().commit();
		entityManager.close();

		if (transactionAdded) {
			fireTransactionCreated(transaction);
			fireTransactionsUpdated();
		}
		fireTransactionUpdated(transaction);
		for (FinanceAccount account : transaction.getAccounts())
			fireAccountUpdated(account);
	}

	/**
	 * Sets a new description for a transaction
	 *
	 * @param transaction the transaction to be updated
	 * @param description the new description
	 */
	public void setTransactionDescription(FinanceTransaction transaction, String description) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		boolean transactionAdded = persistenceAdd(transaction, entityManager);

		transaction.setDescription(description);
		entityManager.merge(transaction);

		entityManager.getTransaction().commit();
		entityManager.close();

		if (transactionAdded) {
			fireTransactionCreated(transaction);
			fireTransactionsUpdated();
		}
		fireTransactionUpdated(transaction);
		for (FinanceAccount account : transaction.getAccounts())
			fireAccountUpdated(account);
	}

	/**
	 * Sets a new type for a transaction
	 *
	 * @param transaction the transaction to be updated
	 * @param type the new transaction type
	 */
	public void setTransactionType(FinanceTransaction transaction, FinanceTransaction.Type type) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		boolean transactionAdded = persistenceAdd(transaction, entityManager);

		transaction.setType(type);
		entityManager.merge(transaction);

		entityManager.getTransaction().commit();
		entityManager.close();

		if (transactionAdded) {
			fireTransactionCreated(transaction);
			fireTransactionsUpdated();
		}
		fireTransactionUpdated(transaction);
		for (FinanceAccount account : transaction.getAccounts())
			fireAccountUpdated(account);
	}

	/**
	 * Sets an expense transaction amount, works only for single-component
	 * expense transactions
	 *
	 * @param transaction the transaction to be updated
	 * @param newAmount the new amount
	 */
	public void setTransactionAmount(FinanceTransaction transaction, double newAmount) {
		if (transaction.getType() != FinanceTransaction.Type.EXPENSEINCOME)
			return;
		if (transaction.getComponents().isEmpty())
			return;

		TransactionComponent component = transaction.getComponents().get(0);
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		boolean transactionAdded = persistenceAdd(transaction, entityManager);

		if (entityManager.find(TransactionComponent.class, component.id) == null)
			entityManager.persist(component);

		transaction.updateComponentRawAmount(component, Math.round(newAmount * 100));
		entityManager.merge(transaction);

		entityManager.getTransaction().commit();
		entityManager.close();

		if (transactionAdded) {
			fireTransactionCreated(transaction);
			fireTransactionsUpdated();
		}
		fireTransactionUpdated(transaction);

		for (FinanceAccount account : transaction.getAccounts())
			fireAccountUpdated(account);
	}

	/**
	 * Sets an expense transaction account, works only for single-component
	 * expense transactions
	 *
	 * @param transaction the transaction to be updated
	 * @param newAccount the new account
	 */
	public void setTransactionAccount(FinanceTransaction transaction, FinanceAccount newAccount) {
		if (transaction.getType() != FinanceTransaction.Type.EXPENSEINCOME)
			return;
		if (transaction.getComponents().isEmpty())
			return;

		TransactionComponent component = transaction.getComponents().get(0);
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		FinanceAccount oldAccount = component.getAccount();

		boolean transactionAdded = persistenceAdd(transaction, entityManager);
		boolean accountAdded = persistenceAdd(newAccount, entityManager);

		if (entityManager.find(TransactionComponent.class, component.id) == null)
			entityManager.persist(component);

		transaction.updateComponentAccount(component, newAccount);
		entityManager.merge(transaction);
		entityManager.merge(newAccount);
		entityManager.merge(oldAccount);

		entityManager.getTransaction().commit();
		entityManager.close();

		if (transactionAdded) {
			fireTransactionCreated(transaction);
			fireTransactionsUpdated();
		}
		fireTransactionUpdated(transaction);

		if (accountAdded)
			fireAccountCreated(newAccount);
		fireAccountUpdated(oldAccount);
		fireAccountUpdated(newAccount);
		fireCurrenciesUpdated();

		for (FinanceAccount account : transaction.getAccounts())
			if (account != oldAccount && account != newAccount)
				fireAccountUpdated(account);
	}

	/**
	 * Sets an transaction component amount
	 *
	 * @param component the component to be updated
	 * @param newAmount the new amount
	 */
	public void setTransactionComponentAmount(TransactionComponent component, double newAmount) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		boolean transactionAdded = persistenceAdd(component.getTransaction(), entityManager);

		if (entityManager.find(TransactionComponent.class, component.id) == null)
			entityManager.persist(component);

		if (component.getTransaction().getComponents().contains(component))
			component.getTransaction().updateComponentRawAmount(component, Math.round(newAmount * 100));
		else {
			component.setRawAmount(Math.round(newAmount * 100));
			component.getTransaction().addComponent(component);
		}

		entityManager.merge(component);
		if (component.getAccount() != null) {
			entityManager.merge(component.getAccount());
		}

		entityManager.getTransaction().commit();
		entityManager.close();

		if (transactionAdded) {
			fireTransactionCreated(component.getTransaction());
			fireTransactionsUpdated();
		}
		fireTransactionUpdated(component.getTransaction());

		fireAccountUpdated(component.getAccount());
	}

	/**
	 * Sets an transaction component account
	 *
	 * @param component the component to be updated
	 * @param newAccount the new account
	 */
	public void setTransactionComponentAccount(TransactionComponent component, FinanceAccount newAccount) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		FinanceAccount oldAccount = component.getAccount();

		boolean transactionAdded = persistenceAdd(component.getTransaction(), entityManager);

		if (entityManager.find(TransactionComponent.class, component.id) == null)
			entityManager.persist(component);

		persistenceAdd(newAccount, entityManager);

		if (component.getTransaction().getComponents().contains(component))
			component.getTransaction().updateComponentAccount(component, newAccount);
		else {
			component.setAccount(newAccount);
			component.getTransaction().addComponent(component);
		}

		entityManager.merge(component);
		if (component.getTransaction() != null)
			entityManager.merge(component.getTransaction());
		if (component.getAccount() != null)
			entityManager.merge(component.getAccount());
		if (oldAccount != null && component.getAccount() != oldAccount)
			entityManager.merge(oldAccount);

		entityManager.getTransaction().commit();
		entityManager.close();

		if (transactionAdded) {
			fireTransactionCreated(component.getTransaction());
			fireTransactionsUpdated();
		}
		fireTransactionUpdated(component.getTransaction());

		fireAccountUpdated(component.getAccount());
		if (component.getAccount() != oldAccount) {
			fireAccountUpdated(oldAccount);
			fireCurrenciesUpdated();
		}
	}

	/**
	 * Sets the new exchange rate
	 *
	 * @param rate the currency rate to be modified
	 * @param newRate the new exchange rate
	 */
	public void setExchangeRate(CurrencyRate rate, double newRate) {
		if (!exchangeRates.contains(rate))
			return;
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		rate.setExchangeRate(newRate);
		entityManager.merge(rate);

		entityManager.getTransaction().commit();
		entityManager.close();

		fireTransactionsUpdated();
		fireCurrenciesUpdated();
		fireAccountsUpdated();
	}

	/**
	 * Sets the default currency
	 *
	 * @param defaultCurrency the new default currency
	 */
	public void setDefaultCurrency(Currency defaultCurrency) {
		if (defaultCurrency == null)
			return;

		this.defaultCurrency = defaultCurrency;

		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		Preferences preferences = getPreferencesFromDatabase(entityManager);
		entityManager.getTransaction().begin();

		preferences.setDefaultCurrency(defaultCurrency);
		entityManager.merge(preferences);

		entityManager.getTransaction().commit();
		entityManager.close();

		fireTransactionsUpdated();
		fireAccountsUpdated();
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
	public void deleteTransactionComponent(TransactionComponent component) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		persistenceAdd(component.getTransaction(), entityManager);

		component.getTransaction().removeComponent(component);

		entityManager.remove(entityManager.find(TransactionComponent.class, component.id));

		entityManager.getTransaction().commit();
		entityManager.close();

		fireTransactionUpdated(component.getTransaction());
		fireAccountUpdated(component.getAccount());
		fireCurrenciesUpdated();
	}

	/**
	 * Deletes a transaction (with all dependencies)
	 *
	 * @param transaction the transaction to delete
	 */
	public void deleteTransaction(FinanceTransaction transaction) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		List<FinanceAccount> affectedAccounts = transaction.getAccounts();

		for (TransactionComponent component : transaction.getComponents())
			entityManager.remove(entityManager.find(TransactionComponent.class, component.id));
		transaction.removeAllComponents();

		entityManager.remove(entityManager.find(FinanceTransaction.class, transaction.id));
		entityManager.getTransaction().commit();
		entityManager.close();

		fireTransactionDeleted(transaction);

		for (FinanceAccount account : affectedAccounts)
			fireAccountUpdated(account);
	}

	/**
	 * Deletes an account (with all dependencies)
	 *
	 * @param account the account to delete
	 */
	public void deleteAccount(FinanceAccount account) {
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		for (FinanceTransaction transaction : getTransactions()) {
			List<TransactionComponent> components = transaction.getComponentsForAccount(account);
			transaction.removeComponents(components);
			if (!components.isEmpty())
				entityManager.merge(transaction);
			for (TransactionComponent component : components)
				entityManager.remove(entityManager.find(TransactionComponent.class, component.id));
		}
		entityManager.remove(entityManager.find(FinanceAccount.class, account.id));

		entityManager.getTransaction().commit();
		entityManager.close();

		populateCurrencies();

		fireTransactionsUpdated();
		fireAccountDeleted(account);
		fireCurrenciesUpdated();
	}

	/**
	 * Recalculates an account's balance based on its transactions
	 *
	 * @param account the account to be updated
	 */
	public void refreshAccountBalance(FinanceAccount account) {
		//Request all transactions from database
		EntityManager tempEntityManager = DatabaseManager.getInstance().createEntityManager();
		CriteriaBuilder criteriaBuilder = tempEntityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		Root<FinanceTransaction> ftr = transactionsCriteriaQuery.from(FinanceTransaction.class);
		FinanceAccount tempAccount = tempEntityManager.find(FinanceAccount.class, account.id);

		//TODO: add paging here
		tempEntityManager.getTransaction().begin();
		tempAccount.updateRawBalance(-tempAccount.getRawBalance());
		for (FinanceTransaction transaction : tempEntityManager.createQuery(transactionsCriteriaQuery).getResultList()) {
			for (TransactionComponent component : transaction.getComponentsForAccount(tempAccount)) {
				tempAccount.updateRawBalance(component.getRawAmount());
			}
		}
		tempEntityManager.getTransaction().commit();

		account.updateRawBalance(-account.getRawBalance() + tempAccount.getRawBalance());

		tempEntityManager.close();

		fireAccountUpdated(account);
	}

	/**
	 * Deletes all orphaned transactions, accounts and transaction components
	 */
	public void cleanup() {
		EntityManager tempEntityManager = DatabaseManager.getInstance().createEntityManager();
		CriteriaBuilder componentCriteriaBuilder = tempEntityManager.getCriteriaBuilder();
		CriteriaQuery<TransactionComponent> componentsCriteriaQuery = componentCriteriaBuilder.createQuery(TransactionComponent.class);
		Root<TransactionComponent> trc = componentsCriteriaQuery.from(TransactionComponent.class);

		CriteriaBuilder transactionCriteriaBuilder = tempEntityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = transactionCriteriaBuilder.createQuery(FinanceTransaction.class);
		Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);

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
		tempEntityManager.close();

		populateCurrencies();

		restoreFromDatabase();

		fireTransactionsUpdated();
		fireAccountsUpdated();
		fireCurrenciesUpdated();
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the list of accounts
	 *
	 * @return the list of accounts
	 */
	public List<FinanceAccount> getAccounts() {
		return getAccountsFromDatabase();
	}

	/**
	 * Returns the list of transactions (from firstTransaction to
	 * lastTransaction)
	 *
	 * @param firstTransaction the first transaction number to be selected
	 * @param lastTransaction the last transaction number to be selected
	 * @return the list of transactions
	 */
	public List<FinanceTransaction> getTransactions(int firstTransaction, int lastTransaction) {
		return getTransactionsFromDatabase(firstTransaction, lastTransaction);
	}

	/**
	 * Returns a specific transaction from database
	 *
	 * @param index the transaction's index
	 * @return all transactions in the database
	 */
	public FinanceTransaction getTransaction(int index) {
		List<FinanceTransaction> transactions = getTransactionsFromDatabase(index, index);
		return transactions.isEmpty() ? null : transactions.get(0);
	}

	/**
	 * Returns all transactions from database
	 *
	 * @return all transactions in the database
	 */
	public List<FinanceTransaction> getTransactions() {
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
	 * Returns the default currency
	 *
	 * @return the default currency
	 */
	public Currency getDefaultCurrency() {
		if (defaultCurrency != null)
			return defaultCurrency;
		else
			return null;
	}

	/*
	 * Assigned event listeners
	 */
	/**
	 * Listener for transaction created events
	 */
	public interface TransactionCreatedEventListener extends EventListener {

		/**
		 * A transaction created callback
		 *
		 * @param newTransaction the transaction that was created
		 */
		void transactionCreated(FinanceTransaction newTransaction);
	}

	/**
	 * Listener for transaction updated events
	 */
	public interface TransactionUpdatedEventListener extends EventListener {

		/**
		 * A transaction updated callback
		 *
		 * @param updatedTransaction the transaction that was updated
		 */
		void transactionUpdated(FinanceTransaction updatedTransaction);

		/**
		 * A transaction updated handler (all transactions have been updated)
		 */
		void transactionsUpdated();
	}

	/**
	 * Listener for transaction deleted events
	 */
	public interface TransactionDeletedEventListener extends EventListener {

		/**
		 * A transaction deleted callback
		 *
		 * @param deletedTransaction the deleted transaction
		 */
		void transactionDeleted(FinanceTransaction deletedTransaction);
	}

	/**
	 * Listener for account created events
	 */
	public interface AccountCreatedEventListener extends EventListener {

		/**
		 * An account created callback
		 *
		 * @param newAccount the account that was created
		 */
		void accountCreated(FinanceAccount newAccount);
	}

	/**
	 * Listener for account updated events
	 */
	public interface AccountUpdatedEventListener extends EventListener {

		/**
		 * An account updated callback
		 *
		 * @param updatedAccount the account that was updated
		 */
		void accountUpdated(FinanceAccount updatedAccount);

		/**
		 * An account updated handler (all accounts have been updated)
		 */
		void accountsUpdated();
	}

	/**
	 * Listener for account deleted events
	 */
	public interface AccountDeletedEventListener extends EventListener {

		/**
		 * An account deleted callback
		 *
		 * @param deletedAccount the deleted account
		 */
		void accountDeleted(FinanceAccount deletedAccount);
	}

	/**
	 * Listener for currency updated events
	 */
	public interface CurrencyUpdatedEventListener extends EventListener {

		/**
		 * A currencies updated handler (all accounts have been updated)
		 */
		void currenciesUpdated();
	}
	/**
	 * List of event listeners
	 */
	@Transient
	protected EventListenerList eventListeners = new EventListenerList();

	/**
	 * Dispatches a transaction updated event
	 *
	 * @param editedTransaction the transaction that was updated
	 */
	protected void fireTransactionUpdated(FinanceTransaction editedTransaction) {
		if (editedTransaction == null)
			return;
		for (TransactionUpdatedEventListener listener : eventListeners.getListeners(TransactionUpdatedEventListener.class))
			listener.transactionUpdated(editedTransaction);
	}

	/**
	 * Dispatches a transactions updated event (all transactions were updated)
	 */
	protected void fireTransactionsUpdated() {
		transactionsCount = getTransactionsCountFromDatabase();
		for (TransactionUpdatedEventListener listener : eventListeners.getListeners(TransactionUpdatedEventListener.class))
			listener.transactionsUpdated();
	}

	/**
	 * Dispatches a transaction created event
	 *
	 * @param newTransaction the transaction that was created
	 */
	protected void fireTransactionCreated(FinanceTransaction newTransaction) {
		if (newTransaction == null)
			return;
		for (TransactionCreatedEventListener listener : eventListeners.getListeners(TransactionCreatedEventListener.class))
			listener.transactionCreated(newTransaction);
	}

	/**
	 * Dispatches a transaction deleted event
	 *
	 * @param deletedTransaction the transaction that was deleted
	 */
	protected void fireTransactionDeleted(FinanceTransaction deletedTransaction) {
		if (deletedTransaction == null)
			return;
		for (TransactionDeletedEventListener listener : eventListeners.getListeners(TransactionDeletedEventListener.class))
			listener.transactionDeleted(deletedTransaction);
	}

	/**
	 * Dispatches an account updated event
	 *
	 * @param editedAccount the account that was updated
	 */
	protected void fireAccountUpdated(FinanceAccount editedAccount) {
		if (editedAccount == null)
			return;
		for (AccountUpdatedEventListener listener : eventListeners.getListeners(AccountUpdatedEventListener.class))
			listener.accountUpdated(editedAccount);
	}

	/**
	 * Dispatches an accounts updated event (all accounts were updated)
	 */
	protected void fireAccountsUpdated() {
		for (AccountUpdatedEventListener listener : eventListeners.getListeners(AccountUpdatedEventListener.class))
			listener.accountsUpdated();
	}

	/**
	 * Dispatches an account created event
	 *
	 * @param newAccount the account that was created
	 */
	protected void fireAccountCreated(FinanceAccount newAccount) {
		if (newAccount == null)
			return;
		for (AccountCreatedEventListener listener : eventListeners.getListeners(AccountCreatedEventListener.class))
			listener.accountCreated(newAccount);
	}

	/**
	 * Dispatches an account deleted event
	 *
	 * @param deletedAccount the account that was deleted
	 */
	protected void fireAccountDeleted(FinanceAccount deletedAccount) {
		if (deletedAccount == null)
			return;
		for (AccountDeletedEventListener listener : eventListeners.getListeners(AccountDeletedEventListener.class))
			listener.accountDeleted(deletedAccount);
	}

	/**
	 * Dispatches a currencies updated event (all accounts were updated)
	 */
	protected void fireCurrenciesUpdated() {
		for (CurrencyUpdatedEventListener listener : eventListeners.getListeners(CurrencyUpdatedEventListener.class))
			listener.currenciesUpdated();
	}

	/**
	 * Adds a new listener for transaction created events
	 *
	 * @param listener the listener
	 */
	public void addTransactionCreatedListener(TransactionCreatedEventListener listener) {
		eventListeners.add(TransactionCreatedEventListener.class, listener);
	}

	/**
	 * Adds a new listener for transaction updated events
	 *
	 * @param listener the listener
	 */
	public void addTransactionUpdatedListener(TransactionUpdatedEventListener listener) {
		eventListeners.add(TransactionUpdatedEventListener.class, listener);
	}

	/**
	 * Adds a new listener for transaction deleted events
	 *
	 * @param listener the listener
	 */
	public void addTransactionDeletedListener(TransactionUpdatedEventListener listener) {
		eventListeners.add(TransactionUpdatedEventListener.class, listener);
	}

	/**
	 * Adds a new listener for account created events
	 *
	 * @param listener the listener
	 */
	public void addAccountCreatedListener(AccountCreatedEventListener listener) {
		eventListeners.add(AccountCreatedEventListener.class, listener);
	}

	/**
	 * Adds a new listener for account updated events
	 *
	 * @param listener the listener
	 */
	public void addAccountUpdatedListener(AccountUpdatedEventListener listener) {
		eventListeners.add(AccountUpdatedEventListener.class, listener);
	}

	/**
	 * Adds a new listener for account deleted events
	 *
	 * @param listener the listener
	 */
	public void addAccountDeletedListener(AccountUpdatedEventListener listener) {
		eventListeners.add(AccountUpdatedEventListener.class, listener);
	}

	/**
	 * Adds a new listener for currency updated events
	 *
	 * @param listener the listener
	 */
	public void addCurrencyUpdatedListener(CurrencyUpdatedEventListener listener) {
		eventListeners.add(CurrencyUpdatedEventListener.class, listener);
	}
}
