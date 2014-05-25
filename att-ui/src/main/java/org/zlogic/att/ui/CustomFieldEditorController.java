/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.net.URL;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import org.zlogic.att.ui.adapters.CustomFieldAdapter;
import org.zlogic.att.ui.adapters.DataManager;

/**
 * Controller for custom field editor
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class CustomFieldEditorController implements Initializable {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	/**
	 * DataManager reference
	 */
	private DataManager dataManager;
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
	 * Confirmation prompt dialog controller
	 */
	private ConfirmationDialogController confirmationDialogController;

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

		//Confirmation dialog
		confirmationDialogController = ConfirmationDialogController.createInstance();
	}

	/**
	 * Sets the window icons
	 *
	 * @param icons the icons to be set
	 */
	public void setWindowIcons(ObservableList<Image> icons) {
		if (deleteButton.getScene().getWindow() instanceof Stage)
			((Stage) deleteButton.getScene().getWindow()).getIcons().setAll(icons);
		confirmationDialogController.setWindowIcons(icons);
	}

	/**
	 * Sets the DataManager reference
	 *
	 * @param dataManager the DataManager reference
	 */
	public void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;
		customFields.setItems(dataManager.getCustomFields());
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
		CustomFieldAdapter newCustomField = dataManager.createCustomField();
		customFields.getSelectionModel().clearSelection();
		customFields.getSelectionModel().select(newCustomField);
	}

	/**
	 * Delete selected custom fields
	 */
	@FXML
	private void deleteCustomField() {
		StringBuilder tasksToDelete = new StringBuilder();
		List<CustomFieldAdapter> customfieldsToDeleteList = new LinkedList(customFields.getSelectionModel().getSelectedItems());
		for (CustomFieldAdapter customfieldToDelete : customfieldsToDeleteList)
			tasksToDelete.append(tasksToDelete.length() > 0 ? "\n" : "").append(customfieldToDelete.nameProperty().get()); //NOI18N
		ConfirmationDialogController.Result result = confirmationDialogController.showDialog(
				messages.getString("CONFIRM_CUSTOM_FIELD_DELETION"),
				MessageFormat.format(messages.getString("ARE_YOU_SURE_YOU_WANT_TO_DELETE_THE_FOLLOWING_CUSTOM_FIELDS"), tasksToDelete)
		);
		if (result == ConfirmationDialogController.Result.CONFIRMED)
			for (CustomFieldAdapter customField : customfieldsToDeleteList)
				dataManager.deleteCustomField(customField);
	}
}
