/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
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
	@Column(unique = true, nullable = false)
	protected String username;
	/**
	 * The password
	 */
	@Column(nullable = false)
	protected String password;
	/**
	 * The user's transactions
	 */
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn
	protected Set<FinanceAccount> accounts;
	/**
	 * The user's transactions
	 */
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn
	protected Set<FinanceTransaction> transactions;

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
		setUsername(username);
		this.password = password;
	}

	/*
	 * Getters/setters
	 */
	public Set<FinanceAccount> getAccounts(){
		return accounts !=null ? Collections.unmodifiableSet(accounts) : Collections.emptySet();
	}
	public Set<FinanceTransaction> getTransactions(){
		return transactions != null ? Collections.unmodifiableSet(transactions) : Collections.emptySet();
	}
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
		if (username == null || username.isEmpty()) {
			this.username = null;
			return;
		}
		this.username = username.trim().toLowerCase();
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
		if (password == null || password.isEmpty()) {
			this.password = null;
			return;
		}
		this.password = password;
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
	public long getVersion() {
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
		if (obj instanceof VogonUser) {
			return id == (((VogonUser) obj).id);
		} else {
			return this == obj;
		}
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 79 * hash + (int) (this.id ^ (this.id >>> 32));
		return hash;
	}
}
