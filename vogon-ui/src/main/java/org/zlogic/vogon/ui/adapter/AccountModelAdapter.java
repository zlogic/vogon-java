/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import java.util.Currency;
import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javax.persistence.EntityManager;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactedChange;

/**
 * Class for storing an account with an overloaded toString method for better
 * customization of how it's rendered in a combo box and other similar places.
 *
 * @author Dmitry Zolotukhin
 */
public class AccountModelAdapter implements AccountInterface {

	/**
	 * Localization messages
	 */
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The account
	 */
	private FinanceAccount account;
	/**
	 * The DataManager instance
	 */
	protected DataManager dataManager;
	/**
	 * The account name property
	 */
	private final StringProperty name = new SimpleStringProperty();
	/**
	 * The account balance property (formatted string)
	 */
	private final StringProperty balance = new SimpleStringProperty("");
	/**
	 * The currency property
	 */
	private final ObjectProperty<CurrencyModelAdapter> currency = new SimpleObjectProperty<>();
	/**
	 * The property indicating if account should be included in the reporting
	 * account's total balance
	 */
	private final BooleanProperty includeInTotal = new SimpleBooleanProperty(true);
	private ChangeListener<Boolean> includeInTotalListener = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
			if (oldValue.equals(newValue))
				return;
			dataManager.getFinanceData().performTransactedChange(new TransactedChange() {
				private boolean includeInTotal;

				public TransactedChange setIncludeInTotal(boolean includeInTotal) {
					this.includeInTotal = includeInTotal;
					return this;
				}

				@Override
				public void performChange(EntityManager entityManager) {
					setAccount(entityManager.find(FinanceAccount.class, account.getId()));
					getAccount().setIncludeInTotal(includeInTotal);
				}
			}.setIncludeInTotal(newValue));
			updateFxProperties();
			dataManager.refreshAccounts();
		}
	};
	private ChangeListener<String> nameListener = new ChangeListener<String>() {
		@Override
		public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
			if (oldValue.equals(newValue))
				return;
			dataManager.getFinanceData().performTransactedChange(new TransactedChange() {
				private String name;

				public TransactedChange setName(String name) {
					this.name = name;
					return this;
				}

				@Override
				public void performChange(EntityManager entityManager) {
					setAccount(entityManager.find(FinanceAccount.class, account.getId()));
					getAccount().setName(name);
				}
			}.setName(newValue));
			updateFxProperties();
			dataManager.updateTransactionsFxProperties();
		}
	};
	private ChangeListener<CurrencyModelAdapter> currencyListener = new ChangeListener<CurrencyModelAdapter>() {
		@Override
		public void changed(ObservableValue<? extends CurrencyModelAdapter> ov, CurrencyModelAdapter oldValue, CurrencyModelAdapter newValue) {
			if (oldValue.equals(newValue))
				return;
			dataManager.getFinanceData().performTransactedChange(new TransactedChange() {
				private Currency currency;

				public TransactedChange setCurrency(Currency currency) {
					this.currency = currency;
					return this;
				}

				@Override
				public void performChange(EntityManager entityManager) {
					setAccount(entityManager.find(FinanceAccount.class, account.getId()));
					getAccount().setCurrency(currency);
				}
			}.setCurrency(newValue.getCurrency()));
			dataManager.getFinanceData().populateCurrencies();
			updateFxProperties();
			dataManager.reloadCurrencies();
			dataManager.updateTransactionsFxProperties();
		}
	};

	/**
	 * Default constructor
	 *
	 * @param account the account for this item
	 * @param dataManager the DataManager instance
	 */
	public AccountModelAdapter(FinanceAccount account, DataManager dataManager) {
		this.account = account;
		this.dataManager = dataManager;

		((AccountModelAdapter) this).updateFxProperties();
	}

	public void refreshBalance() {
		dataManager.getFinanceData().refreshAccountBalance(account);
		dataManager.getFinanceData().performTransactedChange(new TransactedChange() {
			@Override
			public void performChange(EntityManager entityManager) {
				setAccount(entityManager.find(FinanceAccount.class, account.getId()));
			}
		});
		updateFxProperties();
	}

	@Override
	public String toString() {
		if (account != null)
			return account.getName();
		else
			return messages.getString("INVALID_ACCOUNT");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return this == null;
		if (account == null)
			return obj instanceof AccountModelAdapter && ((AccountModelAdapter) obj).account == null;
		if (!(obj instanceof AccountModelAdapter))
			return false;
		AccountModelAdapter adapter = (AccountModelAdapter) obj;
		return account.getId() == adapter.account.getId();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + Objects.hashCode(this.account.getId());
		return hash;
	}

	/**
	 * Returns the account
	 *
	 * @return the account
	 */
	public FinanceAccount getAccount() {
		return account;
	}

	protected void setAccount(FinanceAccount account) {
		this.account = account;
	}

	@Override
	public StringProperty balanceProperty() {
		return balance;
	}

	@Override
	public StringProperty nameProperty() {
		return name;
	}

	@Override
	public ObjectProperty<CurrencyModelAdapter> currencyProperty() {
		return currency;
	}

	@Override
	public BooleanProperty includeInTotalProperty() {
		return includeInTotal;
	}

	/**
	 * Updates the properties from the current account, causing ChangeListeners
	 * to trigger.
	 */
	protected void updateFxProperties() {
		//Remove property change listeners
		includeInTotal.removeListener(includeInTotalListener);
		name.removeListener(nameListener);
		currency.removeListener(currencyListener);
		if (account != null) {
			balance.set(new AmountModelAdapter(account.getBalance(), true, account.getCurrency(), false, FinanceTransaction.Type.UNDEFINED).toString());
			name.set(account.getName());
			currency.set(new CurrencyModelAdapter(account.getCurrency()));
			includeInTotal.setValue(account.getIncludeInTotal());
		}
		//Restore property change listeners
		includeInTotal.addListener(includeInTotalListener);
		name.addListener(nameListener);
		currency.addListener(currencyListener);
	}
}
