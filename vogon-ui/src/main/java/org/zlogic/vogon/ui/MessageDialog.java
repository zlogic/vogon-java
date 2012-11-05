/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zlogic.vogon.ui;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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
 *
 * @author Dmitry
 */
//TODO: replace this with Alert once Jafx FX 3.0/8.0 is released
public class MessageDialog implements Initializable {

	public static void showDialog(String title,String message,boolean callerIsBackgroundThread){
		if(!callerIsBackgroundThread){
			Stage dialogStage = new Stage();
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			Parent root = null;
			FXMLLoader loader = new FXMLLoader(MessageDialog.class.getResource("MessageDialog.fxml")); //NOI18N
				loader.setLocation(MessageDialog.class.getResource("MessageDialog.fxml")); //NOI18N
			try {
				root = (Parent) loader.load();
			} catch (IOException ex) {
				Logger.getLogger(MessageDialog.class.getName()).log(Level.SEVERE, null, ex);
			}
			if(root!=null){
				Scene scene = new Scene(root);
				dialogStage.setTitle(title);
				dialogStage.setScene(scene);
				((MessageDialog) loader.getController()).messageText.setText(message);
				dialogStage.showAndWait();
			}
		}else{
			Platform.runLater(new Runnable() {
				private String title;
				private String message;
				@Override
				public void run() {
					showDialog(title, message,false);
				}
				public Runnable setParameters(String title,String message){
					this.title = title;
					this.message = message;
					return this;
				}
			}.setParameters(title, message));
		}
	}
	
	@FXML
	private Button okButton;
	@FXML
	private Label messageText;
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		//throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@FXML
	private void handleOkAction(ActionEvent event) {
		((Stage)okButton.getScene().getWindow()).close();
	}
}
