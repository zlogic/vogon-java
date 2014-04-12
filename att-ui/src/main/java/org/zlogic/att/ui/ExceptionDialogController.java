/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for the exception dialog
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class ExceptionDialogController implements Initializable {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/ui/messages");
	/**
	 * The exception text label
	 */
	@FXML
	private Label exceptionLabel;
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
			stage.setTitle(messages.getString("ERROR_HAS_OCCURRED"));
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
	 * Shows an exception message
	 *
	 * @param explanation the exception message to display
	 */
	public void showExceptionMessage(String explanation) {
		if (Platform.isFxApplicationThread()) {
			//Show the dialog
			exceptionLabel.setText(explanation);
			stage.showAndWait();
		} else {
			//Required for JavaFX, otherwise dialog won't be displayed
			Platform.runLater(new Runnable() {
				private String explanation;

				@Override
				public void run() {
					showExceptionMessage(explanation);
				}

				public Runnable setParameters(String explanation) {
					this.explanation = explanation;
					return this;
				}
			}.setParameters(explanation));
		}
	}

	/*
	 * Callbacks
	 */
	/**
	 * Hides the window
	 */
	@FXML
	private void hideWindow() {
		rootNode.getScene().getWindow().hide();
	}
}
