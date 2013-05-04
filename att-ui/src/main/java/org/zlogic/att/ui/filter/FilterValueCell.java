/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui.filter;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.zlogic.att.ui.filter.adapters.FilterAdapter;
import org.zlogic.att.ui.filter.adapters.FilterCustomFieldAdapter;
import org.zlogic.att.ui.filter.adapters.FilterDateAdapter;
import org.zlogic.att.ui.filter.adapters.FilterTaskCompletedAdapter;

/**
 * Cell editor for filter values, allowing to use a custom editor for every
 * filter value
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class FilterValueCell extends TableCell<FilterHolder, Object> {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(FilterValueCell.class.getName());
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/filter/messages");

	/**
	 * Constructs a FilterValueTableCell
	 */
	public FilterValueCell() {
		super();
	}

	/**
	 * Prepares the cell for editing and shows the control
	 */
	@Override
	public void startEdit() {
		Object rowItem = getTableRow().getItem();
		if (rowItem == null) {
			log.warning(messages.getString("STARTEDIT_ROWITEM_IS_NULL_ERROR"));
			return;
		}
		FilterAdapter filterItem = null;
		if (rowItem instanceof FilterHolder)
			filterItem = ((FilterHolder) rowItem).filterProperty().get();
		else
			throw new RuntimeException(MessageFormat.format(messages.getString("UNSUPPORTED_ROW_ITEM_TYPE"), new Object[]{rowItem.getClass().getName()}));
		if (filterItem instanceof FilterDateAdapter) {
			FilterDateAdapter filter = (FilterDateAdapter) filterItem;
			TextField textEditor = new TextField();
			textEditor.setText(filter.getConverter().toString(filter.valueProperty().get()));
			textEditor.setOnKeyPressed(new EventHandler<KeyEvent>() {
				private FilterDateAdapter filter;
				private TextField textEditor;

				public EventHandler<KeyEvent> setProperties(FilterDateAdapter filter, TextField textEditor) {
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
		} else if (filterItem instanceof FilterCustomFieldAdapter) {
			FilterCustomFieldAdapter filter = (FilterCustomFieldAdapter) filterItem;
			if (filter.getAllowedValues() == null) {
				cancelEdit();
				return;
			}
			ComboBox<String> comboEditor = new ComboBox<>(filter.getAllowedValues());
			comboEditor.setValue(filter.valueProperty().get());
			comboEditor.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
					commitEdit(newValue);
				}
			});
			setText(null);
			setGraphic(comboEditor);
		} else if (filterItem instanceof FilterTaskCompletedAdapter) {
		} else
			throw new RuntimeException(MessageFormat.format(messages.getString("UNSUPPORTED_FILTER_TYPE"), new Object[]{filterItem.getClass().getName()}));
		super.startEdit();
	}

	/**
	 * Prepares the cell for editing and shows the control
	 *
	 * @param newValue the new object value
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
		FilterAdapter filterItem = null;

		setAlignment(Pos.CENTER_LEFT);
		if (rowItem instanceof FilterHolder)
			filterItem = ((FilterHolder) rowItem).filterProperty().get();
		if (empty || filterItem == null) {
			setText(null);
			setGraphic(null);
		} else if (filterItem instanceof FilterDateAdapter || filterItem instanceof FilterCustomFieldAdapter) {
			setText(getStringValue(item));
			setGraphic(null);
		} else if (filterItem instanceof FilterTaskCompletedAdapter) {
			FilterTaskCompletedAdapter filterBooleanValue = (FilterTaskCompletedAdapter) filterItem;
			CheckBox editor = new CheckBox();
			setAlignment(Pos.CENTER);
			editor.selectedProperty().bindBidirectional(filterBooleanValue.valueProperty());
			setGraphic(editor);
		} else {
			setText(null);
			setGraphic(null);
		}
	}

	/**
	 * Returns the string value for an value item. Uses getTableRow().getItem()
	 * to obtain the while filter.
	 *
	 * @param item the value which should be converted to string
	 */
	private String getStringValue(Object item) {
		if (item == null)
			return null;
		Object rowItem = getTableRow().getItem();
		FilterAdapter filterItem = null;
		if (rowItem instanceof FilterHolder)
			filterItem = ((FilterHolder) rowItem).filterProperty().get();
		if (filterItem instanceof FilterDateAdapter)
			return ((FilterDateAdapter) filterItem).getConverter().toString(item);
		if (filterItem instanceof FilterCustomFieldAdapter)
			return ((FilterCustomFieldAdapter) filterItem).valueProperty().get().toString();
		return null;
	}
}
