/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.interop;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.zlogic.vogon.data.CurrencyRate;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.data.VogonUser;

/**
 * Implementation for exporting data to XML files
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class XmlExporter implements Exporter {

	/**
	 * The output XML stream
	 */
	protected OutputStream outputStream;

	/**
	 * Creates an instance of the XML Exporter
	 *
	 * @param outputStream the output stream to write
	 */
	public XmlExporter(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void exportData(VogonUser owner, Collection<FinanceAccount> accounts, Collection<FinanceTransaction> transactions, Collection<CurrencyRate> currencyRates) throws VogonExportException {
		Map<FinanceTransaction.Type, String> transactionTypes = new TreeMap<>();
		transactionTypes.put(FinanceTransaction.Type.TRANSFER, XmlFields.TRANSACTION_TYPE_TRANSFER_VALUE);
		transactionTypes.put(FinanceTransaction.Type.EXPENSEINCOME, XmlFields.TRANSACTION_TYPE_EXPENSEINCOME_VALUE);
		transactionTypes.put(FinanceTransaction.Type.UNDEFINED, XmlFields.TRANSACTION_TYPE_UNDEFINED_VALUE);
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();

			//TODO: use constants for element names
			// Top element (FinanceData)
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement(XmlFields.ROOT_NODE);
			doc.appendChild(rootElement);

			//Set global parameters
			if (owner.getDefaultCurrency() != null)
				rootElement.setAttribute(XmlFields.DEFAULT_CURRENCY_ATTRIBUTE, owner.getDefaultCurrency().getCurrencyCode());

			//Accounts node
			Element accountsElement = doc.createElement(XmlFields.ACCOUNTS_NODE);
			rootElement.appendChild(accountsElement);

			//Currencies node
			Element currenciesElement = doc.createElement(XmlFields.CURRENCIES_NODE);
			rootElement.appendChild(currenciesElement);

			//Transactions node
			Element transactionsElement = doc.createElement(XmlFields.TRANSACTIONS_NODE);
			rootElement.appendChild(transactionsElement);

			Map<Long, Long> accountRemapping = new HashMap<>();

			//Accounts list
			for (FinanceAccount account : accounts) {
				long id = accountRemapping.isEmpty() ? 0 : Collections.max(accountRemapping.values()) + 1;
				accountRemapping.put(account.getId(), id);
				Element accountElement = doc.createElement(XmlFields.ACCOUNT_NODE);
				accountElement.setAttribute(XmlFields.ID_ATTRIBUTE, Long.toString(id));
				accountElement.setAttribute(XmlFields.NAME_ATTRIBUTE, account.getName());
				accountElement.setAttribute(XmlFields.CURRENCY_ATTRIBUTE, account.getCurrency().getCurrencyCode());
				accountElement.setAttribute(XmlFields.INCLUDE_IN_TOTAL_ATTRIBUTE, Boolean.toString(account.getIncludeInTotal()));
				accountElement.setAttribute(XmlFields.SHOW_IN_LIST_ATTRIBUTE, Boolean.toString(account.getShowInList()));
				//accountElement.setAttribute(XmlFields.BALANCE_ATTRIBUTE, Long.toString(account.getRawBalance()));
				accountsElement.appendChild(accountElement);
			}

			//Currencies list
			if (currencyRates != null)
				for (CurrencyRate rate : currencyRates) {
					Element currencyElement = doc.createElement(XmlFields.CURRENCY_RATE_NODE);
					currencyElement.setAttribute(XmlFields.SOURCE_ATTRIBUTE, rate.getSource().getCurrencyCode());
					currencyElement.setAttribute(XmlFields.DESTINATION_ATTRIBUTE, rate.getDestination().getCurrencyCode());
					currencyElement.setAttribute(XmlFields.RATE_ATTRIBUTE, Double.toString(rate.getExchangeRate()));
					currenciesElement.appendChild(currencyElement);
				}

			//Transactions list
			for (FinanceTransaction transaction : transactions) {
				Element transactionElement = doc.createElement(XmlFields.TRANSACTION_NODE);
				transactionElement.setAttribute(XmlFields.TYPE_ATTRIBUTE, transactionTypes.get(transaction.getType()));
				//transactionElement.setAttribute(XmlFields.ID_ATTRIBUTE, Long.toString(transaction.getId()));
				transactionElement.setAttribute(XmlFields.DESCRIPTION_ATTRIBUTE, transaction.getDescription());
				//transactionElement.setAttribute(XmlFields.AMOUNT_ATTRIBUTE, Long.toString(transaction.getRawAmount()));
				transactionElement.setAttribute(XmlFields.DATE_ATTRIBUTE, XmlFields.DATE_FORMAT.format(transaction.getDate()));
				//Tags list
				String[] tags = transaction.getTags();
				Arrays.sort(tags);
				for (String tag : tags) {
					Element tagElement = doc.createElement(XmlFields.TAG_NODE);
					tagElement.setTextContent(tag);
					transactionElement.appendChild(tagElement);
				}
				//Transaction components list
				for (TransactionComponent component : transaction.getComponents()) {
					Element compomentElement = doc.createElement(XmlFields.TRANSACTION_COMPONENT_NODE);
					long accountId = accountRemapping.get(component.getAccount().getId());
					//compomentElement.setAttribute(XmlFields.ID_ATTRIBUTE, Long.toString(component.getId()));
					compomentElement.setAttribute(XmlFields.ACCOUNT_ATTRIBUTE, Long.toString(accountId));
					compomentElement.setAttribute(XmlFields.AMOUNT_ATTRIBUTE, Long.toString(component.getRawAmount()));
					//compomentElement.setAttribute(XmlFields.TRANSACTION_ATTRIBUTE, Long.toString(component.getTransaction().getId()));
					transactionElement.appendChild(compomentElement);
				}
				transactionsElement.appendChild(transactionElement);
			}

			// Write the content into XML file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //NOI18N
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(outputStream);

			transformer.transform(source, result);
		} catch (ParserConfigurationException | TransformerException e) {
			Logger.getLogger(XmlExporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonExportException(e);
		}
	}
}
