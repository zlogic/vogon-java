/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

/**
 * Class for storing account data
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Entity
public class FinanceAccount implements Serializable {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/data/messages");
	
	/**
	 * Version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The account ID (only for persistence)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	protected Long id;
	/**
	 * JPA version
	 */
	@Version
	private long version = 0L;
	/**
	 * The account owner
	 */
	@ManyToOne
	@JoinColumn
	protected VogonUser owner;
	/**
	 * The account name
	 */
	protected String name;
	/**
	 * The account balance
	 */
	protected Long balance;
	/**
	 * The account currency
	 */
	protected String currency;
	/**
	 * If this account should be included in the total
	 */
	protected Boolean includeInTotal;
	/**
	 * If this account should be shown in the accounts list
	 */
	protected Boolean showInList;
	
	/**
	 * The account's transaction components
	 */
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn
	protected Set<TransactionComponent> transactionComponents;

	/**
	 * Creates an account
	 */
	protected FinanceAccount() {
		includeInTotal = true;
		showInList = true;
	}

	/**
	 * Creates a new FinanceAccount
	 *
	 * @param owner the account owner
	 * @param name the account name
	 * @param currency the account currency
	 */
	public FinanceAccount(VogonUser owner, String name, Currency currency) {
		//TODO: consider removing
		this();
		this.name = name;
		this.balance = 0L;
		this.currency = (currency != null ? currency : Currency.getInstance(Locale.getDefault())).getCurrencyCode();
		FinanceAccount.this.setOwner(owner);
		this.transactionComponents = new HashSet<>();
	}

	/**
	 * Creates a new FinanceAccount by merging another account's properties
	 *
	 * @param owner the account owner
	 * @param account the account from which to merge properties
	 */
	public FinanceAccount(VogonUser owner, FinanceAccount account) {
		//TODO: consider removing or refactoring
		this();
		balance = 0L;
		transactionComponents = new HashSet<>();
		FinanceAccount.this.setOwner(owner);
		FinanceAccount.this.merge(account, false);
	}

	/**
	 * Merges properties from another FinanceAccount instance to this instance;
	 * only merges properties, and not components
	 *
	 * @param account the account from which to merge properties
	 */
	public void merge(FinanceAccount account) {
		merge(account, true);
	}
	
	/**
	 * Merges properties from another FinanceAccount instance to this instance
	 *
	 * @param account the account from which to merge properties
	 * @param verifyVersion true if a version mismatch would throw an exception
	 */
	public void merge(FinanceAccount account, boolean verifyVersion) {
		if (verifyVersion && version != account.version)
			throw new ConcurrentModificationException(messages.getString("ACCOUNT_WAS_ALREADY_UPDATED"));
		includeInTotal = account.includeInTotal != null ? account.includeInTotal : includeInTotal;
		showInList = account.showInList != null ? account.showInList : showInList;
		name = account.name;
		currency = (account.currency != null ? account.currency : Currency.getInstance(Locale.getDefault()).getCurrencyCode());
	}

	/**
	 * Adds a TransactionComponent to this account, updating the balance if it's
	 * a new component
	 *
	 * @param component component to add
	 */
	void addComponent(TransactionComponent component) {
		if (transactionComponents.add(component)) {
			balance += component.getRawAmount();
			component.setAccount(this);
		}
		//Reset balance if no components are remaining
		if (transactionComponents.isEmpty())
			balance = 0L;
	}

	/**
	 * Removes a TransactionComponent fromm this account, updating the balance
	 * if it's was assigned to this account
	 *
	 * @param component component to remove
	 */
	void removeComponent(TransactionComponent component) {
		if (transactionComponents.remove(component)) {
			balance -= component.getRawAmount();
			component.setAccount(null);
		}
		//Reset balance if no components are remaining
		if (transactionComponents.isEmpty())
			balance = 0L;
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns all associated TransactionComponent instances
	 * @return all associated TransactionComponent instances
	 */
	public Set<TransactionComponent> getComponents(){
		return transactionComponents != null ? Collections.unmodifiableSet(transactionComponents) : Collections.emptySet();
	}
	/**
	 * Returns the account name
	 *
	 * @return the account name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the account name
	 *
	 * @param name the account name
	 */
	public void setName(String name) {
		if (name.isEmpty())
			return;
		this.name = name;
	}

	/**
	 * Returns the account currency
	 *
	 * @return the account currency
	 */
	public Currency getCurrency() {
		Currency currencyInstance = Currency.getInstance(this.currency);
		return currencyInstance != null ? currencyInstance : Currency.getInstance(Locale.getDefault());
	}

	/**
	 * Sets the account currency
	 *
	 * @param currency the account currency
	 */
	public void setCurrency(Currency currency) {
		if (currency == null)
			return;
		this.currency = currency.getCurrencyCode();
	}

	/**
	 * Returns if this account should be included in the total for all accounts
	 *
	 * @return true if this account should be included in the total for all
	 * accounts
	 */
	public boolean getIncludeInTotal() {
		return includeInTotal == null ? true : includeInTotal;
	}

	/**
	 * Sets if this account should be included in the total for all accounts
	 *
	 * @param includeInTotal true if this account should be included in the
	 * total for all accounts
	 */
	public void setIncludeInTotal(boolean includeInTotal) {
		this.includeInTotal = includeInTotal;
	}

	/**
	 * Returns if this account should be shown in the account list
	 *
	 * @return true if this account should be shown in account list
	 */
	public boolean getShowInList() {
		return showInList == null ? true : showInList;
	}

	/**
	 * Sets if this account should be shown in the account list
	 *
	 * @param showInList true if this account should be shown in the account
	 * list
	 */
	public void setShowInList(boolean showInList) {
		this.showInList = showInList;
	}

	/**
	 * Returns the transaction owner
	 *
	 * @return the transaction owner
	 */
	public VogonUser getOwner() {
		return owner;
	}

	/**
	 * Sets the transaction owner
	 *
	 * @param owner the owner to set
	 */
	public void setOwner(VogonUser owner) {
		this.owner = owner;
	}

	/**
	 * Returns the raw balance (should be divided by
	 * Constants.rawAmountMultiplier to get the real amount)
	 *
	 * @return the raw balance
	 */
	public long getRawBalance() {
		return balance;
	}
	
	/**
	 * Returns the balance as double
	 *
	 * @return the balance
	 */
	public double getBalance() {
		return balance / Constants.RAW_AMOUNT_MULTIPLIER;
	}

	/**
	 * Returns the ID for this class instance
	 *
	 * @return the ID for this class instance
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Returns the version for this class instance
	 *
	 * @return the version for this class instance
	 */
	public long getVersion() {
		return version;
	}

	/**
	 * Sets the version of this class instance
	 *
	 * @param version the version of this class instance
	 */
	protected void setVersion(long version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FinanceAccount)
			return id != null ? id.equals(((FinanceAccount) obj).id) : false;
		else
			return this == obj;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + (int) (this.id ^ (this.id >>> 32));
		return hash;
	}
}
