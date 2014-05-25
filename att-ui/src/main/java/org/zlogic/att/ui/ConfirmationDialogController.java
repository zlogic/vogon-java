/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for the confirmation dialog
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class ConfirmationDialogController implements Initializable {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(Launcher.class.getName());
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	/**
	 * The message text label
	 */
	@FXML
	private Label messageLabel;
	/**
	 * The root node
	 */
	@FXML
	private Node rootNode;
	/**
	 * The stage for this window
	 */
	@FXML
	private Stage stage;
	/**
	 * Dialog result
	 */
	private Result result;

	/**
	 * Dialog result
	 */
	public enum Result {

		/**
		 * Confirm pressed
		 */
		CONFIRMED,
		/**
		 * Cancel pressed
		 */
		CANCELLED
	};

	/**
	 * Loads the confirmation dialog FXML
	 *
	 * @return the created ConfirmationDialogController
	 */
	public static ConfirmationDialogController createInstance() {
		return createInstance(null);
	}

	/**
	 * Loads the confirmation dialog FXML
	 *
	 * @param icons the window icons
	 * @return the created ConfirmationDialogController
	 */
	public static ConfirmationDialogController createInstance(ObservableList<Image> icons) {
		FXMLLoader loader = new FXMLLoader(ConfirmationDialogController.class.getResource("ConfirmationDialog.fxml"), messages); //NOI18N
		loader.setLocation(ConfirmationDialogController.class.getResource("ConfirmationDialog.fxml")); //NOI18N
		try {
			loader.load();
		} catch (IOException ex) {
			log.log(Level.SEVERE, messages.getString("ERROR_LOADING_FXML"), ex);
		}
		ConfirmationDialogController controller = loader.getController();
		if (icons != null)
			controller.setWindowIcons(icons);
		return controller;
	}

	/**
	 * Initializes the controller
	 *
	 * @param url initialization URL
	 * @param resourceBundle supplied resources
	 */
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		//Prepare the stage
		stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		//Initialize the scene properties
		if (rootNode != null) {
			Scene scene = new Scene((Parent) rootNode);
			stage.setScene(scene);
		}
	}

	/**
	 * Sets the window icons
	 *
	 * @param icons the icons to be set
	 */
	public void setWindowIcons(ObservableList<Image> icons) {
		stage.getIcons().setAll(icons);
	}

	/**
	 * Shows a confirmation message. Can be only called on the Java FX
	 * Application Thread.
	 *
	 * @param title the dialog title
	 * @param message the confirmation message to display
	 * @return the dialog result
	 */
	public Result showDialog(String title, String message) {
		synchronized (this) {
			if (Platform.isFxApplicationThread()) {
				//Show the dialog
				stage.setTitle(title);
				messageLabel.setText(message);
				stage.showAndWait();
				return result;
			} else {
				throw new RuntimeException(messages.getString("CANNOT_SHOW_DIALOG_IN_NON-MODAL_MODE"));
			}
		}
	}

	/*
	 * Callbacks
	 */
	/**
	 * Confirms the dialog
	 */
	@FXML
	private void confirm() {
		result = Result.CONFIRMED;
		rootNode.getScene().getWindow().hide();
	}

	/**
	 * Cancels the dialog
	 */
	@FXML
	private void cancel() {
		result = Result.CANCELLED;
		rootNode.getScene().getWindow().hide();
	}
}
