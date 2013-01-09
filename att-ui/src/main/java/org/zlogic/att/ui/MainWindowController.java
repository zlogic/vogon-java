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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DateTimeStringConverter;
import javafx.util.converter.DefaultStringConverter;
import org.zlogic.att.data.converters.GrindstoneImporter;
import org.zlogic.att.data.converters.Importer;
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
	 private TableColumn<TaskAdapter,Date> columnFirstTime;
	 @FXML
	 private TableColumn<TaskAdapter, Date> columnLastTime;
	@FXML
	private TableColumn<TaskAdapter, Boolean> columnTaskCompleted;
	@FXML
	private Label activeTaskLabel;
	@FXML
	private Button deleteTaskButton;
	@FXML
	private HBox activeTaskPane;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		//Create the task manager
		taskManager = new TaskManager(taskList.getItems());
		reloadTasks();

		taskList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		//Bind the current task panel
		activeTaskPane.managedProperty().bind(activeTaskPane.visibleProperty());
		activeTaskPane.visibleProperty().bind(taskEditorController.timingSegmentProperty().isNotNull());
		taskEditorController.timingSegmentProperty().addListener(new ChangeListener<TimeSegmentAdapter>() {
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
		//Restore settings
		lastDirectory = preferenceStorage.get("lastDirectory", null) == null ? null : new File(preferenceStorage.get("lastDirectory", null)); //NOI18N
		//Cell editors
		taskList.setRowFactory(new Callback<TableView<TaskAdapter>, TableRow<TaskAdapter>>(){

			@Override
			public TableRow<TaskAdapter> call(TableView<TaskAdapter> p) {
				TableRow<TaskAdapter> row = new TableRow<>();
				row.itemProperty().addListener(new ChangeListener<TaskAdapter>() {
					private TableRow<TaskAdapter> row;
					public ChangeListener<TaskAdapter> setRow(TableRow<TaskAdapter> row){
						this.row = row;
						return this;
					}
					@Override
					public void changed(ObservableValue<? extends TaskAdapter> ov, TaskAdapter t, TaskAdapter t1) {
						//TODO: Set the row background based on the timing property
						//if(t1!=null)
						//row.styleProperty().set(t1.nameProperty().get().equals("Support Alcatel 5529OAD MVP reconnection") ?"-fx-background-color: cornsilk; ":"");
					}
				}.setRow(row));
				return row;
			}
		});
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
				return new CheckBoxTableCell<>();
			}
		});
		columnFirstTime.setCellFactory(new Callback<TableColumn<TaskAdapter, Date>, TableCell<TaskAdapter, Date>>() {
			@Override
			public TableCell<TaskAdapter, Date> call(TableColumn<TaskAdapter, Date> p) {
				TextFieldTableCell<TaskAdapter, Date> cell = new TextFieldTableCell<>();
				cell.setConverter(new DateTimeStringConverter());
				return cell;
			}
		});
		columnTotalTime.setCellFactory(new Callback<TableColumn<TaskAdapter, String>, TableCell<TaskAdapter, String>>() {
			@Override
			public TableCell<TaskAdapter, String> call(TableColumn<TaskAdapter, String> p) {
				TextFieldTableCell<TaskAdapter, String> cell = new TextFieldTableCell<>();
				cell.setConverter(new DefaultStringConverter());
				return cell;
			}
		});
		columnLastTime.setCellFactory(new Callback<TableColumn<TaskAdapter, Date>, TableCell<TaskAdapter, Date>>() {
			@Override
			public TableCell<TaskAdapter, Date> call(TableColumn<TaskAdapter, Date> p) {
				TextFieldTableCell<TaskAdapter, Date> cell = new TextFieldTableCell<>();
				cell.setConverter(new DateTimeStringConverter());
				return cell;
			}
		});
		//Set column sizes
		//TODO: make sure this keeps working correctly
		//taskList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		columnTaskName.prefWidthProperty().bind(taskList.widthProperty().multiply(5).divide(10));
		columnTotalTime.prefWidthProperty().bind(taskList.widthProperty().multiply(2).divide(10));
		columnFirstTime.prefWidthProperty().bind(taskList.widthProperty().multiply(1).divide(10));
		columnLastTime.prefWidthProperty().bind(taskList.widthProperty().multiply(1).divide(10));
		columnTaskCompleted.prefWidthProperty().bind(taskList.widthProperty().multiply(1).divide(10).subtract(15));

		//Default sort order
		taskList.getSortOrder().add(columnLastTime);
		columnLastTime.setSortType(TableColumn.SortType.DESCENDING);
		//Task comparator
		Comparator<Date> TaskComparator = new Comparator<Date>() {
			@Override
			public int compare(Date o1, Date o2) {
				return o1.compareTo(o2);
			}
		};
		columnLastTime.setComparator(TaskComparator);
		
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
		//Set the custom fields list reference
		taskEditorController.setCustomFields(customFieldEditorController.getCustomFields());
	}

	protected void reloadTasks() {
		taskManager.reloadTasks();
		taskEditorController.setEditedTaskList(taskList.getSelectionModel().getSelectedItems());
		updateSortOrder();
	}

	protected void reloadCustomFields() {
		customFieldEditorController.reloadCustomFields();
	}
	
	private void updateSortOrder() {
		//TODO: Remove this after it's fixed in Java FX
		//TODO: call this on task updates?
		TableColumn<TaskAdapter, ?>[] sortOrder = taskList.getSortOrder().toArray(new TableColumn[0]);
		taskList.getSortOrder().clear();
		taskList.getSortOrder().addAll(sortOrder);
	}

	/*
	 * Callbacks
	 */
	@FXML
	private void createNewTask() {
		TaskAdapter newTask = new TaskAdapter(taskManager.getPersistenceHelper().createTask(), taskManager);
		taskList.getItems().add(newTask);
		taskList.getSelectionModel().select(newTask);
		updateSortOrder();
	}

	@FXML
	private void deleteSelectedTasks() {
		for (TaskAdapter selectedTask : taskList.getSelectionModel().getSelectedItems()) {
			taskManager.deleteTask(selectedTask);
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
			reloadCustomFields();
		}
	}

	@FXML
	public void stopTimingTask() {
		TimeSegmentAdapter segment = taskEditorController.timingSegmentProperty().get();
		if (segment != null)
			segment.stopTiming();
	}
}
