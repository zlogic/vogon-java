/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.cell;

import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;

/**
 * Cell editor which uses a combo box for selecting a value.
 *
 * @param <BaseType> the row type
 * @param <PropertyType> the cell type
 * @author Dmitry Zolotukhin
 */
public class ComboCellEditor<BaseType, PropertyType> extends TableCell<BaseType, PropertyType> {

	/**
	 * The combo box editor control
	 */
	protected ComboBox<PropertyType> comboField;
	/**
	 * The list of items for the combo box editor control
	 */
	protected List<PropertyType> values;

	/**
	 * Constructs a ComboCellEditor with a list of values
	 *
	 * @param values the values to be displayed in the combo box
	 */
	public ComboCellEditor(List<PropertyType> values) {
		this.values = values;
	}

	/**
	 * Prepares the cell for editing
	 */
	@Override
	public void startEdit() {
		if (comboField == null)
			createComboField();
		comboField.getSelectionModel().select(getItem());
		super.startEdit();
		setText(null);
		setGraphic(comboField);
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
	 * @param item the updated TransactionModelAdapter instance
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
				if (comboField != null)
					comboField.getSelectionModel().select(item);
				setText(null);
				setGraphic(comboField);
			} else {
				setText(getString());
				setGraphic(null);
			}
		}
	}

	/*
	 * Creates the edit control
	 */
	private void createComboField() {
		comboField = new ComboBox<>();
		comboField.getItems().addAll(values);
		comboField.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				if(isEditing())
					commitEdit(comboField.getSelectionModel().getSelectedItem());
			}
		});
	}

	/**
	 * Returns the string value of the edited property (before editing)
	 *
	 * @return the string value of the edited property
	 */
	protected String getString() {
		return getItem() == null ? "" : getItem().toString();
	}
}
