/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;

/**
 * Transactions tab controller.
 *
 * @author Dmitry Zolotukhin
 */
public class TransactionsController implements Initializable {

	private FinanceData financeData;
	@FXML
	private TableView<ModelTransaction> transactionsTable;
	@FXML
	private TableColumn<ModelTransaction, String> columnDescription;
	@FXML
	private TableColumn<ModelTransaction, String> columnDate;
	@FXML
	private TableColumn<ModelTransaction, String> columnTags;
	@FXML
	private TableColumn<ModelTransaction, String> columnAmount;
	@FXML
	private TableColumn<ModelTransaction, String> columnAccount;
	@FXML
	private Pagination transactionsTablePagination;

	@FXML
	private VBox transactionsVBox;
	/**
	 * Page size
	 */
	protected int pageSize = 100;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		//Init columns
		transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		
		transactionsTable.managedProperty().bind(transactionsTable.visibleProperty());
		transactionsVBox.getChildren().remove(transactionsTable);
		//transactionsTable.setVisible(false);
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
		
		List<ModelTransaction> transactionsList = new LinkedList<>();
		for (FinanceTransaction transaction : transactions)
			transactionsList.add(new ModelTransaction(transaction, financeData));
		transactionsTable.getItems().clear();
		transactionsTable.getItems().addAll(transactionsList);
	}
	
	protected void updateTransactions() {
		transactionsTablePagination.setPageCount(getPageCount());
		transactionsTablePagination.setPageFactory(new Callback<Integer, Node>(){
			@Override
			public Node call(Integer p) {
				updatePageTransactions(p);
				return transactionsTable;//transactionsTable;
			}
		});
	}

	public FinanceData getFinanceData() {
		return financeData;
	}

	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;
		updateTransactions();
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
