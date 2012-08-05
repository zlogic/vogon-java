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
public class AccountsTableModel extends AbstractTableModel {

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
					return MessageFormat.format("{0,number,0.00}", new Object[]{account.getBalance()});
				case 2:
					return account.getCurrency().getDisplayName();
			}
		} else {
			row -= data.getAccounts().size();
			ReportingAccount account = reportingAcconts.get(row);
			switch (col) {
				case 0:
					return account.formatString(account.getName());
				case 1:
					return account.formatString(MessageFormat.format("{0,number,0.00}", new Object[]{account.getAmount()}));
				case 2:
					return account.formatString(account.getCurrency().getDisplayName());
			}
		}
		return null;
	}

	@Override
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 0 || columnIndex == 2;
	}
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
}
