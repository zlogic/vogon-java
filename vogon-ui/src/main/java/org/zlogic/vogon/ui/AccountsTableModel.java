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
import org.zlogic.vogon.data.FinanceTransaction;

/**
 * Accounts table model class
 *
 * @author Dmitry Zolotukhin
 */
public class AccountsTableModel extends AbstractTableModel implements FinanceData.AccountCreatedEventListener, FinanceData.AccountUpdatedEventListener, FinanceData.AccountDeletedEventListener {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * Finance Data instance
	 */
	protected FinanceData financeData = null;
	/**
	 * List of reporting (virtual) accounts
	 */
	protected List<ReportingAccount> reportingAcconts = null;
	/**
	 * The locally cached accounts
	 */
	protected List<FinanceAccount> accounts;

	/**
	 * Default constructor for AccountsTableModel
	 */
	public AccountsTableModel() {
	}

	/**
	 * Sets the table financeData
	 *
	 * @param financeData the FinanceData to be used
	 */
	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;
		reportingAcconts = new LinkedList<>();

		accounts = financeData.getAccounts();

		updateReportingAccounts();

		fireTableDataChanged();
	}

	/**
	 * Updates the reporting accounts list
	 */
	protected void updateReportingAccounts() {
		reportingAcconts.clear();

		for (Currency currency : financeData.getCurrencies())
			reportingAcconts.add(new ReportingAccount(MessageFormat.format(messages.getString("TOTAL_ACCOUNT"), new Object[]{currency.getCurrencyCode()}), financeData.getTotalBalance(currency), currency));
		if (financeData.getDefaultCurrency() != null)
			reportingAcconts.add(new ReportingAccount(MessageFormat.format(messages.getString("TOTAL_ALL_ACCOUNTS"), new Object[]{financeData.getDefaultCurrency().getCurrencyCode()}), financeData.getTotalBalance(null), financeData.getDefaultCurrency()));
	}

	/**
	 * Class that acts as a virtual account for reporting reasons (e.g.
	 * displaying a total sum)
	 */
	protected class ReportingAccount {

		/**
		 * The account name/description
		 */
		protected String name;
		/**
		 * The account balance
		 */
		protected double amount;
		/**
		 * The account currency
		 */
		protected Currency currency;

		/**
		 * Default constructor
		 *
		 * @param name the account name/description
		 * @param amount the account balance
		 * @param currency the account currency
		 */
		public ReportingAccount(String name, double amount, Currency currency) {
			this.name = name;
			this.amount = amount;
			this.currency = currency;
		}

		/**
		 * Returns the account name
		 *
		 * @return the account name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns the account balance
		 *
		 * @return the account balance
		 */
		public double getAmount() {
			return amount;
		}

		/**
		 * Returns the account currency
		 *
		 * @return the account currency
		 */
		public Currency getCurrency() {
			return currency;
		}

		/**
		 * Formats the string with HTML to distinguish this account's
		 * financeData from regular accounts
		 *
		 * @param text the text to be formatted
		 * @return the HTML-formatted text
		 */
		public String formatString(String text) {
			return MessageFormat.format(messages.getString("TOTAL_REPORTING_ACCOUNT"), new Object[]{text});
		}
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public int getRowCount() {
		return financeData != null ? accounts.size() + reportingAcconts.size() : 0;
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
			case 3:
				return messages.getString("INCLUDE_IN_TOTAL");
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (row < accounts.size()) {
			FinanceAccount account = accounts.get(row);
			switch (col) {
				case 0:
					return account.getName();
				case 1:
					return new SumTableCell(account.getBalance(), true, account.getCurrency(), false, FinanceTransaction.Type.UNDEFINED);
				case 2:
					return new CurrencyComboItem(account.getCurrency());
				case 3:
					return account.getIncludeInTotal();
			}
		} else {
			row -= accounts.size();
			ReportingAccount account = reportingAcconts.get(row);
			switch (col) {
				case 0:
					return account.formatString(account.getName());
				case 1:
					return new SumTableCell(account.getAmount(), true, account.getCurrency(), false, FinanceTransaction.Type.UNDEFINED);
				case 2:
					return new CurrencyComboItem(account.getCurrency());
				case 3:
					return false;
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
			case 3:
				return Boolean.class;
		}
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		FinanceAccount account = accounts.get(rowIndex);
		switch (columnIndex) {
			case 0:
				financeData.setAccountName(account, (String) aValue);
				break;
			case 2:
				financeData.setAccountCurrency(account, ((CurrencyComboItem) aValue).getCurrency());
				break;
			case 3:
				financeData.setAccountIncludeInTotal(account, (Boolean) aValue);
				break;
		}
		accounts = financeData.getAccounts();
		fireTableRowsUpdated(rowIndex, rowIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return rowIndex < accounts.size() && (columnIndex == 0 || columnIndex == 2 || columnIndex == 3);
	}

	/**
	 * Returns a list of currency items which can be rendered in a Combo box
	 * (used to specifically detect the selected item)
	 *
	 * @return the list of currency items
	 */
	public Object[] getCurrenciesComboList() {
		List<CurrencyComboItem> items = new LinkedList<>();
		for (Currency currency : Currency.getAvailableCurrencies())
			items.add(new CurrencyComboItem(currency));

		return items.toArray();
	}

	/**
	 * Adds an account to the model and FinanceData instance
	 *
	 * @return the new account's index
	 */
	public int addAccount() {
		FinanceAccount account = new FinanceAccount("", financeData.getDefaultCurrency()); //NOI18N
		financeData.createAccount(account);
		accounts = financeData.getAccounts();
		int newAccountIndex = accounts.indexOf(account);
		fireTableRowsInserted(newAccountIndex, newAccountIndex);
		return newAccountIndex;
	}

	/**
	 * Deletes an account from the model and FinanceData instance
	 *
	 * @param rowIndex the row index of the item being deleted
	 */
	public void deleteAccount(int rowIndex) {
		if (rowIndex < accounts.size()) {
			financeData.deleteAccount(accounts.get(rowIndex));
			accounts = financeData.getAccounts();
			fireTableRowsDeleted(rowIndex, rowIndex);
		}
	}

	@Override
	public void accountCreated(FinanceAccount newAccount) {
		accounts = financeData.getAccounts();
		int rowIndex = accounts.indexOf(newAccount);
		fireTableRowsInserted(rowIndex, rowIndex);
	}

	@Override
	public void accountUpdated(FinanceAccount updatedAccount) {
		accounts = financeData.getAccounts();
		updateReportingAccounts();
		int rowIndex = accounts.indexOf(updatedAccount);
		fireTableRowsUpdated(rowIndex, rowIndex);
	}

	@Override
	public void accountsUpdated() {
		accounts = financeData.getAccounts();
		updateReportingAccounts();
		fireTableDataChanged();
	}

	@Override
	public void accountDeleted(FinanceAccount deletedAccount) {
		accounts = financeData.getAccounts();
		updateReportingAccounts();
		fireTableDataChanged();
	}
}
