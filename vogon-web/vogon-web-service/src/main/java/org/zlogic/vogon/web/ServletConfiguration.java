/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.io.File;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11Protocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.MimeMappings;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Configures servlets
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Configuration
public class ServletConfiguration implements EmbeddedServletContainerCustomizer {

	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(ServletConfiguration.class.getName());
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/web/messages");
	/**
	 * Keystore file
	 */
	@Value("${vogon.keystore.file:}")
	private String keystoreFile;
	/**
	 * Keystore password
	 */
	@Value("${vogon.keystore.pass:}")
	private String keystorePass;

	/**
	 * Customizes the ConfigurableEmbeddedServletContainer by adding extra MIME
	 * types
	 *
	 * @param container the ConfigurableEmbeddedServletContainer to customize
	 */
	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
		MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
		mappings.add("woff", "application/font-woff"); //NOI18N
		mappings.add("eot", "application/vnd.ms-fontobject"); //NOI18N
		container.setMimeMappings(mappings);
		if (container instanceof TomcatEmbeddedServletContainerFactory) {
			TomcatEmbeddedServletContainerFactory tomcatContainer = ((TomcatEmbeddedServletContainerFactory) container);
			tomcatContainer.setUriEncoding("utf-8");
			configureSSL(tomcatContainer);
		}
	}

	/**
	 * Configures SSL for Tomcat container
	 *
	 * @param container TomcatEmbeddedServletContainerFactory instance to
	 * configure
	 */
	private void configureSSL(TomcatEmbeddedServletContainerFactory container) {
		if (keystoreFile.isEmpty() || keystorePass.isEmpty()) {
			log.fine(messages.getString("KEYSTORE_FILE_OR_PASSWORD_NOT_DEFINED"));
			return;
		}
		log.severe(MessageFormat.format(messages.getString("USING_KEYSTORE_WITH_PASSWORD"), new Object[]{keystoreFile, keystorePass.replaceAll(".{1}", "*")})); //NOI18N
		Connector connector = new Connector();
		connector.setPort(8443);
		connector.setSecure(true);
		connector.setScheme("https"); //NOI18N
		connector.setURIEncoding(container.getUriEncoding());

		Http11Protocol proto = (Http11Protocol) connector.getProtocolHandler();
		proto.setSSLEnabled(true);
		proto.setKeystoreFile(new File(keystoreFile).getAbsolutePath());
		proto.setKeystorePass(keystorePass);
		proto.setKeystoreType("JKS"); //NOI18N
		proto.setKeyAlias("vogonkey"); //NOI18N

		container.addAdditionalTomcatConnectors(connector);
	}
}
