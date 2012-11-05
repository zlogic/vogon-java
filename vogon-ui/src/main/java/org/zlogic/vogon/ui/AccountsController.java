/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zlogic.vogon.ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;

/**
 *
 * @author Dmitry
 */
public class AccountsController implements Initializable {
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	
	protected FinanceData financeData;
	
	@FXML
	protected TableView<FinanceAccountModelAdapter> accountsTable;
		
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		accountsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}
	
	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;
		updateAccounts();
	}
	
	protected void updateAccounts() {
		accountsTable.getItems().clear();
		for(FinanceAccount account : financeData.getAccounts())
			accountsTable.getItems().add(new FinanceAccountModelAdapter(account));
	}
}
