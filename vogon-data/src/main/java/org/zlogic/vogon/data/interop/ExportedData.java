package org.zlogic.vogon.data.interop;

import java.util.Collection;
import org.zlogic.vogon.data.CurrencyRate;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.VogonUser;

/**
 * Class containing exported data
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class ExportedData {

	/**
	 * The user data
	 */
	private VogonUser user;
	/**
	 * The accounts
	 */
	private Collection<FinanceAccount> accounts;
	/**
	 * The transactions
	 */
	private Collection<FinanceTransaction> transactions;
	/**
	 * The currency rates
	 */
	private Collection<CurrencyRate> currencyRates;

	/**
	 * Default constructor
	 */
	ExportedData() {
	}

	/**
	 * Returns the user
	 *
	 * @return the user
	 */
	public VogonUser getUser() {
		return user;
	}

	/**
	 * Sets the user
	 *
	 * @param user the user to set
	 */
	public void setUser(VogonUser user) {
		this.user = user;
	}

	/**
	 * Returns the accounts
	 *
	 * @return the accounts
	 */
	public Collection<FinanceAccount> getAccounts() {
		return accounts;
	}

	/**
	 * Sets the accounts
	 *
	 * @param accounts the accounts to set
	 */
	public void setAccounts(Collection<FinanceAccount> accounts) {
		this.accounts = accounts;
	}

	/**
	 * Returns the transactions
	 *
	 * @return the transactions
	 */
	public Collection<FinanceTransaction> getTransactions() {
		return transactions;
	}

	/**
	 * Sets the transactions
	 *
	 * @param transactions the transactions to set
	 */
	public void setTransactions(Collection<FinanceTransaction> transactions) {
		this.transactions = transactions;
	}

	/**
	 * Returns the currency rates
	 *
	 * @return the currency rates
	 */
	public Collection<CurrencyRate> getCurrencyRates() {
		return currencyRates;
	}

	/**
	 * Sets the currency rates
	 *
	 * @param currencyRates the currency rates to set
	 */
	public void setCurrencyRates(Collection<CurrencyRate> currencyRates) {
		this.currencyRates = currencyRates;
	}

}
