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
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.data.events.AccountEventHandler;
import org.zlogic.vogon.data.events.TransactionEventHandler;
import org.zlogic.vogon.ui.adapter.AccountModelAdapter;
import org.zlogic.vogon.ui.adapter.AmountModelAdapter;
import org.zlogic.vogon.ui.adapter.TransactionComponentModelAdapter;
import org.zlogic.vogon.ui.cell.AmountCellEditor;
import org.zlogic.vogon.ui.cell.StringValidatorDouble;

/**
 * Controller for editing a transaction's components.
 *
 * @author Dmitry Zolotukhin
 */
public class TransactionComponentsController implements Initializable {

	/**
	 * Localization messages
	 */
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The FinanceData instance
	 */
	protected FinanceData financeData;
	/**
	 * The edited transaction
	 */
	protected FinanceTransaction transaction;
	/**
	 * The transaction components table
	 */
	@FXML
	private TableView<TransactionComponentModelAdapter> transactionComponents;
	/**
	 * The transaction type combo box
	 */
	@FXML
	private ComboBox<TransactionTypeComboItem> transactionType;
	/**
	 * The account column
	 */
	@FXML
	private TableColumn<TransactionComponentModelAdapter, AccountModelAdapter> columnAccount;
	/**
	 * The amount column
	 */
	@FXML
	private TableColumn<TransactionComponentModelAdapter, AmountModelAdapter> columnAmount;
	/**
	 * List of accounts for the accounts combo box
	 */
	protected ObservableList<AccountModelAdapter> accountsComboList = FXCollections.observableList(new LinkedList<AccountModelAdapter>());
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

		updateTransactionTypeCombo();

		//Cell editors
		columnAccount.setCellFactory(new Callback<TableColumn<TransactionComponentModelAdapter, AccountModelAdapter>, TableCell<TransactionComponentModelAdapter, AccountModelAdapter>>() {
			@Override
			public TableCell<TransactionComponentModelAdapter, AccountModelAdapter> call(TableColumn<TransactionComponentModelAdapter, AccountModelAdapter> p) {
				ComboBoxTableCell<TransactionComponentModelAdapter, AccountModelAdapter> cell = new ComboBoxTableCell<>(accountsComboList);
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
	public void setTransaction(FinanceTransaction transaction) {
		this.transaction = null;
		if (transaction != null)
			transactionType.getSelectionModel().select(new TransactionTypeComboItem(transaction.getType()));
		this.transaction = transaction;
		updateComponents();
	}

	/**
	 * Assigns the FinanceData instance
	 *
	 * @param financeData the FinanceData instance
	 */
	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;

		updateAccountsComboList();

		//Listen for Transaction events
		if (financeData.getTransactionListener() instanceof FinanceDataEventDispatcher) {
			((FinanceDataEventDispatcher) financeData.getTransactionListener()).addTransactionEventHandler(new TransactionEventHandler() {
				@Override
				public void transactionCreated(long transactionId) {
				}

				@Override
				public void transactionUpdated(long transactionId) {
					if (transaction != null && transactionId == transaction.getId())
						updateComponents();
				}

				@Override
				public void transactionDeleted(long transactionId) {
				}

				@Override
				public void transactionsUpdated() {
					updateComponents();
				}
			});
		}

		//Listen for Account events
		if (financeData.getTransactionListener() instanceof FinanceDataEventDispatcher) {
			((FinanceDataEventDispatcher) financeData.getTransactionListener()).addAccountEventHandler(new AccountEventHandler() {
				@Override
				public void accountCreated(long accountId) {
					updateAccountsComboList();
				}

				@Override
				public void accountUpdated(long accountId) {
					updateAccountsComboList();
				}

				@Override
				public void accountsUpdated() {
					updateAccountsComboList();
				}

				@Override
				public void accountDeleted(long accountId) {
					updateAccountsComboList();
				}
			});
		}
	}

	/**
	 * Add component button
	 */
	@FXML
	private void handleAddComponent() {
		TransactionComponent component = new TransactionComponent(null, transaction, 0);
		financeData.createTransactionComponent(component);
	}

	/**
	 * Delete component button
	 */
	@FXML
	private void handleDeleteComponent() {
		TransactionComponentModelAdapter selectedItem = transactionComponents.getSelectionModel().getSelectedItem();
		if (selectedItem != null)
			financeData.deleteTransactionComponent(selectedItem.getTransactionComponent());
		transactionComponents.getItems().remove(selectedItem);
	}

	/**
	 * Transaction type combo box has changed
	 */
	@FXML
	private void handleSetTransactionType() {
		TransactionTypeComboItem newType = transactionType.getSelectionModel().getSelectedItem();
		if (transaction != null && newType != null)
			financeData.setTransactionType(transaction, newType.getType());
	}

	/**
	 * Updates the transaction components table from database
	 */
	private void updateComponents() {
		transaction = financeData.getUpdatedTransactionFromDatabase(transaction);
		transactionComponents.getItems().clear();
		if (transaction == null)
			return;
		for (TransactionComponent component : transaction.getComponents())
			transactionComponents.getItems().add(new TransactionComponentModelAdapter(component, financeData));
	}

	/**
	 * Populates the transaction type combo box
	 */
	private void updateTransactionTypeCombo() {
		for (FinanceTransaction.Type currentType : FinanceTransaction.Type.values())
			if (currentType != FinanceTransaction.Type.UNDEFINED) {
				TransactionTypeComboItem currentItem = new TransactionTypeComboItem(currentType);
				transactionType.getItems().add(currentItem);
			}
	}

	/**
	 * Updates the list of account items which will be rendered in a Combo box
	 */
	private void updateAccountsComboList() {
		//Prepare updated list, try to reuse items as much as possible
		List<AccountModelAdapter> items = new LinkedList<>();
		if (financeData != null)
			for (FinanceAccount account : financeData.getAccounts())
				if (account.getIncludeInTotal()) {
					AccountModelAdapter accountAdapter = null;
					for (AccountModelAdapter adapter : accountsComboList)
						if (adapter.getAccount().equals(account) && adapter.nameProperty().get().getValue().equals(account.getName())) {
							accountAdapter = adapter;
							break;
						}
					accountAdapter = accountAdapter == null ? new AccountModelAdapter(account, financeData) : accountAdapter;
					items.add(accountAdapter);
				}
		//Recreate list ONLY if it's changed. Otherwier this messes up list selection operations.
		if (!items.equals(accountsComboList))
			accountsComboList.setAll(items);
	}

	/**
	 * Transaction type combo box item
	 */
	protected class TransactionTypeComboItem {

		/**
		 * The transaction type
		 */
		protected FinanceTransaction.Type type;

		/**
		 * Default constructor
		 *
		 * @param type the transaction type
		 */
		public TransactionTypeComboItem(FinanceTransaction.Type type) {
			this.type = type;
		}

		@Override
		public String toString() {
			switch (type) {
				case EXPENSEINCOME:
					return messages.getString("TRANSACTION_EXPENSE_INCOME");
				case TRANSFER:
					return messages.getString("TRANSACTION_TRANSFER");
				case UNDEFINED:
					return messages.getString("INVALID_TRANSACTION_TYPE");
			}
			return messages.getString("INVALID_TRANSACTION_TYPE");
		}

		/**
		 * Returns the transaction type
		 *
		 * @return the transaction type
		 */
		public FinanceTransaction.Type getType() {
			return type;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof TransactionTypeComboItem && ((TransactionTypeComboItem) obj).type.equals(type);
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 53 * hash + (this.type != null ? this.type.hashCode() : 0);
			return hash;
		}
	}
}
