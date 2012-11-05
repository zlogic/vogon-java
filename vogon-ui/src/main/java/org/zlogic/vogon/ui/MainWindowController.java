/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.zlogic.vogon.data.DatabaseManager;
import org.zlogic.vogon.data.FinanceData;

/**
 * Main entry window controller.
 * @author Dmitry Zolotukhin
 */
public class MainWindowController implements Initializable {
	/**
	 * Last opened directory
	 */
	private Path lastDirectory;

	private FinanceData financeData;
	
	/**
	 * Easy access to preference storage
	 */
	protected java.util.prefs.Preferences preferenceStorage = java.util.prefs.Preferences.userNodeForPackage(Launcher.class);
	
	@FXML
	private TransactionsController transactionsPaneController;
	@FXML
	private void handleMenuExitAction(ActionEvent event) {
		DatabaseManager.getInstance().shutdown();
		Platform.exit();
	}
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		//Restore settings
		lastDirectory = preferenceStorage.get("lastDirectory", null) == null ? null : Paths.get(preferenceStorage.get("lastDirectory", null)); //NOI18N
	}

	public FinanceData getFinanceData() {
		return financeData;
	}

	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;
		transactionsPaneController.setFinanceData(financeData);
	}
}
