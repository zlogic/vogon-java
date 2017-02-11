/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller.serialization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Mapping configuration
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Configuration
public class MappingConfig {

	/**
	 * JSONMapper instance
	 */
	@Autowired
	private JSONMapper jsonMapper;

	/**
	 * Returns the MappingJackson2HttpMessageConverter instance using the
	 * customized JSONMapper as its objectMapper
	 *
	 * @return the customized MappingJackson2HttpMessageConverter instance
	 */
	@Bean
	public MappingJackson2HttpMessageConverter jsonConverter() {
		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
		jsonConverter.setObjectMapper(jsonMapper);
		return jsonConverter;
	}
}
