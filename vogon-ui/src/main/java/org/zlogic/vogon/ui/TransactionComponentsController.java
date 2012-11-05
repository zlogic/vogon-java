/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zlogic.vogon.ui;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.ui.cell.AmountCellEditor;
import org.zlogic.vogon.ui.cell.ComboCellEditor;
import org.zlogic.vogon.ui.cell.StringValidatorDouble;

/**
 *
 * @author Dmitry
 */
public class TransactionComponentsController implements Initializable {
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	
	protected FinanceData financeData;
	protected FinanceTransaction transaction;
	
	@FXML
	private TableView<TransactionComponentModelAdapter> transactionComponents;
	
	@FXML ComboBox<TransactionTypeComboItem> transactionType;
	@FXML TableColumn<TransactionComponentModelAdapter,FinanceAccountModelAdapter> columnAccount;
	@FXML TableColumn<TransactionComponentModelAdapter,AmountAdapter> columnAmount;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		transactionComponents.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		columnAccount.setCellFactory(new Callback<TableColumn<TransactionComponentModelAdapter, FinanceAccountModelAdapter>, TableCell<TransactionComponentModelAdapter, FinanceAccountModelAdapter>>() {
			@Override
			public TableCell<TransactionComponentModelAdapter, FinanceAccountModelAdapter> call(TableColumn<TransactionComponentModelAdapter, FinanceAccountModelAdapter> p) {
				return new ComboCellEditor<>(getAccountsComboList());
			}
		});
		columnAccount.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<TransactionComponentModelAdapter, FinanceAccountModelAdapter>>() {
			@Override
			public void handle(TableColumn.CellEditEvent<TransactionComponentModelAdapter, FinanceAccountModelAdapter> t) {
				t.getRowValue().setAccount(t.getNewValue().getAccount());
			}
		});
		
		columnAmount.setCellFactory(new Callback<TableColumn<TransactionComponentModelAdapter, AmountAdapter>, TableCell<TransactionComponentModelAdapter, AmountAdapter>>() {
			@Override
			public TableCell<TransactionComponentModelAdapter, AmountAdapter> call(TableColumn<TransactionComponentModelAdapter, AmountAdapter> p) {
				return new AmountCellEditor<>(new StringValidatorDouble(),Pos.CENTER_RIGHT);
			}
		});
		columnAmount.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<TransactionComponentModelAdapter, AmountAdapter>>() {
			@Override
			public void handle(TableColumn.CellEditEvent<TransactionComponentModelAdapter, AmountAdapter> t) {
				t.getRowValue().setAmount(t.getNewValue().getBalance());
			}
		});
	}
	
	public void setTransaction(FinanceTransaction transaction){
		this.transaction = transaction;
		updateTransactionTypeCombo(transaction.getType());
		updateComponents();
	}
	
	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;
	}
	
	@FXML
	protected void handleAddComponent(ActionEvent event){
		TransactionComponent component = new TransactionComponent(null, transaction, 0);
		financeData.createTransactionComponent(component);
		transactionComponents.getItems().add(new TransactionComponentModelAdapter(component, financeData));
	}
	@FXML
	protected void handleDeleteComponent(ActionEvent event){
		TransactionComponentModelAdapter selectedItem = transactionComponents.getSelectionModel().getSelectedItem();
		if(selectedItem!=null)
			financeData.deleteTransactionComponent(selectedItem.getTransactionComponent());
		transactionComponents.getItems().remove(selectedItem);
	}
	@FXML
	protected void handleSetTransactionType(ActionEvent event){
		TransactionTypeComboItem newType = transactionType.getSelectionModel().getSelectedItem();
		if(newType!=null)
			financeData.setTransactionType(transaction, newType.getType());
	}
	
	protected void updateComponents(){
		transactionComponents.getItems().clear();
		for(TransactionComponent component:transaction.getComponents())
			transactionComponents.getItems().add(new TransactionComponentModelAdapter(component, financeData));
	}
	
	protected void updateTransactionTypeCombo(FinanceTransaction.Type type) {
		transactionType.setDisable(true);
		transactionType.getItems().clear();
		TransactionTypeComboItem selectedItem = null;
		for (FinanceTransaction.Type currentType : FinanceTransaction.Type.values())
			if (currentType != FinanceTransaction.Type.UNDEFINED) {
				TransactionTypeComboItem currentItem = new TransactionTypeComboItem(currentType);
				transactionType.getItems().add(currentItem);
				if (type == currentType)
					selectedItem = currentItem;
			}
		transactionType.getSelectionModel().select(selectedItem);
		transactionType.setDisable(false);
	}
	
	/**
	 * Returns a list of account items which can be rendered in a Combo box
	 * (used to specifically detect the selected item)
	 *
	 * @return the list of account items
	 */
	public List<FinanceAccountModelAdapter> getAccountsComboList() {
		List<FinanceAccountModelAdapter> items = new LinkedList<>();
		for (FinanceAccount account : financeData.getAccounts())
			if (account.getIncludeInTotal())
				items.add(new FinanceAccountModelAdapter(account));
		return items;
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
	}
}
