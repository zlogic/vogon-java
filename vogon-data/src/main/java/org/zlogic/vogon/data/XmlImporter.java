/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Implementation for importing data from XML files
 *
 * @author Dmitry Zolotukhin
 */
public class XmlImporter implements FileImporter {

	/**
	 * The input CSV file
	 */
	protected File inputFile;

	/**
	 * Creates an instance of the CSV Importer
	 * 
	 * @param inputFile The input file to read
	 */
	public XmlImporter(File inputFile){
		this.inputFile = inputFile;
	}

	/**
	 * Parses and imports a CSV file
	 *
	 * @return A new FinanceData object, initialized from the CSV file
	 * @throws VogonImportException In case of import errors (I/O, format, indexing etc.)
	 * @throws VogonImportLogicalException In case of logical errors (without meaningful stack trace, just to show an error message)
	 */
	@Override
	public FinanceData importFile() throws VogonImportException, VogonImportLogicalException {
		try {
			Map<Long,FinanceTransaction> transactionsMap = new HashMap<>();
			Map<Long,FinanceAccount> accountsMap = new HashMap<>();

			List<FinanceTransaction> transactionsList = new ArrayList<>();
			List<FinanceAccount> accountsList = new ArrayList<>();

			//Read XML
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();

			//Get root node
			Node rootNode = doc.getFirstChild();
			if(rootNode==null || !rootNode.getNodeName().equals("VogonFinanceData"))
				throw new VogonImportLogicalException("Missing VogonFinanceData node in XML");

			Node currentNode = null;

			//Iterate through root children
			Node accountsNode = null, transactionsNode = null;
			for(currentNode=rootNode.getFirstChild();currentNode!=null;currentNode=currentNode.getNextSibling()){
				if(currentNode.getNodeName().equals("Accounts"))
					accountsNode = currentNode;
				if(currentNode.getNodeName().equals("Transactions"))
					transactionsNode = currentNode;
			}

			//Process accounts
			EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();
			for(currentNode=accountsNode.getFirstChild();currentNode!=null;currentNode=currentNode.getNextSibling()){
				if(currentNode.getNodeType()!=Node.ELEMENT_NODE)
					continue;

				//Extract attributes from XML
				NamedNodeMap attributes = currentNode.getAttributes();
				String accountName = attributes.getNamedItem("Name").getNodeValue();
				long accountId = Long.parseLong(attributes.getNamedItem("Id").getNodeValue());
				//long accountBalance = Long.parseLong(attributes.getNamedItem("Balance").getNodeValue());

				//Search existing accounts in DB
				CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
				CriteriaQuery<FinanceAccount> accountsCriteriaQuery = criteriaBuilder.createQuery(FinanceAccount.class);
				Root<FinanceAccount> acc = accountsCriteriaQuery.from(FinanceAccount.class);
				Predicate condition = criteriaBuilder.equal(acc.get(FinanceAccount_.name), accountName);
				accountsCriteriaQuery.where(condition);
				List<FinanceAccount> foundAccounts = entityManager.createQuery(accountsCriteriaQuery).getResultList();

				//Match by account name
				if (!foundAccounts.isEmpty() && foundAccounts.get(0).getName().equals(accountName)) {
					accountsMap.put(foundAccounts.get(0).id,foundAccounts.get(0));
					accountsList.add(foundAccounts.get(0));
				} else {
					FinanceAccount account = new FinanceAccount(accountName);
					accountsMap.put(accountId,account);
					accountsList.add(account);
				}
			}

			//Process transactions
			for(currentNode=transactionsNode.getFirstChild();currentNode!=null;currentNode=currentNode.getNextSibling()){
				if(currentNode.getNodeType()!=Node.ELEMENT_NODE)
					continue;

				//Extract attributes from XML
				NamedNodeMap attributes = currentNode.getAttributes();
				String transactionType = attributes.getNamedItem("Type").getNodeValue();
				long transactionId = Long.parseLong(attributes.getNamedItem("Id").getNodeValue());
				String transactionDescription = attributes.getNamedItem("Description").getNodeValue();
				//String transactionAmount = attributes.getNamedItem("Amount").getNodeValue();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date transactionDate = dateFormat.parse(attributes.getNamedItem("Date").getNodeValue());



				//Create transaction instance
				FinanceTransaction transaction = null;
				if(transactionType.equals(ExpenseTransaction.class.getSimpleName()))
					transaction = new ExpenseTransaction(transactionDescription, null, transactionDate);
				else if(transactionType.equals(TransferTransaction.class.getSimpleName()))
					transaction = new TransferTransaction(transactionDescription, null, transactionDate);
				transactionsMap.put(transactionId, transaction);
				transactionsList.add(transaction);

				//Extract transaction tags and components from XML
				List<String> tagsList = new LinkedList<>();
				Node childNode = null;
				for(childNode=currentNode.getFirstChild();childNode!=null;childNode=childNode.getNextSibling()){
					if(childNode.getNodeType()!=Node.ELEMENT_NODE)
						continue;
					if(childNode.getNodeName().equals("Tag")){
						String tag = childNode.getTextContent();
						tagsList.add(tag);
					}else if(childNode.getNodeName().equals("Component")){
						//long componentId = Long.parseLong(childNode.getAttributes().getNamedItem("Id").getNodeValue());
						long componentAccountId =  Long.parseLong(childNode.getAttributes().getNamedItem("Account").getNodeValue());
						long componentAmount = Long.parseLong(childNode.getAttributes().getNamedItem("Amount").getNodeValue());
						long componentTransactionId = Long.parseLong(childNode.getAttributes().getNamedItem("Transaction").getNodeValue());
						TransactionComponent component = new TransactionComponent(accountsMap.get(componentAccountId),transactionsMap.get(componentTransactionId),componentAmount);
						transaction.addComponent(component);
					}
				}

				String[] tagsArray = new String[tagsList.size()];
				tagsArray = tagsList.toArray(tagsArray);
				transaction.setTags(tagsArray);
			}

			FinanceData result = new FinanceData(transactionsList, accountsList);
			return result;
		} catch (java.io.FileNotFoundException e) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportException(e);
		} catch (java.io.IOException e) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportException(e);
		} catch (VogonImportLogicalException e) {
			throw new VogonImportLogicalException(e);
		} catch (ParserConfigurationException e) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportException(e);
		} catch (SAXException e) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportException(e);
		} catch (NullPointerException e){
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportLogicalException("Missing data from XML", e);
		} catch (DOMException e) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportException(e);
		} catch (ParseException e) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportLogicalException("Invalid data format", e);
		}

	}
}
