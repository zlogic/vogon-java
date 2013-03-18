/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import org.zlogic.vogon.ui.adapter.DataManager;
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
	 * The DataManager instance
	 */
	protected DataManager dataManager;
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
	 * Delete transaction button
	 */
	@FXML
	private Button deleteTransaction;
	/**
	 * Duplicate transaction button
	 */
	@FXML
	private Button duplicateTransaction;
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
				TransactionEditor cell = new TransactionEditor(dataManager);
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
						if (!t1 && t1 != t) {
							transactionEditors.remove(cell);
							updateTransactions();
						}
					}
				}.setData(editingTransactionEditors, cell));
				return cell;
			}
		});

		//Add listeners to scene once the scene is assigned
		transactionsTable.sceneProperty().addListener(new ChangeListener<Scene>() {
			private List<ChangeListener<Boolean>> listeners = new LinkedList<>();

			public ChangeListener<Scene> createListeners() {
				listeners.add(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
						if (oldValue != newValue && !newValue)
							cancelEdit();
					}
				});
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends Scene> ov, Scene oldScene, Scene newScene) {
				for (ChangeListener listener : listeners) {
					if (oldScene != null && oldScene.getWindow() != null)
						oldScene.getWindow().focusedProperty().removeListener(listener);
					if (newScene != null && newScene.getWindow() != null)
						newScene.getWindow().focusedProperty().addListener(listener);
				}
			}
		}.createListeners());

		//Enable/disable buttons
		duplicateTransaction.disableProperty().bind(transactionsTable.getSelectionModel().selectedIndexProperty().lessThan(0));
		deleteTransaction.disableProperty().bind(transactionsTable.getSelectionModel().selectedIndexProperty().lessThan(0));
	}

	/**
	 * Create transaction button
	 */
	@FXML
	private void handleCreateTransaction() {
		cancelEdit();
		dataManager.createTransaction();
	}

	/**
	 * Duplicate transaction button
	 */
	@FXML
	private void handleDuplicateTransaction() {
		cancelEdit();
		if (transactionsTable.getSelectionModel().getSelectedItem() != null)
			dataManager.cloneTransaction(transactionsTable.getSelectionModel().getSelectedItem());
	}

	/**
	 * Delete transaction button
	 */
	@FXML
	private void handleDeleteTransaction() {
		TransactionModelAdapter selectedItem = transactionsTable.getSelectionModel().getSelectedItem();
		cancelEdit();
		if (selectedItem != null)
			dataManager.deleteTransaction(selectedItem);
	}

	/**
	 * Updates transactions for current page from database
	 *
	 * @param currentPage the currently selected page
	 */
	protected void updatePageTransactions(int currentPage) {
		dataManager.setVisibleTransactions(currentPage, pageSize);
	}

	/**
	 * Updates the transactions table
	 */
	protected void updateTransactions() {
		//Update only if editors are not active
		if (!editingTransactionEditors.isEmpty())
			return;
		transactionsTablePagination.setPageCount(getPageCount());
		updatePageTransactions(transactionsTablePagination.getCurrentPageIndex());
	}

	/**
	 * Cancels editing of TransactionEditors (needed on a tab switch)
	 */
	public void cancelEdit() {
		for (TransactionEditor editor : editingTransactionEditors)
			if (editor.isEditing())
				editor.cancelEdit();
		editingTransactionEditors.clear();
	}

	/**
	 * Assigns the DataManager instance
	 *
	 * @param dataManager the DataManager instance
	 */
	public void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;
		transactionsTable.setItems(dataManager.getTransactions());
		updateTransactions();

		//Listen for Transaction events
		//FIXME URGENT
		/*
		 if (financeData.getAccountListener() instanceof FinanceDataEventDispatcher) {
		 ((FinanceDataEventDispatcher) financeData.getAccountListener()).addTransactionEventHandler(new TransactionEventHandler() {
		 @Override
		 public void transactionCreated(long transactionId) {
		 //Update only if editors are not active
		 if (!editingTransactionEditors.isEmpty())
		 return;
		 transactionsTablePagination.setCurrentPageIndex(0);
		 for (TransactionModelAdapter adapter : transactionsTable.getItems())
		 if (adapter.getTransaction().getId() == transactionId) {
		 transactionsTable.getSelectionModel().select(adapter);
		 break;
		 }
		 }
		 });
		 }
		 */
	}

	/**
	 * Returns the number of pages
	 *
	 * @return the number of pages
	 */
	public int getPageCount() {
		return dataManager.getPageCount(pageSize);
	}
}
