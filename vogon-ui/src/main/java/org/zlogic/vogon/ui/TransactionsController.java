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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;

/**
 * Transactions tab controller.
 * @author Dmitry Zolotukhin
 */
public class TransactionsController implements Initializable{
	
	private FinanceData financeData;
	@FXML
	private TableView<ModelTransaction> transactionsTable;
	
	@FXML
	private TableColumn<ModelTransaction,String> columnDescription;
	@FXML
	private TableColumn<ModelTransaction,String> columnDate;
	@FXML
	private TableColumn<ModelTransaction,String> columnTags;
	@FXML
	private TableColumn<ModelTransaction,String> columnAmount;
	@FXML
	private TableColumn<ModelTransaction,String> columnAccount;
	
	@FXML
	private Pagination transactionsTablePagination;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		//Init columns
		transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}
	
	protected void updateTransactions(){
		List<ModelTransaction> transactionsList = new LinkedList<>();
		for(FinanceTransaction transaction : financeData.getTransactions())
			transactionsList.add(new ModelTransaction(transaction, financeData));
		transactionsTable.getItems().addAll(transactionsList);
	}
	
	
	
	public FinanceData getFinanceData() {
		return financeData;
	}

	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;
		updateTransactions();
	}
}
