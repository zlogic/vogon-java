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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import org.zlogic.vogon.ui.adapter.AccountInterface;
import org.zlogic.vogon.ui.adapter.AccountModelAdapter;
import org.zlogic.vogon.ui.adapter.CurrencyModelAdapter;
import org.zlogic.vogon.ui.adapter.DataManager;

/**
 * The Accounts pane
 *
 * @author Dmitry Zolotukhin
 */
public class AccountsController implements Initializable {

	/**
	 * The associated DataManager instance
	 */
	protected DataManager dataManager;
	/**
	 * The Accounts table
	 */
	@FXML
	private TableView<AccountInterface> accountsTable;
	/**
	 * The Name column
	 */
	@FXML
	private TableColumn<AccountInterface, String> columnName;
	/**
	 * The Currency column
	 */
	@FXML
	private TableColumn<AccountInterface, CurrencyModelAdapter> columnCurrency;
	/**
	 * The Balance column
	 */
	@FXML
	private TableColumn<AccountInterface, String> columnBalance;
	/**
	 * The Include in total column
	 */
	@FXML
	private TableColumn<AccountInterface, Boolean> columnIncludeInTotal;
	/**
	 * Delete account button
	 */
	@FXML
	private Button deleteAccount;

	/**
	 * Initializes the Accounts Controller
	 *
	 * @param url the FXML URL
	 * @param rb the FXML ResourceBundle
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		accountsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		//Disable uneditable items
		accountsTable.setRowFactory(new Callback<TableView<AccountInterface>, TableRow<AccountInterface>>() {
			@Override
			public TableRow<AccountInterface> call(TableView<AccountInterface> p) {
				TableRow<AccountInterface> row = new TableRow<>();
				row.itemProperty().addListener(new ChangeListener<AccountInterface>() {
					private TableRow<AccountInterface> row;

					public ChangeListener<AccountInterface> setRow(TableRow<AccountInterface> row) {
						this.row = row;
						return this;
					}

					@Override
					public void changed(ObservableValue<? extends AccountInterface> ov, AccountInterface oldValue, AccountInterface newValue) {
						boolean isEditableAccount = newValue instanceof AccountModelAdapter;
						row.setDisable(!isEditableAccount);
						row.getStyleClass().removeAll("regular-account", "reporting-account");//NOI18N
						row.getStyleClass().add(isEditableAccount ? "regular-account" : "reporting-account");//NOI18N
					}
				}.setRow(row));
				return row;
			}
		});

		//Configure the cells
		columnName.setCellFactory(new Callback<TableColumn<AccountInterface, String>, TableCell<AccountInterface, String>>() {
			@Override
			public TableCell<AccountInterface, String> call(TableColumn<AccountInterface, String> p) {
				TextFieldTableCell<AccountInterface, String> cell = new TextFieldTableCell<>();
				cell.setConverter(new DefaultStringConverter());
				return cell;
			}
		});

		columnBalance.setCellFactory(new Callback<TableColumn<AccountInterface, String>, TableCell<AccountInterface, String>>() {
			@Override
			public TableCell<AccountInterface, String> call(TableColumn<AccountInterface, String> p) {
				TextFieldTableCell<AccountInterface, String> cell = new TextFieldTableCell<>();
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});

		columnCurrency.setCellFactory(new Callback<TableColumn<AccountInterface, CurrencyModelAdapter>, TableCell<AccountInterface, CurrencyModelAdapter>>() {
			@Override
			public TableCell<AccountInterface, CurrencyModelAdapter> call(TableColumn<AccountInterface, CurrencyModelAdapter> p) {
				//TODO: sometimes the balance is not correctly updated in case of a change in currency. Most likely is a Java FX bug.
				ComboBoxTableCell<AccountInterface, CurrencyModelAdapter> cell = new ComboBoxTableCell<>();
				cell.getItems().addAll(getCurrenciesList());
				return cell;
			}
		});
		columnIncludeInTotal.setCellFactory(new Callback<TableColumn<AccountInterface, Boolean>, TableCell<AccountInterface, Boolean>>() {
			@Override
			public TableCell<AccountInterface, Boolean> call(TableColumn<AccountInterface, Boolean> p) {
				CheckBoxTableCell<AccountInterface, Boolean> cell = new CheckBoxTableCell<>();
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});

		//Enable/disable buttons
		deleteAccount.disableProperty().bind(accountsTable.getSelectionModel().selectedIndexProperty().lessThan(0));
	}

	/**
	 * Assigns the DataManager instance
	 *
	 * @param dataManager the DataManager instance
	 */
	public void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;
		accountsTable.setItems(dataManager.getAllAccounts());
	}

	/**
	 * Create account button
	 */
	@FXML
	private void handleCreateAccount() {
		dataManager.createAccount();
	}

	/**
	 * Delete account button
	 */
	@FXML
	private void handleDeleteAccount() {
		AccountInterface selectedItem = accountsTable.getSelectionModel().getSelectedItem();
		if (selectedItem instanceof AccountModelAdapter)
			dataManager.deleteAccount(((AccountModelAdapter) selectedItem));
	}

	/**
	 * Obtains the currencies list
	 *
	 * @return the currencies list
	 */
	protected List<CurrencyModelAdapter> getCurrenciesList() {
		//TODO: use a single ObservableList
		List<CurrencyModelAdapter> result = new LinkedList<>();
		for (CurrencyModelAdapter adapter : CurrencyModelAdapter.getCurrenciesList())
			result.add(adapter);
		return result;
	}
}
