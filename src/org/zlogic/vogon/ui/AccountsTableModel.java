/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

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
	fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
	return 2;
    }

    @Override
    public int getRowCount() {
	return data != null ? data.getNumAccounts() : 0;
    }

    @Override
    public String getColumnName(int col) {
	switch (col) {
	    case 0:
		return i18nBundle.getString("ACCOUNT_NAME");
	    case 1:
		return i18nBundle.getString("ACCOUNT_BALANCE");
	}
	return null;
    }

    @Override
    public Object getValueAt(int row, int col) {
	FinanceAccount account = data.getAccount(row);
	switch (col) {
	    case 0:
		return account.getName();
	    case 1:
		return account.getActualBalance();
	}
	return null;
    }

    @Override
    public Class getColumnClass(int c) {
	return getValueAt(0, c).getClass();
    }
    private java.util.ResourceBundle i18nBundle = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/Bundle");
}
