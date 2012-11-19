/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.events.AccountEventHandler;
import org.zlogic.vogon.data.events.TransactionEventHandler;
import org.zlogic.vogon.ui.adapter.TransactionModelAdapter;
import org.zlogic.vogon.ui.cell.DateCellEditor;
import org.zlogic.vogon.ui.cell.TransactionEditor;

/**
 * Transactions pane controller.
 *
 * @author Dmitry Zolotukhin
 */
public class TransactionsController implements Initializable {

	/**
	 * The FinanceData instance
	 */
	protected FinanceData financeData;
	/**
	 * The transactions list table
	 */
	@FXML
	private TableView<TransactionModelAdapter> transactionsTable;
	/**
	 * The transaction description column
	 */
	@FXML
	private TableColumn<TransactionModelAdapter, String> columnDescription;
	/**
	 * The transaction date column
	 */
	@FXML
	private TableColumn<TransactionModelAdapter, Date> columnDate;
	/**
	 * The transaction tags column
	 */
	@FXML
	private TableColumn<TransactionModelAdapter, List<String>> columnTags;
	/**
	 * The transaction amount column
	 */
	@FXML
	private TableColumn<TransactionModelAdapter, TransactionModelAdapter> columnAmount;
	/**
	 * Pagination control
	 */
	@FXML
	private Pagination transactionsTablePagination;
	/**
	 * Toplevel control
	 */
	@FXML
	private VBox transactionsVBox;
	/**
	 * The list of currently editing TransactionEditors
	 */
	protected List<TransactionEditor> editingTransactionEditors = new LinkedList<>();
	/**
	 * Page size
	 */
	protected int pageSize = 100;

	/**
	 * Initializes the Transactions Controller
	 *
	 * @param url the FXML URL
	 * @param rb the FXML ResourceBundle
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		//Column sizes
		transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		//Remove transactions table since it will be used in paging
		transactionsTable.managedProperty().bind(transactionsTable.visibleProperty());
		transactionsVBox.getChildren().remove(transactionsTable);

		//Configure paging
		transactionsTablePagination.setPageFactory(new Callback<Integer, Node>() {
			@Override
			public Node call(Integer p) {
				updatePageTransactions(p);
				return transactionsTable;//transactionsTable;
			}
		});

		//Cell editors
		columnDescription.setCellFactory(new Callback<TableColumn<TransactionModelAdapter, String>, TableCell<TransactionModelAdapter, String>>() {
			@Override
			public TableCell<TransactionModelAdapter, String> call(TableColumn<TransactionModelAdapter, String> p) {
				TextFieldTableCell<TransactionModelAdapter, String> cell = new TextFieldTableCell<>();
				cell.setConverter(new DefaultStringConverter());
				return cell;
			}
		});
		columnTags.setCellFactory(new Callback<TableColumn<TransactionModelAdapter, List<String>>, TableCell<TransactionModelAdapter, List<String>>>() {
			@Override
			public TableCell<TransactionModelAdapter, List<String>> call(TableColumn<TransactionModelAdapter, List<String>> p) {
				TextFieldTableCell<TransactionModelAdapter, List<String>> cell = new TextFieldTableCell<>();
				cell.setConverter(TransactionModelAdapter.getTagsListConverter());
				return cell;
			}
		});
		columnDate.setCellFactory(new Callback<TableColumn<TransactionModelAdapter, Date>, TableCell<TransactionModelAdapter, Date>>() {
			@Override
			public TableCell<TransactionModelAdapter, Date> call(TableColumn<TransactionModelAdapter, Date> p) {
				DateCellEditor<TransactionModelAdapter> cell = new DateCellEditor<>();
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});
		columnAmount.setCellFactory(new Callback<TableColumn<TransactionModelAdapter, TransactionModelAdapter>, TableCell<TransactionModelAdapter, TransactionModelAdapter>>() {
			@Override
			public TableCell<TransactionModelAdapter, TransactionModelAdapter> call(TableColumn<TransactionModelAdapter, TransactionModelAdapter> p) {
				TransactionEditor cell = new TransactionEditor(financeData);
				cell.setAlignment(Pos.CENTER_RIGHT);
				cell.editingProperty().addListener(new javafx.beans.value.ChangeListener<Boolean>() {
					protected List<TransactionEditor> transactionEditors;
					protected TransactionEditor cell;

					public javafx.beans.value.ChangeListener<Boolean> setData(List<TransactionEditor> transactionEditors, TransactionEditor cell) {
						this.transactionEditors = transactionEditors;
						this.cell = cell;
						return this;
					}

					@Override
					public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
						if (t1 && t1 != t)
							transactionEditors.add(cell);
						if (!t1 && t1 != t){
							transactionEditors.remove(cell);
							updateTransactions();
						}
					}
				}.setData(editingTransactionEditors, cell));
				return cell;
			}
		});
	}

	@FXML
	private void handleCreateTransaction() {
		financeData.createTransaction(new FinanceTransaction("", new String[0], new Date(), FinanceTransaction.Type.EXPENSEINCOME));//NOI18N
	}

	@FXML
	private void handleDeleteTransaction() {
		TransactionModelAdapter selectedItem = transactionsTable.getSelectionModel().getSelectedItem();
		if (selectedItem != null)
			financeData.deleteTransaction(selectedItem.getTransaction());
	}

	/**
	 * Updates transactions for current page from database
	 *
	 * @param currentPage the currently selected page
	 */
	protected void updatePageTransactions(int currentPage) {
		//Configure the transactions indexes
		int firstTransactionIndex = currentPage * pageSize;
		int lastTransactionIndex = firstTransactionIndex + pageSize - 1;
		lastTransactionIndex = Math.min(lastTransactionIndex, financeData.getTransactionCount() - 1);
		firstTransactionIndex = financeData.getTransactionCount() - 1 - firstTransactionIndex;
		lastTransactionIndex = financeData.getTransactionCount() - 1 - lastTransactionIndex;
		List<FinanceTransaction> transactions = financeData.getTransactions(Math.min(firstTransactionIndex, lastTransactionIndex), Math.max(firstTransactionIndex, lastTransactionIndex));
		Collections.reverse(transactions);

		//Update the tables
		List<TransactionModelAdapter> transactionsList = new LinkedList<>();
		for (FinanceTransaction transaction : transactions)
			transactionsList.add(new TransactionModelAdapter(transaction, financeData));
		transactionsTable.getItems().clear();
		transactionsTable.getItems().addAll(transactionsList);
	}

	/**
	 * Updates the transactions table
	 */
	protected void updateTransactions() {
		//Update only if editors are not active
		if(!editingTransactionEditors.isEmpty())
			return;
		transactionsTablePagination.setPageCount(getPageCount());
		updatePageTransactions(transactionsTablePagination.getCurrentPageIndex());
	}

	/**
	 * Cancels editing of TransactionEditors (needed on a tab switch)
	 */
	public void cancelEdit() {
		//TODO: also cancel when the window loses focus
		for (TransactionEditor editor : editingTransactionEditors)
			if (editor.isEditing())
				editor.cancelEdit();
		editingTransactionEditors.clear();
	}

	/**
	 * Assigns the FinanceData instance
	 *
	 * @param financeData the FinanceData instance
	 */
	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;
		updateTransactions();

		//Listen for Transaction events
		if (financeData.getAccountListener() instanceof FinanceDataEventDispatcher) {
			((FinanceDataEventDispatcher) financeData.getAccountListener()).addTransactionEventHandler(new TransactionEventHandler() {
				@Override
				public void transactionCreated(long transactionId) {
					//Update only if editors are not active
					if(!editingTransactionEditors.isEmpty())
						return;
					transactionsTablePagination.setCurrentPageIndex(0);
					for (TransactionModelAdapter adapter : transactionsTable.getItems())
						if (adapter.getTransaction().getId() == transactionId) {
							transactionsTable.getSelectionModel().select(adapter);
							break;
						}
				}

				@Override
				public void transactionUpdated(long transactionId) {
					//Update only if editors are not active
					if(!editingTransactionEditors.isEmpty())
						return;
					updateTransactions();
				}

				@Override
				public void transactionDeleted(long transactionId) {
				}

				@Override
				public void transactionsUpdated() {
					//Update only if editors are not active
					if(!editingTransactionEditors.isEmpty())
						return;
					updateTransactions();
				}
			});
		}

		//Listen for Account events
		if (financeData.getAccountListener() instanceof FinanceDataEventDispatcher) {
			((FinanceDataEventDispatcher) financeData.getAccountListener()).addAccountEventHandler(new AccountEventHandler() {
				@Override
				public void accountCreated(long accountId) {
					updateTransactions();
				}

				@Override
				public void accountUpdated(long accountId) {
					updateTransactions();
				}

				@Override
				public void accountDeleted(long accountId) {
					cancelEdit();
					updateTransactions();
				}

				@Override
				public void accountsUpdated() {
					updateTransactions();
				}
			});
		}
	}

	/**
	 * Returns the number of pages
	 *
	 * @return the number of pages
	 */
	public int getPageCount() {
		return financeData.getTransactionCount() / pageSize + 1;
	}
}
