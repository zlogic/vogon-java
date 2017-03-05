/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * Spring boot runner
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

	/**
	 * The main method to run the application
	 *
	 * @param args arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	/**
	 * Configures the application when being run from a war container
	 *
	 * @param application SpringApplicationBuilder instance
	 * @return SpringApplicationBuilder with the annotations
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}
}
