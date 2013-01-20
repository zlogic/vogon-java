/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.util.converter.DateStringConverter;
import net.sf.dynamicreports.report.exception.DRException;
import org.zlogic.att.data.reporting.DateTools;
import org.zlogic.att.ui.adapters.TaskManager;
import org.zlogic.att.ui.report.Report;

/**
 * Controller for the report window
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class ReportController implements Initializable {

	private TaskManager taskManager;
	private ObjectProperty<File> lastDirectory;
	@FXML
	private WebView viewer;
	@FXML
	private TextField endDate;
	@FXML
	private TextField startDate;
	@FXML
	private HBox statusPane;
	@FXML
	private ProgressIndicator progressIndicator;
	@FXML
	private Label progressLabel;
	@FXML
	private Button buildReportButton;
	@FXML
	private HBox previewReportPane;
	@FXML
	private HBox savePane;
	@FXML
	private Button saveReportButton;
	private ObjectProperty<Date> startDateValue = new SimpleObjectProperty<>();
	private ObjectProperty<Date> endDateValue = new SimpleObjectProperty<>();
	private ObjectProperty<Thread> reportTaskThread = new SimpleObjectProperty<>();
	private ObjectProperty<Report> generatedReport = new SimpleObjectProperty<>();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		//Configure date fields
		startDate.textProperty().bindBidirectional(startDateValue, new DateStringConverter());
		endDate.textProperty().bindBidirectional(endDateValue, new DateStringConverter());

		//Configure background task
		statusPane.managedProperty().bind(statusPane.visibleProperty());
		statusPane.visibleProperty().bind(reportTaskThread.isNotNull());
		buildReportButton.disableProperty().bind(reportTaskThread.isNotNull());

		//Configure preview & save panes
		previewReportPane.managedProperty().bind(previewReportPane.visibleProperty());
		previewReportPane.visibleProperty().bind(generatedReport.isNotNull());
		savePane.managedProperty().bind(savePane.visibleProperty());
		savePane.visibleProperty().bind(generatedReport.isNotNull());
		saveReportButton.disableProperty().bind(generatedReport.isNull());

		//Configure dates
		startDateValue.set(DateTools.getInstance().convertDateToStartOfMonth(new Date()));
		endDateValue.set(DateTools.getInstance().convertDateToEndOfMonth(new Date()));
	}

	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public void setlastDirectory(ObjectProperty<File> lastDirectory) {
		this.lastDirectory = lastDirectory;
	}

	@FXML
	private void buildReport() {
		if (reportTaskThread.getValue() != null)
			return;
		//TODO: ExecutorService?
		Task reportTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				updateMessage("Generating report...");
				Report report = new Report(taskManager);
				report.setStartDate(startDateValue.get());
				report.setEndDate(endDateValue.get());
				report.progressProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
						updateProgress(newValue.doubleValue(), 1);
					}
				});
				report.buildReport();
				generatedReport.set(report);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						viewer.getEngine().loadContent(generatedReport.get().getReportHTML());
					}
				});
				//updateProgress(-1, 1);
				return null;
			}
		};

		progressIndicator.progressProperty().bind(reportTask.progressProperty());
		progressLabel.textProperty().bind(reportTask.messageProperty());
		reportTask.runningProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
				if (!newValue && newValue != oldValue) {
					try {
						reportTaskThread.get().join();
						reportTaskThread.set(null);
					} catch (InterruptedException ex) {
						Logger.getLogger(ReportController.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		});
		reportTaskThread.set(new Thread(reportTask));
		reportTaskThread.get().setDaemon(true);
		reportTaskThread.get().start();
	}

	@FXML
	void saveReport() {
		if (generatedReport.get() == null)
			return;
		// Prepare file chooser dialog
		FileChooser fileChooser = new FileChooser();
		if (lastDirectory.get() != null && lastDirectory.get().exists())
			fileChooser.setInitialDirectory(lastDirectory.get());
		fileChooser.setTitle("Choose where to save the report");
		//Prepare file chooser filter
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));//NOI18N

		//Show the dialog
		File selectedFile;
		if ((selectedFile = fileChooser.showSaveDialog(savePane.getScene().getWindow())) != null) {
			lastDirectory.set(selectedFile.isDirectory() ? selectedFile : selectedFile.getParentFile());

			//Set extension if necessary
			String extension = selectedFile.getName().contains(".") ? selectedFile.getName().substring(selectedFile.getName().lastIndexOf(".")) : null; //NOI18N
			if (extension == null || extension.isEmpty())
				selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".pdf"); //NOI18N
			try {
				generatedReport.get().savePdfReport(selectedFile);
			} catch (FileNotFoundException | DRException ex) {
				Logger.getLogger(ReportController.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
