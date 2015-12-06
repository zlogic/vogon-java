/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Tomcat (via reflection for compatibility with both Tomcat 7 and
 * Tomcat 8)
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Configuration
public class TomcatConfigurer implements EmbeddedServletContainerCustomizer {

	/**
	 * The logger
	 */
	private final static org.slf4j.Logger log = LoggerFactory.getLogger(TomcatConfigurer.class);

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
	 * Configures URI encoding for Tomcat container
	 *
	 * @param container
	 * @throws RuntimeException
	 */
	private void configureUriEncoding(TomcatEmbeddedServletContainerFactory container) throws RuntimeException {
		Charset utf8Charset = Charset.forName("utf-8");//NOI18N
		try {
			log.debug(messages.getString("CONFIGURING_ENCODING_FOR_TOMCAT_7"));
			container.getClass().getMethod("setUriEncoding", String.class).invoke(container, utf8Charset.name()); //NOI18N
			return;
		} catch (NoSuchMethodException ex) {
			log.debug(messages.getString("SERVER_IS_NOT_TOMCAT_7"));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException(messages.getString("CANNOT_CONFIGURE_ENCODING_FOR_TOMCAT_7"), ex);
		}

		try {
			log.debug(messages.getString("CONFIGURING_ENCODING_FOR_TOMCAT_8"));
			container.getClass().getMethod("setUriEncoding", Charset.class).invoke(container, utf8Charset); //NOI18N
			return;
		} catch (NoSuchMethodException ex) {
			log.debug(messages.getString("SERVER_IS_NOT_TOMCAT_8"));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException(messages.getString("CANNOT_CONFIGURE_ENCODING_FOR_TOMCAT_8"), ex);
		}
		throw new RuntimeException(messages.getString("CANNOT_CONFIGURE_ENCODING_FOR_TOMCAT"));
	}

	/**
	 * Configures SSL for Tomcat container
	 *
	 * @param container TomcatEmbeddedServletContainerFactory instance to
	 * configure
	 */
	private void configureSSL(TomcatEmbeddedServletContainerFactory container) {
		if (keystoreFile.isEmpty() || keystorePass.isEmpty()) {
			log.debug(messages.getString("KEYSTORE_FILE_OR_PASSWORD_NOT_DEFINED"));
			return;
		}

		log.info(MessageFormat.format(messages.getString("USING_KEYSTORE_WITH_PASSWORD"), new Object[]{keystoreFile, keystorePass.replaceAll(".{1}", "*")})); //NOI18N
		Object connector;
		Class connectorClass = null;
		try {
			log.debug(messages.getString("CONFIGURING_CONNECTOR"));
			connectorClass = getClass().getClassLoader().loadClass("org.apache.catalina.connector.Connector"); //NOI18N
			connector = connectorClass.newInstance();
			connectorClass.getMethod("setPort", Integer.TYPE).invoke(connector, 8443); //NOI18N
			connectorClass.getMethod("setSecure", Boolean.TYPE).invoke(connector, true); //NOI18N
			connectorClass.getMethod("setScheme", String.class).invoke(connector, "https"); //NOI18N
			connectorClass.getMethod("setURIEncoding", String.class).invoke(connector, container.getUriEncoding().name()); //NOI18N
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException(messages.getString("CANNOT_CONFIGURE_CONNECTOR"), ex);
		}

		Object proto;
		try {
			log.debug(messages.getString("CONFIGURING_PROTOCOLHANDLER_PARAMETERS"));
			proto = connectorClass.getMethod("getProtocolHandler").invoke(connector); //NOI18N
			Class protoClass = proto.getClass();
			log.debug(java.text.MessageFormat.format(messages.getString("CONFIGURING_PROTOCOLHANDLER_CLASS"), new Object[]{protoClass.getCanonicalName()}));
			protoClass.getMethod("setSSLEnabled", Boolean.TYPE).invoke(proto, true); //NOI18N
			protoClass.getMethod("setKeystorePass", String.class).invoke(proto, keystorePass); //NOI18N
			protoClass.getMethod("setKeystoreType", String.class).invoke(proto, "JKS"); //NOI18N
			protoClass.getMethod("setKeyAlias", String.class).invoke(proto, "vogonkey"); //NOI18N
			protoClass.getMethod("setKeystoreFile", String.class).invoke(proto, new File(keystoreFile).getAbsolutePath()); //NOI18N
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException(messages.getString("CANNOT_CONFIGURE_PROTOCOLHANDLER"), ex);
		}

		try {
			log.debug(messages.getString("ADDING_CONNECTOR_TO_TOMCATEMBEDDEDSERVLETCONTAINERFACTORY"));
			Object connectors = Array.newInstance(connectorClass, 1);
			Array.set(connectors, 0, connector);
			container.getClass().getMethod("addAdditionalTomcatConnectors", connectors.getClass()).invoke(container, connectors); //NOI18N
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException(messages.getString("CANNOT_ADD_CONNECTOR_TO_TOMCATEMBEDDEDSERVLETCONTAINERFACTORY"), ex);
		}
	}

	/**
	 * Customizes the ConfigurableEmbeddedServletContainer by configuring Tomcat
	 * options
	 *
	 * @param container the ConfigurableEmbeddedServletContainer to customize
	 */
	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
		if (container instanceof TomcatEmbeddedServletContainerFactory) {
			TomcatEmbeddedServletContainerFactory tomcatContainer = (TomcatEmbeddedServletContainerFactory) container;
			configureUriEncoding(tomcatContainer);
			configureSSL(tomcatContainer);
		}
	}
}
