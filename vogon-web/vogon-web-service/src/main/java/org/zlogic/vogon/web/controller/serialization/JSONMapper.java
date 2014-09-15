/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller.serialization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.web.data.model.FinanceTransactionJson;

/**
 * JSON mapper configuration to not send unnecessary data to client
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Service
public class JSONMapper extends ObjectMapper implements InitializingBean {

	/**
	 * Wrapper class for FinanceTransaction
	 */
	@JsonIgnoreProperties(value = {"owner", "accounts", "fromAccounts", "toAccounts", "amountOk", "currencies"})
	private interface FinanceTransactionAnnotations {

		/**
		 * Disables returning of raw amount
		 */
		@JsonIgnore
		public void getRawAmount();

		/**
		 * Allows returning of amount
		 */
		@JsonProperty
		public void getAmount();

		/**
		 * Disables setting of amount
		 */
		@JsonIgnore
		public void setAmount();

		/**
		 * Disables setting of components (an alternative API is used instead)
		 */
		@JsonIgnore
		public void setComponents();

		/**
		 * Disables getting of components (an alternative API is used instead)
		 */
		@JsonIgnore
		public void getComponents();
	}

	/**
	 * Wrapper class for FinanceAccount
	 */
	@JsonIgnoreProperties({"owner"})
	private interface FinanceAccountAnnotations {
	}

	/**
	 * Adds MixIn Annotations
	 *
	 * @throws Exception in case of errors
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.addMixInAnnotations(FinanceTransactionJson.class, FinanceTransactionAnnotations.class);
		this.addMixInAnnotations(FinanceAccount.class, FinanceAccountAnnotations.class);
		this.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
	}
}
