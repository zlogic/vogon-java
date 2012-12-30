package org.zlogic.att.ui;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.zlogic.att.ui.adapters.TaskAdapter;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for task editor
 * <p/>
 * User: Dmitry Zolotukhin <zlogic@gmail.com>
 * Date: 29.12.12
 * Time: 22:36
 */
public class TaskEditorController implements Initializable {
	private final static Logger log = Logger.getLogger(MainWindowController.class.getName());

	private ObservableList<TaskAdapter> editedTaskList;

	private List<TaskAdapter> boundTasks = new LinkedList<>();

	@FXML
	private TextArea description;

	@FXML
	private TextField name;

	@FXML
	private Button startStop;

	boolean isTimingTask = false;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		updateStartStopText();
	}

	public void setEditedTaskList(ObservableList<TaskAdapter> editedTaskList) {
		this.editedTaskList = editedTaskList;
		editedTaskList.addListener(new ListChangeListener<TaskAdapter>() {
			@Override
			public void onChanged(Change<? extends TaskAdapter> change) {
				updateEditingTasks();
			}
		});
	}

	private void updateEditingTasks() {
		for (TaskAdapter adapter : boundTasks) {
			name.textProperty().unbindBidirectional(adapter.nameProperty());
			description.textProperty().unbindBidirectional(adapter.descriptionProperty());
		}
		boundTasks.clear();
		if (editedTaskList != null && editedTaskList.size() == 1) {
			TaskAdapter task = editedTaskList.get(0);
			name.textProperty().bindBidirectional(task.nameProperty());
			description.textProperty().bindBidirectional(task.descriptionProperty());
			boundTasks.add(task);
			isTimingTask = false;//TODO
			updateStartStopText();
		} else {
			isTimingTask = false;//TODO
			updateStartStopText();
			if (editedTaskList.size() > 1)
				log.severe("Can only edit a single task at a time");//TODO
		}
	}

	protected void updateStartStopText() {
		if (isTimingTask)
			startStop.setText("Stop");
		else
			startStop.setText("Start");
	}

	@FXML
	private void handleStartStop(ActionEvent event) {
		isTimingTask = !isTimingTask;
		updateStartStopText();
	}
}
