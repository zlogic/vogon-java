/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.util.StringConverter;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.ui.cell.CellStatus;

/**
 * Transactions helper class for rendering with property change detection.
 *
 * @author Dmitry Zolotukhin
 */
public class TransactionModelAdapter implements CellStatus {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The associated transaction
	 */
	protected FinanceTransaction transaction;
	/**
	 * The FinanceData instance
	 */
	protected FinanceData financeData;
	/**
	 * The transaction description property
	 */
	private final StringProperty description = new SimpleStringProperty();
	/**
	 * The transaction tags property
	 */
	private final ListProperty<String> tags = new SimpleListProperty<>(FXCollections.observableList(new LinkedList<String>()));
	/**
	 * The transaction date property
	 */
	private final ObjectProperty<Date> date = new SimpleObjectProperty<>();
	/**
	 * The transaction amount property
	 */
	private final ObjectProperty<AmountModelAdapter> amount = new SimpleObjectProperty<>();
	/**
	 * The transaction account(s) property (rendered string)
	 */
	private final StringProperty account = new SimpleStringProperty();

	/**
	 * Default constructor
	 *
	 * @param transaction the associated transaction
	 * @param financeData the associated FinanceData instance
	 */
	public TransactionModelAdapter(FinanceTransaction transaction, FinanceData financeData) {
		this.transaction = transaction;
		this.financeData = financeData;
		updateProperties();

		//Add change listeners
		description.addListener(new ChangeListener<String>() {
			protected FinanceData financeData;
			protected FinanceTransaction transaction;

			public ChangeListener<String> setData(FinanceTransaction transaction, FinanceData financeData) {
				this.transaction = transaction;
				this.financeData = financeData;
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends String> ov, String t, String t1) {
				if (!t1.equals(transaction.getDescription()))
					financeData.setTransactionDescription(transaction, t1);
			}
		}.setData(transaction, financeData));

		tags.addListener(new ListChangeListener<String>() {
			protected FinanceData financeData;
			protected FinanceTransaction transaction;

			public ListChangeListener<String> setData(FinanceTransaction transaction, FinanceData financeData) {
				this.transaction = transaction;
				this.financeData = financeData;
				return this;
			}

			@Override
			public void onChanged(Change<? extends String> change) {
				financeData.setTransactionTags(transaction, change.getList().toArray(new String[0]));
			}
		}.setData(transaction, financeData));

		date.addListener(new ChangeListener<Date>() {
			protected FinanceData financeData;
			protected FinanceTransaction transaction;

			public ChangeListener<Date> setData(FinanceTransaction transaction, FinanceData financeData) {
				this.transaction = transaction;
				this.financeData = financeData;
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends Date> ov, Date t, Date t1) {
				if (!transaction.getDate().equals(t1))
					financeData.setTransactionDate(transaction, t1);
			}
		}.setData(transaction, financeData));
	}

	/**
	 * Returns this class instance (used in JavaFX to use a
	 * TransactionModelAdapter as its own property)
	 *
	 * @return this class instance
	 */
	public TransactionModelAdapter getModelTransaction() {
		return this;
	}

	/**
	 * Returns the associated transaction
	 *
	 * @return the associated transaction
	 */
	public FinanceTransaction getTransaction() {
		return transaction;
	}

	/**
	 * Returns the converter for tags.
	 *
	 * @return the converter for tags
	 */
	public static StringConverter<List<String>> getTagsListConverter() {
		return new StringConverter<List<String>>() {
			@Override
			public String toString(List<String> t) {
				return org.zlogic.vogon.data.Utils.join(t, ","); //NOI18N
			}

			@Override
			public List<String> fromString(String string) {
				return FXCollections.observableList(Arrays.asList(string.split(",")));//NOI18N
			}
		};
	}

	/**
	 * Returns the transaction amount
	 *
	 * @return the transaction amount
	 */
	public AmountModelAdapter getAmount() {
		List<Currency> transactionCurrencies = transaction.getCurrencies();
		Currency currency;
		double amountValue;
		if (transactionCurrencies.size() == 1) {
			amountValue = transaction.getAmount();
			currency = transactionCurrencies.get(0);
		} else {
			amountValue = financeData.getAmountInCurrency(transaction, financeData.getDefaultCurrency());
			currency = financeData.getDefaultCurrency();
		}
		return new AmountModelAdapter(amountValue, transaction.isAmountOk(), currency, transactionCurrencies.size() != 1, transaction.getType());
	}

	/**
	 * Returns the transactions accounts (rendered)
	 *
	 * @return the transactions accounts
	 */
	public String getAccount() {
		//Build the accounts string
		if (transaction.getType() == FinanceTransaction.Type.EXPENSEINCOME) {
			List<FinanceAccount> accountsList = transaction.getAccounts();
			StringBuilder builder = new StringBuilder();
			for (FinanceAccount currentAccount : accountsList)
				builder.append(currentAccount != accountsList.get(0) ? "," : "").append(currentAccount != null ? currentAccount.getName() : messages.getString("INVALID_ACCOUNT")); //NOI18N
			return builder.toString();
		} else if (transaction.getType() == FinanceTransaction.Type.TRANSFER) {
			FinanceAccount[] toAccounts = transaction.getToAccounts();
			FinanceAccount[] fromAccounts = transaction.getFromAccounts();
			StringBuilder builder = new StringBuilder();
			if (fromAccounts.length > 1) {
				builder.append("("); //NOI18N
				for (FinanceAccount currentAccount : fromAccounts)
					builder.append(currentAccount != fromAccounts[0] ? "," : "").append(currentAccount != null ? currentAccount.getName() : messages.getString("INVALID_ACCOUNT")); //NOI18N
				builder.append(")"); //NOI18N
			} else if (fromAccounts.length == 1)
				builder.append(fromAccounts[0].getName());
			builder.append("->"); //NOI18N
			if (toAccounts.length > 1) {
				builder.append("("); //NOI18N
				for (FinanceAccount currentAccount : toAccounts)
					builder.append(currentAccount != toAccounts[0] ? "," : "").append(currentAccount != null ? currentAccount.getName() : messages.getString("INVALID_ACCOUNT")); //NOI18N
				builder.append(")"); //NOI18N
			} else if (toAccounts.length == 1)
				builder.append(toAccounts[0].getName());
			return builder.toString();
		} else
			return ""; //NOI18N
	}

	/**
	 * Returns the transaction description property
	 *
	 * @return the transaction description property
	 */
	public StringProperty descriptionProperty() {
		return description;
	}

	/**
	 * Returns the transaction tags property
	 *
	 * @return the transaction tags property
	 */
	public ListProperty<String> tagsProperty() {
		return tags;
	}

	/**
	 * Returns the transaction date property
	 *
	 * @return the transaction date property
	 */
	public ObjectProperty<Date> dateProperty() {
		return date;
	}

	/**
	 * Returns the transaction amount property
	 *
	 * @return the transaction amount property
	 */
	public ObjectProperty<AmountModelAdapter> amountProperty() {
		return amount;
	}

	/**
	 * Returns the transaction accounts property (rendered to string)
	 *
	 * @return the transaction accounts property
	 */
	public StringProperty accountProperty() {
		return account;
	}

	@Override
	public boolean isOK() {
		return transaction.isAmountOk();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return this == null;
		if (transaction == null)
			return obj instanceof TransactionModelAdapter && ((TransactionModelAdapter) obj).transaction == null;
		if (!(obj instanceof TransactionModelAdapter))
			return false;
		TransactionModelAdapter adapter = (TransactionModelAdapter) obj;
		return transaction.equals(adapter.transaction) && account.get().equals(adapter.account.get()) && amount.get().equals(adapter.amount.get()) && date.get().equals(adapter.date.get()) && description.get().equals(adapter.description.get()) && tags.get().equals(adapter.tags.get());
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 89 * hash + Objects.hashCode(this.transaction);
		hash = 89 * hash + Objects.hashCode(this.description);
		hash = 89 * hash + Objects.hashCode(this.tags);
		hash = 89 * hash + Objects.hashCode(this.date);
		hash = 89 * hash + Objects.hashCode(this.amount);
		hash = 89 * hash + Objects.hashCode(this.account);
		return hash;
	}

	/**
	 * Updates the properties from the current transaction, causing
	 * ChangeListeners to trigger.
	 */
	private void updateProperties() {
		if (transaction != null) {
			description.set(transaction.getDescription());
			date.set(transaction.getDate());
			tags.removeAll(tags.get());
			tags.addAll(transaction.getTags());
			amount.set(getAmount());
			account.set(getAccount());
		}
	}
}
