/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.net.URL;
import java.util.Currency;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import javax.persistence.EntityManager;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.Preferences;
import org.zlogic.vogon.data.TransactedChange;
import org.zlogic.vogon.ui.adapter.CurrencyModelAdapter;
import org.zlogic.vogon.ui.adapter.CurrencyRateModelAdapter;
import org.zlogic.vogon.ui.adapter.DataManager;

/**
 * Currencies pane controller.
 *
 * @author Dmitry Zolotukhin
 */
public class CurrenciesController implements Initializable {

	/**
	 * The associated DataManager instance
	 */
	protected DataManager dataManager;
	/**
	 * The currencies table
	 */
	@FXML
	protected TableView<CurrencyRateModelAdapter> currenciesTable;
	/**
	 * The exchange rate column
	 */
	@FXML
	protected TableColumn<CurrencyRateModelAdapter, Double> columnExchangeRate;
	/**
	 * The default currency combo box
	 */
	@FXML
	protected ComboBox<CurrencyModelAdapter> defaultCurrency;

	/**
	 * Initializes the Currencies Controller
	 *
	 * @param url the FXML URL
	 * @param rb the FXML ResourceBundle
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		currenciesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		columnExchangeRate.setCellFactory(new Callback<TableColumn<CurrencyRateModelAdapter, Double>, TableCell<CurrencyRateModelAdapter, Double>>() {
			@Override
			public TableCell<CurrencyRateModelAdapter, Double> call(TableColumn<CurrencyRateModelAdapter, Double> p) {
				TextFieldTableCell<CurrencyRateModelAdapter, Double> cell = new TextFieldTableCell<>();
				cell.setConverter(new DoubleStringConverter());
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});
	}

	/**
	 * Assigns the DataManager instance
	 *
	 * @param dataManager the DataManager instance
	 */
	public void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;

		defaultCurrency.setItems(dataManager.getCurrencies());
		defaultCurrency.valueProperty().bindBidirectional(dataManager.getDefaultCurrency());

		currenciesTable.setItems(dataManager.getExchangeRates());

		defaultCurrency.valueProperty().addListener(new ChangeListener<CurrencyModelAdapter>() {
			protected FinanceData financeData;

			public ChangeListener<CurrencyModelAdapter> setFinanceData(FinanceData financeData) {
				this.financeData = financeData;
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends CurrencyModelAdapter> ov, CurrencyModelAdapter oldValue, CurrencyModelAdapter newValue) {
				financeData.performTransactedChange(new TransactedChange() {
					private Currency currency;

					public TransactedChange setCurrency(Currency currency) {
						this.currency = currency;
						return this;
					}

					@Override
					public void performChange(EntityManager entityManager) {
						Preferences preferences = financeData.getPreferencesFromDatabase(entityManager);
						preferences.setDefaultCurrency(currency);
					}
				}.setCurrency(newValue.getCurrency()));
			}
		}.setFinanceData(dataManager.getFinanceData()));//FIXME URGENT: move to DataManager
	}
}
