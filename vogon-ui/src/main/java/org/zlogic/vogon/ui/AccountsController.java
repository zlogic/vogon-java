/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.events.AccountEventHandler;
import org.zlogic.vogon.data.events.CurrencyEventHandler;
import org.zlogic.vogon.ui.adapter.AccountInterface;
import org.zlogic.vogon.ui.adapter.AccountModelAdapter;
import org.zlogic.vogon.ui.adapter.CurrencyModelAdapter;
import org.zlogic.vogon.ui.adapter.ObjectWithStatus;
import org.zlogic.vogon.ui.adapter.ReportingAccount;

/**
 * The Accounts pane
 *
 * @author Dmitry Zolotukhin
 */
public class AccountsController implements Initializable {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The associated FinanceData instance
	 */
	protected FinanceData financeData;
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
				TextFieldTableCell cell = new TextFieldTableCell<AccountInterface, ObjectWithStatus<String, Boolean>>() {
					@Override
					public void updateItem(ObjectWithStatus<String, Boolean> item, boolean empty) {
						super.updateItem(item, empty);
						if (!isEmpty() && item.getStatus() != null) {
							setEditable(item.getStatus());
							setFont(Font.font(getFont().getName(), item.getStatus() ? FontWeight.NORMAL : FontWeight.BOLD, getFont().getSize()));
						}
					}
				};
				cell.setConverter(ObjectWithStatus.getConverter());
				return cell;
			}
		});

		columnBalance.setCellFactory(new Callback<TableColumn<AccountInterface, String>, TableCell<AccountInterface, String>>() {
			@Override
			public TableCell<AccountInterface, String> call(TableColumn<AccountInterface, String> p) {
				TextFieldTableCell cell = new TextFieldTableCell<>();
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});

		columnCurrency.setCellFactory(new Callback<TableColumn<AccountInterface, ObjectWithStatus<CurrencyModelAdapter, Boolean>>, TableCell<AccountInterface, ObjectWithStatus<CurrencyModelAdapter, Boolean>>>() {
			@Override
			public TableCell<AccountInterface, ObjectWithStatus<CurrencyModelAdapter, Boolean>> call(TableColumn<AccountInterface, ObjectWithStatus<CurrencyModelAdapter, Boolean>> p) {
				//TODO: sometimes the balance is not correctly updated in case of a change in currency. Most likely is a Java FX bug.
				ComboBoxTableCell cell = new ComboBoxTableCell<AccountInterface, ObjectWithStatus<CurrencyModelAdapter, Boolean>>() {
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
				CheckBoxTableCell cell = new CheckBoxTableCell<AccountInterface, ObjectWithStatus<BooleanProperty, Boolean>>() {
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
				cell.setConverter(ObjectWithStatus.getConverter());
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});
	}

	/**
	 * Assigns the FinanceData instance
	 *
	 * @param financeData the FinanceData instance
	 */
	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;
		updateAccounts();

		//Listen for Account events
		if (financeData.getAccountListener() instanceof FinanceDataEventDispatcher) {
			((FinanceDataEventDispatcher) financeData.getAccountListener()).addAccountEventHandler(new AccountEventHandler() {
				@Override
				public void accountCreated(long accountId) {
					updateAccounts();
				}

				@Override
				public void accountUpdated(long accountId) {
					updateAccounts();
				}

				@Override
				public void accountDeleted(long accountId) {
					updateAccounts();
				}

				@Override
				public void accountsUpdated() {
					updateAccounts();
				}
			});
		}

		//Listen for Currency events
		if (financeData.getAccountListener() instanceof FinanceDataEventDispatcher) {
			((FinanceDataEventDispatcher) financeData.getAccountListener()).addCurrencyEventHandler(new CurrencyEventHandler() {
				@Override
				public void currenciesUpdated() {
					updateAccounts();
				}
			});
		}
	}

	/**
	 * Create account button
	 */
	@FXML
	private void handleCreateAccount() {
		FinanceAccount account = new FinanceAccount("", financeData.getDefaultCurrency()); //NOI18N
		financeData.createAccount(account);
	}

	/**
	 * Delete account button
	 */
	@FXML
	private void handleDeleteAccount() {
		AccountInterface selectedItem = accountsTable.getSelectionModel().getSelectedItem();
		if (selectedItem instanceof AccountModelAdapter)
			financeData.deleteAccount(((AccountModelAdapter) selectedItem).getAccount());
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

	/**
	 * Updates the accounts table from database
	 */
	private void updateAccounts() {
		accountsTable.getItems().clear();
		for (FinanceAccount account : financeData.getAccounts())
			accountsTable.getItems().add(new AccountModelAdapter(account, financeData));

		for (Currency currency : financeData.getCurrencies())
			accountsTable.getItems().add(new ReportingAccount(MessageFormat.format(messages.getString("TOTAL_ACCOUNT"), new Object[]{currency.getCurrencyCode()}), financeData.getTotalBalance(currency), currency));
		if (financeData.getDefaultCurrency() != null)
			accountsTable.getItems().add(new ReportingAccount(MessageFormat.format(messages.getString("TOTAL_ALL_ACCOUNTS"), new Object[]{financeData.getDefaultCurrency().getCurrencyCode()}), financeData.getTotalBalance(null), financeData.getDefaultCurrency()));
	}
}
