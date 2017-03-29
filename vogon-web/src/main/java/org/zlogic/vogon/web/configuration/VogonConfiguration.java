/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.configuration;

import org.springframework.stereotype.Service;

/**
 * Various global configuration options
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Service
public class VogonConfiguration {

	/**
	 * Allow registration
	 */
	private final static String ALLOW_REGISTRATION = "VOGON_ALLOW_REGISTRATION"; //NOI18N

	/**
	 * The transactions page size
	 */
	private static final String TRANSACTIONS_PAGE_SIZE = "VOGON_TRANSACTIONS_PAGE_SIZE"; //NOI18N
	
	/**
	 * Token expires days
	 */
	private final static String TOKEN_EXPIRES_DAYS = "VOGON_TOKEN_EXPIRES_DAYS"; //NOI18N

	/**
	 * Returns true if registration is allowed
	 *
	 * @return true if registration is allowed
	 */
	public boolean isAllowRegistration() {
		String allowRegistration = System.getenv(ALLOW_REGISTRATION);
		if (allowRegistration == null)
			return false;
		return Boolean.parseBoolean(allowRegistration);
	}

	/**
	 * Returns the transactions page size
	 *
	 * @return the transactions page size
	 */
	public int getTransactionsPageSize() {
		String transactionsPageSize = System.getenv(TRANSACTIONS_PAGE_SIZE);
		if (transactionsPageSize == null)
			transactionsPageSize = "100"; //NOI18N
		return Integer.parseInt(transactionsPageSize);
	}
	
	/**
	 * Returns the number of seconds a token lasts before it expires
	 *
	 * @return the number of seconds a token lasts before it expires
	 */
	public int getTokenExpiresSeconds() {
		String tokenExpiresDays = System.getenv(TOKEN_EXPIRES_DAYS);
		if (tokenExpiresDays == null)
			tokenExpiresDays = "14"; //NOI18N
		return 60 * 60 * 24 * Integer.parseInt(tokenExpiresDays);
	}
}
