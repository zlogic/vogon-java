/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;

/**
 * Class for storing an account with an overloaded toString method for better
 * customization of how it's rendered in a combo box.
 *
 * @author Dmitry Zolotukhin
 */
public class AccountModelAdapter {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The account
	 */
	protected FinanceAccount account;
	protected FinanceData financeData;
	private final StringProperty name = new SimpleStringProperty();
	private final StringProperty balance = new SimpleStringProperty();
	private final ObjectProperty<CurrencyModelAdapter> currency = new SimpleObjectProperty();
	private final BooleanProperty includeInTotal = new SimpleBooleanProperty();

	/**
	 * Default constructor
	 *
	 * @param account the account for this item
	 */
	public AccountModelAdapter(final FinanceAccount account, FinanceData financeData) {
		this.account = account;
		this.financeData = financeData;

		updateProperties();

		//Set property change listeners
		includeInTotal.addListener(new ChangeListener<Boolean>() {
			protected FinanceData financeData;
			protected FinanceAccount account;

			public ChangeListener setData(FinanceAccount account, FinanceData financeData) {
				this.account = account;
				this.financeData = financeData;
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
				if (t1.booleanValue() != account.getIncludeInTotal())
					financeData.setAccountIncludeInTotal(account, t1);
			}
		}.setData(account, financeData));

		name.addListener(new ChangeListener<String>() {
			protected FinanceData financeData;
			protected FinanceAccount account;

			public ChangeListener setData(FinanceAccount account, FinanceData financeData) {
				this.account = account;
				this.financeData = financeData;
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends String> ov, String t, String t1) {
				if (!account.getName().equals(t1))
					financeData.setAccountName(account, t1);
			}
		}.setData(account, financeData));

		currency.addListener(new ChangeListener<CurrencyModelAdapter>() {
			protected FinanceData financeData;
			protected FinanceAccount account;

			public ChangeListener setData(FinanceAccount account, FinanceData financeData) {
				this.account = account;
				this.financeData = financeData;
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends CurrencyModelAdapter> ov, CurrencyModelAdapter t, CurrencyModelAdapter t1) {
				financeData.setAccountCurrency(account, t1.getCurrency());
			}
		}.setData(account, financeData));
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
		return obj instanceof AccountModelAdapter && account.equals(((AccountModelAdapter) obj).account);
	}

	@Override
	public int hashCode() {
		return account.hashCode();
	}

	/**
	 * Returns the account
	 *
	 * @return the account
	 */
	public FinanceAccount getAccount() {
		return account;
	}

	public StringProperty balanceProperty() {
		return balance;
	}

	public StringProperty nameProperty() {
		return name;
	}

	public ObjectProperty<CurrencyModelAdapter> currencyProperty() {
		return currency;
	}

	public BooleanProperty includeInTotalProperty() {
		return includeInTotal;
	}

	private void updateProperties() {
		if (account != null) {
			balance.set(new AmountModelAdapter(account.getBalance(), true, account.getCurrency(), false, FinanceTransaction.Type.UNDEFINED).toString());
			name.set(account.getName());
			currency.set(new CurrencyModelAdapter(account.getCurrency()));
			includeInTotal.set(account.getIncludeInTotal());
		}
	}

	public void refresh(FinanceAccount account) {
		this.account = account;
		updateProperties();
	}
}
