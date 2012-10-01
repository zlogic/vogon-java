/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.text.MessageFormat;
import java.util.Currency;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;

/**
 * Transactions table model class
 *
 * @author Dmitry Zolotukhin
 */
public class TransactionsTableModel extends AbstractTableModel implements FinanceData.TransactionCreatedEventListener, FinanceData.TransactionUpdatedEventListener {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	private FinanceData data = null;

	/**
	 * Default constructor for TransactionsTableModel
	 */
	public TransactionsTableModel() {
	}

	/**
	 * Sets the table data
	 *
	 * @param data
	 */
	public void setFinanceData(FinanceData data) {
		this.data = data;
		fireTableDataChanged();
	}

	/**
	 * Translates a model row index to a FinanceData transaction index
	 *
	 * @param rowIndex the model row index
	 * @return the FinanceData transaction index
	 */
	protected int translateRowToFinanceData(int rowIndex) {
		return data.getTransactions().size() - 1 - rowIndex;
	}

	/**
	 * Translates a FinanceData transaction index to a model row index
	 *
	 * @param rowIndex the FinanceData transaction index
	 * @return the model row index
	 */
	protected int translateRowToModel(int rowIndex) {
		return data.getTransactions().size() - 1 - rowIndex;
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public int getRowCount() {
		return data != null ? data.getTransactions().size() : 0;
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
			case 0:
				return messages.getString("TRANSACTION_DESCRIPTION");
			case 1:
				return messages.getString("TRANSACTION_DATE");
			case 2:
				return messages.getString("TRANSACTION_TAGS");
			case 3:
				return messages.getString("TRANSACTION_AMOUNT");
			case 4:
				return messages.getString("TRANSACTION_ACCOUNT");
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		FinanceTransaction transaction = getTransaction(row);
		switch (col) {
			case 0:
				return transaction.getDescription();
			case 1:
				return MessageFormat.format(messages.getString("FORMAT_DATE"), new Object[]{transaction.getDate()});
			case 2:
				return org.zlogic.vogon.data.Utils.join(transaction.getTags(), ","); //NOI18N
			case 3:
				List<Currency> transactionCurrencies = transaction.getCurrencies();
				Currency currency;
				double amount;
				if (transactionCurrencies.size() == 1) {
					amount = transaction.getAmount();
					currency = transactionCurrencies.get(0);
				} else {
					amount = data.getAmountInCurrency(transaction, data.getDefaultCurrency());
					currency = data.getDefaultCurrency();
				}
				return new SumTableCell(amount, transaction.isAmountOk(), currency, transactionCurrencies.size() != 1, transaction.getType());
			case 4:
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
		return null;
	}

	/**
	 * Returns a model index for the specific transaction
	 *
	 * @param transaction the transaction
	 * @return the transaction's index in the model
	 */
	public int getTransactionIndex(FinanceTransaction transaction) {
		int rowIndex = translateRowToModel(data.getTransactions().indexOf(transaction));
		return rowIndex;
	}

	/**
	 * Returns a transaction at a specific index
	 *
	 * @param rowIndex the transaction's model index
	 * @return the transaction
	 */
	public FinanceTransaction getTransaction(int rowIndex) {
		FinanceTransaction transaction = data.getTransactions().get(translateRowToFinanceData(rowIndex));
		return transaction;
	}

	@Override
	public Class getColumnClass(int c) {
		switch (c) {
			case 0:
				return String.class;
			case 1:
				return String.class;
			case 2:
				return String.class;
			case 3:
				return SumTableCell.class;
			case 4:
				return String.class;
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	/**
	 * Deletes a transaction
	 *
	 * @param row the row to be deleted
	 */
	public void deleteTransaction(int row) {
		data.deleteTransaction(data.getTransactions().get(translateRowToFinanceData(row)));
		fireTableRowsDeleted(row, row);
	}

	@Override
	public void transactionCreated(FinanceTransaction newTransaction) {
		int rowIndex = translateRowToModel(data.getTransactions().indexOf(newTransaction));
		fireTableRowsInserted(rowIndex, rowIndex);
	}

	@Override
	public void transactionUpdated(FinanceTransaction updatedTransaction) {
		int rowIndex = translateRowToModel(data.getTransactions().indexOf(updatedTransaction));
		fireTableRowsUpdated(rowIndex, rowIndex);
	}

	@Override
	public void transactionsUpdated() {
		fireTableDataChanged();
	}
}
