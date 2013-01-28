/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.util.Callback;
import org.zlogic.att.ui.adapters.TaskManager;
import org.zlogic.att.ui.filter.EmptyFilter;
import org.zlogic.att.ui.filter.Filter;
import org.zlogic.att.ui.filter.FilterBuilder;
import org.zlogic.att.ui.filter.FilterBuilder.FilterTypeComboItem;

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
	private FilterBuilder filterBuilder;
	@FXML
	private TableColumn<Filter, Object> columnFilterExpression;
	@FXML
	private TableColumn<Filter, FilterBuilder.FilterTypeComboItem> columnFilterType;
	@FXML
	private Button deleteButton;
	@FXML
	private TableView<Filter> filters;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		filters.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		deleteButton.disableProperty().bind(filters.getSelectionModel().selectedItemProperty().isNull());


		//Cell editors
		columnFilterType.setCellFactory(new Callback<TableColumn<Filter, FilterBuilder.FilterTypeComboItem>, TableCell<Filter, FilterBuilder.FilterTypeComboItem>>() {
			@Override
			public TableCell<Filter, FilterBuilder.FilterTypeComboItem> call(TableColumn<Filter, FilterBuilder.FilterTypeComboItem> p) {
				ComboBoxTableCell<Filter, FilterBuilder.FilterTypeComboItem> cell = new ComboBoxTableCell<>(filterBuilder.getAvailableFilters());
				cell.setComboBoxEditable(false);
				cell.itemProperty().addListener(new ChangeListener<FilterBuilder.FilterTypeComboItem>() {
					private ComboBoxTableCell<Filter, FilterBuilder.FilterTypeComboItem> cell;

					public ChangeListener<FilterBuilder.FilterTypeComboItem> setCell(ComboBoxTableCell<Filter, FilterBuilder.FilterTypeComboItem> cell) {
						this.cell = cell;
						return this;
					}

					@Override
					public void changed(ObservableValue<? extends FilterTypeComboItem> ov, FilterTypeComboItem oldValue, FilterTypeComboItem newValue) {
						if (newValue != null)
							filters.getItems().set(cell.getIndex(), newValue.createFilter());
					}
				}.setCell(cell));
				return cell;
			}
		});

		columnFilterExpression.setCellFactory(new Callback<TableColumn<Filter, Object>, TableCell<Filter, Object>>() {
			@Override
			public TableCell<Filter, Object> call(TableColumn<Filter, Object> p) {
				ComboBoxTableCell<Filter, Object> cell = new ComboBoxTableCell<>();
				cell.setComboBoxEditable(false);
				cell.tableRowProperty().addListener(new ChangeListener<TableRow>() {
					private ComboBoxTableCell<Filter, Object> cell;
					private ChangeListener itemChangeListener = new ChangeListener<Filter>() {
						@Override
						public void changed(ObservableValue<? extends Filter> ov, Filter oldValue, Filter newValue) {
							updateCell(newValue);
						}
					};

					public ChangeListener<TableRow> setCell(ComboBoxTableCell<Filter, Object> cell) {
						this.cell = cell;
						return this;
					}

					private void updateCell(Object item) {
						if (item == null || !(item instanceof Filter) && item instanceof EmptyFilter)
							return;
						Filter filter = (Filter) item;
						if (filter != null && !(filter instanceof EmptyFilter)) {
							cell.getItems().setAll(filter.getAvailableValues());
							cell.setComboBoxEditable(filter.isAllowAnyValue());
							cell.setConverter(filter.getConverter());
						} else {
							cell.getItems().clear();
							cell.setComboBoxEditable(false);
						}
					}

					@Override
					public void changed(ObservableValue<? extends TableRow> ov, TableRow oldValue, TableRow newValue) {
						if (oldValue != null)
							oldValue.itemProperty().removeListener(itemChangeListener);
						if (newValue != null) {
							newValue.itemProperty().addListener(itemChangeListener);
							updateCell(newValue.getItem());
						}
					}
				}.setCell(cell));
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
		filters.setItems(taskManager.getFilters());
		filterBuilder = new FilterBuilder(taskManager);
	}

	/*
	 * Callbacks
	 */
	@FXML
	private void addFilter() {
		filters.getItems().add(filterBuilder.createFilter());
	}

	@FXML
	private void closeWindow() {
		filters.getScene().getWindow().hide();
	}

	@FXML
	private void deleteFilter() {
		for (Filter filter : filters.getSelectionModel().getSelectedItems())
			filters.getItems().remove(filter);
	}
}
