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
import org.zlogic.vogon.data.CurrencyRate;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.events.CurrencyEventHandler;
import org.zlogic.vogon.ui.adapter.CurrencyModelAdapter;
import org.zlogic.vogon.ui.adapter.CurrencyRateModelAdapter;

/**
 *
 * @author Dmitry Zolotukhin
 */
public class CurrenciesController implements Initializable {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	protected FinanceData financeData;
	@FXML
	protected TableView<CurrencyRateModelAdapter> currenciesTable;
	@FXML
	protected TableColumn<CurrencyRateModelAdapter, Double> columnExchangeRate;
	@FXML
	protected ComboBox<CurrencyModelAdapter> defaultCurrency;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		currenciesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		columnExchangeRate.setCellFactory(new Callback<TableColumn<CurrencyRateModelAdapter, Double>, TableCell<CurrencyRateModelAdapter, Double>>() {
			@Override
			public TableCell<CurrencyRateModelAdapter, Double> call(TableColumn<CurrencyRateModelAdapter, Double> p) {
				TextFieldTableCell cell = new TextFieldTableCell<>();
				cell.setConverter(new DoubleStringConverter());
				cell.setAlignment(Pos.CENTER_RIGHT);
				return cell;
			}
		});
	}

	public void setFinanceData(FinanceData financeData) {
		this.financeData = financeData;
		updateCurrencies();

		if (financeData.getAccountListener() instanceof FinanceDataEventDispatcher) {
			((FinanceDataEventDispatcher) financeData.getCurrencyListener()).addCurrencyEventHandler(new CurrencyEventHandler() {
				protected FinanceData financeData;

				public CurrencyEventHandler setFinanceData(FinanceData financeData) {
					this.financeData = financeData;
					return this;
				}

				@Override
				public void currenciesUpdated() {
					//TODO
				}
			}.setFinanceData(financeData));
			defaultCurrency.valueProperty().addListener(new ChangeListener<CurrencyModelAdapter>() {
				protected FinanceData financeData;

				public ChangeListener<CurrencyModelAdapter> setFinanceData(FinanceData financeData) {
					this.financeData = financeData;
					return this;
				}

				@Override
				public void changed(ObservableValue<? extends CurrencyModelAdapter> ov, CurrencyModelAdapter t, CurrencyModelAdapter t1) {
					financeData.setDefaultCurrency(t1.getCurrency());
				}
			}.setFinanceData(financeData));
		}
	}

	protected void updateCurrencies() {
		currenciesTable.getItems().removeAll(currenciesTable.getItems());
		for (CurrencyRate rate : financeData.getCurrencyRates())
			currenciesTable.getItems().add(new CurrencyRateModelAdapter(rate, financeData));

		defaultCurrency.getItems().removeAll(defaultCurrency.getItems());
		defaultCurrency.getItems().addAll(CurrencyModelAdapter.getCurrenciesList());
		Currency currentDefaultCurrency = financeData.getDefaultCurrency();
		if (currentDefaultCurrency != null)
			defaultCurrency.getSelectionModel().select(new CurrencyModelAdapter(currentDefaultCurrency));
	}
}
