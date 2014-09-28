/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Version;

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
	 * JPA version
	 */
	@Version
	private long version = 0L;
	/**
	 * The username
	 */
	protected String username;
	/**
	 * The password
	 */
	protected String password;
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
	 * @param password the password
	 */
	public VogonUser(String username, String password) {
		this.username = username;
		this.password = password;
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
	 * Returns the password
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password
	 *
	 * @param password the password
	 */
	public void setPassword(String password) {
		this.password = password;
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
		if (preferences == null)
			preferences = new HashMap<>();
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

	/**
	 * Returns the version for this class instance
	 *
	 * @return the version for this class instance
	 */
	protected long getVersion() {
		return version;
	}

	/**
	 * Sets the version of this class instance
	 *
	 * @param version the version of this class instance
	 */
	public void setVersion(long version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VogonUser)
			return id == (((VogonUser) obj).id);
		else
			return this == obj;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 79 * hash + (int) (this.id ^ (this.id >>> 32));
		return hash;
	}
}
