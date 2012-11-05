/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Simple string cell editor
 *
 * @author Dmitry
 */
public class StringCellEditor<BaseType, PropertyType> extends TableCell<BaseType, PropertyType> {

	private TextField textField;
	private StringCellValidator validator;

	public StringCellEditor(StringCellValidator validator) {
		this.validator = validator;
	}

	@Override
	public void startEdit() {
		super.startEdit();

		if (textField == null) {
			createTextField();
		}
		setText(null);
		setGraphic(textField);
		textField.setText(getString());
		textField.selectAll();
	}

	public void commitIfValid(String item) {
		if (validator.isValid(item))
			super.commitEdit(propertyFromString(item));
		else
			cancelEdit();
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		setText(getString());
		setGraphic(null);
	}

	@Override
	public void updateItem(PropertyType item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			if (isEditing()) {
				if (textField != null) {
					textField.setText(getString());
				}
				setText(null);
				setGraphic(textField);
			} else {
				setText(getString());
				setGraphic(null);
			}
		}
	}

	private void createTextField() {
		textField = new TextField(getString());
		textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
		textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ENTER) {
					commitIfValid(textField.getText());
				} else if (t.getCode() == KeyCode.ESCAPE) {
					cancelEdit();
				}
			}
		});
	}

	protected PropertyType propertyFromString(String value) {
		return (PropertyType) value;
	}

	protected String getString() {
		return getItem() == null ? "" : getItem().toString();
	}
}
