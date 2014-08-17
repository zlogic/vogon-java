/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.util.logging.Logger;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.zlogic.vogon.data.FinanceData;

/**
 * Class for initializing the Java EE services
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Singleton
@Startup
@ManagedBean
public class Initializer {

	/**
	 * The logger
	 */
	private final static Logger logger = Logger.getLogger(Initializer.class.getName());

	/**
	 * The persistence helper instance
	 */
	private FinanceData financeData;

	/**
	 * Initialize and start the FinanceData instance
	 */
	@PostConstruct
	public void start() {
		logger.info("Starting vogon");
		financeData = new FinanceData("file:" + System.getProperty("jboss.server.data.dir") + "/h2db");
	}

	/**
	 * Stop the FinanceData instance
	 */
	@PreDestroy
	public void stop() {
		logger.info("Stopping vogon");
		financeData.shutdown();
	}

	/**
	 * Returns the FinanceData instance
	 *
	 * @return the FinanceData instance
	 */
	public FinanceData getFinanceData() {
		return financeData;
	}
}
