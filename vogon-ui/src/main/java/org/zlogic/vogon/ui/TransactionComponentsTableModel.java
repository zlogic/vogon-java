/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zlogic.vogon.ui;

import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;

/**
 *
 * @author Zlogic
 */
public class TransactionComponentsTableModel extends AbstractTableModel {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	protected FinanceData financeData;
	protected FinanceTransaction editingTransaction;

	/**
	 * Sets the table data
	 *
	 * @param data
	 */
	public void setFinanceData(FinanceData data) {
		this.financeData = data;
		fireTableDataChanged();
	}

	public void setTransaction(FinanceTransaction transaction) {
		editingTransaction = transaction;
		fireTableDataChanged();
	}

	@Override
	public int getRowCount() {
		if (editingTransaction != null)
			return editingTransaction.getComponents().size();
		else
			return 0;
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
			case 0:
				return messages.getString("ACCOUNT_NAME");
			case 1:
				return messages.getString("TRANSACTION_AMOUNT");
		}
		return null;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
			case 0:
				return new FinanceAccountComboItem(editingTransaction.getComponents().get(rowIndex).getAccount());
			case 1:
				return editingTransaction.getComponents().get(rowIndex).getAmount();
		}
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		TransactionComponent component = editingTransaction.getComponents().get(rowIndex);
		switch (columnIndex) {
			case 0:
				financeData.setTransactionComponentAccount(component, ((FinanceAccountComboItem) aValue).getAccount());
				break;
			case 1:
				financeData.setTransactionComponentAmount(component, (double) aValue);
				break;
		}
		fireTableDataChanged();
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
			case 0:
				return FinanceAccountComboItem.class;
			case 1:
				return Double.class;
		}
		return Object.class;
	}

	public Object[] getAccountsComboList() {
		List<FinanceAccountComboItem> items = new LinkedList<>();
		for (FinanceAccount account : financeData.getAccounts()) {
			items.add(new FinanceAccountComboItem(account));
		}
		return items.toArray();
	}

	protected class FinanceAccountComboItem {

		private FinanceAccount account;

		public FinanceAccountComboItem(FinanceAccount account) {
			this.account = account;
		}

		public String toString() {
			if (account != null)
				return account.getName();
			else
				return messages.getString("INVALID_ACCOUNT");
		}

		public boolean equals(Object obj) {
			return obj instanceof FinanceAccountComboItem && account == ((FinanceAccountComboItem) obj).account;
		}

		public FinanceAccount getAccount() {
			return account;
		}
	}

	public void addCompoment() {
		editingTransaction.addComponent(new TransactionComponent(null, editingTransaction, 0));
		fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
	}

	public void deleteComponent(int rowIndex) {
		editingTransaction.removeComponent(editingTransaction.getComponents().get(rowIndex));
		fireTableRowsDeleted(rowIndex, rowIndex);
	}
}
