/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.text.MessageFormat;
import java.util.Currency;
import java.util.List;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;

/**
 * Transactions helper class for rendering.
 *
 * @author Dmitry Zolotukhin
 */
public class ModelTransaction {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	protected FinanceTransaction transaction;
	protected FinanceData financeData;

	public ModelTransaction(FinanceTransaction transaction, FinanceData financeData) {
		this.transaction = transaction;
		this.financeData = financeData;
	}

	public String getDescription() {
		return transaction.getDescription();
	}

	public String getDate() {
		return MessageFormat.format(messages.getString("FORMAT_DATE"), new Object[]{transaction.getDate()});
	}

	public String getTags() {
		return org.zlogic.vogon.data.Utils.join(transaction.getTags(), ","); //NOI18N
	}

	public String getAmount() {
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

		String formattedSum = MessageFormat.format(messages.getString("FORMAT_SUM"),
				transaction.getType() == FinanceTransaction.Type.TRANSFER ? messages.getString("TRANSFER_TRANSACTION_FLAG") : messages.getString("EXPENSE_TRANSACTION_FLAG"),
				amount,
				currency != null ? currency.getCurrencyCode() : messages.getString("INVALID_CURRENCY"),
				transactionCurrencies.size() != 1 ? messages.getString("CURRENCY_CONVERTED") : messages.getString("CURRENCY_NOT_CONVERTED"));
		return formattedSum;
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
}
