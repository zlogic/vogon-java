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
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.util.Callback;
import org.zlogic.att.ui.adapters.TaskManager;
import org.zlogic.att.ui.filter.FilterFactory;
import org.zlogic.att.ui.filter.FilterHolder;
import org.zlogic.att.ui.filter.FilterTypeFactory;
import org.zlogic.att.ui.filter.ui.FilterValueCell;

/**
 * Controller for the filters window
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class FilterEditorController implements Initializable {

	/**
	 * The filter factory used to create filters and display the list of
	 * available filters
	 */
	private FilterFactory filterBuilder;
	/**
	 * Filter list table
	 */
	@FXML
	private TableView<FilterHolder> filters;
	/**
	 * Filter Expression column
	 */
	@FXML
	private TableColumn<FilterHolder, Object> columnFilterExpression;
	/**
	 * Filter Type column
	 */
	@FXML
	private TableColumn<FilterHolder, FilterTypeFactory> columnFilterType;
	/**
	 * Delete button
	 */
	@FXML
	private Button deleteButton;

	/**
	 * Initializes the controller
	 *
	 * @param url initialization URL
	 * @param resourceBundle supplied resources
	 */
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		filters.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		deleteButton.disableProperty().bind(filters.getSelectionModel().selectedItemProperty().isNull());

		//Cell editors
		columnFilterType.setCellFactory(new Callback<TableColumn<FilterHolder, FilterTypeFactory>, TableCell<FilterHolder, FilterTypeFactory>>() {
			@Override
			public TableCell<FilterHolder, FilterTypeFactory> call(TableColumn<FilterHolder, FilterTypeFactory> p) {
				ComboBoxTableCell<FilterHolder, FilterTypeFactory> cell = new ComboBoxTableCell<>(filterBuilder.getAvailableFilters());
				cell.setComboBoxEditable(false);
				return cell;
			}
		});

		columnFilterExpression.setCellFactory(new Callback<TableColumn<FilterHolder, Object>, TableCell<FilterHolder, Object>>() {
			@Override
			public TableCell<FilterHolder, Object> call(TableColumn<FilterHolder, Object> p) {
				return new FilterValueCell();
			}
		});
	}

	/**
	 * Sets the TaskManager reference
	 *
	 * @param taskManager the TaskManager reference
	 */
	public void setTaskManager(TaskManager taskManager) {
		filters.setItems(taskManager.getFilters());
		filterBuilder = new FilterFactory(taskManager);
	}

	/*
	 * Callbacks
	 */
	/**
	 * Hides the window
	 */
	@FXML
	private void hideWindow() {
		filters.getScene().getWindow().hide();
	}

	/**
	 * Adds a new filter
	 */
	@FXML
	private void addFilter() {
		filters.getItems().add(new FilterHolder(filterBuilder.createFilter()));
	}

	/**
	 * Delete selected filters
	 */
	@FXML
	private void deleteFilter() {
		for (FilterHolder filter : filters.getSelectionModel().getSelectedItems())
			filters.getItems().remove(filter);
	}
}
