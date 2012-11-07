/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.events.AccountEventHandler;

/**
 *
 * @author Dmitry Zolotukhin
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
		financeData.setAccountListener(new AccountEventHandler() {
			@Override
			public void accountCreated(FinanceAccount newAccount) {
				FinanceAccountModelAdapter newAccountModelAdapter = new FinanceAccountModelAdapter(newAccount);
				int accountIndex = accountsTable.getItems().indexOf(newAccountModelAdapter);
				if (accountIndex >= 0)
					accountsTable.getSelectionModel().select(newAccountModelAdapter);
			}

			@Override
			public void accountUpdated(FinanceAccount updatedAccount) {
				for (FinanceAccountModelAdapter accountAdapter : accountsTable.getItems())
					if (accountAdapter.getAccount().equals(updatedAccount))
						accountAdapter.refresh(updatedAccount);
				accountsTable.setItems(accountsTable.getItems());
			}

			@Override
			public void accountDeleted(FinanceAccount deletedAccount) {
				List<FinanceAccountModelAdapter> deletedAdapters = new LinkedList<>();
				for (FinanceAccountModelAdapter accountAdapter : accountsTable.getItems())
					if (accountAdapter.getAccount().equals(deletedAccount))
						deletedAdapters.add(accountAdapter);
				accountsTable.getItems().removeAll(deletedAdapters);
			}

			@Override
			public void accountsUpdated() {
				updateAccounts();
			}
		});
	}

	protected void updateAccounts() {
		accountsTable.getItems().removeAll(accountsTable.getItems());
		for (FinanceAccount account : financeData.getAccounts())
			accountsTable.getItems().add(new FinanceAccountModelAdapter(account));
	}
}
