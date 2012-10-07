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
 * Transaction components table model class
 *
 * @author Zlogic
 */
public class TransactionComponentsTableModel extends AbstractTableModel implements FinanceData.AccountCreatedEventListener, FinanceData.AccountUpdatedEventListener, FinanceData.AccountDeletedEventListener {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * Finance Data instance
	 */
	protected FinanceData financeData;
	/**
	 * The transaction currently being edited
	 */
	protected FinanceTransaction editingTransaction;
	/**
	 * The locally cached accounts
	 */
	protected List<FinanceAccount> accounts;

	/**
	 * Sets the table data
	 *
	 * @param financeData the FinanceData to be used
	 */
	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;
		fireTableDataChanged();
	}

	/**
	 * Sets the transaction to be edited
	 *
	 * @param transaction the transaction to be edited
	 */
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
		TransactionComponent component = editingTransaction.getComponents().get(rowIndex);
		switch (columnIndex) {
			case 0:
				return new FinanceAccountComboItem(component.getAccount());
			case 1:
				return new SumTableCell(component.getAmount(), true, component.getAccount() != null ? component.getAccount().getCurrency() : null, false, FinanceTransaction.Type.UNDEFINED);
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
				financeData.setTransactionComponentAmount(component, Double.parseDouble((String) aValue));
				break;
		}
		fireTableRowsUpdated(rowIndex, rowIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
			case 0:
				return FinanceAccountComboItem.class;
			case 1:
				return SumTableCell.class;
		}
		return Object.class;
	}

	@Override
	public void accountCreated(FinanceAccount newAccount) {
		accounts = financeData.getAccounts();
	}

	@Override
	public void accountUpdated(FinanceAccount updatedAccount) {
		accounts = financeData.getAccounts();
	}

	@Override
	public void accountsUpdated() {
		accounts = financeData.getAccounts();
	}

	@Override
	public void accountDeleted(FinanceAccount deletedAccount) {
		accounts = financeData.getAccounts();
	}

	/**
	 * Returns a list of account items which can be rendered in a Combo box
	 * (used to specifically detect the selected item)
	 *
	 * @return the list of account items
	 */
	public Object[] getAccountsComboList() {
		List<FinanceAccountComboItem> items = new LinkedList<>();
		if (accounts == null)
			accounts = financeData.getAccounts();
		for (FinanceAccount account : accounts)
			if (account.getIncludeInTotal())
				items.add(new FinanceAccountComboItem(account));
		return items.toArray();
	}

	/**
	 * Adds a transaction component to the model and FinanceData instance
	 *
	 * @return the new component's index
	 */
	public int addCompoment() {
		TransactionComponent component = new TransactionComponent(null, editingTransaction, 0);
		financeData.createTransactionComponent(component);
		int newComponentIndex = editingTransaction.getComponents().indexOf(component);
		fireTableRowsInserted(newComponentIndex, newComponentIndex);
		return newComponentIndex;
	}

	/**
	 * Deletes a transaction component from the model and FinanceData instance
	 *
	 * @param rowIndex the row index of the item being deleted
	 */
	public void deleteComponent(int rowIndex) {
		financeData.deleteTransactionComponent(editingTransaction.getComponents().get(rowIndex));
		fireTableRowsDeleted(rowIndex, rowIndex);
	}

	/**
	 * Class for storing an account with an overloaded toString method for
	 * better customization of how it's rendered in a combo box.
	 */
	protected class FinanceAccountComboItem {

		/**
		 * The account
		 */
		protected FinanceAccount account;

		/**
		 * Default constructor
		 *
		 * @param account the account for this item
		 */
		public FinanceAccountComboItem(FinanceAccount account) {
			this.account = account;
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
			return obj instanceof FinanceAccountComboItem && account == ((FinanceAccountComboItem) obj).account;
		}

		/**
		 * Returns the account
		 *
		 * @return the account
		 */
		public FinanceAccount getAccount() {
			return account;
		}
	}
}
