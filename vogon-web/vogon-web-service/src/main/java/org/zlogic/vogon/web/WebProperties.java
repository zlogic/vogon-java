/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class for providing access to properties in JSP files
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class WebProperties {

	private static Properties getProps(String path) throws IOException {
		Properties props = new Properties();
		InputStream in = null;
		try {
			in = WebProperties.class.getClassLoader().getResourceAsStream(path);
			props.load(in);
		} finally {
			if (in != null)
				in.close();
		}
		return props;
	}

	public static String getProperty(String name) throws IOException {
		return getProps("props.properties").getProperty(name);
	}
}