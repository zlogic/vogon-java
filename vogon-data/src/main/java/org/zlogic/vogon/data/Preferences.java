/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import java.util.Currency;
import java.util.Map;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * Class for storing application preferences
 *
 * @author Dmitry Zolotukhin
 */
@Entity
public class Preferences implements Serializable {

	/**
	 * Version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The "Currency" key in preferences
	 */
	@Transient
	private static String CURRENCY = "Currency"; //NOI18N
	/**
	 * The preferences ID (only for persistence)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	protected long id;
	/**
	 * Preferences collection in string form
	 */
	@ElementCollection
	Map<String, String> preferences;

	/**
	 * Default constructor
	 */
	public Preferences() {
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
}
