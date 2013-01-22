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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import javafx.util.converter.DateTimeStringConverter;
import javafx.util.converter.DefaultStringConverter;
import org.zlogic.att.ui.adapters.CustomFieldAdapter;
import org.zlogic.att.ui.adapters.CustomFieldValueAdapter;
import org.zlogic.att.ui.adapters.TaskAdapter;
import org.zlogic.att.ui.adapters.TaskManager;
import org.zlogic.att.ui.adapters.TimeSegmentAdapter;

/**
 * Controller for task editor
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class TaskEditorController implements Initializable {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(MainWindowController.class.getName());
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	/**
	 * List of currently edited tasks
	 */
	private ObservableList<TaskAdapter> editedTaskList;
	/**
	 * List of bound tasks which sets most of this form's fields
	 */
	private List<TaskAdapter> boundTasks = new LinkedList<>();
	/**
	 * TaskManager reference
	 */
	private TaskManager taskManager;
	/**
	 * True to ignore updates to edited task (e.g. while editing a time segment)
	 */
	private boolean ignoreEditedTaskUpdates = false;
	/**
	 * Task description
	 */
	@FXML
	private TextArea description;
	/**
	 * Task name
	 */
	@FXML
	private TextField name;
	/**
	 * Total time for task
	 */
	@FXML
	private TextField totalTime;
	/**
	 * Start/Stop button
	 */
	@FXML
	private Button startStop;
	/**
	 * Delete segment button
	 */
	@FXML
	private Button delete;
	/**
	 * Time segment start column
	 */
	@FXML
	private TableColumn<TimeSegmentAdapter, Date> columnStart;
	/**
	 * Time segment end column
	 */
	@FXML
	private TableColumn<TimeSegmentAdapter, Date> columnEnd;
	/**
	 * Time segment duration column
	 */
	@FXML
	private TableColumn<TimeSegmentAdapter, String> columnDuration;
	/**
	 * Time segment description column
	 */
	@FXML
	private TableColumn<TimeSegmentAdapter, String> columnDescription;
	/**
	 * Time segments table
	 */
	@FXML
	private TableView<TimeSegmentAdapter> timeSegments;
	/**
	 * Custom field value column
	 */
	@FXML
	private TableColumn<CustomFieldValueAdapter, String> columnFieldValue;
	/**
	 * Custom field values table
	 */
	@FXML
	private TableView<CustomFieldValueAdapter> customProperties;

	/**
	 * Initializes the controller
	 *
	 * @param url initialization URL
	 * @param resourceBundle supplied resources
	 */
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		updateStartStopText();
		//Row properties
		timeSegments.setRowFactory(new Callback<TableView<TimeSegmentAdapter>, TableRow<TimeSegmentAdapter>>() {
			@Override
			public TableRow<TimeSegmentAdapter> call(TableView<TimeSegmentAdapter> p) {
				TableRow<TimeSegmentAdapter> row = new TableRow<>();
				row.itemProperty().addListener(new ChangeListener<TimeSegmentAdapter>() {
					private TableRow<TimeSegmentAdapter> row;
					private ChangeListener timingChangeListener = new ChangeListener<Boolean>() {
						@Override
						public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
							if (newValue != null) {
								if (newValue)
									row.getStyleClass().add("timing-segment"); //NOI18N
								else
									row.getStyleClass().remove("timing-segment"); //NOI18N
							}
						}
					};

					public ChangeListener<TimeSegmentAdapter> setRow(TableRow<TimeSegmentAdapter> row) {
						this.row = row;
						return this;
					}

					@Override
					public void changed(ObservableValue<? extends TimeSegmentAdapter> ov, TimeSegmentAdapter oldValue, TimeSegmentAdapter newValue) {
						if (oldValue != null)
							oldValue.isTimingProperty().removeListener(timingChangeListener);
						if (newValue != null) {
							newValue.isTimingProperty().addListener(timingChangeListener);
							timingChangeListener.changed(newValue.isTimingProperty(), oldValue != null ? oldValue.isTimingProperty().get() : false, newValue.isTimingProperty().get());
						}
					}
				}.setRow(row));
				return row;
			}
		});

		//Cell editors
		columnFieldValue.setCellFactory(new Callback<TableColumn<CustomFieldValueAdapter, String>, TableCell<CustomFieldValueAdapter, String>>() {
			@Override
			public TableCell<CustomFieldValueAdapter, String> call(TableColumn<CustomFieldValueAdapter, String> p) {
				ComboBoxTableCell<CustomFieldValueAdapter, String> cell = new ComboBoxTableCell<>();
				cell.setConverter(new DefaultStringConverter());
				cell.setComboBoxEditable(true);
				cell.tableRowProperty().addListener(new ChangeListener<TableRow>() {
					private ComboBoxTableCell<CustomFieldValueAdapter, String> cell;

					public ChangeListener<TableRow> setCell(ComboBoxTableCell<CustomFieldValueAdapter, String> cell) {
						this.cell = cell;
						return this;
					}

					@Override
					public void changed(ObservableValue<? extends TableRow> ov, TableRow oldValue, TableRow newValue) {
						if (newValue != null && newValue.getItem() instanceof CustomFieldValueAdapter) {
							ObservableList<String> customFieldValues = taskManager.getCustomFieldValues(((CustomFieldValueAdapter) newValue.getItem()).getCustomField());
							cell.getItems().setAll(customFieldValues != null ? customFieldValues : new LinkedList<String>());
						}
					}
				}.setCell(cell));
				cell.itemProperty().addListener(new ChangeListener<String>() {
					private ComboBoxTableCell<CustomFieldValueAdapter, String> cell;

					public ChangeListener<String> setCell(ComboBoxTableCell<CustomFieldValueAdapter, String> cell) {
						this.cell = cell;
						return this;
					}

					@Override
					public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
						if (newValue != null && cell.getTableRow().getItem() instanceof CustomFieldValueAdapter)
							cell.getItems().setAll(taskManager.getCustomFieldValues(((CustomFieldValueAdapter) cell.getTableRow().getItem()).getCustomField()));
					}
				}.setCell(cell));
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
		columnDuration.setCellFactory(new Callback<TableColumn<TimeSegmentAdapter, String>, TableCell<TimeSegmentAdapter, String>>() {
			@Override
			public TableCell<TimeSegmentAdapter, String> call(TableColumn<TimeSegmentAdapter, String> p) {
				TextFieldTableCell<TimeSegmentAdapter, String> cell = new TextFieldTableCell<>();
				cell.setConverter(new DefaultStringConverter());
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});
		columnStart.setCellFactory(new Callback<TableColumn<TimeSegmentAdapter, Date>, TableCell<TimeSegmentAdapter, Date>>() {
			@Override
			public TableCell<TimeSegmentAdapter, Date> call(TableColumn<TimeSegmentAdapter, Date> p) {
				TextFieldTableCell<TimeSegmentAdapter, Date> cell = new TextFieldTableCell<>();
				cell.setConverter(new DateTimeStringConverter());
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});
		columnEnd.setCellFactory(new Callback<TableColumn<TimeSegmentAdapter, Date>, TableCell<TimeSegmentAdapter, Date>>() {
			@Override
			public TableCell<TimeSegmentAdapter, Date> call(TableColumn<TimeSegmentAdapter, Date> p) {
				TextFieldTableCell<TimeSegmentAdapter, Date> cell = new TextFieldTableCell<>();
				cell.setConverter(new DateTimeStringConverter());
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});

		//Drag'n'drop support
		timeSegments.setOnDragDetected(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Dragboard dragBoard = timeSegments.startDragAndDrop(TransferMode.MOVE);
				ClipboardContent content = new ClipboardContent();
				TimeSegmentAdapter selectedItem = timeSegments.getSelectionModel().getSelectedItem();
				if (selectedItem != null && selectedItem.descriptionProperty().get() != null)
					content.putString(selectedItem.descriptionProperty().get());
				dragBoard.setContent(content);

				event.consume();
			}
		});
		//Enable/disable Delete button
		delete.disableProperty().bind(timeSegments.getSelectionModel().selectedItemProperty().isNull());

		//Set column sizes
		//TODO: make sure this keeps working correctly
		customProperties.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		columnStart.prefWidthProperty().bind(timeSegments.widthProperty().multiply(2).divide(10));
		columnEnd.prefWidthProperty().bind(timeSegments.widthProperty().multiply(2).divide(10));
		columnDuration.prefWidthProperty().bind(timeSegments.widthProperty().multiply(2).divide(10));
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

	/**
	 * Sets the TaskManager reference
	 *
	 * @param taskManager the TaskManager reference
	 */
	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;

		updateCustomFields();
		//TODO: remove listeners if setCustomFields is called once more
		taskManager.getCustomFields().addListener(new ListChangeListener<CustomFieldAdapter>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends CustomFieldAdapter> change) {
				updateCustomFields();
			}
		});
		//Update start/stop button text
		taskManager.timingSegmentProperty().addListener(new ChangeListener<TimeSegmentAdapter>() {
			private ChangeListener<Boolean> startStopListener = new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
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
	}

	/**
	 * Sets the list of tasks to be edited
	 *
	 * @param editedTaskList the list of tasks to be edited
	 */
	public void setEditedTaskList(ObservableList<TaskAdapter> editedTaskList) {
		this.editedTaskList = editedTaskList;
		editedTaskList.addListener(new ListChangeListener<TaskAdapter>() {
			@Override
			public void onChanged(Change<? extends TaskAdapter> change) {
				if (!isIgnoreEditedTaskUpdates())
					updateEditingTasks();
			}
		});
	}

	/**
	 * Returns true if this controller is ignoring updates to edited tasks
	 *
	 * @return true if this controller is ignoring updates to edited tasks
	 */
	public boolean isIgnoreEditedTaskUpdates() {
		return ignoreEditedTaskUpdates;
	}

	/**
	 * Sets if this controller should ignore updates to edited tasks
	 *
	 * @param ignoreEditedTaskUpdates true if this controller is ignoring
	 * updates to edited
	 */
	public void setIgnoreEditedTaskUpdates(boolean ignoreEditedTaskUpdates) {
		this.ignoreEditedTaskUpdates = ignoreEditedTaskUpdates;
	}

	/**
	 * Updates the custom fields table
	 */
	private void updateCustomFields() {
		customProperties.getItems().clear();
		for (CustomFieldAdapter customFieldAdapter : taskManager.getCustomFields())
			customProperties.getItems().add(new CustomFieldValueAdapter(customFieldAdapter, taskManager));
		TaskAdapter task = getEditedTask();
		if (task != null)
			for (CustomFieldValueAdapter customFieldValueAdapter : customProperties.getItems())
				customFieldValueAdapter.setTask(task);
	}

	/**
	 * Updates the currently editing task list, re-binds form elements to new
	 * tasks
	 */
	private void updateEditingTasks() {
		if (boundTasks.containsAll(editedTaskList) && editedTaskList.containsAll(boundTasks))
			return;
		for (TaskAdapter adapter : boundTasks) {
			name.textProperty().unbindBidirectional(adapter.nameProperty());
			description.textProperty().unbindBidirectional(adapter.descriptionProperty());
			totalTime.textProperty().unbind();
		}
		boundTasks.clear();
		TaskAdapter editedTask = getEditedTask();
		if (editedTask != null) {
			name.textProperty().bindBidirectional(editedTask.nameProperty());
			description.textProperty().bindBidirectional(editedTask.descriptionProperty());
			totalTime.textProperty().bind(editedTask.totalTimeProperty());
			boundTasks.add(editedTask);
			for (CustomFieldValueAdapter customFieldValueAdapter : customProperties.getItems())
				customFieldValueAdapter.setTask(editedTask);
			timeSegments.setItems(editedTask.timeSegmentsProperty());
			updateSortOrder();
			updateStartStopText();
		} else {
			updateStartStopText();
			if (editedTaskList.size() > 1)
				log.severe(messages.getString("CAN_ONLY_EDIT_A_SINGLE_TASK_AT_A_TIME"));//TODO
		}
	}

	//TODO: add support for editing multiple tasks
	/**
	 * Returns the edited task, or null if several or no tasks are being edited
	 *
	 * @return the edited task
	 */
	protected TaskAdapter getEditedTask() {
		if (editedTaskList != null && editedTaskList.size() == 1)
			return editedTaskList.get(0);
		return null;
	}

	/**
	 * Updates the time segments sort order
	 */
	private void updateSortOrder() {
		//FIXME: Remove this after it's fixed in Java FX
		TableColumn<TimeSegmentAdapter, ?>[] sortOrder = timeSegments.getSortOrder().toArray(new TableColumn[0]);
		timeSegments.getSortOrder().clear();
		timeSegments.getSortOrder().addAll(sortOrder);
	}

	/**
	 * Returns true if the currently edited task is timing
	 *
	 * @return
	 */
	private boolean isTimingCurrentTask() {
		TaskAdapter editedTask = getEditedTask();
		if (editedTask == null || taskManager.timingSegmentProperty().get() == null)
			return false;
		return editedTask.timeSegmentsProperty().contains(taskManager.timingSegmentProperty().get());
	}

	/**
	 * Updates the Start/Stop button text based on the currently timing task
	 */
	private void updateStartStopText() {
		startStop.setDisable(boundTasks.isEmpty());
		startStop.setText(isTimingCurrentTask() ? messages.getString("STOP") : messages.getString("START"));
	}

	/*
	 * Callbacks
	 */
	/**
	 * Start/Stop button
	 */
	@FXML
	private void handleStartStop() {
		boolean startNewTaskInstead = !getEditedTask().timeSegmentsProperty().contains(taskManager.timingSegmentProperty().get());
		taskManager.stopTiming();
		if (startNewTaskInstead) {
			TaskAdapter task = getEditedTask();
			if (task != null) {
				TimeSegmentAdapter newSegmentAdapter = createTimeSegment();
				taskManager.startTiming(newSegmentAdapter);
			}
		}
		updateStartStopText();
	}

	/**
	 * Create time segment
	 *
	 * @return the created time segment
	 */
	@FXML
	private TimeSegmentAdapter createTimeSegment() {
		TaskAdapter task = getEditedTask();
		if (task != null) {
			TimeSegmentAdapter newSegmentAdapter = task.createTimeSegment();
			updateSortOrder();
			return newSegmentAdapter;
		}
		return null;
	}

	/**
	 * Delete selected time segments
	 */
	@FXML
	private void deleteTimeSegment() {
		for (TimeSegmentAdapter segment : timeSegments.getSelectionModel().getSelectedItems()) {
			taskManager.deleteSegment(segment);
			segment.ownerTaskProperty().get().updateFromDatabase();
		}
	}
}
