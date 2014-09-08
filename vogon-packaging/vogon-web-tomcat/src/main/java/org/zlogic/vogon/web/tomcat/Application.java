/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.tomcat;

/**
 * Spring boot runner
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class Application {

	/**
	 * The main method to run the application
	 *
	 * @param args arguments
	 */
	public static void main(String[] args) {
		org.zlogic.vogon.web.Application.main(args);
	}
}
