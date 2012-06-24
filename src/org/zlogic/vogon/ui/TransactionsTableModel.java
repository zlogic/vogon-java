/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import javax.swing.table.AbstractTableModel;
import org.zlogic.vogon.data.ExpenseTransaction;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;

/**
 * Table model class
 * @author Dmitry Zolotukhin
 */
public class TransactionsTableModel extends AbstractTableModel {

    private FinanceData data = null;

    /**
     * Sets the table data
     * @param data
     */
    public void setFinanceData(FinanceData data){
	this.data= data;
	fireTableDataChanged();
    }
    
    @Override
    public int getColumnCount() {
	return 4;
    }

    @Override
    public int getRowCount() {
	return data != null ? data.getNumTransactions() : 0;
    }

    @Override
    public String getColumnName(int col) {
	switch (col) {
	    case 0:
		return i18nBundle.getString("TRANSACTION_DESCRIPTION");
	    case 1:
		return i18nBundle.getString("TRANSACTION_DATE");
	    case 2:
		return i18nBundle.getString("TRANSACTION_TAGS");
	    case 3:
		return i18nBundle.getString("TRANSACTION_AMOUNT");
	}
	return null;
    }

    @Override
    public Object getValueAt(int row, int col) {
	FinanceTransaction transaction = data.getTransaction(row);
	if (transaction.getClass() == ExpenseTransaction.class) {
	    ExpenseTransaction exTransaction = (ExpenseTransaction) transaction;
	    switch (col) {
		case 0:
		    return exTransaction.getDescription();
		case 1:
		    return exTransaction.getDate().toString();
		case 2:
		    return exTransaction.getTags();
		case 3:
		    return exTransaction.getAmount();
	    }
	}
	return null;
    }

    @Override
    public Class getColumnClass(int c) {
	return getValueAt(0, c).getClass();
    }
    
    private java.util.ResourceBundle i18nBundle= java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/Bundle");
}
