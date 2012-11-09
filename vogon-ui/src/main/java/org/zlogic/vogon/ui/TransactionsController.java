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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.events.TransactionEventHandler;
import org.zlogic.vogon.ui.adapter.TransactionModelAdapter;
import org.zlogic.vogon.ui.cell.DateCellEditor;
import org.zlogic.vogon.ui.cell.StringCellEditor;
import org.zlogic.vogon.ui.cell.StringValidatorDefault;
import org.zlogic.vogon.ui.cell.TransactionEditor;

/**
 * Transactions tab controller.
 *
 * @author Dmitry Zolotukhin
 */
public class TransactionsController implements Initializable {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	protected FinanceData financeData;
	@FXML
	protected TableView<TransactionModelAdapter> transactionsTable;
	@FXML
	protected TableColumn<TransactionModelAdapter, String> columnDescription;
	@FXML
	protected TableColumn<TransactionModelAdapter, Date> columnDate;
	@FXML
	protected TableColumn<TransactionModelAdapter, String> columnTags;
	@FXML
	protected TableColumn<TransactionModelAdapter, TransactionModelAdapter> columnAmount;
	@FXML
	protected TableColumn<TransactionModelAdapter, TransactionModelAdapter> columnAccount;
	@FXML
	protected Pagination transactionsTablePagination;
	@FXML
	protected VBox transactionsVBox;
	/**
	 * Page size
	 */
	protected int pageSize = 100;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		//Column sizes
		transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		//Remove transactions table since it will be used in paging
		transactionsTable.managedProperty().bind(transactionsTable.visibleProperty());
		transactionsVBox.getChildren().remove(transactionsTable);

		//COnfigure paging
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
				return new StringCellEditor<>(new StringValidatorDefault());
			}
		});
		columnDescription.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<TransactionModelAdapter, String>>() {
			@Override
			public void handle(CellEditEvent<TransactionModelAdapter, String> t) {
				t.getRowValue().setDescription(t.getNewValue());
			}
		});
		columnTags.setCellFactory(new Callback<TableColumn<TransactionModelAdapter, String>, TableCell<TransactionModelAdapter, String>>() {
			@Override
			public TableCell<TransactionModelAdapter, String> call(TableColumn<TransactionModelAdapter, String> p) {
				return new StringCellEditor<>(new StringValidatorDefault());
			}
		});
		columnTags.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<TransactionModelAdapter, String>>() {
			@Override
			public void handle(CellEditEvent<TransactionModelAdapter, String> t) {
				t.getRowValue().setDescription(t.getNewValue());
			}
		});
		columnDate.setCellFactory(new Callback<TableColumn<TransactionModelAdapter, Date>, TableCell<TransactionModelAdapter, Date>>() {
			@Override
			public TableCell<TransactionModelAdapter, Date> call(TableColumn<TransactionModelAdapter, Date> p) {
				DateCellEditor cell = new DateCellEditor<>();
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});
		columnTags.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<TransactionModelAdapter, String>>() {
			@Override
			public void handle(CellEditEvent<TransactionModelAdapter, String> t) {
				t.getRowValue().setDescription(t.getNewValue());
			}
		});
		columnAmount.setCellFactory(new Callback<TableColumn<TransactionModelAdapter, TransactionModelAdapter>, TableCell<TransactionModelAdapter, TransactionModelAdapter>>() {
			@Override
			public TableCell<TransactionModelAdapter, TransactionModelAdapter> call(TableColumn<TransactionModelAdapter, TransactionModelAdapter> p) {
				return new TransactionEditor(financeData, Pos.CENTER_RIGHT);
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
	 */
	protected void updatePageTransactions(int currentPage) {
		int firstTransactionIndex = currentPage * pageSize;
		int lastTransactionIndex = firstTransactionIndex + pageSize - 1;
		lastTransactionIndex = Math.min(lastTransactionIndex, financeData.getTransactionCount() - 1);
		firstTransactionIndex = financeData.getTransactionCount() - 1 - firstTransactionIndex;
		lastTransactionIndex = financeData.getTransactionCount() - 1 - lastTransactionIndex;
		List<FinanceTransaction> transactions = financeData.getTransactions(Math.min(firstTransactionIndex, lastTransactionIndex), Math.max(firstTransactionIndex, lastTransactionIndex));
		Collections.reverse(transactions);

		List<TransactionModelAdapter> transactionsList = new LinkedList<>();
		for (FinanceTransaction transaction : transactions)
			transactionsList.add(new TransactionModelAdapter(transaction, financeData));
		transactionsTable.getItems().clear();
		transactionsTable.getItems().addAll(transactionsList);
	}

	protected void updateTransactions() {
		transactionsTablePagination.setPageCount(getPageCount());
		updatePageTransactions(transactionsTablePagination.getCurrentPageIndex());
	}

	public FinanceData getFinanceData() {
		return financeData;
	}

	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;
		updateTransactions();
		financeData.setTransactionListener(new TransactionEventHandler() {
			//TODO: Add handling for account events
			protected FinanceData financeData;

			public TransactionEventHandler setFinanceData(FinanceData financeData) {
				this.financeData = financeData;
				return this;
			}

			@Override
			public void transactionCreated(FinanceTransaction newTransaction) {
				transactionsTablePagination.setCurrentPageIndex(0);
				int index = transactionsTable.getItems().indexOf(newTransaction);
				if (index >= 0)
					transactionsTable.getSelectionModel().select(index);
			}

			@Override
			public void transactionUpdated(FinanceTransaction updatedTransaction) {
				TransactionModelAdapter updatedTransactionModelAdapter = new TransactionModelAdapter(updatedTransaction, financeData);
				int index = transactionsTable.getItems().indexOf(updatedTransaction);
				if (index >= 0)
					transactionsTable.getItems().set(index, updatedTransactionModelAdapter);
			}

			@Override
			public void transactionDeleted(FinanceTransaction deletedTransaction) {
			}

			@Override
			public void transactionsUpdated() {
				updateTransactions();
			}
		}.setFinanceData(financeData));
	}

	/**
	 * Returns the page for a model row
	 *
	 * @param rowIndex the model row
	 * @return the page number
	 */
	protected int getRowPage(int rowIndex) {
		if (transactionsTablePagination.getCurrentPageIndex() < getPageCount())
			return rowIndex / pageSize;
		else
			return -1;
	}

	/**
	 * Returns the number of pages
	 *
	 * @return the number of pages
	 */
	public int getPageCount() {
		return financeData.getTransactionCount() / pageSize + 1;
	}

	/**
	 * Returns the generic page size
	 *
	 * @return the page size
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * Returns the page size for a specific page (last page may be smaller)
	 *
	 * @param pageIndex the page number
	 * @return the page size for a specific page
	 */
	public int getPageSize(int pageIndex) {
		if (financeData == null)
			return 0;
		return Math.min(pageSize, financeData.getTransactionCount() - transactionsTablePagination.getCurrentPageIndex() * pageSize);
	}
}
