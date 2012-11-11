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
import javafx.collections.ListChangeListener.Change;
import javafx.util.StringConverter;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.ui.cell.CellStatus;

/**
 * Transactions helper class for rendering.
 *
 * @author Dmitry Zolotukhin
 */
public class TransactionModelAdapter implements CellStatus {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	protected FinanceTransaction transaction;
	protected FinanceData financeData;
	private final StringProperty description = new SimpleStringProperty();
	private final ListProperty<String> tags = new SimpleListProperty(FXCollections.observableList(new LinkedList<String>()));
	private final ObjectProperty<Date> date = new SimpleObjectProperty();
	private final ObjectProperty<AmountModelAdapter> amount = new SimpleObjectProperty();
	private final StringProperty account = new SimpleStringProperty();

	public TransactionModelAdapter(FinanceTransaction transaction, FinanceData financeData) {
		this.transaction = transaction;
		this.financeData = financeData;
		updateProperties();

		description.addListener(new ChangeListener<String>() {
			protected FinanceData financeData;
			protected FinanceTransaction transaction;

			public ChangeListener setData(FinanceTransaction transaction, FinanceData financeData) {
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

			public ListChangeListener setData(FinanceTransaction transaction, FinanceData financeData) {
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

			public ChangeListener setData(FinanceTransaction transaction, FinanceData financeData) {
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

	public TransactionModelAdapter getModelTransaction() {
		return this;
	}

	public FinanceTransaction getTransaction() {
		return transaction;
	}

	public static StringConverter getTagsListConverter() {
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

	public String getAccount() {
		if (transaction.getType() == FinanceTransaction.Type.EXPENSEINCOME) {
			List<FinanceAccount> accountsList = transaction.getAccounts();
			StringBuilder builder = new StringBuilder();
			for (FinanceAccount account : accountsList)
				builder.append(account != accountsList.get(0) ? "," : "").append(account != null ? account.getName() : messages.getString("INVALID_ACCOUNT")); //NOI18N
			return builder.toString();
		} else if (transaction.getType() == FinanceTransaction.Type.TRANSFER) {
			FinanceAccount[] toAccounts = transaction.getToAccounts();
			FinanceAccount[] fromAccounts = transaction.getFromAccounts();
			StringBuilder builder = new StringBuilder();
			if (fromAccounts.length > 1) {
				builder.append("("); //NOI18N
				for (FinanceAccount account : fromAccounts)
					builder.append(account != fromAccounts[0] ? "," : "").append(account != null ? account.getName() : messages.getString("INVALID_ACCOUNT")); //NOI18N
				builder.append(")"); //NOI18N
			} else if (fromAccounts.length == 1)
				builder.append(fromAccounts[0].getName());
			builder.append("->"); //NOI18N
			if (toAccounts.length > 1) {
				builder.append("("); //NOI18N
				for (FinanceAccount account : toAccounts)
					builder.append(account != toAccounts[0] ? "," : "").append(account != null ? account.getName() : messages.getString("INVALID_ACCOUNT")); //NOI18N
				builder.append(")"); //NOI18N
			} else if (toAccounts.length == 1)
				builder.append(toAccounts[0].getName());
			return builder.toString();
		} else
			return ""; //NOI18N
	}

	public StringProperty descriptionProperty() {
		return description;
	}

	public ListProperty<String> tagsProperty() {
		return tags;
	}

	public ObjectProperty<Date> dateProperty() {
		return date;
	}

	public ObjectProperty<AmountModelAdapter> amountProperty() {
		return amount;
	}

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
		if (obj instanceof FinanceTransaction)
			return transaction.equals((FinanceTransaction) obj);
		return obj instanceof TransactionModelAdapter && transaction.equals(((TransactionModelAdapter) obj).transaction);
	}

	@Override
	public int hashCode() {
		return transaction.hashCode();
	}

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
