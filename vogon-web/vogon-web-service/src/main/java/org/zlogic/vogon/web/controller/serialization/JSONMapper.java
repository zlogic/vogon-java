/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller.serialization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.InitializingBean;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;

/**
 * JSON mapper configuration to not send unnecessary data to client
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class JSONMapper extends ObjectMapper implements InitializingBean {

	/**
	 * Wrapper class for TransactionComponent
	 */
	@JsonIgnoreProperties({"transaction"})
	private static class TransactionComponentJson extends TransactionComponent {

	}

	/**
	 * Wrapper class for FinanceTransaction
	 */
	@JsonIgnoreProperties({"owner"})
	private static class TransactionJson extends FinanceTransaction {

	}

	/**
	 * Wrapper class for FinanceAccount
	 */
	@JsonIgnoreProperties({"owner"})
	private static class AccountJson extends FinanceAccount {

	}

	/**
	 * Adds MixIn Annotations
	 *
	 * @throws Exception in case of errors
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.addMixInAnnotations(TransactionComponent.class, TransactionComponentJson.class);
		this.addMixInAnnotations(FinanceTransaction.class, TransactionJson.class);
		this.addMixInAnnotations(FinanceAccount.class, AccountJson.class);
	}
}
