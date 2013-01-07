/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.net.URL;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.converter.DateTimeStringConverter;
import javafx.util.converter.DefaultStringConverter;
import org.zlogic.att.data.PersistenceHelper;
import org.zlogic.att.data.TimeSegment;
import org.zlogic.att.ui.adapters.CustomFieldAdapter;
import org.zlogic.att.ui.adapters.CustomFieldValueAdapter;
import org.zlogic.att.ui.adapters.TaskAdapter;
import org.zlogic.att.ui.adapters.TimeSegmentAdapter;

/**
 * Controller for task editor
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class TaskEditorController implements Initializable {

	private final static Logger log = Logger.getLogger(MainWindowController.class.getName());
	private ObservableList<TaskAdapter> editedTaskList;
	private List<TaskAdapter> boundTasks = new LinkedList<>();
	private ObservableList<CustomFieldAdapter> customFields;
	private PersistenceHelper storageManager = new PersistenceHelper();
	@FXML
	private TextArea description;
	@FXML
	private TextField name;
	@FXML
	private Button startStop;
	@FXML
	private TableColumn<TimeSegmentAdapter, Date> columnStart;
	@FXML
	private TableColumn<TimeSegmentAdapter, Date> columnEnd;
	@FXML
	private TableColumn<TimeSegmentAdapter, String> columnDescription;
	@FXML
	private TableView<TimeSegmentAdapter> timeSegments;
	@FXML
	private TableColumn<CustomFieldValueAdapter, String> columnFieldValue;
	@FXML
	private TableView<CustomFieldValueAdapter> customProperties;
	private ObjectProperty<TimeSegmentAdapter> currentSegment = new SimpleObjectProperty<>();

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
		columnDescription.setCellFactory(new Callback<TableColumn<TimeSegmentAdapter, String>, TableCell<TimeSegmentAdapter, String>>() {
			@Override
			public TableCell<TimeSegmentAdapter, String> call(TableColumn<TimeSegmentAdapter, String> p) {
				TextFieldTableCell<TimeSegmentAdapter, String> cell = new TextFieldTableCell<>();
				cell.setConverter(new DefaultStringConverter());
				return cell;
			}
		});
		columnStart.setCellFactory(new Callback<TableColumn<TimeSegmentAdapter, Date>, TableCell<TimeSegmentAdapter, Date>>() {
			@Override
			public TableCell<TimeSegmentAdapter, Date> call(TableColumn<TimeSegmentAdapter, Date> p) {
				TextFieldTableCell<TimeSegmentAdapter, Date> cell = new TextFieldTableCell<>();
				cell.setConverter(new DateTimeStringConverter());
				return cell;
			}
		});
		columnEnd.setCellFactory(new Callback<TableColumn<TimeSegmentAdapter, Date>, TableCell<TimeSegmentAdapter, Date>>() {
			@Override
			public TableCell<TimeSegmentAdapter, Date> call(TableColumn<TimeSegmentAdapter, Date> p) {
				TextFieldTableCell<TimeSegmentAdapter, Date> cell = new TextFieldTableCell<>();
				cell.setConverter(new DateTimeStringConverter());
				return cell;
			}
		});
		//Update start/stop button text
		currentSegment.addListener(new ChangeListener<TimeSegmentAdapter>() {
			private ChangeListener<Boolean> startStopListener = new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
					if (newValue != null && !newValue)
						currentSegment.set(null);
					updateStartStopText();
				}
			};

			@Override
			public void changed(ObservableValue<? extends TimeSegmentAdapter> ov, TimeSegmentAdapter oldValue, TimeSegmentAdapter newValue) {
				if (oldValue != null)
					oldValue.isTimingProperty().removeListener(startStopListener);
				if (newValue != null)
					newValue.isTimingProperty().addListener(startStopListener);
			}
		});
		//Set column sizes
		//TODO: make sure this keeps working correctly
		customProperties.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		columnStart.prefWidthProperty().bind(timeSegments.widthProperty().multiply(3).divide(10));
		columnEnd.prefWidthProperty().bind(timeSegments.widthProperty().multiply(3).divide(10));
		columnDescription.prefWidthProperty().bind(timeSegments.widthProperty().multiply(4).divide(10).subtract(15));

		//Default sort order
		timeSegments.getSortOrder().add(columnStart);
		columnStart.setSortType(TableColumn.SortType.DESCENDING);

		//Date comparator
		Comparator<Date> dateComparator = new Comparator<Date>() {
			@Override
			public int compare(Date o1, Date o2) {
				return o1.compareTo(o2);
			}
		};
		columnStart.setComparator(dateComparator);
		columnEnd.setComparator(dateComparator);
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

	public ObjectProperty<TimeSegmentAdapter> timingSegmentProperty() {
		return currentSegment;
	}

	private void updateCustomFields() {
		customProperties.getItems().clear();
		for (CustomFieldAdapter customFieldAdapter : customFields) {
			customProperties.getItems().add(new CustomFieldValueAdapter(customFieldAdapter));
		}
		TaskAdapter task = getEditedTask();
		if (task != null)
			for (CustomFieldValueAdapter customFieldValueAdapter : customProperties.getItems())
				customFieldValueAdapter.setTask(task);
	}

	private void updateEditingTasks() {
		for (TaskAdapter adapter : boundTasks) {
			name.textProperty().unbindBidirectional(adapter.nameProperty());
			description.textProperty().unbindBidirectional(adapter.descriptionProperty());
		}
		boundTasks.clear();
		TaskAdapter editedTask = getEditedTask();
		if (editedTask != null) {
			name.textProperty().bindBidirectional(editedTask.nameProperty());
			description.textProperty().bindBidirectional(editedTask.descriptionProperty());
			boundTasks.add(editedTask);
			for (CustomFieldValueAdapter customFieldValueAdapter : customProperties.getItems())
				customFieldValueAdapter.setTask(editedTask);
			timeSegments.setItems(editedTask.timeSegmentsProperty());
			updateSortOrder();
			updateStartStopText();
		} else {
			updateStartStopText();
			if (editedTaskList.size() > 1)
				log.severe("Can only edit a single task at a time");//TODO
		}
	}

	//TODO: add support for editing multiple tasks
	protected TaskAdapter getEditedTask() {
		if (editedTaskList != null && editedTaskList.size() == 1)
			return editedTaskList.get(0);
		return null;
	}

	private void updateSortOrder() {
		//TODO: Remove this after it's fixed in Java FX
		TableColumn<TimeSegmentAdapter, ?>[] sortOrder = timeSegments.getSortOrder().toArray(new TableColumn[0]);
		timeSegments.getSortOrder().clear();
		timeSegments.getSortOrder().addAll(sortOrder);
	}

	private boolean isTimingCurrentTask() {
		TaskAdapter editedTask = getEditedTask();
		if (editedTask == null || currentSegment == null)
			return false;
		return editedTask.timeSegmentsProperty().contains(currentSegment.get());
	}

	private void updateStartStopText() {
		startStop.setDisable(boundTasks.isEmpty());
		startStop.setText(isTimingCurrentTask() ? "Stop" : "Start");
	}

	@FXML
	private void handleStartStop() {
		if (currentSegment.get() != null) {
			currentSegment.get().stopTiming();
			currentSegment.set(null);
		} else {
			TaskAdapter task = getEditedTask();
			if (task != null) {
				TimeSegment newSegment = storageManager.createTimeSegment(task.getTask());
				TimeSegmentAdapter newSegmentAdapter = new TimeSegmentAdapter(newSegment);
				currentSegment.setValue(newSegmentAdapter);
				timeSegments.getItems().add(newSegmentAdapter);
				updateSortOrder();
				currentSegment.get().startTiming();
			}
		}
		updateStartStopText();
	}
}
