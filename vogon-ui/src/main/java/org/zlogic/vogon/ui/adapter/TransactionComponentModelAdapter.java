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
import javax.persistence.EntityManager;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactedChange;
import org.zlogic.vogon.data.TransactionComponent;

/**
 * Class for storing a transaction component with property change detection.
 *
 * @author Dmitry Zolotukhin
 */
public class TransactionComponentModelAdapter {

	/**
	 * Localization messages
	 */
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
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
	private final ObjectProperty<AccountInterface> account = new SimpleObjectProperty<>();
	/**
	 * The transaction property
	 */
	private final ObjectProperty<TransactionModelAdapter> transaction = new SimpleObjectProperty<>();
	/**
	 * The amount property
	 */
	private final ObjectProperty<AmountModelAdapter> amount = new SimpleObjectProperty<>();
	private ChangeListener<AccountInterface> accountListener = new ChangeListener<AccountInterface>() {
		@Override
		public void changed(ObservableValue<? extends AccountInterface> ov, AccountInterface oldValue, AccountInterface newValue) {
			if (oldValue.equals(newValue))
				return;

			FinanceAccount newAccount = (newValue instanceof AccountModelAdapter) ? ((AccountModelAdapter) newValue).getAccount() : null;

			dataManager.getFinanceData().performTransactedChange(new TransactedChange() {
				private FinanceAccount account;

				public TransactedChange setAccount(FinanceAccount account) {
					this.account = account;
					return this;
				}

				@Override
				public void performChange(EntityManager entityManager) {
					setTransactionComponent(dataManager.getFinanceData().getUpdatedTransactionComponentFromDatabase(entityManager, transactionComponent));
					getTransactionComponent().getTransaction().updateComponentAccount(getTransactionComponent(), account);
				}
			}.setAccount(newAccount));
			updateFxProperties();
		}
	};
	private ChangeListener<AmountModelAdapter> amountListener = new ChangeListener<AmountModelAdapter>() {
		@Override
		public void changed(ObservableValue<? extends AmountModelAdapter> ov, AmountModelAdapter oldValue, AmountModelAdapter newValue) {
			if (oldValue.equals(newValue))
				return;

			dataManager.getFinanceData().performTransactedChange(new TransactedChange() {
				private double amount;

				public TransactedChange setAmount(double amount) {
					this.amount = amount;
					return this;
				}

				@Override
				public void performChange(EntityManager entityManager) {
					setTransactionComponent(dataManager.getFinanceData().getUpdatedTransactionComponentFromDatabase(entityManager, transactionComponent));
					getTransactionComponent().getTransaction().updateComponentRawAmount(getTransactionComponent(), amount);
				}
			}.setAmount(newValue.getAmount()));
			updateFxProperties();
		}
	};

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
	public ObjectProperty<AccountInterface> accountProperty() {
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
	 * Returns the transaction property
	 *
	 * @return the transaction property
	 */
	public ObjectProperty<TransactionModelAdapter> transactionProperty() {
		return transaction;
	}

	/**
	 * Updates the properties from the current transaction component, causing
	 * ChangeListeners to trigger.
	 */
	protected void updateFxProperties() {
		//Remove property change listeners
		account.removeListener(accountListener);
		amount.removeListener(amountListener);
		if (transactionComponent != null) {
			AccountInterface showAccount = dataManager.findAccountAdapter(transactionComponent.getAccount());
			if (showAccount == null)
				showAccount = new ReportingAccount(messages.getString("INVALID_ACCOUNT"), 0, null);

			transaction.set(dataManager.findTransactionAdapter(transactionComponent.getTransaction()));
			account.set(showAccount);
			amount.set(new AmountModelAdapter(transactionComponent.getAmount(), true, transactionComponent.getAccount() != null ? transactionComponent.getAccount().getCurrency() : null, false, FinanceTransaction.Type.UNDEFINED));

			//Update parent properties as well
			if (transaction.get() != null) {
				transaction.get().updateFromDatabase();
				transaction.get().updateFxProperties();
			}
			if (account.get() instanceof AccountModelAdapter) {
				((AccountModelAdapter) account.get()).setAccount(transactionComponent.getAccount());
				((AccountModelAdapter) account.get()).updateFxProperties();
			}
		}
		//Restore property change listeners
		account.addListener(accountListener);
		amount.addListener(amountListener);
	}
}
