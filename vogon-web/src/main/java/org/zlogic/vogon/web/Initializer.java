/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;

/**
 * Class for initializing the Java EE services
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic@gmail.com">zlogic@gmail.com</a>
 */
@Singleton
@Startup
public class Initializer {

	private final static Logger logger = Logger.getLogger(Initializer.class.getName());

	/**
	 * The persistence helper instance
	 */
	private FinanceData financeData;

	public Initializer() {

	}

	@PostConstruct
	public void start() {
		logger.info("Starting vogon");
		financeData = new FinanceData("file:" + System.getProperty("jboss.server.data.dir") + "/h2db");
		for (FinanceAccount account : financeData.getAccounts())
			logger.log(Level.INFO, "Account: {0}", account.getName());
	}

	@PreDestroy
	public void stop() {
		logger.info("Stopping vogon");
		financeData.shutdown();
	}
}
