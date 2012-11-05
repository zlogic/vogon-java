/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zlogic.vogon.ui;

import org.zlogic.vogon.data.FinanceAccount;

/**
 * Class for storing an account with an overloaded toString method for better
 * customization of how it's rendered in a combo box.
 *
 * @author Dmitry
 */
public class FinanceAccountModelAdapter {

	private java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");
	/**
	 * The account
	 */
	protected FinanceAccount account;

	/**
	 * Default constructor
	 *
	 * @param account the account for this item
	 */
	public FinanceAccountModelAdapter(FinanceAccount account) {
		this.account = account;
	}

	@Override
	public String toString() {
		if (account != null)
			return account.getName();
		else
			return messages.getString("INVALID_ACCOUNT");
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return this == null;
		if(account==null)
			return obj instanceof FinanceAccountModelAdapter && ((FinanceAccountModelAdapter) obj).account == null;
		return obj instanceof FinanceAccountModelAdapter && account.equals(((FinanceAccountModelAdapter) obj).account);
	}

	@Override
	public int hashCode() {
		return account.hashCode();
	}

	/**
	 * Returns the account
	 *
	 * @return the account
	 */
	public FinanceAccount getAccount() {
		return account;
	}
}
