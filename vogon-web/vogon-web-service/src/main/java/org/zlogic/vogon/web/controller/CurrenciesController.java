/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zlogic.vogon.web.data.model.CurrencyFull;

/**
 * Spring MVC controller for currencies
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Controller
@RequestMapping(value = "/service/currencies")
@Transactional
public class CurrenciesController {

	/**
	 * Creates the wrapped currency list
	 *
	 * @return the wrapped currency list
	 */
	@Bean
	public Collection<CurrencyFull> currencies() {
		List<CurrencyFull> currencies = new ArrayList<>();
		for (Currency currency : Currency.getAvailableCurrencies())
			currencies.add(new CurrencyFull(currency));
		Collections.sort(currencies);
		return currencies;
	}

	/**
	 * Returns all currencies
	 *
	 * @return the currencies
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Collection<CurrencyFull> getAllCurrencies() {
		return currencies();
	}
}
