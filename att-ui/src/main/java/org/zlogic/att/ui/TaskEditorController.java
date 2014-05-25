/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.net.URL;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
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
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import javafx.util.converter.DateTimeStringConverter;
import javafx.util.converter.DefaultStringConverter;
import org.zlogic.att.ui.adapters.CustomFieldAdapter;
import org.zlogic.att.ui.adapters.CustomFieldValueAdapter;
import org.zlogic.att.ui.adapters.DataManager;
import org.zlogic.att.ui.adapters.DurationFormatter;
import org.zlogic.att.ui.adapters.TaskAdapter;
import org.zlogic.att.ui.adapters.TimeSegmentAdapter;

/**
 * Controller for task editor
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class TaskEditorController implements Initializable {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(TaskEditorController.class.getName());
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
	 * The item which was started to be dragged
	 */
	private TimeSegmentAdapter dragSegment;
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
	 * Custom field name column
	 */
	@FXML
	private TableColumn<CustomFieldValueAdapter, String> columnField;
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
	 * Confirmation prompt dialog controller
	 */
	private ConfirmationDialogController confirmationDialogController;

	/**
	 * Returns the drag-n-drop source TimeSegmentAdapter only if source matches
	 * the time segments table.
	 *
	 * @param source the drag-n-drop gesture source
	 * @return the drag-n-drop source TimeSegmentAdapter
	 */
	public TimeSegmentAdapter getDragSource(Object source) {
		return source == timeSegments ? dragSegment : null;
	}

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
			Duration tasksTime = Duration.ZERO;
			for (TaskAdapter taskAdapter : monitorTasks)
				tasksTime = tasksTime.plus(taskAdapter.getTask().getTotalTime());
			totalTime.setText(DurationFormatter.formatDuration(tasksTime));
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
		 *
		 * @param tasks the list to be bound
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
	 * Task list change listener
	 */
	private ListChangeListener<TaskAdapter> taskChangeListener = new ListChangeListener<TaskAdapter>() {
		private List<TaskAdapter> previousItems = new LinkedList<>();

		@Override
		public void onChanged(ListChangeListener.Change<? extends TaskAdapter> change) {
			//Check to see if items haven't changed
			if (change.getList().containsAll(previousItems) && previousItems.containsAll(change.getList()))
				return;
			//Update tasks
			previousItems.clear();
			previousItems.addAll(change.getList());
			updateEditingTasks();
		}
	};

	/**
	 * Time segment cell monitoring which blocks external updates until edit is
	 * completed
	 *
	 * @param <S> the object type
	 * @param <T> the table cell type
	 */
	private class UpdateBlockingTableCell<S, T> extends TextFieldTableCell<S, T> {

		@Override
		public void cancelEdit() {
			dataManager.editingCellsProperty().remove(this);
			super.cancelEdit();
		}

		@Override
		public void startEdit() {
			dataManager.editingCellsProperty().add(this);
			super.startEdit();
		}

		@Override
		public void commitEdit(T t) {
			dataManager.editingCellsProperty().remove(this);
			super.commitEdit(t);
		}

		public void updateItem(T item, boolean empty) {
			/*
			 if(item!=getItem() || empty!=isEmpty()){
			 TimeSegmentAdapter timeSegment = (TimeSegmentAdapter) getTableRow().getItem();
			 if(timeSegment!=null)
			 timeSegment.pauseUpdates.set(false);
			 }*/
			super.updateItem(item, empty);
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
	 * Property to store if a single task is currently selected (vs. a
	 * null-selection i the task list)
	 */
	@FXML
	private BooleanProperty editingSingleTask = new SimpleBooleanProperty(false);

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
						if (newValue != null)
							newValue.isTimingProperty().addListener(timingChangeListener);
						timingChangeListener.changed(null, oldValue != null ? oldValue.isTimingProperty().get() : false, newValue != null ? newValue.isTimingProperty().get() : false);
					}
				}.setRow(row));
				return row;
			}
		});

		//Cell editors
		columnFieldValue.setCellFactory(new Callback<TableColumn<CustomFieldValueAdapter, String>, TableCell<CustomFieldValueAdapter, String>>() {
			@Override
			public TableCell<CustomFieldValueAdapter, String> call(TableColumn<CustomFieldValueAdapter, String> p) {
				ComboBoxTableCell<CustomFieldValueAdapter, String> cell = new ComboBoxTableCell<CustomFieldValueAdapter, String>() {
					@Override
					public void cancelEdit() {
						dataManager.editingCellsProperty().remove(this);
						super.cancelEdit();
					}

					@Override
					public void startEdit() {
						dataManager.editingCellsProperty().add(this);
						super.startEdit();
					}

					@Override
					public void commitEdit(String t) {
						dataManager.editingCellsProperty().remove(this);
						super.commitEdit(t);
					}
				};
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
				cell.setOnKeyPressed(new TableCellBadShortcutsInterceptor(cell.editingProperty()));
				return cell;
			}
		});
		columnDescription.setCellFactory(new Callback<TableColumn<TimeSegmentAdapter, String>, TableCell<TimeSegmentAdapter, String>>() {
			@Override
			public TableCell<TimeSegmentAdapter, String> call(TableColumn<TimeSegmentAdapter, String> p) {
				TextFieldTableCell<TimeSegmentAdapter, String> cell = new UpdateBlockingTableCell<>();
				cell.setConverter(new DefaultStringConverter());
				cell.setOnKeyPressed(new TableCellBadShortcutsInterceptor(cell.editingProperty()));
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
				TextFieldTableCell<TimeSegmentAdapter, Date> cell = new UpdateBlockingTableCell<>();
				cell.setConverter(new DateTimeStringConverter());
				cell.setAlignment(Pos.CENTER_RIGHT);
				cell.setOnKeyPressed(new TableCellBadShortcutsInterceptor(cell.editingProperty()));
				return cell;
			}
		});
		columnEnd.setCellFactory(new Callback<TableColumn<TimeSegmentAdapter, Date>, TableCell<TimeSegmentAdapter, Date>>() {
			@Override
			public TableCell<TimeSegmentAdapter, Date> call(TableColumn<TimeSegmentAdapter, Date> p) {
				TextFieldTableCell<TimeSegmentAdapter, Date> cell = new UpdateBlockingTableCell<>();
				cell.setConverter(new DateTimeStringConverter());
				cell.setAlignment(Pos.CENTER_RIGHT);
				cell.setOnKeyPressed(new TableCellBadShortcutsInterceptor(cell.editingProperty()));
				return cell;
			}
		});

		//Avoid cursor reset while editing a task name
		name.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (dataManager == null)
					return;
				if (newValue)
					dataManager.editingCellsProperty().add(name);
				else
					dataManager.editingCellsProperty().remove(name);
			}
		});

		//Drag'n'drop support
		timeSegments.setOnDragDetected(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Dragboard dragBoard = timeSegments.startDragAndDrop(TransferMode.MOVE);
				ClipboardContent content = new ClipboardContent();
				TimeSegmentAdapter focusedItem = timeSegments.getFocusModel().getFocusedItem();
				if (focusedItem != null && focusedItem.descriptionProperty().get() != null)
					content.putString(focusedItem.descriptionProperty().get());
				dragBoard.setContent(content);

				dragSegment = focusedItem;
				dataManager.draggingTaskProperty().set(true);

				event.consume();
			}
		});
		timeSegments.setOnDragDone(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				dataManager.draggingTaskProperty().set(false);
				dragSegment = null;

				event.consume();
			}

		});
		//Enable/disable Delete button and task fields
		delete.disableProperty().bind(segmentSelectionSize.lessThanOrEqualTo(0).or(editingSingleTask.not()));
		name.editableProperty().bind(editingSingleTask);
		totalTime.disableProperty().bind(editingSingleTask.not());
		description.editableProperty().bind(editingSingleTask);
		timeSegments.disableProperty().bind(editingSingleTask.not());
		addSegment.disableProperty().bind(editingSingleTask.not());

		//Update the selection size property
		timeSegments.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TimeSegmentAdapter>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends TimeSegmentAdapter> change) {
				segmentSelectionSize.set(change.getList().size());
			}
		});

		//Set column sizing policy
		customProperties.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		timeSegments.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		//Default sort order
		timeSegments.getSortOrder().add(columnEnd);
		columnEnd.setSortType(TableColumn.SortType.DESCENDING);

		//Date comparator
		Comparator<Date> dateComparator = new Comparator<Date>() {
			@Override
			public int compare(Date o1, Date o2) {
				return o1.compareTo(o2);
			}
		};
		columnStart.setComparator(dateComparator);
		columnEnd.setComparator(dateComparator);

		//Confirmation dialog
		confirmationDialogController = ConfirmationDialogController.createInstance();
	}

	/**
	 * Sets the window icons
	 *
	 * @param icons the icons to be set
	 */
	public void setWindowIcons(ObservableList<Image> icons) {
		confirmationDialogController.setWindowIcons(icons);
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
		dataManager.addTasksUpdatedListener(new EventHandler() {
			@Override
			public void handle(Event event) {
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
		if (this.editedTaskList != null)
			this.editedTaskList.removeListener(taskChangeListener);
		this.editedTaskList = editedTaskList;
		if (!ignoreEditedTaskUpdates)
			editedTaskList.addListener(taskChangeListener);
	}

	/**
	 * Sets if this controller should ignore updates to edited tasks
	 *
	 * @param ignoreEditedTaskUpdates true if this controller is ignoring
	 * updates to edited
	 */
	public void setIgnoreEditedTaskUpdates(boolean ignoreEditedTaskUpdates) {
		this.ignoreEditedTaskUpdates = ignoreEditedTaskUpdates;
		if (ignoreEditedTaskUpdates)
			editedTaskList.removeListener(taskChangeListener);
		else
			editedTaskList.addListener(taskChangeListener);
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
				customFieldValueAdapter.setTasks(editedTaskList);
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
		}
		boundTasks.clear();
		dragSegment = null;
		TaskAdapter editedTask = getEditedTask();
		editingSingleTask.set(editedTask != null);
		customProperties.setDisable(!editingSingleTask.get());
		if (editingSingleTask.get()) {
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

			String defaultFieldString;
			if (editedTaskList.size() > 1) {
				for (TaskAdapter task : editedTaskList) {
					extractedSegments.addAll(task.timeSegmentsProperty());
				}
				timeChangeListener.bind(editedTaskList);
				timeChangeListener.changed(null, null, null);
				for (CustomFieldValueAdapter customFieldValueAdapter : customProperties.getItems())
					customFieldValueAdapter.setTasks(editedTaskList);
				customProperties.setDisable(false);

				defaultFieldString = messages.getString("MULTIPLE_TASKS_SELECTED");
			} else {
				for (CustomFieldValueAdapter customFieldValueAdapter : customProperties.getItems())
					customFieldValueAdapter.setTasks(null);
				timeChangeListener.unbind();

				defaultFieldString = ""; //NOI18N
			}
			name.setText(defaultFieldString);
			description.setText(defaultFieldString);
			totalTime.setText(defaultFieldString);
		}
		updateSortOrder();
	}

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
	 * Returns the list of currently selected time segments
	 *
	 * @return the list of currently selected time segments
	 */
	protected ObservableList<TimeSegmentAdapter> getSelectedTimeSegments() {
		return timeSegments.getSelectionModel().getSelectedItems();
	}

	/**
	 * Updates the time segments sort order
	 */
	private void updateSortOrder() {
		if (dataManager.pauseUpdatesProperty().get()) {
			log.log(Level.SEVERE, messages.getString("CANCELLING_INCORRECT_UPDATESORTORDER"), "pauseUpdatesProperty"); //NOI18N
			return;
		} else if (timeSegments.getEditingCell() != null && timeSegments.getEditingCell().getColumn() != -1 && timeSegments.getEditingCell().getRow() != -1) {
			log.log(Level.SEVERE, messages.getString("CANCELLING_INCORRECT_UPDATESORTORDER"), "editingCellProperty"); //NOI18N
			return;
		}
		TimeSegmentAdapter focusedAdapter = timeSegments.getFocusModel().getFocusedItem();
		TableColumn focusedColumn = timeSegments.getFocusModel().getFocusedCell().getTableColumn();
		timeSegments.getSortPolicy().call(timeSegments);
		//Restore lost focus
		if (focusedAdapter != null && timeSegments.getFocusModel().getFocusedItem() == null)
			timeSegments.getFocusModel().focus(timeSegments.getItems().indexOf(focusedAdapter), focusedColumn);
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
			return newSegmentAdapter;
		}
		return null;
	}

	/**
	 * Delete selected time segments
	 */
	@FXML
	private void deleteTimeSegment() {
		StringBuilder tasksToDelete = new StringBuilder();
		List<TimeSegmentAdapter> segmentsToDeleteList = new LinkedList(timeSegments.getSelectionModel().getSelectedItems());
		for (TimeSegmentAdapter segmentToDelete : segmentsToDeleteList)
			tasksToDelete.append(tasksToDelete.length() > 0 ? "\n" : "").append(segmentToDelete.descriptionProperty().get()); //NOI18N
		ConfirmationDialogController.Result result = confirmationDialogController.showDialog(
				messages.getString("CONFIRM_TIME_SEGMENT_DELETION"),
				MessageFormat.format(messages.getString("ARE_YOU_SURE_YOU_WANT_TO_DELETE_THE_FOLLOWING_TIME_SEGMENTS"), tasksToDelete)
		);
		if (result != ConfirmationDialogController.Result.CONFIRMED)
			return;
		for (TimeSegmentAdapter segment : segmentsToDeleteList) {
			dataManager.deleteSegment(segment);
			segment.ownerTaskProperty().get().updateFromDatabase();
		}
		if (editedTaskList.size() > 1)
			updateEditingTasks();
	}
}
