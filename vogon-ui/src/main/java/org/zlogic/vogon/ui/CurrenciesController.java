/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.net.URL;
import java.util.ResourceBundle;
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
import org.zlogic.vogon.ui.adapter.CurrencyModelAdapter;
import org.zlogic.vogon.ui.adapter.CurrencyRateModelAdapter;
import org.zlogic.vogon.ui.adapter.DataManager;

/**
 * Currencies pane controller.
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
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
		defaultCurrency.valueProperty().bindBidirectional(dataManager.defaultCurrencyProperty());

		currenciesTable.setItems(dataManager.getExchangeRates());
	}
}
