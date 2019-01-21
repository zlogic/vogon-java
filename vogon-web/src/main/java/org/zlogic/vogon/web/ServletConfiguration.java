/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Configures servlets
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Configuration
public class ServletConfiguration implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
	/**
	 * Customizes the ConfigurableEmbeddedServletContainer by adding extra MIME
	 * types
	 *
	 * @param factory the ConfigurableServletWebServerFactory to customize
	 */
	@Override
	public void customize(ConfigurableServletWebServerFactory factory) {
		MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
		mappings.add("woff", "application/font-woff"); //NOI18N
		mappings.add("eot", "application/vnd.ms-fontobject"); //NOI18N
		factory.setMimeMappings(mappings);
	}
}
