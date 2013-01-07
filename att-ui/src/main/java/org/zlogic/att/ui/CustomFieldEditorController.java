/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import org.zlogic.att.data.CustomField;
import org.zlogic.att.data.PersistenceHelper;
import org.zlogic.att.ui.adapters.CustomFieldAdapter;

/**
 * Controller for custom field editor
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class CustomFieldEditorController implements Initializable {

	private PersistenceHelper storageManager = new PersistenceHelper();
	@FXML
	private TableColumn<CustomFieldAdapter, String> columnCustomField;
	@FXML
	private TableView<CustomFieldAdapter> customFields;

	@FXML
	public void addCustomField() {
		CustomFieldAdapter newCustomField = new CustomFieldAdapter(storageManager.createCustomField());
		customFields.getItems().add(newCustomField);
		customFields.getSelectionModel().select(newCustomField);
	}

	public void deleteCustomField() {
		for (CustomFieldAdapter customField : customFields.getSelectionModel().getSelectedItems())
			storageManager.deleteCustomField(customField.getCustomField());
		customFields.getItems().removeAll(customFields.getSelectionModel().getSelectedItems());
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		reloadCustomFields();

		customFields.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		//Cell editors
		columnCustomField.setCellFactory(new Callback<TableColumn<CustomFieldAdapter, String>, TableCell<CustomFieldAdapter, String>>() {
			@Override
			public TableCell<CustomFieldAdapter, String> call(TableColumn<CustomFieldAdapter, String> p) {
				TextFieldTableCell<CustomFieldAdapter, String> cell = new TextFieldTableCell<>();
				cell.setConverter(new DefaultStringConverter());
				return cell;
			}
		});
	}

	private void reloadCustomFields() {
		customFields.getItems().clear();
		for (CustomField customField : storageManager.getCustomFields())
			customFields.getItems().add(new CustomFieldAdapter(customField));
	}

	public ObservableList<CustomFieldAdapter> getCustomFields() {
		return customFields.getItems();
	}

	@FXML
	private void hideWindow() {
		customFields.getScene().getWindow().hide();
	}
}