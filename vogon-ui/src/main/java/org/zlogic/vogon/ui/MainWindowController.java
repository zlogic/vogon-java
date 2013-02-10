/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.zlogic.vogon.data.DatabaseManager;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.interop.CsvImporter;
import org.zlogic.vogon.data.interop.FileExporter;
import org.zlogic.vogon.data.interop.FileImporter;
import org.zlogic.vogon.data.interop.VogonExportException;
import org.zlogic.vogon.data.interop.VogonImportLogicalException;
import org.zlogic.vogon.data.interop.XmlExporter;
import org.zlogic.vogon.data.interop.XmlImporter;

/**
 * Main entry window controller.
 *
 * @author Dmitry Zolotukhin
 */
public class MainWindowController implements Initializable {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * Last opened directory
	 */
	private File lastDirectory;
	/**
	 * The FinanceData instance
	 */
	private FinanceData financeData;
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
	/**
	 * The root container
	 */
	@FXML
	private VBox mainWindow;
	/**
	 * The status pane
	 */
	@FXML
	private HBox statusPane;
	/**
	 * The controller for the Transactions pane
	 */
	@FXML
	private TransactionsController transactionsPaneController;
	/**
	 * The controller for the Accounts pane
	 */
	@FXML
	private AccountsController accountsPaneController;
	/**
	 * The controller for the Analytics pane
	 */
	@FXML
	private AnalyticsController analyticsPaneController;
	/**
	 * The controller for the Currencies pane
	 */
	@FXML
	private CurrenciesController currenciesPaneController;
	/**
	 * The progress indicator for background tasks
	 */
	@FXML
	private ProgressIndicator progressIndicator;
	/**
	 * The label displaying the current background task's progress
	 */
	@FXML
	private Label progressLabel;
	/**
	 * The Export menu item
	 */
	@FXML
	private MenuItem menuItemImport;
	/**
	 * The Import menu item
	 */
	@FXML
	private MenuItem menuItemExport;
	/**
	 * The Recalculate Balance
	 */
	@FXML
	private MenuItem menuItemRecalculateBalance;
	/**
	 * The Cleanup DB menu item
	 */
	@FXML
	private MenuItem menuItemCleanupDB;
	/**
	 * The Accordion control
	 */
	@FXML
	private Accordion accordion;
	/**
	 * The Transactions pane inside the Accordion
	 */
	@FXML
	private TitledPane transactionsAccordionPane;

	/**
	 * Exit menu item
	 */
	@FXML
	private void handleMenuExitAction() {
		completeTaskThread();
		DatabaseManager.getInstance().shutdown();
		Platform.exit();
	}

	/**
	 * Import menu item
	 */
	@FXML
	private void handleMenuImportAction() {
		// Prepare file chooser dialog
		FileChooser fileChooser = new FileChooser();
		if (lastDirectory != null && lastDirectory.exists())
			fileChooser.setInitialDirectory(lastDirectory);
		fileChooser.setTitle(messages.getString("CHOOSE_FILES_TO_IMPORT"));
		//Prepare file chooser filter
		Map<String, String> extensionFilters = new TreeMap<>();
		extensionFilters.put(".xml", messages.getString("XML_FILES"));//NOI18N
		extensionFilters.put(".csv", messages.getString("CSV_FILES_(COMMA-SEPARATED)"));//NOI18N
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(messages.getString("XML_FILES"), "*.xml"));//NOI18N
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(messages.getString("CSV_FILES_(COMMA-SEPARATED)"), "*.csv"));//NOI18N

		//Show the dialog
		File selectedFile;
		if ((selectedFile = fileChooser.showOpenDialog(mainWindow.getScene().getWindow())) != null) {
			lastDirectory = selectedFile.isDirectory() ? selectedFile : selectedFile.getParentFile();
			preferenceStorage.put("lastDirectory", lastDirectory.toString()); //NOI18N

			//Choose the importer based on the file extension
			FileImporter importer = null;
			String extension = selectedFile.isFile() ? selectedFile.getName().substring(selectedFile.getName().lastIndexOf(".")) : null; //NOI18N
			extension = extensionFilters.get(extension);
			if (extension == null)
				importer = null;
			else if (extension.equals(messages.getString("CSV_FILES_(COMMA-SEPARATED)")))
				importer = new CsvImporter(selectedFile);
			else if (extension.equals(messages.getString("XML_FILES")))
				importer = new XmlImporter(selectedFile);

			//Prepare the background task
			Task<Void> task = new Task<Void>() {
				private FileImporter importer;

				public Task<Void> setImporter(FileImporter importer) {
					this.importer = importer;
					return this;
				}

				@Override
				protected Void call() throws Exception {
					try {
						updateMessage(messages.getString("TASK_IMPORTING_DATA"));
						updateProgress(-1, 1);
						if (importer == null)
							throw new VogonImportLogicalException(messages.getString("UNKNOWN_FILE_TYPE"));
						financeData.importData(importer);
						transactionsPaneController.setFinanceData(financeData);
					} catch (VogonImportLogicalException ex) {
						Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
						MessageDialog.showDialog(messages.getString("IMPORT_EXCEPTION_DIALOG_TITLE"), new MessageFormat(messages.getString("IMPORT_EXCEPTION_DIALOG_TEXT")).format(new Object[]{ex.getLocalizedMessage(), org.zlogic.vogon.data.Utils.getStackTrace(ex)}), true);
					} catch (Exception ex) {
						Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
						MessageDialog.showDialog(messages.getString("IMPORT_EXCEPTION_DIALOG_TITLE"), new MessageFormat(messages.getString("IMPORT_EXCEPTION_DIALOG_TEXT")).format(new Object[]{ex.getLocalizedMessage(), org.zlogic.vogon.data.Utils.getStackTrace(ex)}), true);
					} finally {
						updateProgress(1, 1);
						updateMessage("");//NOI18N
					}
					return null;
				}
			}.setImporter(importer);
			//Run the task
			startTaskThread(task);
		}
	}

	/**
	 * Export menu item
	 */
	@FXML
	private void handleMenuExportAction() {
		// Prepare file chooser dialog
		FileChooser fileChooser = new FileChooser();
		if (lastDirectory != null && lastDirectory.exists())
			fileChooser.setInitialDirectory(lastDirectory);
		fileChooser.setTitle(messages.getString("CHOOSE_FILES_TO_EXPORT"));
		//Prepare file chooser filter
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(messages.getString("XML_FILES"), "*.xml"));//NOI18N

		//Show the dialog
		File selectedFile;
		if ((selectedFile = fileChooser.showSaveDialog(mainWindow.getScene().getWindow())) != null) {
			lastDirectory = selectedFile.isDirectory() ? selectedFile : selectedFile.getParentFile();
			preferenceStorage.put("lastDirectory", lastDirectory.toString()); //NOI18N

			//Set extension if necessary
			String extension = selectedFile.getName().lastIndexOf(".") >= 0 ? selectedFile.getName().substring(selectedFile.getName().lastIndexOf(".")) : null; //NOI18N
			if (extension == null || extension.isEmpty())
				selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".xml"); //NOI18N

			FileExporter exporter = new XmlExporter(selectedFile);
			//Prepare the background task
			Task<Void> task = new Task<Void>() {
				private FileExporter exporter;

				public Task<Void> setExporter(FileExporter exporter) {
					this.exporter = exporter;
					return this;
				}

				@Override
				protected Void call() throws Exception {
					try {
						updateMessage(messages.getString("TASK_EXPORTING_DATA"));
						updateProgress(-1, 1);

						financeData.exportData(exporter);
					} catch (VogonExportException ex) {
						Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
						MessageDialog.showDialog(messages.getString("EXPORT_EXCEPTION_DIALOG_TITLE"), new MessageFormat(messages.getString("EXPORT_EXCEPTION_DIALOG_TEXT")).format(new Object[]{ex.getLocalizedMessage(), org.zlogic.vogon.data.Utils.getStackTrace(ex)}), true);
					} catch (Exception ex) {
						Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
						MessageDialog.showDialog(messages.getString("EXPORT_EXCEPTION_DIALOG_TITLE"), new MessageFormat(messages.getString("EXPORT_EXCEPTION_DIALOG_TEXT")).format(new Object[]{ex.getLocalizedMessage(), org.zlogic.vogon.data.Utils.getStackTrace(ex)}), true);
					} finally {
						updateProgress(1, 1);
						updateMessage("");//NOI18N
					}
					return null;
				}
			}.setExporter(exporter);
			//Run the task
			startTaskThread(task);
		}
	}

	/**
	 * Cleanup DB menu item
	 */
	@FXML
	private void handleMenuCleanupDBAction() {
		//Prepare the task
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				updateMessage(messages.getString("TASK_CLEANING_UP_DB"));
				updateProgress(-1, 1);

				financeData.cleanup();

				updateProgress(1, 1);
				updateMessage("");//NOI18N
				return null;
			}
		};
		//Run the task
		startTaskThread(task);
	}

	/**
	 * Recalculate balance menu item
	 */
	@FXML
	private void handleMenuRecalculateBalanceAction() {
		//Prepare the task
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				updateMessage(messages.getString("TASK_RECALCULATING_BALANCE"));
				updateProgress(-1, 1);

				for (FinanceAccount account : financeData.getAccounts())
					financeData.refreshAccountBalance(account);

				updateProgress(1, 1);
				updateMessage("");//NOI18N
				return null;
			}
		};
		//Run the task
		startTaskThread(task);
	}

	/**
	 * Starts the background task (disables task-related components, show the
	 * progress pane)
	 */
	private void beginBackgroundTask() {
		statusPane.setVisible(true);
		menuItemImport.setDisable(true);
		menuItemExport.setDisable(true);
		menuItemRecalculateBalance.setDisable(true);
		menuItemCleanupDB.setDisable(true);
	}

	/**
	 * Ends the background task (enables task-related components, hides the
	 * progress pane)
	 */
	private void endBackgroundTask() {
		statusPane.setVisible(false);
		menuItemImport.setDisable(false);
		menuItemExport.setDisable(false);
		menuItemRecalculateBalance.setDisable(false);
		menuItemCleanupDB.setDisable(false);
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

	/**
	 * Initializes the Main Window Controller
	 *
	 * @param url the FXML URL
	 * @param rb the FXML ResourceBundle
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		//Restore settings
		lastDirectory = preferenceStorage.get("lastDirectory", null) == null ? null : new File(preferenceStorage.get("lastDirectory", null)); //NOI18N

		//Configure components
		statusPane.managedProperty().bind(statusPane.visibleProperty());
		statusPane.setVisible(false);
		accordion.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
			@Override
			public void changed(ObservableValue<? extends TitledPane> ov, TitledPane t, TitledPane t1) {
				if (t == transactionsAccordionPane && t1 != transactionsAccordionPane)
					transactionsPaneController.cancelEdit();
			}
		});
		analyticsPaneController.setBackgroundTaskProcessor(new Callback<Task<Void>, Void>() {
			@Override
			public Void call(Task<Void> p) {
				startTaskThread(p);
				return null;
			}
		});
	}

	/**
	 * Assigns the FinanceData instance
	 *
	 * @param financeData the FinanceData instance
	 */
	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;
		transactionsPaneController.setFinanceData(financeData);
		accountsPaneController.setFinanceData(financeData);
		analyticsPaneController.setFinanceData(financeData);
		currenciesPaneController.setFinanceData(financeData);
	}
}
