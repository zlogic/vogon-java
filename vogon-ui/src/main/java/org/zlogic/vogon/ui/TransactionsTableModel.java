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
	/**
	 * FinanceData instance
	 */
	protected FinanceData financeData = null;
	/**
	 * Page size
	 */
	protected int pageSize = 100;
	/**
	 * Currently selected page
	 */
	protected int currentPage = 0;

	/**
	 * Default constructor for TransactionsTableModel
	 */
	public TransactionsTableModel() {
	}

	/**
	 * Sets the table financeData
	 *
	 * @param financeData the FinanceData to be used
	 */
	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;
		fireTableDataChanged();
	}

	/**
	 * Returns the page for a model row
	 *
	 * @param rowIndex the model row
	 * @return the page number
	 */
	protected int getRowPage(int rowIndex) {
		if (currentPage < getPageCount())
			return rowIndex / pageSize;
		else
			return -1;
	}

	/**
	 * Returns the number of pages
	 *
	 * @return the number of pages
	 */
	public int getPageCount() {
		return financeData.getTransactions().size() / pageSize + 1;
	}

	/**
	 * Returns the generic page size
	 *
	 * @return the page size
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * Returns the page size for a specific page (last page may be smaller)
	 *
	 * @param pageIndex the page number
	 * @return the page size for a specific page
	 */
	public int getPageSize(int pageIndex) {
		if (financeData == null)
			return 0;
		return Math.min(pageSize, financeData.getTransactions().size() - currentPage * pageSize);
	}

	/**
	 * Sets the current page
	 *
	 * @param pageIndex the page to be set
	 * @return the selected page index; if pageIndex is out of range, the
	 * closest valid value
	 */
	public int setCurrentPage(int pageIndex) {
		if (pageIndex < 0)
			pageIndex = 0;
		else if (pageIndex >= getPageCount())
			pageIndex = getPageCount() - 1;
		currentPage = pageIndex;
		fireTableDataChanged();
		return pageIndex;
	}

	/**
	 * Translates a model row index to a FinanceData transaction index
	 *
	 * @param rowIndex the model row index
	 * @return the FinanceData transaction index
	 */
	protected int translateRowToFinanceData(int rowIndex) {
		return financeData.getTransactions().size() - 1 - rowIndex;
	}

	/**
	 * Translates a FinanceData transaction index to a model row index
	 *
	 * @param rowIndex the FinanceData transaction index
	 * @return the model row index
	 */
	protected int translateRowToModel(int rowIndex) {
		return financeData.getTransactions().size() - 1 - rowIndex;
	}

	/**
	 * Returns the absolute index of a paged row
	 *
	 * @param rowIndex the index of a row on the current page
	 * @return the absolute row index
	 */
	protected int translatePagedRowToModelRow(int rowIndex) {
		return rowIndex + currentPage * pageSize;
	}

	/**
	 * Returns the page-related index of an absolute model row index
	 *
	 * @param rowIndex the absolute index of a row in the model
	 * @return the page-related index
	 */
	protected int translateModelRowToPagedRow(int rowIndex) {
		return rowIndex % pageSize;
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public int getRowCount() {
		return financeData != null ? getPageSize(currentPage) : 0;
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
					amount = financeData.getAmountInCurrency(transaction, financeData.getDefaultCurrency());
					currency = financeData.getDefaultCurrency();
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
	 * Returns a page-related index for the specific transaction
	 *
	 * @param transaction the transaction
	 * @return the transaction's index in the model (on the current page)
	 */
	public int getTransactionIndex(FinanceTransaction transaction) {
		int rowIndex = translateRowToModel(financeData.getTransactions().indexOf(transaction));
		rowIndex = translateModelRowToPagedRow(rowIndex);
		return rowIndex;
	}

	/**
	 * Returns a transaction at a specific index
	 *
	 * @param rowIndex the transaction's model index
	 * @return the transaction
	 */
	public FinanceTransaction getTransaction(int rowIndex) {
		rowIndex = translatePagedRowToModelRow(rowIndex);
		FinanceTransaction transaction = financeData.getTransactions().get(translateRowToFinanceData(rowIndex));
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
	 * @param rowIndex the rowIndex to be deleted
	 */
	public void deleteTransaction(int rowIndex) {
		rowIndex = translatePagedRowToModelRow(rowIndex);
		financeData.deleteTransaction(financeData.getTransactions().get(translateRowToFinanceData(rowIndex)));
		fireTableRowsDeleted(rowIndex, rowIndex);
	}

	@Override
	public void transactionCreated(FinanceTransaction newTransaction) {
		int rowIndex = translateRowToModel(financeData.getTransactions().indexOf(newTransaction));
		rowIndex = translateModelRowToPagedRow(rowIndex);
		fireTableRowsInserted(rowIndex, rowIndex);
	}

	@Override
	public void transactionUpdated(FinanceTransaction updatedTransaction) {
		int rowIndex = translateRowToModel(financeData.getTransactions().indexOf(updatedTransaction));
		rowIndex = translateModelRowToPagedRow(rowIndex);
		fireTableRowsUpdated(rowIndex, rowIndex);
	}

	@Override
	public void transactionsUpdated() {
		fireTableDataChanged();
	}
}
