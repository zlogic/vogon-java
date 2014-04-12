/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.util.Callback;
import org.zlogic.vogon.ui.adapter.AccountInterface;
import org.zlogic.vogon.ui.adapter.AmountModelAdapter;
import org.zlogic.vogon.ui.adapter.DataManager;
import org.zlogic.vogon.ui.adapter.TransactionComponentModelAdapter;
import org.zlogic.vogon.ui.adapter.TransactionModelAdapter;
import org.zlogic.vogon.ui.adapter.TransactionTypeModelAdapter;
import org.zlogic.vogon.ui.cell.AmountCellEditor;
import org.zlogic.vogon.ui.cell.StringValidatorDouble;

/**
 * Controller for editing a transaction's components.
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
public class TransactionComponentsController implements Initializable {

	/**
	 * The DataManager instance
	 */
	protected DataManager dataManager;
	/**
	 * The edited transaction
	 */
	protected TransactionModelAdapter transaction;
	/**
	 * The transaction components table
	 */
	@FXML
	private TableView<TransactionComponentModelAdapter> transactionComponents;
	/**
	 * The transaction type combo box
	 */
	@FXML
	private ComboBox<TransactionTypeModelAdapter> transactionType;
	/**
	 * The account column
	 */
	@FXML
	private TableColumn<TransactionComponentModelAdapter, AccountInterface> columnAccount;
	/**
	 * The amount column
	 */
	@FXML
	private TableColumn<TransactionComponentModelAdapter, AmountModelAdapter> columnAmount;
	/**
	 * List of accounts for the accounts combo box
	 */
	protected ObservableList<AccountInterface> accountsComboList;
	/**
	 * Delete account button
	 */
	@FXML
	private Button deleteComponent;

	/**
	 * Initializes the Transaction Components editor
	 *
	 * @param url the FXML URL
	 * @param rb the FXML ResourceBundle
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		transactionComponents.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		//Cell editors
		columnAccount.setCellFactory(new Callback<TableColumn<TransactionComponentModelAdapter, AccountInterface>, TableCell<TransactionComponentModelAdapter, AccountInterface>>() {
			@Override
			public TableCell<TransactionComponentModelAdapter, AccountInterface> call(TableColumn<TransactionComponentModelAdapter, AccountInterface> p) {
				ComboBoxTableCell<TransactionComponentModelAdapter, AccountInterface> cell = new ComboBoxTableCell<>(accountsComboList);
				return cell;
			}
		});

		columnAmount.setCellFactory(new Callback<TableColumn<TransactionComponentModelAdapter, AmountModelAdapter>, TableCell<TransactionComponentModelAdapter, AmountModelAdapter>>() {
			@Override
			public TableCell<TransactionComponentModelAdapter, AmountModelAdapter> call(TableColumn<TransactionComponentModelAdapter, AmountModelAdapter> p) {
				AmountCellEditor<TransactionComponentModelAdapter> cell = new AmountCellEditor<>(new StringValidatorDouble());
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});

		//Enable/disable buttons
		deleteComponent.disableProperty().bind(transactionComponents.getSelectionModel().selectedIndexProperty().lessThan(0));
	}

	/**
	 * Sets the edited transaction
	 *
	 * @param transaction the edited transaction
	 */
	public void setTransaction(TransactionModelAdapter transaction) {
		if (this.transaction != null)
			transactionType.valueProperty().unbindBidirectional(this.transaction.typeProperty());
		this.transaction = null;
		if (transaction != null) {
			transactionType.valueProperty().bindBidirectional(transaction.typeProperty());
			this.transaction = transaction;
			transactionComponents.setItems(transaction.transactionComponentsProperty());
		} else {
			transactionComponents.setItems(FXCollections.<TransactionComponentModelAdapter>emptyObservableList());
		}
	}

	/**
	 * Assigns the DataManager instance
	 *
	 * @param dataManager the DataManager instance
	 */
	public void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;

		accountsComboList = dataManager.getAccounts();
		transactionType.setItems(dataManager.getTransactionTypes());
	}

	/**
	 * Add component button
	 */
	@FXML
	private void handleAddComponent() {
		transaction.createComponent();
	}

	/**
	 * Delete component button
	 */
	@FXML
	private void handleDeleteComponent() {
		TransactionComponentModelAdapter selectedItem = transactionComponents.getSelectionModel().getSelectedItem();
		if (selectedItem != null)
			transaction.deleteComponent(selectedItem);
	}
}
