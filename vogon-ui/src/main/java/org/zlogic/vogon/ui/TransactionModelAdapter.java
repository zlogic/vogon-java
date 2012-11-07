/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.util.Currency;
import java.util.Date;
import java.util.List;
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

	public TransactionModelAdapter(FinanceTransaction transaction, FinanceData financeData) {
		this.transaction = transaction;
		this.financeData = financeData;
	}

	public String getDescription() {
		return transaction.getDescription();
	}

	public void setDescription(String description) {
		financeData.setTransactionDescription(transaction, description);
	}

	public Date getDate() {
		return transaction.getDate();
	}

	public void setDate(Date date) {
		financeData.setTransactionDate(transaction, date);
	}

	public String getTags() {
		return org.zlogic.vogon.data.Utils.join(transaction.getTags(), ","); //NOI18N
	}

	public void setTags(String tags) {
		financeData.setTransactionTags(transaction, tags.split(","));//NOI18N
	}

	public TransactionModelAdapter getModelTransaction() {
		return this;
	}

	public FinanceTransaction getTransaction() {
		return transaction;
	}

	public AmountAdapter getAmount() {
		List<Currency> transactionCurrencies = transaction.getCurrencies();
		Currency currency;
		double amount;
		if (transactionCurrencies.size() == 1) {
			amount = transaction.getAmount();
			currency = transactionCurrencies.get(0);
		} else {
			amount = financeData.getAmountInCurrency(transaction, financeData.getDefaultCurrency());
			currency = financeData.getDefaultCurrency();
		}
		return new AmountAdapter(amount, transaction.isAmountOk(), currency, transactionCurrencies.size() != 1, transaction.getType());
	}

	public String getAccount() {
		if (transaction.getType() == FinanceTransaction.Type.EXPENSEINCOME) {
			List<FinanceAccount> accounts = transaction.getAccounts();
			StringBuilder builder = new StringBuilder();
			for (FinanceAccount account : accounts)
				builder.append(account != accounts.get(0) ? "," : "").append(account != null ? account.getName() : messages.getString("INVALID_ACCOUNT")); //NOI18N
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
		return obj instanceof TransactionModelAdapter && transaction.equals(((TransactionModelAdapter) obj).transaction);
	}

	@Override
	public int hashCode() {
		return transaction.hashCode();
	}
}
