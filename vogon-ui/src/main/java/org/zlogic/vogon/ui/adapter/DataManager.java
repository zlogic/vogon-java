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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.zlogic.vogon.data.ApplicationShuttingDownException;
import org.zlogic.vogon.data.Constants;
import org.zlogic.vogon.data.CurrencyRate;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
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
	private ObservableList<TransactionModelAdapter> transactions = FXCollections.observableList(new LinkedList<TransactionModelAdapter>());
	private ObservableList<AccountInterface> allAccounts = FXCollections.observableList(new LinkedList<AccountInterface>());
	private ObservableList<AccountModelAdapter> accounts = FXCollections.observableList(new LinkedList<AccountModelAdapter>());
	private ObservableList<CurrencyModelAdapter> currencies = FXCollections.observableList(new LinkedList<CurrencyModelAdapter>());
	private ObservableList<CurrencyRateModelAdapter> exchangeRates = FXCollections.observableList(new LinkedList<CurrencyRateModelAdapter>());
	/**
	 * Preferred currency
	 */
	private ObjectProperty<CurrencyModelAdapter> defaultCurrency = new SimpleObjectProperty<>();
	/**
	 * Contains exchange rates
	 */
	private IntegerProperty firstTransactionIndex = new SimpleIntegerProperty(0);
	private IntegerProperty lastTransactionIndex = new SimpleIntegerProperty(0);

	public DataManager() {
		reloadData();
	}

	/**
	 * Updates the data table from database
	 */
	private void reloadData() {
		refreshAccounts();
		reloadCurrencies();

		transactions.clear();

		//Update transactions
		for (FinanceTransaction transaction : financeData.getTransactions(firstTransactionIndex.get(), lastTransactionIndex.get()))
			transactions.add(new TransactionModelAdapter(transaction, this));

		//Update other attributes
		defaultCurrency.setValue(new CurrencyModelAdapter(financeData.getDefaultCurrency()));
	}

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

	public void refreshAccounts() {
		//Remove reporting accounts
		List<AccountInterface> reportingAccounts = new LinkedList<>();
		for (AccountInterface account : allAccounts)
			if (account instanceof ReportingAccount)
				reportingAccounts.add(account);
		allAccounts.removeAll(reportingAccounts);

		//Update accounts
		List<AccountModelAdapter> orphanedAccounts = new LinkedList<>(accounts);
		for (FinanceAccount account : financeData.getAccounts()) {
			AccountModelAdapter existingAccount = findAccountAdapter(account);
			if (existingAccount == null) {
				accounts.add(new AccountModelAdapter(account, this));
				allAccounts.add(new AccountModelAdapter(account, this));
			} else {
				existingAccount.setAccount(account);
				existingAccount.updateFxProperties();
				orphanedAccounts.remove(existingAccount);
			}
		}

		accounts.removeAll(orphanedAccounts);
		allAccounts.removeAll(orphanedAccounts);

		//Recreate reporting accounts
		for (Currency currency : financeData.getCurrencies())
			allAccounts.add(new ReportingAccount(MessageFormat.format(messages.getString("TOTAL_ACCOUNT"), new Object[]{currency.getCurrencyCode()}), getTotalBalance(currency), currency));
		if (financeData.getDefaultCurrency() != null)
			allAccounts.add(new ReportingAccount(MessageFormat.format(messages.getString("TOTAL_ALL_ACCOUNTS"), new Object[]{financeData.getDefaultCurrency().getCurrencyCode()}), getTotalBalance(null), financeData.getDefaultCurrency()));
	}

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

	public void importData(FileImporter importer) throws ApplicationShuttingDownException, VogonImportException, VogonImportLogicalException {
		financeData.importData(importer);
		reloadData();
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
				totalBalance += Math.round(account.getAccount().getBalance() * financeData.getExchangeRate(account.getAccount().getCurrency(), financeData.getDefaultCurrency()) * Constants.rawAmountMultiplier);
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

	public TransactionModelAdapter findTransactionAdapter(TransactionModelAdapter transaction) {
		for (TransactionModelAdapter adapter : transactions) {
			if (adapter.getTransaction().equals(transaction.getTransaction()))
				return adapter;
		}
		return null;
	}

	public AccountModelAdapter findAccountAdapter(FinanceAccount account) {
		for (AccountInterface adapter : accounts) {
			if (adapter instanceof AccountModelAdapter && ((AccountModelAdapter) adapter).getAccount().equals(account))
				return (AccountModelAdapter) adapter;
		}
		return null;
	}

	public AccountModelAdapter createAccount() {
		AccountModelAdapter account = new AccountModelAdapter(financeData.createAccount("", defaultCurrency.get().getCurrency()), this); //NOI18N
		accounts.add(0, account);
		allAccounts.add(0, account);
		return account;
	}

	public TransactionModelAdapter createTransaction() {
		TransactionModelAdapter transaction = new TransactionModelAdapter(financeData.createTransaction("", new String[0], new Date(), FinanceTransaction.Type.EXPENSEINCOME), this); //NOI18N
		transactions.add(0, transaction);
		return transaction;
	}

	public TransactionModelAdapter cloneTransaction(TransactionModelAdapter transaction) {
		TransactionModelAdapter clonedTransaction = new TransactionModelAdapter(financeData.cloneTransaction(transaction.getTransaction()), this); //NOI18N
		transactions.add(0, clonedTransaction);
		return clonedTransaction;
	}

	public void deleteAccount(AccountModelAdapter account) {
		financeData.deleteAccount(account.getAccount());
		accounts.remove(account);
		allAccounts.remove(account);
		//FIXME URGENT: refresh affected transactions from database
		//FIXME URGENT: update reporting accounts
	}

	public void deleteTransaction(TransactionModelAdapter transaction) {
		financeData.deleteTransaction(transaction.getTransaction());
		transactions.remove(transaction);
		refreshAccounts();
	}

	/*
	 * Getters/setters
	 */
	public ObservableList<TransactionModelAdapter> getTransactions() {
		return transactions;
	}

	public ObservableList<AccountInterface> getAllAccounts() {
		return allAccounts;
	}

	public ObservableList<AccountModelAdapter> getAccounts() {
		return accounts;
	}

	public ObservableList<CurrencyRateModelAdapter> getExchangeRates() {
		return exchangeRates;
	}

	public ObservableList<CurrencyModelAdapter> getCurrencies() {
		return currencies;
	}

	public ObjectProperty<CurrencyModelAdapter> getDefaultCurrency() {
		return defaultCurrency;
	}

	public int getPageCount(int pageSize) {
		return financeData.getTransactionCount() / pageSize + 1;
	}
}
