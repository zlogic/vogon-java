/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.awt.Color;
import java.awt.Component;
import java.text.MessageFormat;
import java.util.Currency;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * Helper class for rendering/editing finance amounts (with currency)
 *
 * @author Dmitry
 */
public class SumTableCell {

	protected double balance;
	protected Currency currency;
	protected boolean isOk;

	public SumTableCell(double balance, boolean isOk, Currency currency) {
		this.balance = balance;
		this.currency = currency;
		this.isOk = isOk;
	}

	public SumTableCell(double balance, boolean isOk) {
		this.balance = balance;
		this.isOk = isOk;
	}

	public SumTableCell(double balance) {
		this.balance = balance;
		this.isOk = true;
	}

	@Override
	public String toString() {
		String formattedSum = MessageFormat.format("{0,number,0.00} {1}", balance, currency != null ? currency.getCurrencyCode() : "###");
		return formattedSum;
	}

	public static TableCellRenderer getRenderer() {
		SumModelRenderer renderer = new SumModelRenderer();
		renderer.setHorizontalAlignment(JLabel.RIGHT);
		return renderer;
	}

	public static TableCellEditor getEditor() {
		JTextField textField = new JTextField();
		textField.setBorder(new LineBorder(Color.black));//Borrowed from JTable
		textField.setHorizontalAlignment(JLabel.RIGHT);
		return new SumModelEditor(textField);
	}

	protected static class SumModelRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value instanceof SumTableCell && !((SumTableCell) value).isOk)
				setBackground(Color.red);
			else
				setBackground(null);
			Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			return component;
		}
	}

	protected static class SumModelEditor extends DefaultCellEditor {

		public SumModelEditor(final JTextField textField) {
			super(textField);
		}

		public SumModelEditor(final JCheckBox checkBox) {
			super(checkBox);
		}

		public SumModelEditor(final JComboBox comboBox) {
			super(comboBox);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			return super.getTableCellEditorComponent(table, ((SumTableCell) value).balance, isSelected, row, column);
		}
	}
}
