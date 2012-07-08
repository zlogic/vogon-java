/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.File;
import java.text.MessageFormat;
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

/**
 * Implementation for exporting data to XML files
 * 
 * @author Zlogic
 */
public class XmlExporter implements FileExporter {

	/**
	 * The output XML file
	 */
	protected File outputFile;

	/**
	 * Creates an instance of the XML Exporter
	 * 
	 * @param outputFile The output file to write
	 */
	public XmlExporter(File outputFile){
		this.outputFile = outputFile;
	}
	/**
	 * Exports data into an XML file
	 * 
	 * @param financeData the data to be exported
	 * @throws VogonExportException In case of any import errors (I/O, format etc.) 
	 */
	@Override
	public void exportFile(FinanceData financeData) throws VogonExportException {
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();

			// Top element (FinanceData)
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("VogonFinanceData");
			doc.appendChild(rootElement);

			//Accounts node
			Element accountsElement = doc.createElement("Accounts");
			rootElement.appendChild(accountsElement);

			//Transactions node
			Element transactionsElement = doc.createElement("Transactions");
			rootElement.appendChild(transactionsElement);

			//Accounts list
			for(FinanceAccount account : financeData.getAccounts()){
				Element accountElement = doc.createElement("Account");
				accountElement.setAttribute("Id",Long.toString(account.id));
				accountElement.setAttribute("Name", account.getName());
				//accountElement.setAttribute("Balance", Long.toString(account.getRawBalance()));
				accountsElement.appendChild(accountElement);
			}

			//Transactions list
			for(FinanceTransaction transaction : financeData.getTransactions()){
				Element transactionElement = doc.createElement("Transaction");
				transactionElement.setAttribute("Type", transaction.getClass().getSimpleName());
				transactionElement.setAttribute("Id",Long.toString(transaction.id));
				transactionElement.setAttribute("Description",transaction.getDescription());
				//transactionElement.setAttribute("Amount", Long.toString(transaction.getRawAmount()));
				transactionElement.setAttribute("Date", MessageFormat.format("{0,date,yyyy-MM-dd}", new Object[]{transaction.getDate()})); //$NON-NLS-1$
				//Tags list
				for(String tag : transaction.getTags()){
					Element tagElement = doc.createElement("Tag");
					tagElement.setTextContent(tag);
					transactionElement.appendChild(tagElement);
				}
				//Transaction components list
				for(TransactionComponent component : transaction.getComponents()){
					Element compomentElement = doc.createElement("Component");
					compomentElement.setAttribute("Id",Long.toString(component.id));
					compomentElement.setAttribute("Account", Long.toString(component.getAccount().id));
					compomentElement.setAttribute("Amount", Long.toString(component.getRawAmount()));
					compomentElement.setAttribute("Transaction",Long.toString(component.getTransaction().id));
					transactionElement.appendChild(compomentElement);
				}
				transactionsElement.appendChild(transactionElement);
			}

			// Write the content into XML file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(outputFile);

			transformer.transform(source, result);
		} catch (ParserConfigurationException | TransformerException e) {
			Logger.getLogger(CsvImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonExportException(e);
		}
	}

}
