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
import org.zlogic.att.ui.filter.Filter;
import org.zlogic.att.ui.filter.FilterFactory;
import org.zlogic.att.ui.filter.FilterHolder;
import org.zlogic.att.ui.filter.FilterTypeFactory;
import org.zlogic.att.ui.filter.FilterValueCell;

/**
 * Controller for the filters window
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class FilterEditorController implements Initializable {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	private FilterFactory filterBuilder;
	@FXML
	private TableColumn<FilterHolder, Object> columnFilterExpression;
	@FXML
	private TableColumn<FilterHolder, FilterTypeFactory> columnFilterType;
	@FXML
	private Button deleteButton;
	@FXML
	private TableView<FilterHolder> filters;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
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
		filters.getItems().clear();
		for (Filter filter : taskManager.getFilters())
			filters.getItems().add(new FilterHolder(filter));
		filterBuilder = new FilterFactory(taskManager);
	}

	/*
	 * Callbacks
	 */
	@FXML
	private void addFilter() {
		filters.getItems().add(new FilterHolder(filterBuilder.createFilter()));
	}

	@FXML
	private void closeWindow() {
		filters.getScene().getWindow().hide();
	}

	@FXML
	private void deleteFilter() {
		for (FilterHolder filter : filters.getSelectionModel().getSelectedItems())
			filters.getItems().remove(filter);
	}
}
