package org.zlogic.att.ui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Zlogic
 * Date: 27.12.12
 * Time: 21:06
 * To change this template use File | Settings | File Templates.
 */
public class Launcher extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		initApplication();
		Parent root;
		FXMLLoader loader;
		try {
			//Load FXML
			loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
			loader.setLocation(getClass().getResource("MainWindow.fxml"));
			root = (Parent) loader.load();
		} catch (IOException ex) {
			java.util.logging.Logger.getLogger(Launcher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
			return;
		}

		//Create scene
		Scene scene = new Scene(root);

		//Set scene properties
		stage.setTitle("Awesome Time Tracker");

		stage.setScene(scene);

		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent windowEvent) {
				//TODO
			}
		});

		stage.show();
	}

	/**
	 * Performs initialization of application's dependencies
	 */
	private void initApplication() {
		/*
		 * Configure logging to load config from classpath
		 */
		String loggingFile = System.getProperty("java.util.logging.config.file");
		if (loggingFile == null || loggingFile.isEmpty()) {
			try {
				java.net.URL url = ClassLoader.getSystemClassLoader().getResource("logging.properties");
				if (url != null)
					java.util.logging.LogManager.getLogManager().readConfiguration(url.openStream());
			} catch (IOException | SecurityException e) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error when loading logging configuration", e);
			}
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
