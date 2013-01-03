package org.zlogic.att.ui;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import org.zlogic.att.ui.adapters.CustomFieldAdapter;
import org.zlogic.att.ui.adapters.CustomFieldValueAdapter;
import org.zlogic.att.ui.adapters.TaskAdapter;
import org.zlogic.att.ui.adapters.TimeSegmentAdapter;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller for task editor
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

	@FXML
	private TableColumn<TimeSegmentAdapter, ?> columnEnd;

	@FXML
	private TableColumn<TimeSegmentAdapter, ?> columnStart;

	@FXML
	private TableColumn<TimeSegmentAdapter, ?> columnDescription;

	@FXML
	private TableView<TimeSegmentAdapter> timeSegments;

	@FXML
	private TableColumn<CustomFieldValueAdapter, String> columnFieldValue;
	@FXML
	private TableView<CustomFieldValueAdapter> customProperties;

	boolean isTimingTask = false;

	private ObservableList<CustomFieldAdapter> customFields;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		updateStartStopText();
		//Cell editors
		columnFieldValue.setCellFactory(new Callback<TableColumn<CustomFieldValueAdapter, String>, TableCell<CustomFieldValueAdapter, String>>() {
			@Override
			public TableCell<CustomFieldValueAdapter, String> call(TableColumn<CustomFieldValueAdapter, String> p) {
				TextFieldTableCell<CustomFieldValueAdapter, String> cell = new TextFieldTableCell<>();
				cell.setConverter(new DefaultStringConverter());
				return cell;
			}
		});

		//Set column sizes
		//TODO: make sure this keeps working correctly
		customProperties.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		columnStart.prefWidthProperty().bind(timeSegments.widthProperty().multiply(3).divide(10));
		columnEnd.prefWidthProperty().bind(timeSegments.widthProperty().multiply(3).divide(10));
		columnDescription.prefWidthProperty().bind(timeSegments.widthProperty().multiply(4).divide(10).subtract(15));
	}

	public void setCustomFields(ObservableList<CustomFieldAdapter> customFields) {
		this.customFields = customFields;
		updateCustomFields();
		customFields.addListener(new ListChangeListener<CustomFieldAdapter>() {
			@Override
			public void onChanged(Change<? extends CustomFieldAdapter> change) {
				updateCustomFields();
			}
		});//TODO: remove listener if setCustomFields is called once more
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

	private void updateCustomFields() {
		customProperties.getItems().clear();
		for (CustomFieldAdapter customFieldAdapter : customFields) {
			customProperties.getItems().add(new CustomFieldValueAdapter(customFieldAdapter));
		}
		if (editedTaskList != null && editedTaskList.size() == 1) {
			TaskAdapter task = editedTaskList.get(0);
			for (CustomFieldValueAdapter customFieldValueAdapter : customProperties.getItems())
				customFieldValueAdapter.setTask(task);
		}
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
			for (CustomFieldValueAdapter customFieldValueAdapter : customProperties.getItems())
				customFieldValueAdapter.setTask(task);
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
		startStop.setDisable(boundTasks.isEmpty());
		startStop.setText(isTimingTask ? "Stop" : "Start");
	}

	@FXML
	private void handleStartStop(ActionEvent event) {
		isTimingTask = !isTimingTask;
		updateStartStopText();
	}
}
