/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Very simple dialog class. TODO: replace this with Alert once Java FX 3.0/8.0
 * is released
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
 */
public class MessageDialog implements Initializable {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(MessageDialog.class.getName());

	//TODO: make this non-static to load the dialog FXML just once and use window icons
	/**
	 * Shows the dialog (modal)
	 *
	 * @param title the dialog window's title
	 * @param message the message to be displayed
	 */
	public static void showDialog(String title, String message) {
		if (Platform.isFxApplicationThread()) {
			//Load FXML
			Stage dialogStage = new Stage();
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			Parent root = null;
			FXMLLoader loader = new FXMLLoader(MessageDialog.class.getResource("MessageDialog.fxml")); //NOI18N
			loader.setLocation(MessageDialog.class.getResource("MessageDialog.fxml")); //NOI18N
			try {
				root = (Parent) loader.load();
			} catch (IOException ex) {
				log.log(Level.SEVERE, null, ex);
			}
			//Initialize the scene properties
			if (root != null) {
				Scene scene = new Scene(root);
				dialogStage.setTitle(title);
				dialogStage.setScene(scene);
				((MessageDialog) loader.getController()).messageText.setText(message);
				dialogStage.showAndWait();
			}
		} else {
			//Required for JavaFX, otherwise dialog won't be displayed
			Platform.runLater(new Runnable() {
				private String title;
				private String message;

				@Override
				public void run() {
					showDialog(title, message);
				}

				public Runnable setParameters(String title, String message) {
					this.title = title;
					this.message = message;
					return this;
				}
			}.setParameters(title, message));
		}
	}
	/**
	 * The "OK" button
	 */
	@FXML
	private Button okButton;
	/**
	 * The message label
	 */
	@FXML
	private Label messageText;

	/**
	 * Initializes the Dialog
	 *
	 * @param url the FXML URL
	 * @param rb the FXML ResourceBundle
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
	}

	/**
	 * OK button pressed
	 */
	@FXML
	private void handleOkAction() {
		((Stage) okButton.getScene().getWindow()).close();
	}
}
