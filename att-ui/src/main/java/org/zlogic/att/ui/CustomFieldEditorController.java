/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import org.zlogic.att.ui.adapters.CustomFieldAdapter;
import org.zlogic.att.ui.adapters.TaskManager;

/**
 * Controller for custom field editor
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class CustomFieldEditorController implements Initializable {

	/**
	 * TaskManager reference
	 */
	private TaskManager taskManager;
	/**
	 * Delete button
	 */
	@FXML
	private Button deleteButton;
	/**
	 * Custom field column
	 */
	@FXML
	private TableColumn<CustomFieldAdapter, String> columnCustomField;
	/**
	 * Custom fields table
	 */
	@FXML
	private TableView<CustomFieldAdapter> customFields;

	/**
	 * Initializes the controller
	 *
	 * @param url initialization URL
	 * @param resourceBundle supplied resources
	 */
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		customFields.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		deleteButton.disableProperty().bind(customFields.getSelectionModel().selectedItemProperty().isNull());

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

	/**
	 * Sets the TaskManager reference
	 *
	 * @param taskManager the TaskManager reference
	 */
	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
		customFields.setItems(taskManager.getCustomFields());
	}

	/*
	 * Callbacks
	 */
	/**
	 * Hides the window
	 */
	@FXML
	private void hideWindow() {
		customFields.getScene().getWindow().hide();
	}

	/**
	 * Adds a new custom field
	 */
	@FXML
	private void addCustomField() {
		CustomFieldAdapter newCustomField = taskManager.createCustomField();
		customFields.getSelectionModel().clearSelection();
		customFields.getSelectionModel().select(newCustomField);
	}

	/**
	 * Delete selected custom fields
	 */
	@FXML
	private void deleteCustomField() {
		for (CustomFieldAdapter customField : customFields.getSelectionModel().getSelectedItems())
			taskManager.deleteCustomField(customField);
	}
}
