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
import javax.persistence.EntityManager;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactedChange;
import org.zlogic.vogon.ui.adapter.AccountModelAdapter;
import org.zlogic.vogon.ui.adapter.AmountModelAdapter;
import org.zlogic.vogon.ui.adapter.DataManager;
import org.zlogic.vogon.ui.adapter.TransactionComponentModelAdapter;
import org.zlogic.vogon.ui.adapter.TransactionModelAdapter;
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
	protected ObservableList<AccountModelAdapter> accountsComboList;
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
	public void setTransaction(TransactionModelAdapter transaction) {
		this.transaction = null;
		if (transaction != null)
			transactionType.getSelectionModel().select(new TransactionTypeComboItem(transaction.typeProperty().get()));
		this.transaction = transaction;
		//TODO: Get components from transaction (bind)
	}

	/**
	 * Assigns the DataManager instance
	 *
	 * @param dataManager the DataManager instance
	 */
	public void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;

		accountsComboList = dataManager.getAccounts();
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
		transactionComponents.getItems().remove(selectedItem);
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
