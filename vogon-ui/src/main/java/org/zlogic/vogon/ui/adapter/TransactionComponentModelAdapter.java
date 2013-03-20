/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;

/**
 * Class for storing a transaction component with property change detection.
 *
 * @author Dmitry Zolotukhin
 */
public class TransactionComponentModelAdapter {

	/**
	 * The DataManager instance
	 */
	protected DataManager dataManager;
	/**
	 * The associated transaction component
	 */
	protected TransactionComponent transactionComponent;
	/**
	 * The account property
	 */
	private final ObjectProperty<AccountModelAdapter> account = new SimpleObjectProperty<>();
	/**
	 * The amount property
	 */
	private final ObjectProperty<AmountModelAdapter> amount = new SimpleObjectProperty<>();

	/**
	 * Constructor for TransactionComponentModelAdapter
	 *
	 * @param transactionComponent the transaction component
	 * @param financeData the financeData instance
	 */
	public TransactionComponentModelAdapter(TransactionComponent transactionComponent, DataManager dataManager) {
		this.transactionComponent = transactionComponent;
		this.dataManager = dataManager;
		((TransactionComponentModelAdapter) this).updateFxProperties();

		//Set property change listeners
		account.addListener(new ChangeListener<AccountModelAdapter>() {
			protected DataManager dataManager;
			protected TransactionComponent transactionComponent;

			public ChangeListener<AccountModelAdapter> setData(TransactionComponent transactionComponent, DataManager dataManager) {
				this.transactionComponent = transactionComponent;
				this.dataManager = dataManager;
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends AccountModelAdapter> ov, AccountModelAdapter t, AccountModelAdapter t1) {
				//FIXME URGENT
				/*
				 financeData.setTransactionComponentAccount(transactionComponent, t1.getAccount());
				 */
			}
		}.setData(transactionComponent, dataManager));
		amount.addListener(new ChangeListener<AmountModelAdapter>() {
			protected DataManager dataManager;
			protected TransactionComponent transactionComponent;

			public ChangeListener<AmountModelAdapter> setData(TransactionComponent transactionComponent, DataManager dataManager) {
				this.transactionComponent = transactionComponent;
				this.dataManager = dataManager;
				return this;
			}

			@Override
			public void changed(ObservableValue<? extends AmountModelAdapter> ov, AmountModelAdapter t, AmountModelAdapter t1) {
				//FIXME URGENT
				/*
				 financeData.setTransactionComponentAmount(transactionComponent, t1.getAmount());
				 */
			}
		}.setData(transactionComponent, dataManager));
	}

	/**
	 * Returns the associated Transaction Component
	 *
	 * @return the associated Transaction Component
	 */
	public TransactionComponent getTransactionComponent() {
		return transactionComponent;
	}

	protected void setTransactionComponent(TransactionComponent transactionComponent) {
		this.transactionComponent = transactionComponent;
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
	 * Returns the amount property
	 *
	 * @return the amount property
	 */
	public ObjectProperty<AmountModelAdapter> amountProperty() {
		return amount;
	}

	/**
	 * Updates the properties from the current transaction component, causing
	 * ChangeListeners to trigger.
	 */
	protected void updateFxProperties() {
		if (transactionComponent != null) {
			account.set(dataManager.findAccountAdapter(transactionComponent.getAccount()));//FIXME URGENT: check this is not null & display incorrect account if required
			amount.set(new AmountModelAdapter(transactionComponent.getAmount(), true, transactionComponent.getAccount() != null ? transactionComponent.getAccount().getCurrency() : null, false, FinanceTransaction.Type.UNDEFINED));
		}
	}
}
