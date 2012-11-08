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
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.events.AccountEventHandler;
import org.zlogic.vogon.ui.adapter.AccountModelAdapter;
import org.zlogic.vogon.ui.adapter.CurrencyModelAdapter;
import org.zlogic.vogon.ui.cell.ComboCellEditor;

/**
 *
 * @author Dmitry Zolotukhin
 */
public class AccountsController implements Initializable {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	protected FinanceData financeData;
	@FXML
	protected TableView<AccountModelAdapter> accountsTable;
	@FXML
	protected TableColumn<AccountModelAdapter,String> columnName;
	@FXML
	protected TableColumn<AccountModelAdapter,CurrencyModelAdapter> columnCurrency;
	@FXML
	protected TableColumn<AccountModelAdapter,String> columnBalance;
	@FXML
	protected TableColumn<AccountModelAdapter,Boolean> columnIncludeInTotal;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		accountsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		columnName.setCellFactory(new Callback<TableColumn<AccountModelAdapter, String>, TableCell<AccountModelAdapter, String>>() {
			@Override
			public TableCell<AccountModelAdapter, String> call(TableColumn<AccountModelAdapter, String> p) {
				TextFieldTableCell cell = new TextFieldTableCell<>();
				cell.setConverter(new DefaultStringConverter());
				return cell;
			}
		});
		
		columnBalance.setCellFactory(new Callback<TableColumn<AccountModelAdapter, String>, TableCell<AccountModelAdapter, String>>() {
			@Override
			public TableCell<AccountModelAdapter, String> call(TableColumn<AccountModelAdapter, String> p) {
				TextFieldTableCell cell = new TextFieldTableCell<>();
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});
		
		columnCurrency.setCellFactory(new Callback<TableColumn<AccountModelAdapter, CurrencyModelAdapter>, TableCell<AccountModelAdapter, CurrencyModelAdapter>>() {
			@Override
			public TableCell<AccountModelAdapter, CurrencyModelAdapter> call(TableColumn<AccountModelAdapter, CurrencyModelAdapter> p) {
				ComboBoxTableCell cell = new ComboBoxTableCell();
				cell.getItems().addAll(CurrencyModelAdapter.getCurrenciesList());
				return cell;
			}
		});
		
		columnIncludeInTotal.setCellFactory(new Callback<TableColumn<AccountModelAdapter, Boolean>, TableCell<AccountModelAdapter, Boolean>>() {
			@Override
			public TableCell<AccountModelAdapter, Boolean> call(TableColumn<AccountModelAdapter, Boolean> p) {
				CheckBoxTableCell cell = new CheckBoxTableCell<>();
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
	}

	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;
		updateAccounts();
		financeData.setAccountListener(new AccountEventHandler() {
			protected FinanceData financeData;
			public AccountEventHandler setFinanceData(FinanceData financeData){
				this.financeData = financeData;
				return this;
			}
			@Override
			public void accountCreated(FinanceAccount newAccount) {
				int accountIndex = accountsTable.getItems().indexOf(newAccount);
				if (accountIndex >= 0)
					accountsTable.getSelectionModel().select(accountIndex);
			}

			@Override
			public void accountUpdated(FinanceAccount updatedAccount) {
				for (AccountModelAdapter accountAdapter : accountsTable.getItems())
					if (accountAdapter.getAccount().equals(updatedAccount))
						accountAdapter.refresh(updatedAccount);
				accountsTable.setItems(accountsTable.getItems());
			}

			@Override
			public void accountDeleted(FinanceAccount deletedAccount) {
				List<AccountModelAdapter> deletedAdapters = new LinkedList<>();
				for (AccountModelAdapter accountAdapter : accountsTable.getItems())
					if (accountAdapter.getAccount().equals(deletedAccount))
						deletedAdapters.add(accountAdapter);
				accountsTable.getItems().removeAll(deletedAdapters);
			}

			@Override
			public void accountsUpdated() {
				updateAccounts();
			}
		}.setFinanceData(financeData));
	}

	@FXML
	protected void handleCreateAccount(){
		FinanceAccount account = new FinanceAccount("", financeData.getDefaultCurrency()); //NOI18N
		financeData.createAccount(account);
	}
	
	@FXML
	protected void handleDeleteAccount(){
		AccountModelAdapter selectedItem = accountsTable.getSelectionModel().getSelectedItem();
		financeData.deleteAccount(selectedItem.getAccount());
	}
	
	protected void updateAccounts() {
		accountsTable.getItems().removeAll(accountsTable.getItems());
		for (FinanceAccount account : financeData.getAccounts())
			accountsTable.getItems().add(new AccountModelAdapter(account,financeData));
	}
}
