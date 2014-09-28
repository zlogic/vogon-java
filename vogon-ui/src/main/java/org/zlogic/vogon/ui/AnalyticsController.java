/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.report.DateBalance;
import org.zlogic.vogon.data.report.Report;
import org.zlogic.vogon.data.report.ReportFactory;
import org.zlogic.vogon.data.report.TagExpense;
import org.zlogic.vogon.ui.adapter.AccountInterface;
import org.zlogic.vogon.ui.adapter.AccountModelAdapter;
import org.zlogic.vogon.ui.adapter.AmountModelAdapter;
import org.zlogic.vogon.ui.adapter.DataManager;
import org.zlogic.vogon.ui.cell.DateConverter;

/**
 * The Analytics pane
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class AnalyticsController implements Initializable {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(AnalyticsController.class.getName());
	/**
	 * Localization messages
	 */
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The reportFactory generator
	 */
	protected ReportFactory reportFactory;
	/**
	 * The finance data reference
	 */
	protected DataManager dataManager;
	/**
	 * The processor for background tasks
	 */
	private Callback<Task<Void>, Void> backgroundTaskProcessor;
	/**
	 * Start date text field
	 */
	@FXML
	private DatePicker startDateField;
	/**
	 * End date text field
	 */
	@FXML
	private DatePicker endDateField;
	/**
	 * Transfer transactions checkbox
	 */
	@FXML
	private CheckBox transferTransactionsCheckbox;
	/**
	 * Income transactions checkbox
	 */
	@FXML
	private CheckBox incomeTransactionsCheckbox;
	/**
	 * Expense transactions checkbox
	 */
	@FXML
	private CheckBox expenseTransactionsCheckbox;
	/**
	 * The tags selection table
	 */
	@FXML
	private TableView<TagSelectionAdapter> tagsSelectionTable;

	/**
	 * The tags selection table's checkbox column
	 */
	@FXML
	private TableColumn<TagSelectionAdapter, String> tagsSelectionTagColumn;
	/**
	 * The tags selection table's checkbox column
	 */
	@FXML
	private TableColumn<TagSelectionAdapter, Boolean> tagsSelectionShowColumn;
	/**
	 * The accounts selection table
	 */
	@FXML
	private TableView<AccountSelectionAdapter> accountsSelectionTable;
	/**
	 * The accounts selection table's checkbox column
	 */
	@FXML
	private TableColumn<AccountSelectionAdapter, Boolean> accountsSelectionShowColumn;
	/**
	 * The tags reporting table
	 */
	@FXML
	private TableView<TagResultAdapter> tagsResultTable;
	/**
	 * The tags reporting table amount column
	 */
	@FXML
	private TableColumn<TagResultAdapter, AmountModelAdapter> tagsResultAmountColumn;
	/**
	 * The transactions reporting table
	 */
	@FXML
	private TableView<TransactionResultAdapter> transactionsResultTable;
	/**
	 * The transactions reporting table date column
	 */
	@FXML
	private TableColumn<TransactionResultAdapter, Date> transactionsResultDateColumn;
	/**
	 * The transactions reporting table amount column
	 */
	@FXML
	private TableColumn<TransactionResultAdapter, AmountModelAdapter> transactionsResultAmountColumn;
	/**
	 * The tags chart
	 */
	@FXML
	private PieChart tagsChart;
	/**
	 * The balance chart
	 */
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
		//Configure columns
		tagsSelectionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		accountsSelectionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		tagsResultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		transactionsResultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		//Configure checkbox columns
		tagsSelectionShowColumn.setCellFactory(new Callback<TableColumn<TagSelectionAdapter, Boolean>, TableCell<TagSelectionAdapter, Boolean>>() {
			@Override
			public TableCell<TagSelectionAdapter, Boolean> call(TableColumn<TagSelectionAdapter, Boolean> p) {
				CheckBoxTableCell<TagSelectionAdapter, Boolean> cell = new CheckBoxTableCell<>();
				cell.setEditable(true);
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});

		accountsSelectionShowColumn.setCellFactory(new Callback<TableColumn<AccountSelectionAdapter, Boolean>, TableCell<AccountSelectionAdapter, Boolean>>() {
			@Override
			public TableCell<AccountSelectionAdapter, Boolean> call(TableColumn<AccountSelectionAdapter, Boolean> p) {
				CheckBoxTableCell<AccountSelectionAdapter, Boolean> cell = new CheckBoxTableCell<>();
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
				TextFieldTableCell<TagResultAdapter, AmountModelAdapter> cell = new TextFieldTableCell<>();
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});

		transactionsResultAmountColumn.setCellFactory(new Callback<TableColumn<TransactionResultAdapter, AmountModelAdapter>, TableCell<TransactionResultAdapter, AmountModelAdapter>>() {
			@Override
			public TableCell<TransactionResultAdapter, AmountModelAdapter> call(TableColumn<TransactionResultAdapter, AmountModelAdapter> p) {
				TextFieldTableCell<TransactionResultAdapter, AmountModelAdapter> cell = new TextFieldTableCell<>();
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});

		transactionsResultDateColumn.setCellFactory(new Callback<TableColumn<TransactionResultAdapter, Date>, TableCell<TransactionResultAdapter, Date>>() {
			@Override
			public TableCell<TransactionResultAdapter, Date> call(TableColumn<TransactionResultAdapter, Date> p) {
				TextFieldTableCell<TransactionResultAdapter, Date> cell = new TextFieldTableCell<>();
				cell.setConverter(new DateConverter(DateFormat.getDateInstance(DateFormat.LONG)));
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});

		//Set sort order
		tagsSelectionTable.getSortOrder().add(tagsSelectionTagColumn);
		tagsSelectionTagColumn.setSortType(TableColumn.SortType.ASCENDING);
		tagsSelectionTagColumn.setComparator(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
	}

	/**
	 * Generate Report button handler
	 */
	@FXML
	private void handleGenerateReport() {
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() {
				updateProgress(-1, 1);
				updateMessage(messages.getString("TASK_GENERATING_REPORT"));
				//Set report parameters
				reportFactory.setEarliestDate(Date.from(startDateField.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
				reportFactory.setLatestDate(Date.from(endDateField.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));

				List<FinanceAccount> selectedAccounts = new LinkedList<>();
				for (AccountSelectionAdapter accountAdapter : accountsSelectionTable.getItems())
					if (accountAdapter.enabledProperty().get())
						selectedAccounts.add(accountAdapter.accountProperty().get().getAccount());
				reportFactory.setSelectedAccounts(selectedAccounts);

				List<String> selectedTags = new LinkedList<>();
				for (TagSelectionAdapter tagAdapter : tagsSelectionTable.getItems())
					if (tagAdapter.enabledProperty().get())
						selectedTags.add(tagAdapter.tagProperty().get());
				reportFactory.setSelectedTags(selectedTags);

				reportFactory.setEnabledTransferTransactions(transferTransactionsCheckbox.selectedProperty().get());
				reportFactory.setEnabledIncomeTransactions(incomeTransactionsCheckbox.selectedProperty().get());
				reportFactory.setEnabledExpenseTransactions(expenseTransactionsCheckbox.selectedProperty().get());

				//Generate the report
				Report report = null;
				try {
					report = dataManager.getFinanceData().buildReport(reportFactory);
				} catch (Exception ex) {
					log.log(Level.SEVERE, null, ex);
					MessageDialog.showDialog(messages.getString("REPORT_EXCEPTION_DIALOG_TITLE"), new MessageFormat(messages.getString("REPORT_EXCEPTION_DIALOG_TEXT")).format(new Object[]{ex.getLocalizedMessage(), ExceptionLogger.getInstance().getExceptionStacktrace(ex)}));
				}

				//Update the UI
				Platform.runLater(new Runnable() {
					protected List<TagExpense> tagExpenses;
					protected List<FinanceTransaction> transactions;
					protected Map<String, DateBalance<Double>> balanceGraph;

					public Runnable setData(List<TagExpense> tagExpenses, List<FinanceTransaction> transactions, Map<String, DateBalance<Double>> balanceGraph) {
						this.tagExpenses = tagExpenses;
						this.transactions = transactions;
						this.balanceGraph = balanceGraph;
						return this;
					}

					@Override
					public void run() {
						updateTagsResultTable(tagExpenses);
						updateTransactionsResultTable(transactions);//TODO: add paging
						updateTagsChart();
						updateBalanceChart(balanceGraph);
					}
				}.setData(report.getTagExpenses(), report.getTransactions(), report.getAccountsBalanceGraph()));

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
	 * Selects all accounts
	 */
	@FXML
	private void handleSelectAllAccounts() {
		for (AccountSelectionAdapter account : accountsSelectionTable.getItems())
			account.enabledProperty().set(true);
	}

	/**
	 * Unselects all accounts
	 */
	@FXML
	private void handleUnselectAllAccounts() {
		for (AccountSelectionAdapter account : accountsSelectionTable.getItems())
			account.enabledProperty().set(false);
	}

	/**
	 * Selects all tags
	 */
	@FXML
	private void handleSelectAllTags() {
		for (TagSelectionAdapter tag : tagsSelectionTable.getItems())
			tag.enabledProperty().set(true);
	}

	/**
	 * Unselects all tags
	 */
	@FXML
	private void handleUnselectAllTags() {
		for (TagSelectionAdapter tag : tagsSelectionTable.getItems())
			tag.enabledProperty().set(false);
	}

	/**
	 * Updates the table for selecting tags
	 *
	 * @param tags the tags to set
	 */
	private void updateTagsSelectionTable(Collection<String> tags) {
		tagsSelectionTable.getItems().clear();
		for (String tag : tags)
			tagsSelectionTable.getItems().add(new TagSelectionAdapter(tag, true));
		tagsSelectionTable.getSortPolicy().call(tagsSelectionTable);
	}

	/**
	 * Updates the table for selecting accounts
	 */
	private void updateAccountsSelectionTable() {
		accountsSelectionTable.getItems().clear();
		for (AccountInterface account : dataManager.getAccounts())
			if (account instanceof AccountModelAdapter)
				accountsSelectionTable.getItems().add(new AccountSelectionAdapter((AccountModelAdapter) account, ((AccountModelAdapter) account).includeInTotalProperty().get()));
	}

	/**
	 * Updates the resulting tags table
	 *
	 * @param values the list of expenses grouped by tag
	 */
	private void updateTagsResultTable(List<TagExpense> values) {
		tagsResultTable.getItems().clear();
		Currency defaultCurrency = dataManager.defaultCurrencyProperty().get().getCurrency();
		for (TagExpense tagExpense : values) {
			double amount = 0;
			boolean amountConverted = false;
			for (Map.Entry<Currency, Double> currencyAmount : tagExpense.getAmounts().entrySet())
				if (currencyAmount.getKey() == defaultCurrency) {
					amount += currencyAmount.getValue();
				} else {
					amountConverted = true;
					amount += dataManager.convertAmount(currencyAmount.getValue(), currencyAmount.getKey(), defaultCurrency);
				}

			tagsResultTable.getItems().add(new TagResultAdapter(tagExpense.getTag(), amount, defaultCurrency, amountConverted));
		}
	}

	/**
	 * Updates the resulting transactions table
	 *
	 * @param values the list of transactions matching the criteria
	 */
	private void updateTransactionsResultTable(List<FinanceTransaction> values) {
		transactionsResultTable.getItems().clear();
		for (FinanceTransaction transaction : values)
			transactionsResultTable.getItems().add(new TransactionResultAdapter(transaction, dataManager));
	}

	/**
	 * Updates the tags chart (uses the tags table for source data)
	 */
	private void updateTagsChart() {
		ObservableList<PieChart.Data> data = FXCollections.observableList(new LinkedList<PieChart.Data>());
		for (TagResultAdapter tagResult : tagsResultTable.getItems()) {
			//TagResultAdapter tagResult = new TagResultAdapter(tagExpense.getTag(), tagExpense.getAmount(), tagExpense.getCurrency(), tagExpense.isCurrencyConverted());
			String tagLabel = MessageFormat.format(messages.getString("PIECHART_TAG_FORMAT"), new Object[]{tagResult.tagProperty().get(), tagResult.amountProperty().get().toString()});
			data.add(new PieChart.Data(tagLabel, dataManager.getFinanceData().getExchangeRate(tagResult.amountProperty().get().getCurrency(), dataManager.defaultCurrencyProperty().get().getCurrency())
					* Math.abs(tagResult.amountProperty().get().getAmount())));
		}

		tagsChart.dataProperty().set(data);
	}

	/**
	 * Updates the balance graph
	 *
	 * @param values the balance graph, grouped by date
	 */
	private void updateBalanceChart(Map<String, DateBalance<Double>> values) {
		if (!balanceChart.getData().isEmpty())
			balanceChart.getXAxis().invalidateRange(new LinkedList<String>());

		balanceChart.getData().clear();

		XYChart.Series<String, Double> series = new XYChart.Series<>();

		Currency defaultCurrency = dataManager.defaultCurrencyProperty().get().getCurrency();
		Map<Date, Double> convertedValues = new TreeMap<>();

		for (Map.Entry<String, DateBalance<Double>> dateBalanceCurrency : values.entrySet()) {
			Currency currency = Currency.getInstance(dateBalanceCurrency.getKey());
			for (Map.Entry<Date, Double> dateBalance : dateBalanceCurrency.getValue().getData().entrySet()) {
				if (!convertedValues.containsKey(dateBalance.getKey()))
					convertedValues.put(dateBalance.getKey(), 0.0);
				double amount = convertedValues.get(dateBalance.getKey());
				if (currency == defaultCurrency)
					amount += dateBalance.getValue();
				else
					amount += dataManager.convertAmount(dateBalance.getValue(), currency, defaultCurrency);
				convertedValues.put(dateBalance.getKey(), amount);
			}
		}
		for (Map.Entry<Date, Double> entry : convertedValues.entrySet())
			series.getData().add(new XYChart.Data<>(DateFormat.getDateInstance(DateFormat.FULL).format(entry.getKey()), entry.getValue(), entry.getKey()));
		balanceChart.getData().add(series);
	}

	/**
	 * Assigns the DataManager instance
	 *
	 * @param dataManager the DataManager instance
	 */
	public void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;

		//Update the form
		reportFactory = dataManager.getFinanceData().getReportFactory();

		startDateField.setValue(LocalDateTime.ofInstant(Instant.ofEpochMilli(reportFactory.getEarliestDate().getTime()), ZoneId.systemDefault()).toLocalDate());
		endDateField.setValue(LocalDateTime.ofInstant(Instant.ofEpochMilli(reportFactory.getLatestDate().getTime()), ZoneId.systemDefault()).toLocalDate());

		updateTagsSelectionTable(dataManager.getFinanceData().getAllTags());//TODO: move this to FinanceData
		updateAccountsSelectionTable();//TODO: move this to FinanceData (add "select for reportFactory" property to regular adapter)
	}

	/**
	 * Sets the background task processor callback
	 *
	 * @param backgroundTaskProcessor the background task processor callback
	 * (otherwise the report generation will run in the current thread)
	 */
	public void setBackgroundTaskProcessor(Callback<Task<Void>, Void> backgroundTaskProcessor) {
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
		private final ObjectProperty<AccountModelAdapter> account = new SimpleObjectProperty<>();
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
		 * @param dataManager the associated DataManager instance (used only for
		 * currency conversion)
		 */
		public TransactionResultAdapter(FinanceTransaction transaction, DataManager dataManager) {
			this.description.set(transaction.getDescription());
			double transactionAmount;
			Currency transactionCurrency;
			if (transaction.getCurrencies().size() == 1) {
				transactionAmount = transaction.getAmount();
				transactionCurrency = transaction.getCurrencies().get(0);
			} else {
				transactionAmount = dataManager.getFinanceData().getAmountInCurrency(transaction, dataManager.defaultCurrencyProperty().get().getCurrency());
				transactionCurrency = dataManager.defaultCurrencyProperty().get().getCurrency();
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
