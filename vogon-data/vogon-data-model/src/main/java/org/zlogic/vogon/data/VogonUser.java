/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

/**
 * Class for storing user data
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Entity
public class VogonUser implements Serializable {

	/**
	 * Version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The user ID (only for persistence)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	protected long id;
	/**
	 * The username
	 */
	protected String username;
	/**
	 * The user's accounts
	 */
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	protected List<FinanceAccount> accounts;
	/**
	 * The user's transaction
	 */
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	protected List<FinanceTransaction> transactions;
	/**
	 * The "Currency" key in preferences
	 */
	@Transient
	private static String CURRENCY = "Currency"; //NOI18N
	/**
	 * Preferences collection in string form
	 */
	@ElementCollection
	Map<String, String> preferences;

	/**
	 * Creates a user
	 */
	protected VogonUser() {

	}

	/**
	 * Creates a user
	 *
	 * @param username the user name
	 */
	public VogonUser(String username) {
		this.username = username;
	}
	/*
	 * Getters/setters
	 */

	/**
	 * Returns the username
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username
	 *
	 * @param username the username
	 */
	public void setUsername(String username) {
		if (username.isEmpty())
			return;
		this.username = username;
	}

	/**
	 * Returns the default (preferred) currency
	 *
	 * @return the default currency
	 */
	public Currency getDefaultCurrency() {
		if (preferences != null && preferences.containsKey(CURRENCY))
			return Currency.getInstance(preferences.get(CURRENCY));
		return null;
	}

	/**
	 * Sets the default (preferred) currency
	 *
	 * @param currency the new preferred currency
	 */
	public void setDefaultCurrency(Currency currency) {
		preferences.put(CURRENCY, currency.getCurrencyCode());
	}

	/**
	 * Returns the ID for this class instance
	 *
	 * @return the ID for this class instance
	 */
	public long getId() {
		return id;
	}
}
