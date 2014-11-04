/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
	 * The user's preferred currency
	 */
	private String defaultCurrency;
	/**
	 * This user's authorities
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<String> authorities;

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
		if (defaultCurrency != null)
			return Currency.getInstance(defaultCurrency);
		return null;
	}

	/**
	 * Sets the default (preferred) currency
	 *
	 * @param currency the new preferred currency
	 */
	public void setDefaultCurrency(Currency currency) {
		defaultCurrency = currency.getCurrencyCode();
	}

	/**
	 * Returns the user's assigned authorities
	 *
	 * @return the user's assigned authorities
	 */
	public Set<String> getAuthorities() {
		if (authorities == null)
			authorities = new HashSet<>();
		return authorities;
	}

	/**
	 * Assigns new authorities to user
	 *
	 * @param authorities the new authorities for user
	 */
	public void setAuthorities(String... authorities) {
		if (this.authorities == null)
			this.authorities = new HashSet<>();
		this.authorities.clear();
		this.authorities.addAll(Arrays.asList(authorities));
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
