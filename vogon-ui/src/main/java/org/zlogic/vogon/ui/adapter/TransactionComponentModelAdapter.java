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
 *
 * @author Dmitry Zolotukhin
 */
public class TransactionComponentModelAdapter {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	protected FinanceData financeData;
	protected TransactionComponent transactionComponent;
	protected final ObjectProperty<AccountModelAdapter> account = new SimpleObjectProperty<>();
	protected final ObjectProperty<AmountModelAdapter> amount = new SimpleObjectProperty<>();

	public TransactionComponentModelAdapter(TransactionComponent transactionComponent, FinanceData financeData) {
		this.transactionComponent = transactionComponent;
		this.financeData = financeData;
		updateProperties();
	}

	public void setAccount(FinanceAccount account) {
		financeData.setTransactionComponentAccount(transactionComponent, account);
		updateProperties();
	}

	public void setAmount(double amount) {
		financeData.setTransactionComponentAmount(transactionComponent, amount);
		updateProperties();
	}

	public TransactionComponent getTransactionComponent() {
		return transactionComponent;
	}

	public ObjectProperty<AccountModelAdapter> accountProperty() {
		return account;
	}

	public ObjectProperty<AmountModelAdapter> amountProperty() {
		return amount;
	}

	private void updateProperties() {
		if (transactionComponent != null) {
			account.set(new AccountModelAdapter(transactionComponent.getAccount(), financeData));
			amount.set(new AmountModelAdapter(transactionComponent.getAmount(), true, transactionComponent.getAccount() != null ? transactionComponent.getAccount().getCurrency() : null, false, FinanceTransaction.Type.UNDEFINED));
		}
	}
}
