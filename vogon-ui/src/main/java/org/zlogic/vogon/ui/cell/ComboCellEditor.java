/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zlogic.vogon.ui.cell;

import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;

/**
 *
 * @author Dmitry
 */
public class ComboCellEditor<BaseType, PropertyType> extends TableCell<BaseType, PropertyType> {
	protected ComboBox<PropertyType> comboField;
	protected List<PropertyType> values;
	
	public ComboCellEditor(List<PropertyType> values){
		this.values = values;
	}
	
	@Override
	public void startEdit() {
		super.startEdit();

		if (comboField == null) {
			createComboField();
		}
		setText(null);
		setGraphic(comboField);
		comboField.getSelectionModel().select(getItem());
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
				if (comboField != null) {
					comboField.getSelectionModel().select(item);
				}
				setText(null);
				setGraphic(comboField);
			} else {
				setText(getString());
				setGraphic(null);
			}
		}
	}

	private void createComboField() {
		comboField = new ComboBox<>();
		comboField.getItems().addAll(values);
		comboField.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent t) {
				commitEdit(comboField.getSelectionModel().getSelectedItem());
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
