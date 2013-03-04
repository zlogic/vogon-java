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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import javafx.util.converter.DateTimeStringConverter;
import javafx.util.converter.DefaultStringConverter;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatterBuilder;
import org.zlogic.att.ui.adapters.CustomFieldAdapter;
import org.zlogic.att.ui.adapters.CustomFieldValueAdapter;
import org.zlogic.att.ui.adapters.DataManager;
import org.zlogic.att.ui.adapters.TaskAdapter;
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
	 * DataManager reference
	 */
	private DataManager dataManager;
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
	private ToggleButton startStop;
	/**
	 * Add segment button
	 */
	@FXML
	private Button addSegment;
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
	 * Listener for monitoring the task time property and updating the total.
	 * Used when multiple tasks are selected to update the total time.
	 */
	private class TimeChangeListener implements ChangeListener<String> {

		/**
		 * List of tasks monitored for changes
		 */
		private List<TaskAdapter> monitorTasks = new LinkedList();

		/**
		 * Updates the totalTime text when a task's totalTime property change is
		 * detected
		 *
		 * @param ov the totalTime ObservableValue (not used)
		 * @param oldValue the totalTime old String value (not used)
		 * @param newValue the totalTime new String value (not used)
		 */
		@Override
		public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
			Period tasksTime = new Period();
			for (TaskAdapter taskAdapter : monitorTasks)
				tasksTime = tasksTime.plus(taskAdapter.getTask().getTotalTime());
			totalTime.setText(tasksTime.toString(new PeriodFormatterBuilder().printZeroIfSupported().appendHours().appendSeparator(":").minimumPrintedDigits(2).appendMinutes().appendSeparator(":").appendSeconds().toFormatter()));
		}

		/**
		 * Unbinds this class from all monitored TaskAdapters
		 */
		public void unbind() {
			for (TaskAdapter taskAdapter : monitorTasks)
				taskAdapter.totalTimeProperty().removeListener(this);
		}

		/**
		 * Binds this class to a list of TaskAdapters. If a new list is
		 * supplied, re-binds to the new list.
		 */
		public void bind(List<TaskAdapter> tasks) {
			for (TaskAdapter existingTask : monitorTasks)
				if (!tasks.contains(existingTask))
					existingTask.totalTimeProperty().removeListener(this);

			for (TaskAdapter newTask : tasks)
				if (!monitorTasks.contains(newTask))
					newTask.totalTimeProperty().addListener(this);

			monitorTasks.clear();
			monitorTasks.addAll(tasks);
		}
	};
	/**
	 * Default TimeChangeListener instance
	 */
	private TimeChangeListener timeChangeListener = new TimeChangeListener();
	/**
	 * Property to store the number of selected segments
	 */
	@FXML
	private IntegerProperty segmentSelectionSize = new SimpleIntegerProperty(0);

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
							ObservableList<String> customFieldValues = dataManager.getFilteredCustomFieldValues(((CustomFieldValueAdapter) newValue.getItem()).getCustomField());
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
						if (newValue != null && cell.getTableRow().getItem() instanceof CustomFieldValueAdapter) {
							ObservableList<String> customFieldValues = dataManager.getFilteredCustomFieldValues(((CustomFieldValueAdapter) cell.getTableRow().getItem()).getCustomField());
							cell.getItems().setAll(customFieldValues != null ? customFieldValues : new LinkedList<String>());
						}
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
		delete.disableProperty().bind(segmentSelectionSize.lessThanOrEqualTo(0));

		//Update the selection size property
		timeSegments.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TimeSegmentAdapter>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends TimeSegmentAdapter> change) {
				segmentSelectionSize.set(change.getList().size());
			}
		});

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
	 * Sets the DataManager reference
	 *
	 * @param dataManager the DataManager reference
	 */
	public void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;

		updateCustomFields();
		//TODO: remove listeners if setCustomFields is called once more
		this.dataManager.getCustomFields().addListener(new ListChangeListener<CustomFieldAdapter>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends CustomFieldAdapter> change) {
				updateCustomFields();
			}
		});
		//Update start/stop button text
		this.dataManager.timingSegmentProperty().addListener(new ChangeListener<TimeSegmentAdapter>() {
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
				else
					updateStartStopText();
			}
		});

		//Auto update sort order
		dataManager.taskUpdatedProperty().addListener(new ChangeListener<Date>() {
			@Override
			public void changed(ObservableValue<? extends Date> ov, Date oldValue, Date newValue) {
				updateSortOrder();
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
		for (CustomFieldAdapter customFieldAdapter : dataManager.getCustomFields())
			customProperties.getItems().add(new CustomFieldValueAdapter(customFieldAdapter, dataManager));
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
		for (TaskAdapter taskAdapter : boundTasks) {
			name.textProperty().unbindBidirectional(taskAdapter.nameProperty());
			description.textProperty().unbindBidirectional(taskAdapter.descriptionProperty());
			totalTime.textProperty().unbind();
			name.setText(messages.getString("MULTIPLE_TASKS_SELECTED"));
			description.setText(messages.getString("MULTIPLE_TASKS_SELECTED"));
			totalTime.setText(messages.getString("MULTIPLE_TASKS_SELECTED"));
		}
		boundTasks.clear();
		TaskAdapter editedTask = getEditedTask();
		boolean editingSingleTask = editedTask != null;
		name.setEditable(editingSingleTask);
		description.setEditable(editingSingleTask);
		addSegment.setDisable(!editingSingleTask);
		customProperties.setDisable(!editingSingleTask);
		if (editingSingleTask) {
			timeChangeListener.unbind();
			name.textProperty().bindBidirectional(editedTask.nameProperty());
			description.textProperty().bindBidirectional(editedTask.descriptionProperty());
			totalTime.textProperty().bind(editedTask.totalTimeProperty());
			boundTasks.add(editedTask);
			for (CustomFieldValueAdapter customFieldValueAdapter : customProperties.getItems())
				customFieldValueAdapter.setTask(editedTask);
			timeSegments.setItems(editedTask.timeSegmentsProperty());
			updateStartStopText();
		} else {
			updateStartStopText();
			//Set items manually instead of binding with task
			ObservableList<TimeSegmentAdapter> extractedSegments = FXCollections.observableList(new LinkedList<TimeSegmentAdapter>());
			timeSegments.setItems(extractedSegments);
			if (editedTaskList.size() > 1) {
				for (TaskAdapter task : editedTaskList) {
					extractedSegments.addAll(task.timeSegmentsProperty());
				}
				timeChangeListener.bind(editedTaskList);
				timeChangeListener.changed(null, null, null);
			} else
				timeChangeListener.unbind();
		}
		updateSortOrder();
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
		//TODO: call this on task updates?
		if (timeSegments.getEditingCell() != null && timeSegments.getEditingCell().getRow() >= 0)
			return;
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
		if (editedTask == null || dataManager.timingSegmentProperty().get() == null)
			return false;
		return editedTask.timeSegmentsProperty().contains(dataManager.timingSegmentProperty().get());
	}

	/**
	 * Updates the Start/Stop button text based on the currently timing task
	 */
	private void updateStartStopText() {
		startStop.setDisable(boundTasks.isEmpty());
		startStop.setSelected(isTimingCurrentTask());
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
		boolean startNewTaskInstead = !getEditedTask().timeSegmentsProperty().contains(dataManager.timingSegmentProperty().get());
		dataManager.stopTiming();
		if (startNewTaskInstead) {
			TaskAdapter task = getEditedTask();
			if (task != null) {
				TimeSegmentAdapter newSegmentAdapter = createTimeSegment();
				dataManager.startTiming(newSegmentAdapter);
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
			dataManager.deleteSegment(segment);
			segment.ownerTaskProperty().get().updateFromDatabase();
		}
		if (editedTaskList.size() > 1)
			updateEditingTasks();
	}
}
