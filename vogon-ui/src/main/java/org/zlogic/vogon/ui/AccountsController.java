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
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;
import org.zlogic.vogon.ui.adapter.AccountInterface;
import org.zlogic.vogon.ui.adapter.AccountModelAdapter;
import org.zlogic.vogon.ui.adapter.CurrencyModelAdapter;
import org.zlogic.vogon.ui.adapter.DataManager;
import org.zlogic.vogon.ui.adapter.ObjectWithStatus;

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
	private TableColumn<AccountInterface, ObjectWithStatus<String, Boolean>> columnName;
	/**
	 * The Currency column
	 */
	@FXML
	private TableColumn<AccountInterface, ObjectWithStatus<CurrencyModelAdapter, Boolean>> columnCurrency;
	/**
	 * The Balance column
	 */
	@FXML
	private TableColumn<AccountInterface, String> columnBalance;
	/**
	 * The Include in total column
	 */
	@FXML
	private TableColumn<AccountInterface, ObjectWithStatus<BooleanProperty, Boolean>> columnIncludeInTotal;
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

		//Configure the cells
		columnName.setCellFactory(new Callback<TableColumn<AccountInterface, ObjectWithStatus<String, Boolean>>, TableCell<AccountInterface, ObjectWithStatus<String, Boolean>>>() {
			@Override
			public TableCell<AccountInterface, ObjectWithStatus<String, Boolean>> call(TableColumn<AccountInterface, ObjectWithStatus<String, Boolean>> p) {
				TextFieldTableCell<AccountInterface, ObjectWithStatus<String, Boolean>> cell = new TextFieldTableCell<AccountInterface, ObjectWithStatus<String, Boolean>>() {
					@Override
					public void updateItem(ObjectWithStatus<String, Boolean> item, boolean empty) {
						super.updateItem(item, empty);
						if (!isEmpty() && item.getStatus() != null) {
							setEditable(item.getStatus());
							setFont(Font.font(getFont().getName(), item.getStatus() ? FontWeight.NORMAL : FontWeight.BOLD, getFont().getSize()));
						}
					}
				};
				cell.setConverter(ObjectWithStatus.getConverter(String.class, Boolean.class));
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

		columnCurrency.setCellFactory(new Callback<TableColumn<AccountInterface, ObjectWithStatus<CurrencyModelAdapter, Boolean>>, TableCell<AccountInterface, ObjectWithStatus<CurrencyModelAdapter, Boolean>>>() {
			@Override
			public TableCell<AccountInterface, ObjectWithStatus<CurrencyModelAdapter, Boolean>> call(TableColumn<AccountInterface, ObjectWithStatus<CurrencyModelAdapter, Boolean>> p) {
				//TODO: sometimes the balance is not correctly updated in case of a change in currency. Most likely is a Java FX bug.
				ComboBoxTableCell<AccountInterface, ObjectWithStatus<CurrencyModelAdapter, Boolean>> cell = new ComboBoxTableCell<AccountInterface, ObjectWithStatus<CurrencyModelAdapter, Boolean>>() {
					@Override
					public void updateItem(ObjectWithStatus<CurrencyModelAdapter, Boolean> item, boolean empty) {
						super.updateItem(item, empty);
						if (!isEmpty())
							setEditable(item.getStatus());
					}
				};
				cell.getItems().addAll(getCurrenciesList());
				return cell;
			}
		});

		columnIncludeInTotal.setCellFactory(new Callback<TableColumn<AccountInterface, ObjectWithStatus<BooleanProperty, Boolean>>, TableCell<AccountInterface, ObjectWithStatus<BooleanProperty, Boolean>>>() {
			@Override
			public TableCell<AccountInterface, ObjectWithStatus<BooleanProperty, Boolean>> call(TableColumn<AccountInterface, ObjectWithStatus<BooleanProperty, Boolean>> p) {
				CheckBoxTableCell<AccountInterface, ObjectWithStatus<BooleanProperty, Boolean>> cell = new CheckBoxTableCell<AccountInterface, ObjectWithStatus<BooleanProperty, Boolean>>() {
					@Override
					public void updateItem(ObjectWithStatus<BooleanProperty, Boolean> item, boolean empty) {
						super.updateItem(item, empty);
						setSelectedStateCallback(new Callback<Integer, ObservableValue<Boolean>>() {
							@Override
							public ObservableValue<Boolean> call(Integer p) {
								return accountsTable.getItems().get(p).includeInTotalProperty().get().getValue();
							}
						});
						if (!isEmpty())
							setEditable(item.getStatus());
					}
				};
				cell.setConverter(ObjectWithStatus.getConverter(BooleanProperty.class, Boolean.class));
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
	protected List<ObjectWithStatus<CurrencyModelAdapter, Boolean>> getCurrenciesList() {
		List<ObjectWithStatus<CurrencyModelAdapter, Boolean>> result = new LinkedList<>();
		for (CurrencyModelAdapter adapter : CurrencyModelAdapter.getCurrenciesList())
			result.add(new ObjectWithStatus<>(adapter, true));
		return result;
	}
}
