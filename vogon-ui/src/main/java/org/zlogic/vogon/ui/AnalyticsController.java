/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
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
 * The Analytics pane
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
	/**
	 * The date format for resulting tables
	 */
	protected DateFormat dateFormat;
	/**
	 * The processor for background tasks
	 */
	private Callback<Task, Void> backgroundTaskProcessor;
	@FXML
	private TextField startDateField;
	@FXML
	private TextField endDateField;
	@FXML
	private CheckBox transferTransactionsCheckbox;
	@FXML
	private CheckBox incomeTransactionsCheckbox;
	@FXML
	private CheckBox expenseTransactionsCheckbox;
	@FXML
	private TableView<TagSelectionAdapter> tagsSelectionTable;
	@FXML
	private TableColumn<TagSelectionAdapter, Boolean> tagsSelectionShowColumn;
	@FXML
	private TableView<AccountSelectionAdapter> accountsSelectionTable;
	@FXML
	private TableColumn<AccountSelectionAdapter, Boolean> accountsSelectionShowColumn;
	@FXML
	private TableView<TagResultAdapter> tagsResultTable;
	@FXML
	private TableColumn<TagResultAdapter, AmountModelAdapter> tagsResultAmountColumn;
	@FXML
	private TableView<TransactionResultAdapter> transactionsResultTable;
	@FXML
	private TableColumn<TransactionResultAdapter, Date> transactionsResultDateColumn;
	@FXML
	private TableColumn<TransactionResultAdapter, AmountModelAdapter> transactionsResultAmountColumn;
	@FXML
	private PieChart tagsChart;
	@FXML
	private LineChart<String, Double> balanceChart;

	/**
	 * Initializes the Analytics Controller
	 *
	 * @param url the FXML URL
	 * @param rb the FXML ResourceBundle
	 */
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

		//Configure result columns
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

	/**
	 * Generate Report button handler
	 */
	@FXML
	private void handleGenerateReport() {
		Task task = new Task() {
			@Override
			protected Void call() {
				updateProgress(-1, 1);
				updateMessage(messages.getString("TASK_GENERATING_REPORT"));
				//Set report parameters
				try {
					report.setEarliestDate(dateFormat.parse(startDateField.getText()));
					report.setLatestDate(dateFormat.parse(endDateField.getText()));
				} catch (ParseException ex) {
					MessageDialog.showDialog(messages.getString("ANALYTICS_REPORT_EXCEPTION_DIALOG_TITLE"), new MessageFormat(messages.getString("ANALYTICS_REPORT_EXCEPTION_DIALOG_TEXT")).format(new Object[]{ex.getLocalizedMessage(), org.zlogic.vogon.data.Utils.getStackTrace(ex)}), true);
					Logger.getLogger(AnalyticsController.class.getName()).log(Level.SEVERE, null, ex);
				}

				List<FinanceAccount> selectedAccounts = new LinkedList<>();
				for (AccountSelectionAdapter accountAdapter : accountsSelectionTable.getItems())
					if (accountAdapter.enabledProperty().get())
						selectedAccounts.add(accountAdapter.accountProperty().get().getAccount());
				report.setSelectedAccounts(selectedAccounts);

				List<String> selectedTags = new LinkedList<>();
				for (TagSelectionAdapter tagAdapter : tagsSelectionTable.getItems())
					if (tagAdapter.enabledProperty().get())
						selectedTags.add(tagAdapter.tagProperty().get());
				report.setSelectedTags(selectedTags);

				report.setEnabledTransferTransactions(transferTransactionsCheckbox.selectedProperty().get());
				report.setEnabledIncomeTransactions(incomeTransactionsCheckbox.selectedProperty().get());
				report.setEnabledExpenseTransactions(expenseTransactionsCheckbox.selectedProperty().get());

				//Generate the report
				List<Report.TagExpense> tagExpenses = report.getTagExpenses();
				List<FinanceTransaction> transactions = report.getTransactions();
				Map<Date, Double> balanceGraph = report.getAccountsBalanceGraph();

				//Update the UI
				Platform.runLater(new Runnable() {
					protected List<Report.TagExpense> tagExpenses;
					protected List<FinanceTransaction> transactions;
					protected Map<Date, Double> balanceGraph;

					public Runnable setData(List<Report.TagExpense> tagExpenses, List<FinanceTransaction> transactions, Map<Date, Double> balanceGraph) {
						this.tagExpenses = tagExpenses;
						this.transactions = transactions;
						this.balanceGraph = balanceGraph;
						return this;
					}

					@Override
					public void run() {
						updateTagsResultTable(tagExpenses);
						updateTransactionsResultTable(transactions);//TODO: add paging
						updateTagsChart(tagExpenses);
						updateBalanceChart(balanceGraph);
					}
				}.setData(tagExpenses, transactions, balanceGraph));

				updateProgress(1, 1);
				updateMessage("");//NOI18N
				return null;
			}
		};
		if (backgroundTaskProcessor == null)
			task.run();
		else
			backgroundTaskProcessor.call(task);
	}

	/**
	 * Updates the table for selecting tags
	 */
	private void updateTagsSelectionTable() {
		tagsSelectionTable.getItems().clear();
		List<String> tags = report.getAllTags();
		Collections.sort(tags);
		for (String tag : tags)
			tagsSelectionTable.getItems().add(new TagSelectionAdapter(tag, true));
	}

	/**
	 * Updates the table for selecting accounts
	 */
	private void updateAccountsSelectionTable() {
		accountsSelectionTable.getItems().clear();
		for (FinanceAccount account : report.getAllAccounts())
			accountsSelectionTable.getItems().add(new AccountSelectionAdapter(new AccountModelAdapter(account, financeData), account.getIncludeInTotal()));
	}

	/**
	 * Updates the resulting tags table
	 *
	 * @param values the list of expenses grouped by tag
	 */
	private void updateTagsResultTable(List<Report.TagExpense> values) {
		tagsResultTable.getItems().clear();
		for (Report.TagExpense tagExpense : values)
			tagsResultTable.getItems().add(new TagResultAdapter(tagExpense.getTag(), tagExpense.getAmount(), tagExpense.getCurrency(), tagExpense.isCurrencyConverted()));
	}

	/**
	 * Updates the resulting transactions table
	 *
	 * @param values the list of transactions matching the criteria
	 */
	private void updateTransactionsResultTable(List<FinanceTransaction> values) {
		transactionsResultTable.getItems().clear();
		for (FinanceTransaction transaction : values)
			transactionsResultTable.getItems().add(new TransactionResultAdapter(transaction, financeData));
	}

	/**
	 * Updates the tags chart
	 *
	 * @param values the list of expenses grouped by tag
	 */
	private void updateTagsChart(List<Report.TagExpense> values) {
		ObservableList<PieChart.Data> data = FXCollections.observableList(new LinkedList<PieChart.Data>());
		for (Report.TagExpense tagExpense : values) {
			TagResultAdapter tagResult = new TagResultAdapter(tagExpense.getTag(), tagExpense.getAmount(), tagExpense.getCurrency(), tagExpense.isCurrencyConverted());
			String tagLabel = MessageFormat.format(messages.getString("PIECHART_TAG_FORMAT"), new Object[]{tagExpense.getTag(), tagResult.amountProperty().get().toString()});
			data.add(new PieChart.Data(tagLabel, financeData.getExchangeRate(tagExpense.getCurrency(), financeData.getDefaultCurrency())
					* Math.abs(tagExpense.getAmount())));
		}

		tagsChart.dataProperty().set(data);
	}

	/**
	 * Updates the balance graph
	 *
	 * @param values the balance graph, grouped by date
	 */
	private void updateBalanceChart(Map<Date, Double> values) {
		if (!balanceChart.getData().isEmpty())
			balanceChart.getXAxis().invalidateRange(new LinkedList<String>());

		balanceChart.getData().clear();

		XYChart.Series<String, Double> series = new XYChart.Series<>();
		for (Map.Entry<Date, Double> entry : values.entrySet())
			series.getData().add(new XYChart.Data<>(DateFormat.getDateInstance(DateFormat.FULL).format(entry.getKey()), entry.getValue(), entry.getKey()));
		balanceChart.getData().add(series);
	}

	/**
	 * Assigns the FinanceData instance
	 *
	 * @param financeData the FinanceData instance
	 */
	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;

		//Update the form
		report = new Report(financeData);

		startDateField.setText(dateFormat.format(report.getEarliestDate()));
		endDateField.setText(dateFormat.format(report.getLatestDate()));

		updateTagsSelectionTable();
		updateAccountsSelectionTable();

		//Listen for Account events
		if (financeData.getAccountListener() instanceof FinanceDataEventDispatcher) {
			((FinanceDataEventDispatcher) financeData.getAccountListener()).addAccountEventHandler(new AccountEventHandler() {
				@Override
				public void accountCreated(long accountId) {
					updateAccountsSelectionTable();
				}

				@Override
				public void accountUpdated(long accountId) {
					updateAccountsSelectionTable();
				}

				@Override
				public void accountsUpdated() {
					updateAccountsSelectionTable();
				}

				@Override
				public void accountDeleted(long accountId) {
					updateAccountsSelectionTable();
				}
			});
		}

		//Listen for Transaction events
		if (financeData.getAccountListener() instanceof FinanceDataEventDispatcher) {
			((FinanceDataEventDispatcher) financeData.getAccountListener()).addTransactionEventHandler(new TransactionEventHandler() {
				@Override
				public void transactionCreated(long transactionId) {
					updateTagsSelectionTable();
				}

				@Override
				public void transactionUpdated(long transactionId) {
					updateTagsSelectionTable();
				}

				@Override
				public void transactionsUpdated() {
				}

				@Override
				public void transactionDeleted(long transactionId) {
					updateTagsSelectionTable();
				}
			});
		}
	}

	/**
	 * Sets the background task processor callback
	 *
	 * @param backgroundTaskProcessor the background task processor callback
	 * (otherwise the report generation will run in the current thread)
	 */
	public void setBackgroundTaskProcessor(Callback<Task, Void> backgroundTaskProcessor) {
		this.backgroundTaskProcessor = backgroundTaskProcessor;
	}

	/**
	 * Class for representing an account with a checkbox to select accounts to
	 * be used in the report
	 */
	protected class AccountSelectionAdapter {

		/**
		 * The associated account property
		 */
		private final ObjectProperty<AccountModelAdapter> account = new SimpleObjectProperty();
		/**
		 * The account enabled property
		 */
		private final BooleanProperty enabled = new SimpleBooleanProperty();

		/**
		 * Constructs a AccountSelectionAdapter
		 *
		 * @param account the associated account
		 * @param enabled true if the account should be enabled
		 */
		public AccountSelectionAdapter(AccountModelAdapter account, boolean enabled) {
			this.account.set(account);
			this.enabled.set(enabled);
		}

		/**
		 * Returns the account property
		 *
		 * @return the account property
		 */
		public ObjectProperty<AccountModelAdapter> accountProperty() {
			return account;
		}

		/**
		 * Returns the account enabled property
		 *
		 * @return the account enabled property
		 */
		public BooleanProperty enabledProperty() {
			return enabled;
		}
	}

	/**
	 * Class for representing a tag with a checkbox to select tags to be used in
	 * the report
	 */
	protected class TagSelectionAdapter {

		/**
		 * The associated tag property
		 */
		private final StringProperty tag = new SimpleStringProperty();
		/**
		 * The tag enabled property
		 */
		private final BooleanProperty enabled = new SimpleBooleanProperty();

		/**
		 * Constructs a TagSelectionAdapter
		 *
		 * @param tag the associated tag
		 * @param enabled true if the tag should be enabled
		 */
		public TagSelectionAdapter(String tag, boolean enabled) {
			this.tag.set(tag);
			this.enabled.set(enabled);
		}

		/**
		 * Returns the tag property
		 *
		 * @return the tag property
		 */
		public StringProperty tagProperty() {
			return tag;
		}

		/**
		 * Returns the tag enabled property
		 *
		 * @return the tag enabled property
		 */
		public BooleanProperty enabledProperty() {
			return enabled;
		}
	}

	/**
	 * Class for representing a resulting tag with its expense amount
	 */
	protected class TagResultAdapter {

		/**
		 * The tag property
		 */
		protected final StringProperty tag = new SimpleStringProperty();
		/**
		 * The tag amount property
		 */
		protected final ObjectProperty<AmountModelAdapter> amount = new SimpleObjectProperty<>();

		/**
		 * Constructs a TagResultAdapter
		 *
		 * @param tag the tag
		 * @param amount the tag's expense amount
		 * @param currency the tag's currency
		 * @param isCurrencyConverted true if the currency was converted
		 */
		public TagResultAdapter(String tag, double amount, Currency currency, boolean isCurrencyConverted) {
			this.tag.set(tag);
			this.amount.set(new AmountModelAdapter(amount, true, currency, isCurrencyConverted, FinanceTransaction.Type.UNDEFINED));
		}

		/**
		 * Returns the tag property
		 *
		 * @return the tag property
		 */
		public StringProperty tagProperty() {
			return tag;
		}

		/**
		 * Returns the tag's expense amount property
		 *
		 * @return the tag's expense amount property
		 */
		public ObjectProperty<AmountModelAdapter> amountProperty() {
			return amount;
		}
	}

	/**
	 * Class for representing a resulting transaction with minimum attributes
	 */
	protected class TransactionResultAdapter {

		/**
		 * The transaction description property
		 */
		private final StringProperty description = new SimpleStringProperty();
		/**
		 * The transaction amount property
		 */
		private final ObjectProperty<AmountModelAdapter> amount = new SimpleObjectProperty<>();
		/**
		 * The transaction date property
		 */
		private final ObjectProperty<Date> date = new SimpleObjectProperty<>();

		/**
		 * Constructs a TransactionResultAdapter
		 *
		 * @param transaction the transaction
		 * @param financeData the associated FinanceData instance (used only for
		 * currency conversion)
		 */
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

		/**
		 * Returns the transaction description property
		 *
		 * @return the transaction description property
		 */
		public StringProperty descriptionProperty() {
			return description;
		}

		/**
		 * Returns the transaction amount property
		 *
		 * @return the transaction amount property
		 */
		public ObjectProperty<AmountModelAdapter> amountProperty() {
			return amount;
		}

		/**
		 * Returns the transaction date property
		 *
		 * @return the transaction date property
		 */
		public ObjectProperty<Date> dateProperty() {
			return date;
		}
	}
}
