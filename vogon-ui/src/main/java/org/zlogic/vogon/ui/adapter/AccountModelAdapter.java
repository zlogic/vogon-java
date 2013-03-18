/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;

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
	private final ObjectProperty<ObjectWithStatus<String, Boolean>> name = new SimpleObjectProperty<>();
	/**
	 * The account balance property (formatted string)
	 */
	private final StringProperty balance = new SimpleStringProperty();
	/**
	 * The currency property
	 */
	private final ObjectProperty<ObjectWithStatus<CurrencyModelAdapter, Boolean>> currency = new SimpleObjectProperty<>();
	/**
	 * The property indicating if account should be included in the reporting
	 * account's total balance
	 */
	private final ObjectProperty<ObjectWithStatus<BooleanProperty, Boolean>> includeInTotal = new SimpleObjectProperty<>(new ObjectWithStatus<BooleanProperty, Boolean>(new SimpleBooleanProperty(true), true));

	/**
	 * Default constructor
	 *
	 * @param account the account for this item
	 * @param dataManager the DataManager instance
	 */
	public AccountModelAdapter(FinanceAccount account, DataManager dataManager) {
		this.account = account;
		this.dataManager = dataManager;

		updateFxProperties();

		//Set property change listeners
		includeInTotal.getValue().getValue().addListener(new ChangeListener<Boolean>() {
			protected DataManager dataManager;
			protected FinanceAccount account;

			public ChangeListener<Boolean> setData(FinanceAccount account, DataManager dataManager) {
				this.account = account;
				this.dataManager = dataManager;
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
				//FIXME URGENT
				/*
				 if (t1.booleanValue() != account.getIncludeInTotal())
				 financeData.setAccountIncludeInTotal(account, t1);
				 */
			}
		}.setData(account, dataManager));

		name.addListener(new ChangeListener<ObjectWithStatus<String, Boolean>>() {
			protected DataManager dataManager;
			protected FinanceAccount account;

			public ChangeListener<ObjectWithStatus<String, Boolean>> setData(FinanceAccount account, DataManager dataManager) {
				this.account = account;
				this.dataManager = dataManager;
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends ObjectWithStatus<String, Boolean>> ov, ObjectWithStatus<String, Boolean> t, ObjectWithStatus<String, Boolean> t1) {
				//FIXME URGENT
				/*
				 if (!account.getName().equals(t1.getValue()))
				 financeData.setAccountName(account, t1.getValue());
				 */
			}
		}.setData(account, dataManager));

		currency.addListener(new ChangeListener<ObjectWithStatus<CurrencyModelAdapter, Boolean>>() {
			protected DataManager dataManager;
			protected FinanceAccount account;

			public ChangeListener<ObjectWithStatus<CurrencyModelAdapter, Boolean>> setData(FinanceAccount account, DataManager dataManager) {
				this.account = account;
				this.dataManager = dataManager;
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends ObjectWithStatus<CurrencyModelAdapter, Boolean>> ov, ObjectWithStatus<CurrencyModelAdapter, Boolean> t, ObjectWithStatus<CurrencyModelAdapter, Boolean> t1) {
				//FIXME URGENT
				/*
				 financeData.setAccountCurrency(account, t1.getValue().getCurrency());
				 */
			}
		}.setData(account, dataManager));
	}

	public void refreshBalance() {
		//FIXME URGENT
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
		return account.equals(adapter.account) && balance.get().equals(adapter.balance.get()) && currency.get().equals(adapter.currency.get()) && includeInTotal.get().equals(adapter.includeInTotal.get()) && name.get().equals(adapter.name.get());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + Objects.hashCode(this.account);
		hash = 79 * hash + Objects.hashCode(this.name);
		hash = 79 * hash + Objects.hashCode(this.balance);
		hash = 79 * hash + Objects.hashCode(this.currency);
		hash = 79 * hash + Objects.hashCode(this.includeInTotal);
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

	@Override
	public StringProperty balanceProperty() {
		return balance;
	}

	@Override
	public ObjectProperty<ObjectWithStatus<String, Boolean>> nameProperty() {
		return name;
	}

	@Override
	public ObjectProperty<ObjectWithStatus<CurrencyModelAdapter, Boolean>> currencyProperty() {
		return currency;
	}

	@Override
	public ObjectProperty<ObjectWithStatus<BooleanProperty, Boolean>> includeInTotalProperty() {
		return includeInTotal;
	}

	/**
	 * Updates the properties from the current account, causing ChangeListeners
	 * to trigger.
	 */
	protected void updateFxProperties() {
		if (account != null) {
			balance.set(new AmountModelAdapter(account.getBalance(), true, account.getCurrency(), false, FinanceTransaction.Type.UNDEFINED).toString());
			name.set(new ObjectWithStatus<>(account.getName(), true));
			currency.set(new ObjectWithStatus<>(new CurrencyModelAdapter(account.getCurrency()), true));
			includeInTotal.get().getValue().setValue(account.getIncludeInTotal());
			includeInTotal.set(new ObjectWithStatus<>(includeInTotal.get().getValue(), true));
		}
	}

	protected void setAccount(FinanceAccount account) {
		this.account = account;
	}
}
