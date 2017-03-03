/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class with functions used in most tests
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class TestUtils {

	/**
	 * Returns the JPA configuration properties map
	 *
	 * @return the JPA configuration properties map
	 */
	public static Map<String, Object> getJpaProperties() {
		Map<String, Object> jpaProperties = new HashMap<>();
		String dbConnectionURL = "jdbc:h2:mem:test"; //NOI18N
		jpaProperties.put("javax.persistence.jdbc.url", dbConnectionURL); //NOI18N
		jpaProperties.put("javax.persistence.jdbc.user", ""); //NOI18N
		jpaProperties.put("javax.persistence.jdbc.password", ""); //NOI18N
		jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect"); //NOI18N
		jpaProperties.put("hibernate.connection.driver_class", "org.h2.Driver"); //NOI18N
		return jpaProperties;
	}

	/**
	 * Parses a date in JSON format
	 *
	 * @param date the date string to parse
	 * @return the parsed date
	 */
	public static Date parseJSONDate(String date) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(date); //NOI18N
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}
}
