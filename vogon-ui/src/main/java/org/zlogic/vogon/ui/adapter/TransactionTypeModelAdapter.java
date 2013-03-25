/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui.adapter;

import org.zlogic.vogon.data.FinanceTransaction;
import static org.zlogic.vogon.data.FinanceTransaction.Type.EXPENSEINCOME;
import static org.zlogic.vogon.data.FinanceTransaction.Type.TRANSFER;
import static org.zlogic.vogon.data.FinanceTransaction.Type.UNDEFINED;

/**
 * Transaction type helper class for rendering.
 *
 * @author Dmitry Zolotukhin
 */
public class TransactionTypeModelAdapter {

	/**
	 * Localization messages
	 */
	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The transaction type
	 */
	protected FinanceTransaction.Type type;

	/**
	 * Default constructor
	 *
	 * @param type the transaction type
	 */
	public TransactionTypeModelAdapter(FinanceTransaction.Type type) {
		this.type = type;
	}

	@Override
	public String toString() {
		switch (type) {
			case EXPENSEINCOME:
				return messages.getString("TRANSACTION_EXPENSE_INCOME");
			case TRANSFER:
				return messages.getString("TRANSACTION_TRANSFER");
			case UNDEFINED:
				return messages.getString("INVALID_TRANSACTION_TYPE");
		}
		return messages.getString("INVALID_TRANSACTION_TYPE");
	}

	/**
	 * Returns the transaction type
	 *
	 * @return the transaction type
	 */
	public FinanceTransaction.Type getType() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TransactionTypeModelAdapter && ((TransactionTypeModelAdapter) obj).type.equals(type);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 53 * hash + (this.type != null ? this.type.hashCode() : 0);
		return hash;
	}
}
