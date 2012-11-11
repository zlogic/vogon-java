/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.Report;
import org.zlogic.vogon.data.events.AccountEventHandler;
import org.zlogic.vogon.data.events.TransactionEventHandler;
import org.zlogic.vogon.ui.adapter.AccountModelAdapter;
import org.zlogic.vogon.ui.adapter.AmountModelAdapter;
import org.zlogic.vogon.ui.adapter.ObjectWithStatus;
import org.zlogic.vogon.ui.cell.DateConverter;

/**
 *
 * @author Dmitry Zolotukhin
 */
public class AnalyticsController implements Initializable {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The report generator
	 */
	protected Report report;
	/**
	 * The finance data reference
	 */
	protected FinanceData financeData;
	protected DateFormat dateFormat;
	@FXML
	protected TextField startDateField;
	@FXML
	protected TextField endDateField;
	@FXML
	protected CheckBox transferTransactionsCheckbox;
	@FXML
	protected CheckBox incomeTransactionsCheckbox;
	@FXML
	protected CheckBox expenseTransactionsCheckbox;
	@FXML
	protected TableView<TagSelectionAdapter> tagsSelectionTable;
	@FXML
	protected TableColumn<TagSelectionAdapter, String> tagsSelectionTagColumn;
	@FXML
	protected TableColumn<TagSelectionAdapter, Boolean> tagsSelectionShowColumn;
	@FXML
	protected TableView<AccountSelectionAdapter> accountsSelectionTable;
	@FXML
	protected TableColumn<AccountSelectionAdapter, AccountModelAdapter> accountsSelectionAccountColumn;
	@FXML
	protected TableColumn<AccountSelectionAdapter, Boolean> accountsSelectionShowColumn;
	@FXML
	protected TableView<TagResultAdapter> tagsResultTable;
	@FXML
	protected TableColumn<TagResultAdapter, String> tagsResultTagColumn;
	@FXML
	protected TableColumn<TagResultAdapter, AmountModelAdapter> tagsResultAmountColumn;
	@FXML
	protected TableView<TransactionResultAdapter> transactionsResultTable;
	@FXML
	protected TableColumn<TransactionResultAdapter, String> transactionsResultTransactionColumn;
	@FXML
	protected TableColumn<TransactionResultAdapter, Date> transactionsResultDateColumn;
	@FXML
	protected TableColumn<TransactionResultAdapter, AmountModelAdapter> transactionsResultAmountColumn;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		dateFormat = new SimpleDateFormat(messages.getString("PARSER_DATE"));
		//Configure columns
		tagsSelectionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		accountsSelectionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		tagsResultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		transactionsResultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		//Configure checkbox columns
		tagsSelectionShowColumn.setCellFactory(new Callback<TableColumn<TagSelectionAdapter, Boolean>, TableCell<TagSelectionAdapter, Boolean>>() {
			@Override
			public TableCell<TagSelectionAdapter, Boolean> call(TableColumn<TagSelectionAdapter, Boolean> p) {
				CheckBoxTableCell cell = new CheckBoxTableCell<>();
				cell.setConverter(ObjectWithStatus.getConverter());
				cell.setEditable(true);
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});

		accountsSelectionShowColumn.setCellFactory(new Callback<TableColumn<AccountSelectionAdapter, Boolean>, TableCell<AccountSelectionAdapter, Boolean>>() {
			@Override
			public TableCell<AccountSelectionAdapter, Boolean> call(TableColumn<AccountSelectionAdapter, Boolean> p) {
				CheckBoxTableCell cell = new CheckBoxTableCell<>();
				cell.setSelectedStateCallback(new Callback<Integer, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(Integer p) {
						return accountsSelectionTable.getItems().get(p).enabledProperty();
					}
				});
				cell.setAlignment(Pos.CENTER);
				cell.setEditable(true);
				return cell;
			}
		});

		tagsResultAmountColumn.setCellFactory(new Callback<TableColumn<TagResultAdapter, AmountModelAdapter>, TableCell<TagResultAdapter, AmountModelAdapter>>() {
			@Override
			public TableCell<TagResultAdapter, AmountModelAdapter> call(TableColumn<TagResultAdapter, AmountModelAdapter> p) {
				TextFieldTableCell cell = new TextFieldTableCell<>();
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});

		transactionsResultAmountColumn.setCellFactory(new Callback<TableColumn<TransactionResultAdapter, AmountModelAdapter>, TableCell<TransactionResultAdapter, AmountModelAdapter>>() {
			@Override
			public TableCell<TransactionResultAdapter, AmountModelAdapter> call(TableColumn<TransactionResultAdapter, AmountModelAdapter> p) {
				TextFieldTableCell cell = new TextFieldTableCell<>();
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});

		transactionsResultDateColumn.setCellFactory(new Callback<TableColumn<TransactionResultAdapter, Date>, TableCell<TransactionResultAdapter, Date>>() {
			@Override
			public TableCell<TransactionResultAdapter, Date> call(TableColumn<TransactionResultAdapter, Date> p) {
				TextFieldTableCell cell = new TextFieldTableCell<>();
				cell.setConverter(new DateConverter(DateFormat.getDateInstance(DateFormat.LONG)));
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});
	}

	@FXML
	protected void handleGenerateReport() {
		//Set report parameters
		try {
			report.setEarliestDate(dateFormat.parse(startDateField.getText()));
			report.setLatestDate(dateFormat.parse(endDateField.getText()));
		} catch (ParseException ex) {
			Logger.getLogger(AnalyticsController.class.getName()).log(Level.SEVERE, null, ex);
		}

		List<FinanceAccount> selectedAccounts = new LinkedList<>();
		for (AccountSelectionAdapter accountAdapter : accountsSelectionTable.getItems())
			selectedAccounts.add(accountAdapter.accountProperty().get().getAccount());
		report.setSelectedAccounts(selectedAccounts);

		List<String> selectedTags = new LinkedList<>();
		for (TagSelectionAdapter tagAdapter : tagsSelectionTable.getItems())
			selectedTags.add(tagAdapter.tagProperty().get());
		report.setSelectedTags(selectedTags);

		report.setEnabledTransferTransactions(transferTransactionsCheckbox.selectedProperty().get());
		report.setEnabledIncomeTransactions(incomeTransactionsCheckbox.selectedProperty().get());
		report.setEnabledExpenseTransactions(expenseTransactionsCheckbox.selectedProperty().get());

		updateTagsResultTable(report.getTagExpenses());
		updateTransactionsResultTable(report.getTransactions());//TODO: add paging
	}

	protected void updateTagsSelectionTable() {
		tagsSelectionTable.getItems().removeAll(tagsSelectionTable.getItems());
		for (String tag : report.getAllTags())
			tagsSelectionTable.getItems().add(new TagSelectionAdapter(tag, true));
	}

	protected void updateAccountsSelectionTable() {
		accountsSelectionTable.getItems().removeAll(accountsSelectionTable.getItems());
		for (FinanceAccount account : report.getAllAccounts())
			accountsSelectionTable.getItems().add(new AccountSelectionAdapter(new AccountModelAdapter(account, financeData), account.getIncludeInTotal()));
	}

	protected void updateTagsResultTable(List<Report.TagExpense> values) {
		for (Report.TagExpense tagExpense : values)
			tagsResultTable.getItems().add(new TagResultAdapter(tagExpense.getTag(), tagExpense.getAmount(), tagExpense.getCurrency(), tagExpense.isCurrencyConverted()));
	}

	protected void updateTransactionsResultTable(List<FinanceTransaction> values) {
		for (FinanceTransaction transaction : values)
			transactionsResultTable.getItems().add(new TransactionResultAdapter(transaction, financeData));
	}

	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;
		report = new Report(financeData);

		startDateField.setText(dateFormat.format(report.getEarliestDate()));
		endDateField.setText(dateFormat.format(report.getLatestDate()));

		if (financeData.getAccountListener() instanceof FinanceDataEventDispatcher) {
			((FinanceDataEventDispatcher) financeData.getAccountListener()).addAccountEventHandler(new AccountEventHandler() {
				@Override
				public void accountCreated(FinanceAccount newAccount) {
					updateAccountsSelectionTable();
				}

				@Override
				public void accountUpdated(FinanceAccount updatedAccount) {
					updateAccountsSelectionTable();
				}

				@Override
				public void accountsUpdated() {
					updateAccountsSelectionTable();
				}

				@Override
				public void accountDeleted(FinanceAccount deletedAccount) {
					updateAccountsSelectionTable();
				}
			});
		}


		if (financeData.getAccountListener() instanceof FinanceDataEventDispatcher) {
			((FinanceDataEventDispatcher) financeData.getAccountListener()).addTransactionEventHandler(new TransactionEventHandler() {
				@Override
				public void transactionCreated(FinanceTransaction newTransaction) {
					updateTagsSelectionTable();
				}

				@Override
				public void transactionUpdated(FinanceTransaction updatedTransaction) {
					updateTagsSelectionTable();
				}

				@Override
				public void transactionsUpdated() {
				}

				@Override
				public void transactionDeleted(FinanceTransaction deletedTransaction) {
					updateTagsSelectionTable();
				}
			});
		}
		updateTagsSelectionTable();
		updateAccountsSelectionTable();
	}

	protected class AccountSelectionAdapter {

		protected final ObjectProperty<AccountModelAdapter> account = new SimpleObjectProperty();
		protected final BooleanProperty enabled = new SimpleBooleanProperty();

		public AccountSelectionAdapter(AccountModelAdapter account, boolean enabled) {
			this.account.set(account);
			this.enabled.set(enabled);
		}

		public ObjectProperty<AccountModelAdapter> accountProperty() {
			return account;
		}

		public BooleanProperty enabledProperty() {
			return enabled;
		}
	}

	protected class TagSelectionAdapter {

		protected final StringProperty tag = new SimpleStringProperty();
		protected final BooleanProperty enabled = new SimpleBooleanProperty();

		public TagSelectionAdapter(String tag, boolean enabled) {
			this.tag.set(tag);
			this.enabled.set(enabled);
		}

		public StringProperty tagProperty() {
			return tag;
		}

		public BooleanProperty enabledProperty() {
			return enabled;
		}
	}

	protected class TagResultAdapter {

		protected final StringProperty tag = new SimpleStringProperty();
		protected final ObjectProperty<AmountModelAdapter> amount = new SimpleObjectProperty<>();

		public TagResultAdapter(String tag, double amount, Currency currency, boolean isCurrencyConverted) {
			this.tag.set(tag);
			this.amount.set(new AmountModelAdapter(amount, true, currency, isCurrencyConverted, FinanceTransaction.Type.UNDEFINED));
		}

		public StringProperty tagProperty() {
			return tag;
		}

		public ObjectProperty<AmountModelAdapter> amountProperty() {
			return amount;
		}
	}

	protected class TransactionResultAdapter {

		protected final StringProperty description = new SimpleStringProperty();
		protected final ObjectProperty<AmountModelAdapter> amount = new SimpleObjectProperty<>();
		protected final ObjectProperty<Date> date = new SimpleObjectProperty<>();

		public TransactionResultAdapter(FinanceTransaction transaction, FinanceData financeData) {
			this.description.set(transaction.getDescription());
			double transactionAmount;
			Currency transactionCurrency;
			if (transaction.getCurrencies().size() == 1) {
				transactionAmount = transaction.getAmount();
				transactionCurrency = transaction.getCurrencies().get(0);
			} else {
				transactionAmount = financeData.getAmountInCurrency(transaction, financeData.getDefaultCurrency());
				transactionCurrency = financeData.getDefaultCurrency();
			}
			this.amount.set(new AmountModelAdapter(transactionAmount, transaction.isAmountOk(), transactionCurrency, transaction.getCurrencies().size() != 1, transaction.getType()));
			date.set(transaction.getDate());
		}

		public StringProperty descriptionProperty() {
			return description;
		}

		public ObjectProperty<AmountModelAdapter> amountProperty() {
			return amount;
		}

		public ObjectProperty<Date> dateProperty() {
			return date;
		}
	}
}
