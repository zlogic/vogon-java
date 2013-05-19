/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Java FX launcher/initializer
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class Launcher extends Application {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(Launcher.class.getName());
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	/**
	 * The main window controller
	 */
	private MainWindowController controller;

	/**
	 * Creates the JavaFX scene and associated objects
	 *
	 * @param stage the root stage
	 * @throws Exception if an initialization exception occurs
	 */
	@Override
	public void start(Stage stage) throws Exception {
		initApplication();
		Parent root;
		FXMLLoader loader;
		try {
			//Load FXML
			loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"), messages); //NOI18N
			loader.setLocation(getClass().getResource("MainWindow.fxml")); //NOI18N
			root = (Parent) loader.load();
		} catch (IOException ex) {
			log.log(java.util.logging.Level.SEVERE, null, ex);
			throw ex;
		}

		//Create scene
		Scene scene = new Scene(root);

		//Set scene properties
		stage.setTitle(messages.getString("AWESOME_TIME_TRACKER"));
		stage.setScene(scene);
		stage.getIcons().addAll(getIconImages());

		controller = loader.getController();
		controller.setWindowIcons(stage.getIcons());
		initTrayIcon(stage, controller);
		stage.show();
	}

	/**
	 * Performs the final cleanup before stopping the Java FX application
	 */
	@Override
	public void stop() {
		if (controller != null)
			controller.completeTaskThread();
	}

	/**
	 * Initializes the tray icon
	 *
	 * @param primaryStage the stage to be shown/hidden
	 * @param controller MainWindowController instance which will be assigned a
	 * ShutdownProcedure
	 */
	private void initTrayIcon(Stage primaryStage, MainWindowController controller) {
		Platform.setImplicitExit(false);
		Platform.runLater(new Runnable() {
			private Stage primaryStage;
			private MainWindowController controller;

			public Runnable setParameters(Stage primaryStage, MainWindowController controller) {
				this.primaryStage = primaryStage;
				this.controller = controller;
				return this;
			}

			@Override
			public void run() {
				TrayIcon trayIcon = new TrayIcon(primaryStage, controller.exceptionHandlerProperty());
				controller.setShutdownProcedure(new Runnable() {
					private TrayIcon trayIcon;

					public Runnable setTrayIcon(TrayIcon trayIcon) {
						this.trayIcon = trayIcon;
						return this;
					}

					@Override
					public void run() {
						controller.getDataManager().shutdown();
						trayIcon.exitApplication();
					}
				}.setTrayIcon(trayIcon));
			}
		}.setParameters(primaryStage, controller));
	}

	/**
	 * Returns the list of icons
	 *
	 * @return the list of icons
	 */
	public List<Image> getIconImages() {
		List<Image> images = new LinkedList<>();
		int[] iconSizes = new int[]{16, 24, 32, 48, 64, 128, 256, 512};
		for (int iconName : iconSizes)
			images.add(new Image(MessageFormat.format("org/zlogic/att/ui/icon/att-tilt-{0}.png", new Object[]{Integer.toString(iconName)}))); //NOI18N
		return images;
	}

	/**
	 * Performs initialization of application's dependencies
	 */
	private void initApplication() {
		//Configure logging to load config from classpath
		String loggingFile = System.getProperty("java.util.logging.config.file"); //NOI18N
		if (loggingFile == null || loggingFile.isEmpty()) {
			try {
				java.net.URL url = Thread.currentThread().getContextClassLoader().getResource("logging.properties"); //NOI18N
				if (url != null)
					java.util.logging.LogManager.getLogManager().readConfiguration(url.openStream());
			} catch (IOException | SecurityException e) {
				log.log(Level.SEVERE, messages.getString("ERROR_WHEN_LOADING_LOGGING_CONFIGURATION"), e);
				System.err.println(messages.getString("ERROR_WHEN_LOADING_LOGGING_CONFIGURATION"));
			}
		}
	}

	/**
	 * Java main method
	 *
	 * @param args application arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
