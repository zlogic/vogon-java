/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.text.MessageFormat;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;

/**
 * Accounts table model class
 *
 * @author Dmitry Zolotukhin
 */
public class AccountsTableModel extends AbstractTableModel implements FinanceData.AccountCreatedEventListener, FinanceData.AccountUpdatedEventListener, FinanceData.AccountDeletedEventListener {

	private FinanceData data = null;
	private List<ReportingAccount> reportingAcconts = null;

	/**
	 * Default constructor for AccountsTableModel
	 */
	public AccountsTableModel() {
	}

	/**
	 * Sets the table data
	 *
	 * @param data
	 */
	public void setFinanceData(FinanceData data) {
		this.data = data;
		reportingAcconts = new LinkedList<>();

		for (Currency currency : data.getCurrencies())
			reportingAcconts.add(new ReportingAccount(MessageFormat.format("Total {0}", new Object[]{currency.getCurrencyCode()}), data.getTotalBalance(currency), currency));
		if (data.getDefaultCurrency() != null)
			reportingAcconts.add(new ReportingAccount(MessageFormat.format("Total for all accounts in {0}", new Object[]{data.getDefaultCurrency().getCurrencyCode()}), data.getTotalBalance(null), data.getDefaultCurrency()));
		fireTableDataChanged();
	}

	protected class ReportingAccount {

		protected String name;
		protected double amount;
		protected Currency currency;

		public ReportingAccount(String name, double amount, Currency currency) {
			this.name = name;
			this.amount = amount;
			this.currency = currency;
		}

		public String getName() {
			return name;
		}

		public double getAmount() {
			return amount;
		}

		public Currency getCurrency() {
			return currency;
		}

		public String formatString(String text) {
			return MessageFormat.format("<html><b>{0}</b></html>", new Object[]{text});
		}
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return data != null ? data.getAccounts().size() + reportingAcconts.size() : 0;
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
			case 0:
				return messages.getString("ACCOUNT_NAME");
			case 1:
				return messages.getString("ACCOUNT_BALANCE");
			case 2:
				return messages.getString("ACCOUNT_CURRENCY");
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (row < data.getAccounts().size()) {
			FinanceAccount account = data.getAccounts().get(row);
			switch (col) {
				case 0:
					return account.getName();
				case 1:
					return new SumTableCell(account.getBalance(), account.getCurrency());
				case 2:
					return new CurrencyComboItem(account.getCurrency());
			}
		} else {
			row -= data.getAccounts().size();
			ReportingAccount account = reportingAcconts.get(row);
			switch (col) {
				case 0:
					return account.formatString(account.getName());
				case 1:
					return new SumTableCell(account.getAmount(), account.getCurrency());
				case 2:
					return new CurrencyComboItem(account.getCurrency());
			}
		}
		return null;
	}

	@Override
	public Class getColumnClass(int c) {
		switch (c) {
			case 0:
				return String.class;
			case 1:
				return SumTableCell.class;
			case 2:
				return CurrencyComboItem.class;
		}
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		FinanceAccount account = data.getAccounts().get(rowIndex);
		switch (columnIndex) {
			case 0:
				data.setAccountName(account, (String) aValue);
				break;
			case 2:
				data.setAccountCurrency(account, ((CurrencyComboItem) aValue).getCurrency());
				break;
		}
		fireTableRowsUpdated(rowIndex, rowIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return rowIndex < data.getAccounts().size() && (columnIndex == 0 || columnIndex == 2);
	}
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");

	public Object[] getCurrenciesComboList() {
		List<CurrencyComboItem> items = new LinkedList<>();
		for (Currency currency : Currency.getAvailableCurrencies())
			items.add(new CurrencyComboItem(currency));

		return items.toArray();
	}

	public int addAccount() {
		data.createAccount(new FinanceAccount("", data.getDefaultCurrency()));
		fireTableRowsInserted(data.getAccounts().size() - 1, data.getAccounts().size() - 1);
		return data.getAccounts().size() - 1;
	}

	public void deleteAccount(int rowIndex) {
		if (rowIndex < data.getAccounts().size()) {
			data.deleteAccount(data.getAccounts().get(rowIndex));
			fireTableRowsDeleted(rowIndex, rowIndex);
		}
	}

	@Override
	public void accountCreated(FinanceAccount newAccount) {
		int rowIndex = data.getAccounts().indexOf(newAccount);
		fireTableRowsInserted(rowIndex, rowIndex);
	}

	@Override
	public void accountUpdated(FinanceAccount updatedAccount) {
		int rowIndex = data.getAccounts().indexOf(updatedAccount);
		fireTableRowsUpdated(rowIndex, rowIndex);
	}

	@Override
	public void accountsUpdated() {
		fireTableDataChanged();
	}

	@Override
	public void accountDeleted(FinanceAccount deletedAccount) {
		fireTableDataChanged();
	}

	protected class CurrencyComboItem {

		private Currency currency;

		public CurrencyComboItem(Currency currency) {
			this.currency = currency;
		}

		@Override
		public String toString() {
			if (currency != null)
				return currency.getDisplayName();
			else
				return "";
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof CurrencyComboItem && currency == ((CurrencyComboItem) obj).currency;
		}

		public Currency getCurrency() {
			return currency;
		}
	}
}
