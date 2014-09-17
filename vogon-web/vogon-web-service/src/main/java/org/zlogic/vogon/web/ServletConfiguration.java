/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.MimeMappings;
import org.springframework.context.annotation.Configuration;

/**
 * Configures servlets
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Configuration
public class ServletConfiguration implements EmbeddedServletContainerCustomizer {

	/**
	 * Customizes the ConfigurableEmbeddedServletContainer by adding extra MIME
	 * types
	 *
	 * @param container the ConfigurableEmbeddedServletContainer to customize
	 */
	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
		MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
		mappings.add("woff", "application/font-woff");
		container.setMimeMappings(mappings);
	}
}
