/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.persistence.EntityManager;
import org.zlogic.vogon.data.ApplicationShuttingDownException;
import org.zlogic.vogon.data.Constants;
import org.zlogic.vogon.data.CurrencyRate;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.Preferences;
import org.zlogic.vogon.data.TransactedChange;
import org.zlogic.vogon.data.interop.FileImporter;
import org.zlogic.vogon.data.interop.VogonImportException;
import org.zlogic.vogon.data.interop.VogonImportLogicalException;

/**
 * Placeholder class to contain Java FX adapters. This class is a central point
 * for contacting the Data Storage layer.
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class DataManager {

	/**
	 * Localization messages
	 */
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The persistence helper instance
	 */
	private FinanceData financeData = new FinanceData();
	/**
	 * Transactions (currently visible)
	 */
	private ObservableList<TransactionModelAdapter> transactions = FXCollections.observableList(new LinkedList<TransactionModelAdapter>());
	/**
	 * All accounts, including reporting
	 */
	private ObservableList<AccountInterface> allAccounts = FXCollections.observableList(new LinkedList<AccountInterface>());
	/**
	 * Persisted accounts
	 */
	private ObservableList<AccountInterface> accounts = FXCollections.observableList(new LinkedList<AccountInterface>());
	/**
	 * Currencies
	 */
	private ObservableList<CurrencyModelAdapter> currencies = FXCollections.observableList(new LinkedList<CurrencyModelAdapter>());
	/**
	 * Exchange rates
	 */
	private ObservableList<CurrencyRateModelAdapter> exchangeRates = FXCollections.observableList(new LinkedList<CurrencyRateModelAdapter>());
	/**
	 * Transaction types
	 */
	private ObservableList<TransactionTypeModelAdapter> transactionTypes = FXCollections.observableList(new LinkedList<TransactionTypeModelAdapter>());
	/**
	 * Preferred currency
	 */
	private ObjectProperty<CurrencyModelAdapter> defaultCurrency = new SimpleObjectProperty<>();
	/**
	 * Listener for changes of default currency (saves to database)
	 */
	private ChangeListener<CurrencyModelAdapter> defaultCurrencyListener = new ChangeListener<CurrencyModelAdapter>() {
		@Override
		public void changed(ObservableValue<? extends CurrencyModelAdapter> ov, CurrencyModelAdapter oldValue, CurrencyModelAdapter newValue) {
			financeData.performTransactedChange(new TransactedChange() {
				private Currency currency;
				
				public TransactedChange setCurrency(Currency currency) {
					this.currency = currency;
					return this;
				}
				
				@Override
				public void performChange(EntityManager entityManager) {
					Preferences preferences = financeData.getPreferencesFromDatabase(entityManager);
					preferences.setDefaultCurrency(currency);
				}
			}.setCurrency(newValue.getCurrency()));
			refreshReportingAccounts();
		}
	};
	/**
	 * Index of first transaction on current page
	 */
	private IntegerProperty firstTransactionIndex = new SimpleIntegerProperty(0);
	/**
	 * Index of last transaction on current page
	 */
	private IntegerProperty lastTransactionIndex = new SimpleIntegerProperty(0);

	/**
	 * Constructor for DataManager. Loads data from database.
	 */
	public DataManager() {
		for (FinanceTransaction.Type currentType : FinanceTransaction.Type.values())
			if (currentType != FinanceTransaction.Type.UNDEFINED)
				transactionTypes.add(new TransactionTypeModelAdapter(currentType));
		
		reloadData();
	}

	/**
	 * Updates the data table from database
	 */
	private void reloadData() {
		//Update currencies first
		defaultCurrency.removeListener(defaultCurrencyListener);
		defaultCurrency.setValue(new CurrencyModelAdapter(financeData.getDefaultCurrency()));
		defaultCurrency.addListener(defaultCurrencyListener);
		
		refreshAccounts();
		reloadCurrencies();
		
		transactions.clear();

		//Update transactions
		for (FinanceTransaction transaction : financeData.getTransactions(firstTransactionIndex.get(), lastTransactionIndex.get()))
			transactions.add(new TransactionModelAdapter(transaction, this));
	}

	/**
	 * Sets the visible transactions range
	 *
	 * @param currentPage the current page
	 * @param pageSize page size
	 */
	public void setVisibleTransactions(int currentPage, int pageSize) {
		int firstTransactionIndexValue = currentPage * pageSize;
		int lastTransactionIndexValue = firstTransactionIndexValue + pageSize - 1;
		lastTransactionIndexValue = Math.min(lastTransactionIndexValue, financeData.getTransactionCount() - 1);
		firstTransactionIndexValue = financeData.getTransactionCount() - 1 - firstTransactionIndexValue;
		lastTransactionIndexValue = financeData.getTransactionCount() - 1 - lastTransactionIndexValue;
		
		this.firstTransactionIndex.set(firstTransactionIndexValue);
		this.lastTransactionIndex.set(lastTransactionIndexValue);
		reloadTransactions();
	}

	/**
	 * Refreshes reporting accounts
	 */
	private void refreshReportingAccounts() {
		//Remove existing reporting accounts
		List<AccountInterface> reportingAccounts = new LinkedList<>();
		for (AccountInterface account : allAccounts)
			if (account instanceof ReportingAccount)
				reportingAccounts.add(account);
		allAccounts.removeAll(reportingAccounts);

		//Recreate reporting accounts
		for (Currency currency : financeData.getCurrencies())
			allAccounts.add(new ReportingAccount(MessageFormat.format(messages.getString("TOTAL_ACCOUNT"), new Object[]{currency.getCurrencyCode()}), getTotalBalance(currency), currency));
		if (defaultCurrency.get() != null && defaultCurrency.get().getCurrency() != null)
			allAccounts.add(new ReportingAccount(MessageFormat.format(messages.getString("TOTAL_ALL_ACCOUNTS"), new Object[]{defaultCurrency.get().getCurrency().getCurrencyCode()}), getTotalBalance(null), defaultCurrency.get().getCurrency()));
	}

	/**
	 * Refreshes all accounts from database.
	 */
	public void refreshAccounts() {
		//Update accounts
		List<AccountInterface> orphanedAccounts = new LinkedList<>(accounts);
		for (FinanceAccount account : financeData.getAccounts()) {
			AccountModelAdapter existingAccount = findAccountAdapter(account);
			if (existingAccount == null) {
				AccountModelAdapter newAccount = new AccountModelAdapter(account, this);
				accounts.add(newAccount);
				allAccounts.add(newAccount);
			} else {
				existingAccount.setAccount(account);
				existingAccount.updateFxProperties();
				orphanedAccounts.remove(existingAccount);
			}
		}
		
		accounts.removeAll(orphanedAccounts);
		allAccounts.removeAll(orphanedAccounts);
		
		refreshReportingAccounts();
	}

	/**
	 * Reloads transactions from database. Discards old TransactionModelAdapter
	 * instances.
	 */
	public void reloadTransactions() {
		List<FinanceTransaction> newTransactions = financeData.getTransactions(
				Math.min(firstTransactionIndex.get(), lastTransactionIndex.get()),
				Math.max(firstTransactionIndex.get(), lastTransactionIndex.get()));
		Collections.reverse(newTransactions);
		
		List<TransactionModelAdapter> newTransactionAdapters = new LinkedList<>();
		for (FinanceTransaction transaction : newTransactions)
			newTransactionAdapters.add(new TransactionModelAdapter(transaction, this));
		
		transactions.setAll(newTransactionAdapters);
	}

	/**
	 * Performs updateFxProperties for all transactions
	 */
	public void updateTransactionsFxProperties() {
		for (TransactionModelAdapter transaction : transactions)
			transaction.updateFxProperties();
	}

	/**
	 * Reloads currency rates from database
	 */
	public void reloadCurrencies() {
		List<CurrencyRateModelAdapter> newExchangeRates = new LinkedList<>();
		for (CurrencyRate rate : financeData.getCurrencyRates())
			newExchangeRates.add(new CurrencyRateModelAdapter(rate, this));
		exchangeRates.setAll(newExchangeRates);
		
		List<CurrencyModelAdapter> newCurrencies = new LinkedList<>();
		for (Currency currency : financeData.getCurrencies())
			newCurrencies.add(new CurrencyModelAdapter(currency));
		currencies.setAll(newCurrencies);
	}

	/**
	 * Imports data into database. If process is shutting down, the change is
	 * ignored.
	 *
	 * @param importer a configured FileImporter instance
	 * @throws VogonImportException in case of import errors (I/O, format,
	 * indexing etc.)
	 * @throws VogonImportLogicalException in case of logical errors (without
	 * meaningful stack trace, just to show an error message)
	 * @throws ApplicationShuttingDownException if application is shutting down
	 * and database requests are ignored
	 */
	public void importData(FileImporter importer) throws ApplicationShuttingDownException, VogonImportException, VogonImportLogicalException {
		financeData.importData(importer);
		if (Platform.isFxApplicationThread())
			reloadData();
		else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					reloadData();
				}
			});
		}
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
		for (AccountInterface accountInterface : accounts) {
			if (!(accountInterface instanceof AccountModelAdapter))
				continue;
			AccountModelAdapter account = (AccountModelAdapter) accountInterface;
			if (!account.getAccount().getIncludeInTotal())
				continue;
			if (account.getAccount().getCurrency() == currency)
				totalBalance += account.getAccount().getRawBalance();
			else if (currency == null)
				totalBalance += Math.round(account.getAccount().getBalance() * financeData.getExchangeRate(account.getAccount().getCurrency(), defaultCurrency.get().getCurrency()) * Constants.rawAmountMultiplier);
		}
		return totalBalance / Constants.rawAmountMultiplier;
	}

	/**
	 * Begins the shutdown process. Blocks access to the database.
	 */
	public void shutdown() {
		financeData.shutdown();
	}

	/**
	 * Returns the FinanceData instance
	 *
	 * @return the FinanceData instance
	 */
	public FinanceData getFinanceData() {
		return financeData;
	}

	/**
	 * Finds an existing TransactionModelAdapter wrapper for a transaction
	 *
	 * @param transaction the transaction to find
	 * @return the wrapping TransactionModelAdapter or null if not found
	 */
	public TransactionModelAdapter findTransactionAdapter(FinanceTransaction transaction) {
		for (TransactionModelAdapter adapter : transactions) {
			if (adapter.getTransaction().equals(transaction))
				return adapter;
		}
		return null;
	}

	/**
	 * Finds an existing AccountModelAdapter wrapper for an account
	 *
	 * @param account the account to find
	 * @return the wrapping AccountModelAdapter or null if not found
	 */
	public AccountModelAdapter findAccountAdapter(FinanceAccount account) {
		for (AccountInterface adapter : accounts) {
			if (adapter instanceof AccountModelAdapter && ((AccountModelAdapter) adapter).getAccount().equals(account))
				return (AccountModelAdapter) adapter;
		}
		return null;
	}

	/**
	 * Finds an existing AccountModelAdapter wrapper for a
	 * FinanceTransaction.Type
	 *
	 * @param type the FinanceTransaction.Type
	 * @return the wrapping TransactionTypeModelAdapter or null if not found
	 */
	public TransactionTypeModelAdapter findTransactionType(FinanceTransaction.Type type) {
		for (TransactionTypeModelAdapter adapter : transactionTypes)
			if (adapter.getType().equals(type))
				return adapter;
		return null;
	}

	/**
	 * Creates a new account and persists it in the database
	 *
	 * @return the new account
	 */
	public AccountModelAdapter createAccount() {
		AccountModelAdapter account = new AccountModelAdapter(financeData.createAccount("", defaultCurrency.get().getCurrency()), this); //NOI18N
		accounts.add(0, account);
		allAccounts.add(0, account);
		return account;
	}

	/**
	 * Creates a new transaction and persists it in the database
	 *
	 * @return the new transaction
	 */
	public TransactionModelAdapter createTransaction() {
		TransactionModelAdapter transaction = new TransactionModelAdapter(financeData.createTransaction("", new String[0], new Date(), FinanceTransaction.Type.EXPENSEINCOME), this); //NOI18N
		transactions.add(0, transaction);
		return transaction;
	}

	/**
	 * Clones a transaction and persists the clone in the database
	 *
	 * @param transaction the transaction to clone
	 * @return the transaction clone
	 */
	public TransactionModelAdapter cloneTransaction(TransactionModelAdapter transaction) {
		TransactionModelAdapter clonedTransaction = new TransactionModelAdapter(financeData.cloneTransaction(transaction.getTransaction()), this); //NOI18N
		transactions.add(0, clonedTransaction);
		refreshAccounts();
		return clonedTransaction;
	}

	/**
	 * Deletes an account from the database
	 *
	 * @param account the account to delete
	 */
	public void deleteAccount(AccountModelAdapter account) {
		financeData.deleteAccount(account.getAccount());
		accounts.remove(account);
		allAccounts.remove(account);
		
		refreshReportingAccounts();

		//Refresh affected transactions from database
		for (TransactionModelAdapter transaction : transactions) {
			if (transaction.getTransaction().getAccounts().contains(account.getAccount())) {
				transaction.updateFromDatabase();
				transaction.updateFxProperties();
			}
		}
	}

	/**
	 * Deletes a transaction from the database
	 *
	 * @param transaction the transaction to delete
	 */
	public void deleteTransaction(TransactionModelAdapter transaction) {
		financeData.deleteTransaction(transaction.getTransaction());
		transactions.remove(transaction);
		refreshAccounts();
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the list of transactions on the current page
	 *
	 * @return the list of transactions on the current page
	 */
	public ObservableList<TransactionModelAdapter> getTransactions() {
		return transactions;
	}

	/**
	 * Returns the list of all accounts, including persisted and reporting
	 *
	 * @return the list of all accounts
	 */
	public ObservableList<AccountInterface> getAllAccounts() {
		return allAccounts;
	}

	/**
	 * Returns the list of persisted accounts
	 *
	 * @return the list of persisted accounts
	 */
	public ObservableList<AccountInterface> getAccounts() {
		return accounts;
	}

	/**
	 * Returns the list of exchange rates
	 *
	 * @return the list of exchange rates
	 */
	public ObservableList<CurrencyRateModelAdapter> getExchangeRates() {
		return exchangeRates;
	}

	/**
	 * Returns the list of currencies
	 *
	 * @return the list of currencies
	 */
	public ObservableList<CurrencyModelAdapter> getCurrencies() {
		return currencies;
	}

	/**
	 * Returns the default currency property
	 *
	 * @return the default currency property
	 */
	public ObjectProperty<CurrencyModelAdapter> defaultCurrencyProperty() {
		return defaultCurrency;
	}

	/**
	 * Returns the list of transaction types
	 *
	 * @return the list of transaction types
	 */
	public ObservableList<TransactionTypeModelAdapter> getTransactionTypes() {
		return transactionTypes;
	}

	/**
	 * Returns the total number of pages
	 *
	 * @param pageSize number of transactions on one page
	 * @return the total number of pages
	 */
	public int getPageCount(int pageSize) {
		return financeData.getTransactionCount() / pageSize + 1;
	}
}
