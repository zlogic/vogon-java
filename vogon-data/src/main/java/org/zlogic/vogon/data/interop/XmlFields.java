/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.interop;

import java.text.SimpleDateFormat;

/**
 * XML importer/exporter field names
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class XmlFields {

	protected static final String ROOT_NODE = "VogonFinanceData"; //NOI18N
	protected static final String ACCOUNTS_NODE = "Accounts"; //NOI18N
	protected static final String CURRENCIES_NODE = "Currencies"; //NOI18N
	protected static final String TRANSACTIONS_NODE = "Transactions"; //NOI18N
	protected static final String DEFAULT_CURRENCY_ATTRIBUTE = "DefaultCurrency"; //NOI18N
	protected static final String ACCOUNT_NODE = "Account"; //NOI18N
	protected static final String CURRENCY_RATE_NODE = "CurrencyRate"; //NOI18N
	protected static final String TRANSACTION_NODE = "Transaction"; //NOI18N
	protected static final String TRANSACTION_COMPONENT_NODE = "Component"; //NOI18N
	protected static final String TAG_NODE = "Tag"; //NOI18N
	protected static final String ID_ATTRIBUTE = "Id"; //NOI18N
	protected static final String NAME_ATTRIBUTE = "Name"; //NOI18N
	protected static final String CURRENCY_ATTRIBUTE = "Currency"; //NOI18N
	protected static final String INCLUDE_IN_TOTAL_ATTRIBUTE = "IncludeInTotal"; //NOI18N
	protected static final String SHOW_IN_LIST_ATTRIBUTE = "ShowInList"; //NOI18N
	protected static final String SOURCE_ATTRIBUTE = "Source"; //NOI18N
	protected static final String DESTINATION_ATTRIBUTE = "Destination"; //NOI18N
	protected static final String RATE_ATTRIBUTE = "Rate"; //NOI18N
	protected static final String TYPE_ATTRIBUTE = "Type"; //NOI18N
	protected static final String DESCRIPTION_ATTRIBUTE = "Description"; //NOI18N
	protected static final String DATE_ATTRIBUTE = "Date"; //NOI18N
	protected static final String ACCOUNT_ATTRIBUTE = "Account"; //NOI18N
	protected static final String AMOUNT_ATTRIBUTE = "Amount"; //NOI18N
	protected static final String TRANSACTION_ATTRIBUTE = "Transaction"; //NOI18N
	protected static final String TRANSACTION_TYPE_EXPENSEINCOME_VALUE = "ExpenseIncome"; //NOI18N
	protected static final String TRANSACTION_TYPE_TRANSFER_VALUE = "Transfer"; //NOI18N
	protected static final String TRANSACTION_TYPE_UNDEFINED_VALUE = ""; //NOI18N
	protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"); //NOI18N
}
