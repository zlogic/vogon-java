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

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


/**
 * Class for storing the complete finance data
 *
 * @author Dmitry Zolotukhin
 */
public class FinanceData {
	/**
	 * Contains all finance transactions
	 */
	protected java.util.List<FinanceTransaction> transactions;

	/**
	 * Contains a list of all tags (cached)
	 */
	protected java.util.Set<String> tags;

	/**
	 * Preferred currency
	 */
	protected Currency defaultCurrency;

	/**
	 * The associated entity manager
	 */
	protected EntityManager currentEntityManager;
	
	/**
	 * Default constructor
	 */
	public FinanceData() {
		restoreFromDatabase();
	}

	/**
	 * Imports and persists data into this instance by using the output of the specified FileImporter
	 * 
	 * @param importer A configured FileImporter instance
	 * @throws VogonImportException In case of import errors (I/O, format, indexing etc.)
	 * @throws VogonImportLogicalException In case of logical errors (without meaningful stack trace, just to show an error message)
	 */
	public void importData(FileImporter importer) throws VogonImportException, VogonImportLogicalException{
		importer.importFile();

		restoreFromDatabase();

		populateCurrencies();
		if(!getCurrencies().contains(defaultCurrency))
			if(getCurrencies().size()>0)
				setDefaultCurrency(getCurrencies().contains(Currency.getInstance(Locale.getDefault()))?Currency.getInstance(Locale.getDefault()):getCurrencies().get(0));
			else
				setDefaultCurrency(Currency.getInstance(Locale.getDefault()));
	}

	/**
	 * Exports data by using the specified FileExporter
	 * 
	 * @param exporter A configured FileExporter instance
	 * @throws VogonExportException In case of export errors (I/O, format, indexing etc.)
	 */
	public void exportData(FileExporter exporter) throws VogonExportException{
		exporter.exportFile(this);
	}

	/**
	 * Restores all data from the persistence database
	 */
	public void restoreFromDatabase(){
		if(currentEntityManager!=null)
			currentEntityManager.close();
		currentEntityManager = DatabaseManager.getInstance().createEntityManager();
		transactions = getTransactionsFromDatabase();
		defaultCurrency = getDefaultCurrencyFromDatabase();
	}

	/**
	 * Retrieves all transactions from the database
	 * 
	 * @return the list of all transactions stored in the database
	 */
	public List<FinanceTransaction> getTransactionsFromDatabase(){
		EntityManager entityManager = currentEntityManager;
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		Root<FinanceTransaction> tr = transactionsCriteriaQuery.from(FinanceTransaction.class);
		transactionsCriteriaQuery.orderBy(
				criteriaBuilder.asc(tr.get(FinanceTransaction_.transactionDate)),
				criteriaBuilder.asc(tr.get(FinanceTransaction_.id))
				);

		return entityManager.createQuery(transactionsCriteriaQuery).getResultList();
	}

	/**
	 * Retrieves all accounts from the database
	 * 
	 * @return the list of all accounts stored in the database
	 */
	public List<FinanceAccount> getAccountsFromDatabase(){
		EntityManager entityManager = currentEntityManager;
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceAccount> accountsCriteriaQuery = criteriaBuilder.createQuery(FinanceAccount.class);

		return entityManager.createQuery(accountsCriteriaQuery).getResultList();
	}

	/**
	 * Retrieves all currency exchange rates from the database
	 * 
	 * @return the list of all currency exchange rates stored in the database
	 */
	public List<CurrencyRate> getCurrencyRatesFromDatabase(){
		EntityManager entityManager = currentEntityManager;
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<CurrencyRate> exchangeRatesCriteriaQuery = criteriaBuilder.createQuery(CurrencyRate.class);
		Root<CurrencyRate> er = exchangeRatesCriteriaQuery.from(CurrencyRate.class);
		exchangeRatesCriteriaQuery.orderBy(
				criteriaBuilder.asc(er.get(CurrencyRate_.source)),
				criteriaBuilder.asc(er.get(CurrencyRate_.destination))
				);
		return entityManager.createQuery(exchangeRatesCriteriaQuery).getResultList();
	}

	/**
	 * Retrieves the Preferences class instance from the database
	 * 
	 * @return the Preferences class instance, or a new persisted instance if the database doesn't contain any
	 */
	public Preferences getPreferencesFromDatabase(){
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Preferences> exchangeRatesCriteriaQuery = criteriaBuilder.createQuery(Preferences.class);
		List<Preferences> listPreferences = entityManager.createQuery(exchangeRatesCriteriaQuery).getResultList();
		if(listPreferences.isEmpty()){
			Preferences preferences = new Preferences();
			entityManager.getTransaction().begin();
			entityManager.persist(preferences);
			entityManager.getTransaction().commit();
			entityManager.close();
			return preferences;
		}
		entityManager.close();
		return listPreferences.get(0);
	}

	protected Currency getDefaultCurrencyFromDatabase(){
		Preferences preferences = getPreferencesFromDatabase();
		Currency currency = preferences.getDefaultCurrency();
		if(currency == null){
			currency = Currency.getInstance(Locale.getDefault());

			EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
			entityManager.getTransaction().begin();
			entityManager.merge(preferences);
			entityManager.getTransaction().commit();
			entityManager.close();
		}
		return currency;
	}

	/**
	 * Internal helper function
	 * Adds an account to the list & persists it (if necessary)
	 * Safe to call even if the account already exists
	 * Should only be called from an started transaction
	 * 
	 * @param account the account to be added
	 * @param entityManager the entity manager
	 */
	protected void persistenceAdd(FinanceAccount account,EntityManager entityManager){
		if(account==null)
			return;
		
		if(entityManager.find(FinanceAccount.class,account.id)==null)
			entityManager.persist(account);

		populateCurrencies();
	}

	/**
	 * Internal helper function
	 * Adds a transaction to the list & persists it and its components (if necessary)
	 * Safe to call even if the transaction already exists
	 * Should only be called from an started transaction
	 * 
	 * @param transaction the transaction to be added
	 * @param entityManager the entity manager
	 */
	protected void persistenceAdd(FinanceTransaction transaction,EntityManager entityManager){
		if(transaction==null)
			return;

		if(!transactions.contains(transaction)){
			transactions.add(transaction);
			for(TransactionComponent component : transaction.getComponents())
				if(!entityManager.contains(component))
					entityManager.persist(component);
		}

		if(!entityManager.contains(transaction))
			entityManager.persist(transaction);
	}

	/**
	 * Returns the total balance for all accounts with a specific currency
	 * 
	 * @param currency the currency (or null if the balance should be calculated for all currencies)
	 * @return the total balance
	 */
	public double getTotalBalance(Currency currency){
		long totalBalance = 0;
		for(FinanceAccount account : getAccountsFromDatabase())
			if(account.getCurrency()==currency)
				totalBalance += account.getRawBalance();
			else if(currency==null)
				totalBalance += Math.round(account.getBalance()*getExchangeRate(account.getCurrency(), getDefaultCurrency())*100);
		return totalBalance/100.0;
	}

	/**
	 * Returns the list of all currencies used in this instance
	 * 
	 * @return the list of used currencies
	 */
	public List<Currency> getCurrencies(){
		List<Currency> currencies = new LinkedList<>();
		for(CurrencyRate rate : getCurrencyRatesFromDatabase()){
			if(!currencies.contains(rate.getSource()))
				currencies.add(rate.getSource());
			if(!currencies.contains(rate.getDestination()))
				currencies.add(rate.getDestination());
		}
		return currencies;
	}

	/**
	 * Automatically creates missing currency exchange rates
	 * Should only be called from an started transaction
	 */
	protected void populateCurrencies(){
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();

		entityManager.getTransaction().begin();
		//Search for missing currencies
		List <CurrencyRate> usedRates = new LinkedList<>();
		List <CurrencyRate> exchangeRates = getCurrencyRatesFromDatabase();
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<String> accountCurrenciesCriteriaQuery = criteriaBuilder.createQuery(String.class);
		Root<FinanceAccount> acc = accountCurrenciesCriteriaQuery.from(FinanceAccount.class);
		accountCurrenciesCriteriaQuery.select(acc.get(FinanceAccount_.currency));
		accountCurrenciesCriteriaQuery.groupBy(acc.get(FinanceAccount_.currency));
		List<String> accountCurrencies = entityManager.createQuery(accountCurrenciesCriteriaQuery).getResultList();
		
		for(String currency1Code : accountCurrencies){
			for(String currency2Code : accountCurrencies){
				Currency currency1 = Currency.getInstance(currency1Code), currency2 = Currency.getInstance(currency2Code);
				if(currency1!=currency2){
					CurrencyRate rateFrom = null, rateTo = null;
					for(CurrencyRate rate : exchangeRates){
						if(rate.getSource()==currency1 && rate.getDestination()==currency2)
							rateFrom = rate;
						if(rate.getDestination()==currency1 && rate.getSource()==currency2)
							rateTo = rate;
					}
					if(rateFrom==null){
						CurrencyRate rate = new CurrencyRate(currency1, currency2, 1.0);
						entityManager.persist(rate);
						exchangeRates.add(rate);
						usedRates.add(rate);
					}else if(!usedRates.contains(rateFrom))
						usedRates.add(rateFrom);
					if(rateTo==null){
						CurrencyRate rate = new CurrencyRate(currency2, currency1, 1.0);
						entityManager.persist(rate);
						exchangeRates.add(rate);
						usedRates.add(rate);
					}else if(!usedRates.contains(rateTo))
						usedRates.add(rateTo);
				}
			}
		}

		//Remove orphaned currencies
		for(CurrencyRate rate : exchangeRates){
			if(!usedRates.contains(rate))
				entityManager.remove(entityManager.find(CurrencyRate.class,rate.id));
		}

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	/**
	 * Updates the account in database. Adds the account to the persistence if needed.
	 * 
	 * @param account The account to be updated
	 */
	public void updateAccount(FinanceAccount account){
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();

		persistenceAdd(account,entityManager);
		
		entityManager.merge(account);

		entityManager.getTransaction().commit();
		entityManager.close();
		
		populateCurrencies();
	}

	/**
	 * Sets new tags for a transaction
	 * 
	 * @param transaction The transaction to be updated
	 * @param tags The new tags
	 */
	public void setTransactionTags(FinanceTransaction transaction,String[] tags){
		EntityManager entityManager = currentEntityManager;
		entityManager.getTransaction().begin();
		transaction.setTags(tags);

		persistenceAdd(transaction,entityManager);

		entityManager.getTransaction().commit();
	}

	/**
	 * Sets a new date for a transaction
	 * 
	 * @param transaction The transaction to be updated
	 * @param date The new date
	 */
	public void setTransactionDate(FinanceTransaction transaction,Date date){
		EntityManager entityManager = currentEntityManager;
		entityManager.getTransaction().begin();
		transaction.setDate(date);

		persistenceAdd(transaction,entityManager);

		entityManager.getTransaction().commit();
	}

	/**
	 * Sets a new description for a transaction
	 * 
	 * @param transaction The transaction to be updated
	 * @param description The new description
	 */
	public void setTransactionDescription(FinanceTransaction transaction,String description){
		EntityManager entityManager = currentEntityManager;
		entityManager.getTransaction().begin();
		transaction.setDescription(description);

		persistenceAdd(transaction,entityManager);

		entityManager.getTransaction().commit();
	}

	/**
	 * Sets an expense transaction amount, works only for single-component expense transactions
	 * 
	 * @param transaction The transaction to be updated
	 * @param newAmount The new amount
	 */
	public void setTransactionAmount(ExpenseTransaction transaction,double newAmount){
		if(transaction.getComponents().isEmpty())
			return;

		TransactionComponent component = transaction.getComponents().get(0);
		EntityManager entityManager = currentEntityManager;
		entityManager.getTransaction().begin();

		transaction.updateComponentRawAmount(component,Math.round(newAmount*100));

		persistenceAdd(transaction,entityManager);

		if(!entityManager.contains(component))
			entityManager.persist(component);
		entityManager.getTransaction().commit();
	}

	/**
	 * Sets an expense transaction account, works only for single-component expense transactions
	 * 
	 * @param transaction The transaction to be updated
	 * @param newAccount The new account
	 */
	public void setTransactionAccount(ExpenseTransaction transaction,FinanceAccount newAccount){
		if(transaction.getComponents().isEmpty())
			return;

		TransactionComponent component = transaction.getComponents().get(0);
		EntityManager entityManager = currentEntityManager;
		entityManager.getTransaction().begin();

		transaction.updateComponentAccount(component,newAccount);

		persistenceAdd(transaction,entityManager);

		if(!entityManager.contains(component))
			entityManager.persist(component);

		persistenceAdd(newAccount,entityManager);

		entityManager.getTransaction().commit();
	}


	/**
	 * Sets an transaction component amount
	 * 
	 * @param component The component to be updated
	 * @param newAmount The new amount
	 */
	public void setTransactionComponentAmount(TransactionComponent component,double newAmount){
		EntityManager entityManager = currentEntityManager;
		entityManager.getTransaction().begin();

		if(component.getTransaction().getComponents().contains(component))
			component.getTransaction().updateComponentRawAmount(component,Math.round(newAmount*100));
		else{
			component.setRawAmount(Math.round(newAmount*100));
			component.getTransaction().addComponent(component);
		}

		persistenceAdd(component.getTransaction(),entityManager);

		if(!entityManager.contains(component))
			entityManager.persist(component);
		entityManager.getTransaction().commit();
	}

	/**
	 * Sets an transaction component account
	 * 
	 * @param component The component to be updated
	 * @param newAccount The new account
	 */
	public void setTransactionComponentAccount(TransactionComponent component,FinanceAccount newAccount){
		EntityManager entityManager = currentEntityManager;
		entityManager.getTransaction().begin();

		if(component.getTransaction().getComponents().contains(component))
			component.getTransaction().updateComponentAccount(component,newAccount);
		else{
			component.setAccount(newAccount);
			component.getTransaction().addComponent(component);
		}


		persistenceAdd(component.getTransaction(),entityManager);

		if(!entityManager.contains(component))
			entityManager.persist(component);

		persistenceAdd(newAccount,entityManager);
		entityManager.getTransaction().commit();
	}

	/**
	 * Updates the exchange rate in database
	 * 
	 * @param rate The currency rate to be updated
	 */
	public void updateExchangeRate(CurrencyRate rate){
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		if(rate==entityManager.find(CurrencyRate.class, rate.id))
			return;
		
		entityManager.getTransaction().begin();
		entityManager.merge(rate);
		entityManager.getTransaction().commit();
		
		entityManager.close();
	}


	/**
	 * Sets the default currency
	 * 
	 * @param defaultCurrency The new default currency
	 */
	public void setDefaultCurrency(Currency defaultCurrency){
		if(defaultCurrency==null)
			return;

		this.defaultCurrency = defaultCurrency;

		Preferences preferences = getPreferencesFromDatabase();
		preferences.setDefaultCurrency(defaultCurrency);

		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.merge(preferences);
		entityManager.getTransaction().commit();
		
		entityManager.close();
	}

	/**
	 * Returns an exchange rate for a pair of currencies
	 * 
	 * @param source The source currency
	 * @param destination The target currency
	 * @return The source=>target exchange rate
	 */
	public double getExchangeRate(Currency source, Currency destination){
		if(source==destination)
			return 1.0;
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Double> exchangeRatesCriteriaQuery = criteriaBuilder.createQuery(Double.class);
		Root<CurrencyRate> er = exchangeRatesCriteriaQuery.from(CurrencyRate.class);
		exchangeRatesCriteriaQuery.select(er.get(CurrencyRate_.exchangeRate));
		exchangeRatesCriteriaQuery.where(criteriaBuilder.equal(er.get(CurrencyRate_.source),source.getCurrencyCode()),criteriaBuilder.equal(er.get(CurrencyRate_.source),source.getCurrencyCode()));
		List<Double> results = entityManager.createQuery(exchangeRatesCriteriaQuery).getResultList();
		entityManager.close();
		return results.isEmpty()?Double.NaN:results.get(0);
	}

	/**
	 * Returns the transaction amount converted to a specific currency
	 * 
	 * @param transaction The transaction
	 * @param currency The target currency
	 * @return The transaction amount, converted to the target currency
	 */
	public double getAmountInCurrency(FinanceTransaction transaction,Currency currency){
		double amount = 0;
		for(TransactionComponent component : transaction.getComponents()){
			double rate = getExchangeRate(component.getAccount()!=null?component.getAccount().getCurrency():null, currency);
			amount += rate*component.getAmount();
		}
		return amount;
	}

	/**
	 * Deletes a transaction component (with all dependencies)
	 * 
	 * @param component The transaction component to delete
	 */
	public void deleteTransactionComponent(TransactionComponent component){
		EntityManager entityManager = currentEntityManager;
		entityManager.getTransaction().begin();

		persistenceAdd(component.getTransaction(),entityManager);

		component.getTransaction().removeComponent(component);

		entityManager.remove(component);
		entityManager.getTransaction().commit();
	}

	/**
	 * Deletes a transaction (with all dependencies)
	 * 
	 * @param transaction The transaction to delete
	 */
	public void deleteTransaction(FinanceTransaction transaction){
		EntityManager entityManager = currentEntityManager;
		entityManager.getTransaction().begin();

		transactions.remove(transaction);

		for(TransactionComponent component : transaction.getComponents())
			entityManager.remove(component);
		transaction.removeAllComponents();

		entityManager.remove(transaction);
		entityManager.getTransaction().commit();
	}

	/**
	 * Deletes an account (with all dependencies)
	 * 
	 * @param account The account to delete
	 */
	public void deleteAccount(FinanceAccount account){
		EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
		entityManager.getTransaction().begin();
		

		int deleted = entityManager.createQuery("DELETE from TransactionComponent component WHERE component.account = :acc").setParameter("acc", account).executeUpdate();
		
		/*
		for(FinanceTransaction transaction : transactions){
			List<TransactionComponent> components = transaction.getComponentsForAccount(account);
			transaction.removeComponents(components);
			for(TransactionComponent component:components)
				entityManager.remove(component);
		}
		*/
		
		account = entityManager.find(FinanceAccount.class,account.id);
		if(account!=null)
			entityManager.remove(account);


		entityManager.getTransaction().commit();
		entityManager.close();
		populateCurrencies();
	}

	/**
	 * Recalculates an account's balance based on its transactions
	 * 
	 * @param account the account to be updated
	 */
	public void refreshAccountBalance(FinanceAccount account){
		EntityManager entityManager = currentEntityManager;
		entityManager.getTransaction().begin();
		account.updateRawBalance(-account.getRawBalance());
		for(FinanceTransaction transaction:transactions){
			for(TransactionComponent component:transaction.getComponentsForAccount(account)){
				account.updateRawBalance(component.getRawAmount());
			}
		}
		entityManager.getTransaction().commit();
	}

	/**
	 * Deletes all orphaned transactions, accounts and transaction components
	 */
	public void cleanup(){
		EntityManager entityManager = currentEntityManager;
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FinanceTransaction> transactionsCriteriaQuery = criteriaBuilder.createQuery(FinanceTransaction.class);
		CriteriaQuery<FinanceAccount> accountsCriteriaQuery = criteriaBuilder.createQuery(FinanceAccount.class);
		CriteriaQuery<TransactionComponent> componentsCriteriaQuery = criteriaBuilder.createQuery(TransactionComponent.class);

		//Get all data from DB
		List<FinanceTransaction> transactionsDB = entityManager.createQuery(transactionsCriteriaQuery).getResultList();
		List<FinanceAccount> accountsDB = entityManager.createQuery(accountsCriteriaQuery).getResultList();
		List<TransactionComponent> componentsDB = entityManager.createQuery(componentsCriteriaQuery).getResultList();

		//Remove OK items from list
		transactionsDB.removeAll(transactions);
		for(FinanceTransaction transaction : transactions)
			componentsDB.removeAll(transaction.getComponents());

		transactions.removeAll(transactionsDB);

		entityManager.getTransaction().begin();
		for(FinanceTransaction transaction : transactionsDB){
			for(TransactionComponent component : transaction.getComponents())
				entityManager.remove(component);
			componentsDB.removeAll(transaction.getComponents());
			transaction.removeAllComponents();
			entityManager.remove(transaction);
		}

		for(FinanceAccount account : accountsDB)
			entityManager.remove(account);

		for(TransactionComponent component : componentsDB){
			if(component.getTransaction()!=null)
				component.getTransaction().removeComponent(component);
			component.setTransaction(null);
			entityManager.remove(component);
		}


		entityManager.getTransaction().commit();
		
		populateCurrencies();
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the list of accounts
	 * @return the list of accounts
	 */
	public List<FinanceAccount> getAccounts(){
		return getAccountsFromDatabase();
	}

	/**
	 * Returns the list of transactions
	 * @return the list of transactions
	 */
	public List<FinanceTransaction> getTransactions(){
		return transactions;
	}

	/**
	 * Returns the list of currency rates
	 * @return the list of currency rates
	 */
	public List<CurrencyRate> getCurrencyRates(){
		return getCurrencyRatesFromDatabase();
	}

	/**
	 * Returns the default currency
	 * @return the default currency
	 */
	public Currency getDefaultCurrency(){
		if(defaultCurrency!=null)
			return defaultCurrency;
		else
			return null;
	}
}
