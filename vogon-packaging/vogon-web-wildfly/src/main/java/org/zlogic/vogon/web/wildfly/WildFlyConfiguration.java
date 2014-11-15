/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.wildfly;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * WildFly configuration options
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Configuration
public class WildFlyConfiguration {

	/**
	 * The CommonsMultipartResolver to be used by Spring
	 * MultipartAutoConfiguration
	 *
	 * @return the CommonsMultipartResolver class instance
	 */
	@Bean
	public MultipartResolver multipartResolver() {
		return new CommonsMultipartResolver();
	}
}
