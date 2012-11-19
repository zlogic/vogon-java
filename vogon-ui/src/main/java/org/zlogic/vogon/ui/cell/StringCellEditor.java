/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Simple string cell editor
 *
 * @param <BaseType> the row type
 * @param <PropertyType> the cell type
 * @author Dmitry Zolotukhin
 */
public class StringCellEditor<BaseType, PropertyType> extends TableCell<BaseType, PropertyType> {

	private Class<PropertyType> classPropertyType;
	
	/**
	 * The editor component
	 */
	protected TextField textField;
	/**
	 * The cell value validator
	 */
	protected StringCellValidator validator;

	/**
	 * Constructs a StringCellEditor with a validator
	 *
	 * @param validator the value validator
	 */
	public StringCellEditor(StringCellValidator validator,Class<PropertyType> classPropertyType) {
		this.validator = validator;
		this.classPropertyType = classPropertyType;
	}

	/**
	 * Prepares the cell for editing
	 */
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

	/**
	 * Performs a commit only if the item is valid
	 *
	 * @param item the item to be committed
	 */
	public void commitIfValid(String item) {
		if (validator.isValid(item))
			super.commitEdit(propertyFromString(item));
		else
			cancelEdit();
	}

	/**
	 * Cancels cell editing
	 */
	@Override
	public void cancelEdit() {
		super.cancelEdit();
		setText(getString());
		setGraphic(null);
	}

	/**
	 * Performs an item update
	 *
	 * @param item the updated cell type class instance
	 * @param empty true if the item is empty
	 */
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

	/**
	 * Creates the editor
	 */
	private void createTextField() {
		textField = new TextField(getString());
		textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
		textField.setAlignment(getAlignment());
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

	/**
	 * Returns the cell item from the string value. Should be overridden if a
	 * complex parser should be used.
	 *
	 * @param value the value to be parsed
	 * @return value, converted to the cell class type
	 */
	protected PropertyType propertyFromString(String value) {
		if(classPropertyType.isAssignableFrom(String.class))
			try {
				return classPropertyType.getConstructor(String.class).newInstance(value);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException ex) {
				Logger.getLogger(StringCellEditor.class.getName()).log(Level.SEVERE, null, ex);
			}
		return null;
	}

	/**
	 * Returns the string value of the edited property (before editing)
	 *
	 * @return the string value of the edited property
	 */
	protected String getString() {
		return getItem() == null ? "" : getItem().toString();//NOI18N
	}
}
