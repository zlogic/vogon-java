/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.wildfly;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * WildFly configuration options
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Configuration
public class WildFlyConfiguration {

	/**
	 * The MultipartResolver proxy class which makes CommonsMultipartResolver to
	 * be detected by Spring MultipartAutoConfiguration
	 *
	 * @return the MultipartResolver proxy class instance
	 */
	@Bean
	public StandardServletMultipartResolver multipartResolver() {
		return new MultipartResolver();
	}
}
