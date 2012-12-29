package org.zlogic.att.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
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
	}

	private void reloadTasks() {
		taskList.getItems().clear();
		for (Task task : storageManager.getAllTasks())
			taskList.getItems().add(new TaskAdapter(task));
	}

	@FXML
	private void createNewTask() {
		taskList.getItems().add(new TaskAdapter(storageManager.createTask()));
	}
}
