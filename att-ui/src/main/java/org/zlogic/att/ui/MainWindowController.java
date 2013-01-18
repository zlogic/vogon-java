/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DateTimeStringConverter;
import javafx.util.converter.DefaultStringConverter;
import org.zlogic.att.data.converters.GrindstoneImporter;
import org.zlogic.att.data.converters.Importer;
import org.zlogic.att.ui.adapters.CustomFieldAdapter;
import org.zlogic.att.ui.adapters.CustomFieldValueAdapter;
import org.zlogic.att.ui.adapters.TaskAdapter;
import org.zlogic.att.ui.adapters.TaskManager;
import org.zlogic.att.ui.adapters.TimeSegmentAdapter;

/**
 * Controller for the main window
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class MainWindowController implements Initializable {

	private final static Logger log = Logger.getLogger(MainWindowController.class.getName());
	private TaskManager taskManager;
	private File lastDirectory;
	/**
	 * Easy access to preference storage
	 */
	protected java.util.prefs.Preferences preferenceStorage = java.util.prefs.Preferences.userNodeForPackage(Launcher.class);
	/**
	 * The background task thread (used for synchronization & clean termination)
	 */
	protected Thread backgroundThread;
	/**
	 * The background task
	 */
	protected Task<Void> backgroundTask;
	private Runnable shutdownProcedure;
	private Stage customFieldEditorStage;
	private CustomFieldEditorController customFieldEditorController;
	@FXML
	private VBox rootPane;
	@FXML
	private TaskEditorController taskEditorController;
	@FXML
	private TableView<TaskAdapter> taskList;
	@FXML
	private TableColumn<TaskAdapter, String> columnTaskName;
	@FXML
	private TableColumn<TaskAdapter, String> columnTotalTime;
	@FXML
	private TableColumn<TaskAdapter, Date> columnFirstTime;
	@FXML
	private TableColumn<TaskAdapter, Date> columnLastTime;
	@FXML
	private TableColumn<TaskAdapter, Boolean> columnTaskCompleted;
	@FXML
	private Label activeTaskLabel;
	@FXML
	private Button duplicateTaskButton;
	@FXML
	private Button deleteTaskButton;
	@FXML
	private HBox activeTaskPane;
	@FXML
	private HBox statusPane;
	@FXML
	private ProgressIndicator progressIndicator;
	@FXML
	private Label progressLabel;
	@FXML
	private MenuItem menuItemCleanupDB;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		//Default sort order
		taskList.getSortOrder().add(columnLastTime);
		columnLastTime.setSortType(TableColumn.SortType.DESCENDING);
		//Task comparator
		Comparator<Date> TaskComparator = new Comparator<Date>() {
			@Override
			public int compare(Date o1, Date o2) {
				if (o1 == null && o2 != null)
					return 1;
				if (o1 != null && o2 == null)
					return -1;
				if (o1 == null && o2 == null)
					return 0;
				return o1.compareTo(o2);
			}
		};
		columnLastTime.setComparator(TaskComparator);
		//Create the task manager
		taskManager = new TaskManager();
		taskList.setItems(taskManager.tasksProperty());
		reloadTasks();

		//Auto update sort order
		taskManager.taskUpdatedProperty().addListener(new ChangeListener<Date>() {
			@Override
			public void changed(ObservableValue<? extends Date> ov, Date oldValue, Date newValue) {
				updateSortOrder();
			}
		});

		taskList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		//Bind the current task panel
		activeTaskPane.managedProperty().bind(activeTaskPane.visibleProperty());
		activeTaskPane.visibleProperty().bind(taskManager.timingSegmentProperty().isNotNull());
		taskManager.timingSegmentProperty().addListener(new ChangeListener<TimeSegmentAdapter>() {
			private ChangeListener<TaskAdapter> taskChangedListener = new ChangeListener<TaskAdapter>() {
				@Override
				public void changed(ObservableValue<? extends TaskAdapter> ov, TaskAdapter oldValue, TaskAdapter newValue) {
					activeTaskLabel.textProperty().unbind();
					activeTaskLabel.textProperty().bind(newValue.nameProperty());
				}
			};

			@Override
			public void changed(ObservableValue<? extends TimeSegmentAdapter> ov, TimeSegmentAdapter oldValue, TimeSegmentAdapter newValue) {
				if (newValue != null && newValue.equals(oldValue))
					return;
				if (oldValue != null)
					oldValue.ownerTaskProperty().removeListener(taskChangedListener);
				if (newValue != null) {
					activeTaskLabel.textProperty().unbind();
					activeTaskLabel.textProperty().bind(newValue.ownerTaskProperty().get().nameProperty());
					newValue.ownerTaskProperty().addListener(taskChangedListener);
				}
			}
		});

		deleteTaskButton.disableProperty().bind(taskList.getSelectionModel().selectedItemProperty().isNull());
		duplicateTaskButton.disableProperty().bind(taskList.getSelectionModel().selectedItemProperty().isNull());
		//Restore settings
		lastDirectory = preferenceStorage.get("lastDirectory", null) == null ? null : new File(preferenceStorage.get("lastDirectory", null)); //NOI18N
		//Row properties
		taskList.setRowFactory(new Callback<TableView<TaskAdapter>, TableRow<TaskAdapter>>() {
			@Override
			public TableRow<TaskAdapter> call(TableView<TaskAdapter> p) {
				TableRow<TaskAdapter> row = new TableRow<>();
				row.itemProperty().addListener(new ChangeListener<TaskAdapter>() {
					private TableRow<TaskAdapter> row;
					private ChangeListener timingChangeListener = new ChangeListener<Boolean>() {
						@Override
						public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
							if (newValue != null) {
								if (newValue)
									row.getStyleClass().add("timing-segment");
								else
									row.getStyleClass().remove("timing-segment");
							}
						}
					};

					public ChangeListener<TaskAdapter> setRow(TableRow<TaskAdapter> row) {
						this.row = row;
						return this;
					}

					@Override
					public void changed(ObservableValue<? extends TaskAdapter> ov, TaskAdapter oldValue, TaskAdapter newValue) {
						if (oldValue != null)
							oldValue.isTimingProperty().removeListener(timingChangeListener);
						if (newValue != null) {
							newValue.isTimingProperty().addListener(timingChangeListener);
							timingChangeListener.changed(newValue.isTimingProperty(), oldValue != null ? oldValue.isTimingProperty().get() : false, newValue.isTimingProperty().get());
						}
					}
				}.setRow(row));
				//Drag'n'drop support
				//TODO: create a separate class
				row.setOnDragEntered(new EventHandler<DragEvent>() {
					private TableRow<TaskAdapter> row;

					public EventHandler<DragEvent> setRow(TableRow<TaskAdapter> row) {
						this.row = row;
						return this;
					}

					@Override
					public void handle(DragEvent event) {
						if (event.getGestureSource() instanceof TableView && ((TableView) event.getGestureSource()).getSelectionModel().getSelectedItem() instanceof TimeSegmentAdapter)
							row.getStyleClass().add("drag-accept-task");
						event.consume();
					}
				}.setRow(row));
				row.setOnDragExited(new EventHandler<DragEvent>() {
					private TableRow<TaskAdapter> row;

					public EventHandler<DragEvent> setRow(TableRow<TaskAdapter> row) {
						this.row = row;
						return this;
					}

					@Override
					public void handle(DragEvent event) {
						row.getStyleClass().remove("drag-accept-task");
						event.consume();
					}
				}.setRow(row));
				row.setOnDragOver(new EventHandler<DragEvent>() {
					@Override
					public void handle(DragEvent event) {
						if (event.getGestureSource() instanceof TableView && ((TableView) event.getGestureSource()).getSelectionModel().getSelectedItem() instanceof TimeSegmentAdapter)
							event.acceptTransferModes(TransferMode.MOVE);
						event.consume();
					}
				});
				row.setOnDragDropped(new EventHandler<DragEvent>() {
					private TableRow<TaskAdapter> row;

					public EventHandler<DragEvent> setRow(TableRow<TaskAdapter> row) {
						this.row = row;
						return this;
					}

					@Override
					public void handle(DragEvent event) {
						boolean success = false;
						if (event.getGestureSource() instanceof TableView) {
							Object selectedItem = ((TableView) event.getGestureSource()).getSelectionModel().getSelectedItem();
							if (selectedItem instanceof TimeSegmentAdapter) {
								event.setDropCompleted(success);
								((TimeSegmentAdapter) selectedItem).ownerTaskProperty().set(row.getItem());
							}
						}
						event.setDropCompleted(success);
						event.consume();
					}
				}.setRow(row));
				return row;
			}
		;
		});
		//Cell editors
		columnTaskName.setCellFactory(new Callback<TableColumn<TaskAdapter, String>, TableCell<TaskAdapter, String>>() {
			@Override
			public TableCell<TaskAdapter, String> call(TableColumn<TaskAdapter, String> p) {
				TextFieldTableCell<TaskAdapter, String> cell = new TextFieldTableCell<>();
				cell.setConverter(new DefaultStringConverter());
				return cell;
			}
		});
		columnTaskCompleted.setCellFactory(new Callback<TableColumn<TaskAdapter, Boolean>, TableCell<TaskAdapter, Boolean>>() {
			@Override
			public TableCell<TaskAdapter, Boolean> call(TableColumn<TaskAdapter, Boolean> taskAdapterBooleanTableColumn) {
				CheckBoxTableCell<TaskAdapter, Boolean> cell = new CheckBoxTableCell<>();
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
		columnTotalTime.setCellFactory(new Callback<TableColumn<TaskAdapter, String>, TableCell<TaskAdapter, String>>() {
			@Override
			public TableCell<TaskAdapter, String> call(TableColumn<TaskAdapter, String> p) {
				TextFieldTableCell<TaskAdapter, String> cell = new TextFieldTableCell<>();
				cell.setConverter(new DefaultStringConverter());
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});
		columnFirstTime.setCellFactory(new Callback<TableColumn<TaskAdapter, Date>, TableCell<TaskAdapter, Date>>() {
			@Override
			public TableCell<TaskAdapter, Date> call(TableColumn<TaskAdapter, Date> p) {
				TextFieldTableCell<TaskAdapter, Date> cell = new TextFieldTableCell<>();
				cell.setConverter(new DateTimeStringConverter());
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});
		columnLastTime.setCellFactory(new Callback<TableColumn<TaskAdapter, Date>, TableCell<TaskAdapter, Date>>() {
			@Override
			public TableCell<TaskAdapter, Date> call(TableColumn<TaskAdapter, Date> p) {
				TextFieldTableCell<TaskAdapter, Date> cell = new TextFieldTableCell<>();
				cell.setConverter(new DateTimeStringConverter());
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});

		//Set column sizes
		//TODO: make sure this keeps working correctly
		//taskList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		columnTaskName.prefWidthProperty().bind(taskList.widthProperty().multiply(10).divide(20));
		columnTotalTime.prefWidthProperty().bind(taskList.widthProperty().multiply(2).divide(20));
		columnFirstTime.prefWidthProperty().bind(taskList.widthProperty().multiply(3).divide(20));
		columnLastTime.prefWidthProperty().bind(taskList.widthProperty().multiply(3).divide(20));
		columnTaskCompleted.prefWidthProperty().bind(taskList.widthProperty().multiply(2).divide(20).subtract(15));

		//Menu items and status pane
		menuItemCleanupDB.disableProperty().bind(statusPane.visibleProperty());
		statusPane.managedProperty().bind(statusPane.visibleProperty());
		statusPane.setVisible(false);

		//Load other windows
		loadWindowCustomFieldEditor();
		taskEditorController.setTaskManager(taskManager);
	}

	public void setShutdownProcedure(Runnable shutdownProcedure) {
		this.shutdownProcedure = shutdownProcedure;
	}

	private void loadWindowCustomFieldEditor() {
		//Load FXML
		customFieldEditorStage = new Stage();
		customFieldEditorStage.initModality(Modality.NONE);
		Parent root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomFieldEditor.fxml")); //NOI18N
		loader.setLocation(getClass().getResource("CustomFieldEditor.fxml")); //NOI18N
		try {
			root = (Parent) loader.load();
		} catch (IOException ex) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error loading FXML", ex);
		}
		//Initialize the scene properties
		if (root != null) {
			Scene scene = new Scene(root);
			customFieldEditorStage.setTitle("Custom field editor");
			customFieldEditorStage.setScene(scene);
			//((CustomFieldEditorController) loader.getController()).messageText.setText(message);
		}
		//Set the task manager
		customFieldEditorController = loader.getController();
		customFieldEditorController.setTaskManager(taskManager);
	}

	protected void reloadTasks() {
		taskManager.reloadTasks();
		taskEditorController.setEditedTaskList(taskList.getSelectionModel().getSelectedItems());
		updateSortOrder();
	}

	private void updateSortOrder() {
		//FIXME: Remove this after it's fixed in Java FX
		//TODO: call this on task updates?
		if (taskList.getEditingCell() != null && taskList.getEditingCell().getRow() >= 0)
			return;
		TableColumn<TaskAdapter, ?>[] sortOrder = taskList.getSortOrder().toArray(new TableColumn[0]);
		taskEditorController.setIgnoreEditedTaskUpdates(true);
		taskList.getSortOrder().clear();
		taskList.getSortOrder().addAll(sortOrder);
		taskEditorController.setIgnoreEditedTaskUpdates(false);
	}

	/*
	 * Background task processing
	 */
	/**
	 * Starts the background task (disables task-related components, show the
	 * progress pane)
	 */
	private void beginBackgroundTask() {
		statusPane.setVisible(true);
	}

	/**
	 * Ends the background task (enables task-related components, hides the
	 * progress pane)
	 */
	private void endBackgroundTask() {
		statusPane.setVisible(false);
	}

	/**
	 * Starts a task in a background thread
	 *
	 * @param task the task to be started
	 */
	protected void startTaskThread(Task<Void> task) {
		synchronized (this) {
			//Wait for previous task to compete
			completeTaskThread();
			backgroundTask = task;

			progressIndicator.progressProperty().bind(task.progressProperty());
			progressLabel.textProperty().bind(task.messageProperty());

			//Automatically run beginTask/endTask before the actual task is processed
			backgroundThread = new Thread(
					new Runnable() {
						protected Task<Void> task;

						public Runnable setTask(Task<Void> task) {
							this.task = task;
							return this;
						}

						@Override
						public void run() {
							beginBackgroundTask();
							task.run();
							endBackgroundTask();
						}
					}.setTask(task));
			backgroundThread.setDaemon(true);
			backgroundThread.start();
		}
	}

	/**
	 * Waits for the current task to complete
	 */
	protected void completeTaskThread() {
		synchronized (this) {
			if (backgroundThread != null) {
				try {
					backgroundThread.join();
					backgroundThread = null;
				} catch (InterruptedException ex) {
					Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			if (backgroundTask != null) {
				progressIndicator.progressProperty().unbind();
				progressLabel.textProperty().unbind();
				backgroundTask = null;
			}
		}
	}

	/*
	 * Callbacks
	 */
	@FXML
	private void createNewTask() {
		TaskAdapter newTask = taskManager.createTask();
		taskList.getSelectionModel().clearSelection();
		updateSortOrder();
		taskList.getSelectionModel().select(newTask);
	}

	@FXML
	private void deleteSelectedTasks() {
		for (TaskAdapter selectedTask : taskList.getSelectionModel().getSelectedItems()) {
			taskManager.deleteTask(selectedTask);
		}
		updateSortOrder();
	}

	@FXML
	private void duplicateSelectedTasks() {
		for (TaskAdapter selectedTask : taskList.getSelectionModel().getSelectedItems()) {
			TaskAdapter newTask = taskManager.createTask();
			newTask.nameProperty().set(selectedTask.nameProperty().get());
			newTask.descriptionProperty().set(selectedTask.descriptionProperty().get());
			for (CustomFieldAdapter customField : taskManager.getCustomFields()) {
				CustomFieldValueAdapter customFieldValue = new CustomFieldValueAdapter(customField, taskManager);
				customFieldValue.setTask(newTask);
				customFieldValue.valueProperty().set(selectedTask.getTask().getCustomField(customField.getCustomField()));
			}
		}
		updateSortOrder();
	}

	@FXML
	private void showCustomFieldEditor() {
		customFieldEditorStage.showAndWait();
	}

	@FXML
	private void exit() {
		if (shutdownProcedure != null)
			shutdownProcedure.run();
	}

	@FXML
	private void importGrindstoneData() {
		// Prepare file chooser dialog
		FileChooser fileChooser = new FileChooser();
		if (lastDirectory != null && lastDirectory.exists())
			fileChooser.setInitialDirectory(lastDirectory);
		fileChooser.setTitle("Choose file to import");
		//Prepare file chooser filter
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Exported Grindstone XML files", "*.xml"));

		//Show the dialog
		File selectedFile;
		if ((selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow())) != null) {
			lastDirectory = selectedFile.isDirectory() ? selectedFile : selectedFile.getParentFile();
			preferenceStorage.put("lastDirectory", lastDirectory.toString());

			//Choose the importer based on the file extension
			Importer importer = null;
			String extension = selectedFile.isFile() ? selectedFile.getName().substring(selectedFile.getName().lastIndexOf(".")) : null; //NOI18N
			if (extension.equals(".xml")) {
				log.fine("Extension matched");
				importer = new GrindstoneImporter(selectedFile);
			}
			//Import data
			if (importer != null)
				taskManager.getPersistenceHelper().importData(importer);
			else
				log.fine("Extension not recognized");
			reloadTasks();
		}
	}

	@FXML
	private void stopTimingTask() {
		TimeSegmentAdapter segment = taskManager.timingSegmentProperty().get();
		if (segment != null)
			segment.stopTiming();
	}

	@FXML
	private void cleanupDB() {
		//Prepare the task
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				updateMessage("Cleaning up DB");
				updateProgress(-1, 1);

				taskManager.getPersistenceHelper().cleanupDB();

				updateProgress(1, 1);
				updateMessage("");
				return null;
			}
		};
		//Run the task
		startTaskThread(task);
	}
}
