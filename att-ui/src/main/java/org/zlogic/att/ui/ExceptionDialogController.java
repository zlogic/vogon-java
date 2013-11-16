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
public class ExceptionDialogController implements Initializable, ExceptionHandler {

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

	@Override
	public void showException(String explanation, Throwable ex) {
		if (Platform.isFxApplicationThread()) {
			//Show the dialog
			if (explanation != null)
				exceptionLabel.setText(explanation);
			else if (ex != null && ex.getMessage() != null)
				exceptionLabel.setText(ex.getMessage());
			else
				exceptionLabel.setText(messages.getString("UNKNOWN_ERROR"));
			stage.showAndWait();
		} else {
			//Required for JavaFX, otherwise dialog won't be displayed
			Platform.runLater(new Runnable() {
				private String explanation;
				private Throwable ex;

				@Override
				public void run() {
					showException(explanation, ex);
				}

				public Runnable setParameters(String explanation, Throwable ex) {
					this.explanation = explanation;
					this.ex = ex;
					return this;
				}
			}.setParameters(explanation, ex));
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
