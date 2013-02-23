/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.ui;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for the exception dialog
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class ExceptionDialogController implements Initializable, ExceptionHandler {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(ExceptionDialogController.class.getName());
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
	 * The parent window for this exception dialog
	 */
	private Node parentNode;
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
	 * Returns the parent window for this dialog
	 *
	 * @return the parent window for this dialog
	 */
	public Node getParentNode() {
		return parentNode;
	}

	/**
	 * Sets the parent window for this dialog
	 *
	 * @param parentNode the parent window for this dialog
	 */
	public void setParentNode(Node parentNode) {
		this.parentNode = parentNode;
	}

	@Override
	public void showException(String explanation, Throwable ex, boolean callerIsBackgroundThread) {
		if (!callerIsBackgroundThread) {
			//Show the dialog
			stage.getIcons().setAll(((Stage) parentNode.getScene().getWindow()).getIcons());
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
					showException(explanation, ex, false);
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
		exceptionLabel.getScene().getWindow().hide();
	}
}
