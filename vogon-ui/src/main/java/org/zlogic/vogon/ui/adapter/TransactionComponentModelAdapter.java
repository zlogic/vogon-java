/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;

/**
 * Class for storing a transaction component with property change detection.
 *
 * @author Dmitry Zolotukhin
 */
public class TransactionComponentModelAdapter {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The FinanceData instance
	 */
	protected FinanceData financeData;
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
	public TransactionComponentModelAdapter(TransactionComponent transactionComponent, FinanceData financeData) {
		this.transactionComponent = transactionComponent;
		this.financeData = financeData;
		updateProperties();
	}

	/**
	 * Sets the component's account
	 *
	 * @param account the account to be set
	 */
	public void setAccount(FinanceAccount account) {
		financeData.setTransactionComponentAccount(transactionComponent, account);
		updateProperties();
	}

	/**
	 * Sets the component's amount
	 *
	 * @param amount the amount to be set
	 */
	public void setAmount(double amount) {
		financeData.setTransactionComponentAmount(transactionComponent, amount);
		updateProperties();
	}

	/**
	 * Returns the associated Transaction Component
	 *
	 * @return the associated Transaction Component
	 */
	public TransactionComponent getTransactionComponent() {
		return transactionComponent;
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
	private void updateProperties() {
		if (transactionComponent != null) {
			account.set(new AccountModelAdapter(transactionComponent.getAccount(), financeData));
			amount.set(new AmountModelAdapter(transactionComponent.getAmount(), true, transactionComponent.getAccount() != null ? transactionComponent.getAccount().getCurrency() : null, false, FinanceTransaction.Type.UNDEFINED));
		}
	}
}
