/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.interop;

import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Collection;
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
		transactionTypes.put(FinanceTransaction.Type.TRANSFER, "Transfer"); //NOI18N
		transactionTypes.put(FinanceTransaction.Type.EXPENSEINCOME, "ExpenseIncome"); //NOI18N
		transactionTypes.put(FinanceTransaction.Type.UNDEFINED, ""); //NOI18N
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();

			//TODO: use constants for element names
			// Top element (FinanceData)
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("VogonFinanceData"); //NOI18N
			doc.appendChild(rootElement);

			//Set global parameters
			rootElement.setAttribute("DefaultCurrency", owner.getDefaultCurrency().getCurrencyCode()); //NOI18N

			//Accounts node
			Element accountsElement = doc.createElement("Accounts"); //NOI18N
			rootElement.appendChild(accountsElement);

			//Currencies node
			Element currenciesElement = doc.createElement("Currencies"); //NOI18N
			rootElement.appendChild(currenciesElement);

			//Transactions node
			Element transactionsElement = doc.createElement("Transactions"); //NOI18N
			rootElement.appendChild(transactionsElement);

			//Accounts list
			for (FinanceAccount account : accounts) {
				Element accountElement = doc.createElement("Account"); //NOI18N
				accountElement.setAttribute("Id", Long.toString(account.getId())); //NOI18N
				accountElement.setAttribute("Name", account.getName()); //NOI18N
				accountElement.setAttribute("Currency", account.getCurrency().getCurrencyCode()); //NOI18N
				accountElement.setAttribute("IncludeInTotal", Boolean.toString(account.getIncludeInTotal())); //NOI18N
				accountElement.setAttribute("ShowInList", Boolean.toString(account.getShowInList())); //NOI18N
				//accountElement.setAttribute("Balance", Long.toString(account.getRawBalance()));
				accountsElement.appendChild(accountElement);
			}

			//Currencies list
			if (currencyRates != null)
				for (CurrencyRate rate : currencyRates) {
					Element currencyElement = doc.createElement("CurrencyRate"); //NOI18N
					currencyElement.setAttribute("Source", rate.getSource().getCurrencyCode()); //NOI18N
					currencyElement.setAttribute("Destination", rate.getDestination().getCurrencyCode()); //NOI18N
					currencyElement.setAttribute("Rate", Double.toString(rate.getExchangeRate())); //NOI18N
					currenciesElement.appendChild(currencyElement);
				}

			//Transactions list
			for (FinanceTransaction transaction : transactions) {
				Element transactionElement = doc.createElement("Transaction"); //NOI18N
				transactionElement.setAttribute("Type", transactionTypes.get(transaction.getType())); //NOI18N
				transactionElement.setAttribute("Id", Long.toString(transaction.getId())); //NOI18N
				transactionElement.setAttribute("Description", transaction.getDescription()); //NOI18N
				//transactionElement.setAttribute("Amount", Long.toString(transaction.getRawAmount()));
				transactionElement.setAttribute("Date", MessageFormat.format("{0,date,yyyy-MM-dd}", new Object[]{transaction.getDate()})); //NOI18N
				//Tags list
				for (String tag : transaction.getTags()) {
					Element tagElement = doc.createElement("Tag"); //NOI18N
					tagElement.setTextContent(tag);
					transactionElement.appendChild(tagElement);
				}
				//Transaction components list
				for (TransactionComponent component : transaction.getComponents()) {
					Element compomentElement = doc.createElement("Component"); //NOI18N
					compomentElement.setAttribute("Id", Long.toString(component.getId())); //NOI18N
					compomentElement.setAttribute("Account", Long.toString(component.getAccount().getId())); //NOI18N
					compomentElement.setAttribute("Amount", Long.toString(component.getRawAmount())); //NOI18N
					compomentElement.setAttribute("Transaction", Long.toString(component.getTransaction().getId())); //NOI18N
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
