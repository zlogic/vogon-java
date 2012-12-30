package org.zlogic.att.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import org.zlogic.att.data.PersistenceHelper;
import org.zlogic.att.data.Task;
import org.zlogic.att.ui.adapters.TaskAdapter;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for the main window
 * <p/>
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 29.12.12
 * Time: 22:18
 */
public class MainWindowController implements Initializable {
	private final static Logger log = Logger.getLogger(MainWindowController.class.getName());
	private PersistenceHelper storageManager = new PersistenceHelper();

	@FXML
	private TaskEditorController taskEditorController;

	@FXML
	private TableView<TaskAdapter> taskList;

	@FXML
	private TableColumn<TaskAdapter, String> columnTaskName;

	@FXML
	private TableColumn<TaskAdapter, Boolean> columnTaskEnabled;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		taskList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		reloadTasks();
		//Cell editors
		columnTaskName.setCellFactory(new Callback<TableColumn<TaskAdapter, String>, TableCell<TaskAdapter, String>>() {
			@Override
			public TableCell<TaskAdapter, String> call(TableColumn<TaskAdapter, String> p) {
				TextFieldTableCell<TaskAdapter, String> cell = new TextFieldTableCell<>();
				cell.setConverter(new DefaultStringConverter());
				return cell;
			}
		});
		columnTaskEnabled.setCellFactory(new Callback<TableColumn<TaskAdapter, Boolean>, TableCell<TaskAdapter, Boolean>>() {
			@Override
			public TableCell<TaskAdapter, Boolean> call(TableColumn<TaskAdapter, Boolean> taskAdapterBooleanTableColumn) {
				return new CheckBoxTableCell<>();
			}
		});

		//Set column sizes
		//TODO: make sure this keeps working correctly
		//taskList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		columnTaskName.prefWidthProperty().bind(taskList.widthProperty().multiply(9).divide(10));
		columnTaskEnabled.prefWidthProperty().bind(taskList.widthProperty().multiply(1).divide(10).subtract(15));
	}

	private void reloadTasks() {
		taskList.getItems().clear();
		for (Task task : storageManager.getAllTasks())
			taskList.getItems().add(new TaskAdapter(task));
		taskEditorController.setEditedTaskList(taskList.getSelectionModel().getSelectedItems());
	}

	@FXML
	private void createNewTask() {
		taskList.getItems().add(new TaskAdapter(storageManager.createTask()));
	}
}
