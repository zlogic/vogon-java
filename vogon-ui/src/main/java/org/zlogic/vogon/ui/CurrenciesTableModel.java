/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;
import org.zlogic.vogon.data.CurrencyRate;
import org.zlogic.vogon.data.FinanceData;

/**
 * Currency table model class
 *
 * @author Zlogic
 */
public class CurrenciesTableModel extends AbstractTableModel implements FinanceData.CurrencyUpdatedEventListener {

	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	protected FinanceData financeData;

	/**
	 * Sets the table data
	 *
	 * @param data
	 */
	public void setFinanceData(FinanceData data) {
		this.financeData = data;
		fireTableDataChanged();
	}

	@Override
	public int getRowCount() {
		return financeData != null ? financeData.getCurrencyRates().size() : 0;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
			case 0:
				return messages.getString("CURRENCY_SOURCE");
			case 1:
				return messages.getString("CURRENCY_DESTINATION");
			case 2:
				return messages.getString("CURRENCY_EXCHANGE_RATE");
		}
		return null;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
			case 0:
			case 1:
				return String.class;
			case 2:
				return Double.class;
		}
		return Object.class;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		CurrencyRate rate = financeData.getCurrencyRates().get(rowIndex);
		switch (columnIndex) {
			case 0:
				return rate.getSource().getDisplayName();
			case 1:
				return rate.getDestination().getDisplayName();
			case 2:
				return rate.getExchangeRate();
		}
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 2) {
			CurrencyRate rate = financeData.getCurrencyRates().get(rowIndex);
			financeData.setExchangeRate(rate, (Double) aValue);
			fireTableRowsUpdated(rowIndex, rowIndex);
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 2;
	}

	public List<CurrencyComboItem> getCurrenciesComboList() {
		List<CurrencyComboItem> items = new LinkedList<>();
		for (Currency currency : financeData.getCurrencies())
			items.add(new CurrencyComboItem(currency));
		return items;
	}

	public CurrencyComboItem getDefaultCurrency() {
		return new CurrencyComboItem(financeData.getDefaultCurrency());
	}

	@Override
	public void currenciesUpdated() {
		fireTableDataChanged();
	}

	protected class CurrencyComboItem {

		private Currency currency;

		public CurrencyComboItem(Currency currency) {
			this.currency = currency;
		}

		public String toString() {
			if (currency != null)
				return currency.getDisplayName();
			else
				return messages.getString("INVALID_CURRENCY");
		}

		public boolean equals(Object obj) {
			if (obj instanceof CurrencyComboItem)
				return currency == ((CurrencyComboItem) obj).currency;
			if (obj instanceof Currency)
				return currency == (Currency) obj;
			return false;
		}

		public Currency getCurrency() {
			return currency;
		}
	}
}
