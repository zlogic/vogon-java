/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;

/**
 * Class for storing an account with an overloaded toString method for better
 * customization of how it's rendered in a combo box.
 *
 * @author Dmitry Zolotukhin
 */
public class FinanceAccountModelAdapter {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The account
	 */
	protected FinanceAccount account;
	private final StringProperty name = new SimpleStringProperty();
	private final StringProperty balance = new SimpleStringProperty();
	private final StringProperty currency = new SimpleStringProperty();
	private final BooleanProperty includeInTotal = new SimpleBooleanProperty();

	/**
	 * Default constructor
	 *
	 * @param account the account for this item
	 */
	public FinanceAccountModelAdapter(FinanceAccount account) {
		this.account = account;
		updateProperties();
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
			return obj instanceof FinanceAccountModelAdapter && ((FinanceAccountModelAdapter) obj).account == null;
		return obj instanceof FinanceAccountModelAdapter && account.equals(((FinanceAccountModelAdapter) obj).account);
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

	public String getName() {
		return account.getName();
	}

	public String getCurrency() {
		return account.getCurrency().getDisplayName();
	}

	public boolean getIncludeInTotal() {
		return account.getIncludeInTotal();
	}

	public String getBalance() {
		return new AmountAdapter(account.getBalance(), true, account.getCurrency(), false, FinanceTransaction.Type.UNDEFINED).toString();
	}

	public StringProperty balanceProperty() {
		return balance;
	}

	public StringProperty nameProperty() {
		return name;
	}

	public StringProperty currencyProperty() {
		return currency;
	}

	public BooleanProperty includeInTotalProperty() {
		return includeInTotal;
	}

	private void updateProperties() {
		if (account != null) {
			balance.set(getBalance());
			name.set(getName());
			currency.set(getCurrency());
			includeInTotal.set(getIncludeInTotal());
		}
	}

	public void refresh(FinanceAccount account) {
		this.account = account;
		updateProperties();
	}
}
