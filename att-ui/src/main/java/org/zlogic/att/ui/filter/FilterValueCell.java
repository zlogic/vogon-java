/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zlogic.att.ui.filter;

import java.text.MessageFormat;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Cell editor for filter values, allowing to use a custom editor for every
 * filter value
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class FilterValueCell extends TableCell<FilterHolder, Object> {

	Object rowItemP;

	public FilterValueCell() {
		super();
	}

	/**
	 * Prepares the cell for editing and shows the control
	 */
	@Override
	public void startEdit() {
		Object rowItem = getTableRow().getItem();
		Filter filterItem = null;
		if (rowItem instanceof FilterHolder)
			filterItem = ((FilterHolder) rowItem).filterProperty().get();
		if (filterItem instanceof FilterTextValue) {
			FilterTextValue filter = (FilterTextValue) filterItem;
			TextField textEditor = new TextField();
			textEditor.setText(filter.getConverter().toString(filter.valueProperty().get()));
			textEditor.setOnKeyPressed(new EventHandler<KeyEvent>() {
				private FilterTextValue filter;
				private TextField textEditor;

				public EventHandler<KeyEvent> setProperties(FilterTextValue filter, TextField textEditor) {
					this.filter = filter;
					this.textEditor = textEditor;
					return this;
				}

				@Override
				public void handle(KeyEvent t) {
					if (t.getCode() == KeyCode.ENTER) {
						commitEdit(filter.getConverter().fromString(textEditor.getText()));
					} else if (t.getCode() == KeyCode.ESCAPE) {
						cancelEdit();
					}
				}
			}.setProperties(filter, textEditor));
			setGraphic(textEditor);
			setText(null);
		} else
			throw new RuntimeException(MessageFormat.format("Unsupported row item type: {0}", new Object[]{filterItem.getClass().getName()}));
		super.startEdit();
	}

	/**
	 * Prepares the cell for editing and shows the control
	 */
	@Override
	public void commitEdit(Object newValue) {
		super.commitEdit(newValue);
		setGraphic(null);
	}

	/**
	 * Prepares the cell for editing and shows the control
	 */
	@Override
	public void cancelEdit() {
		super.cancelEdit();
		setGraphic(null);
	}

	/**
	 * Performs an item update
	 *
	 * @param item the updated TransactionModelAdapter instance
	 * @param empty true if the item is empty
	 */
	@Override
	public void updateItem(Object item, boolean empty) {
		super.updateItem(item, empty);
		Object rowItem = getTableRow().getItem();
		Filter filterItem = null;
		if (rowItem instanceof FilterHolder)
			filterItem = ((FilterHolder) rowItem).filterProperty().get();
		if (empty) {
			setText(null);
			setGraphic(null);
		} else if (filterItem instanceof FilterTextValue) {
			setText(getStringValue(item));
		} else {
			setText(null);
		}
	}

	private String getStringValue(Object item) {
		if (item == null)
			return null;
		Object rowItem = getTableRow().getItem();
		Filter filterItem = null;
		if (rowItem instanceof FilterHolder)
			filterItem = ((FilterHolder) rowItem).filterProperty().get();
		if (filterItem instanceof FilterTextValue)
			return ((FilterTextValue) filterItem).getConverter().toString(item);
		return null;
	}
}
