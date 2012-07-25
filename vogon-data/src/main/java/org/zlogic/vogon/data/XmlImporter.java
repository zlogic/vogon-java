/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
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
	 * @throws VogonImportException In case of import errors (I/O, format, indexing etc.)
	 * @throws VogonImportLogicalException In case of logical errors (without meaningful stack trace, just to show an error message)
	 */
	@Override
	public void importFile() throws VogonImportException, VogonImportLogicalException {
		try {
			EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
			entityManager.getTransaction().begin();

			Map<Long,FinanceTransaction> transactionsMap = new HashMap<>();
			Map<Long,FinanceAccount> accountsMap = new HashMap<>();

			//Read XML
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();

			//Get root node
			Node rootNode = doc.getFirstChild();
			if(rootNode==null || !rootNode.getNodeName().equals("VogonFinanceData")) //$NON-NLS-1$
				throw new VogonImportLogicalException(Messages.XmlImporter_Error_Missing_VogonFinanceData_node_in_XML);

			Node currentNode = null;

			//Iterate through root children
			Node accountsNode = null, currenciesNode = null, transactionsNode = null;
			for(currentNode=rootNode.getFirstChild();currentNode!=null;currentNode=currentNode.getNextSibling()){
				if(currentNode.getNodeName().equals("Accounts")) //$NON-NLS-1$
					accountsNode = currentNode;
				if(currentNode.getNodeName().equals("Currencies")) //$NON-NLS-1$
					currenciesNode = currentNode;
				if(currentNode.getNodeName().equals("Transactions")) //$NON-NLS-1$
					transactionsNode = currentNode;
			}

			//Process default properties
			{
				String defaultCurrency = rootNode.getAttributes().getNamedItem("DefaultCurrency").getNodeValue(); //$NON-NLS-1$

				CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
				CriteriaQuery<Preferences> accountsCriteriaQuery = criteriaBuilder.createQuery(Preferences.class);
				Preferences foundPreferences = entityManager.createQuery(accountsCriteriaQuery).getSingleResult();

				if(foundPreferences == null){
					Preferences preferences = new Preferences();
					entityManager.persist(preferences);
					preferences.setDefaultCurrency(Currency.getInstance(defaultCurrency));
				} else if(foundPreferences.getDefaultCurrency()==null){
					foundPreferences.setDefaultCurrency(Currency.getInstance(defaultCurrency));
				}
			}

			//Process accounts
			for(currentNode=accountsNode.getFirstChild();currentNode!=null;currentNode=currentNode.getNextSibling()){
				if(currentNode.getNodeType()!=Node.ELEMENT_NODE)
					continue;

				//Extract attributes from XML
				NamedNodeMap attributes = currentNode.getAttributes();
				String accountName = attributes.getNamedItem("Name").getNodeValue(); //$NON-NLS-1$
				long accountId = Long.parseLong(attributes.getNamedItem("Id").getNodeValue()); //$NON-NLS-1$
				String currency = attributes.getNamedItem("Currency")!=null?attributes.getNamedItem("Currency").getNodeValue():null; //$NON-NLS-1$ //$NON-NLS-2$
				//long accountBalance = Long.parseLong(attributes.getNamedItem("Balance").getNodeValue());

				//Search existing accounts in DB
				CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
				CriteriaQuery<FinanceAccount> accountsCriteriaQuery = criteriaBuilder.createQuery(FinanceAccount.class);
				Root<FinanceAccount> acc = accountsCriteriaQuery.from(FinanceAccount.class);
				Predicate condition = criteriaBuilder.equal(acc.get(FinanceAccount_.name), accountName);
				accountsCriteriaQuery.where(condition);
				FinanceAccount foundAccount = entityManager.createQuery(accountsCriteriaQuery).getSingleResult();

				//Match by account name
				if (foundAccount!=null && foundAccount.getName().equals(accountName)) {
					accountsMap.put(accountId,foundAccount);
				} else {
					FinanceAccount account = new FinanceAccount(accountName,currency!=null?Currency.getInstance(currency):null);
					accountsMap.put(accountId,account);
					entityManager.persist(account);
				}
			}

			//Process currencies
			for(currentNode=currenciesNode.getFirstChild();currentNode!=null;currentNode=currentNode.getNextSibling()){
				if(currentNode.getNodeType()!=Node.ELEMENT_NODE)
					continue;

				//Extract attributes from XML
				NamedNodeMap attributes = currentNode.getAttributes();
				String sourceCurrencyName = attributes.getNamedItem("Source").getNodeValue(); //$NON-NLS-1$
				String destinationCurrencyName = attributes.getNamedItem("Destination").getNodeValue(); //$NON-NLS-1$
				double exchangeRate = Double.parseDouble(attributes.getNamedItem("Rate").getNodeValue()); //$NON-NLS-1$

				//Search existing currency rates in DB
				CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
				CriteriaQuery<CurrencyRate> currencyCriteriaQuery = criteriaBuilder.createQuery(CurrencyRate.class);
				Root<CurrencyRate> rate = currencyCriteriaQuery.from(CurrencyRate.class);
				Predicate sourceCondition = criteriaBuilder.equal(rate.get(CurrencyRate_.source), sourceCurrencyName);
				Predicate destinationCondition = criteriaBuilder.equal(rate.get(CurrencyRate_.destination), destinationCurrencyName);
				currencyCriteriaQuery.where(criteriaBuilder.and(sourceCondition,destinationCondition));
				CurrencyRate foundCurrencyRate = entityManager.createQuery(currencyCriteriaQuery).getSingleResult();

				//Match by currency source and destination
				if (foundCurrencyRate == null || !(foundCurrencyRate.getSource().getCurrencyCode().equals(sourceCurrencyName) && foundCurrencyRate.getDestination().getCurrencyCode().equals(destinationCurrencyName))) {
					CurrencyRate currencyRate = new CurrencyRate(Currency.getInstance(sourceCurrencyName), Currency.getInstance(destinationCurrencyName), exchangeRate);
					entityManager.persist(currencyRate);
				}
			}

			//Process transactions
			for(currentNode=transactionsNode.getFirstChild();currentNode!=null;currentNode=currentNode.getNextSibling()){
				if(currentNode.getNodeType()!=Node.ELEMENT_NODE)
					continue;

				//Extract attributes from XML
				NamedNodeMap attributes = currentNode.getAttributes();
				String transactionType = attributes.getNamedItem("Type").getNodeValue(); //$NON-NLS-1$
				long transactionId = Long.parseLong(attributes.getNamedItem("Id").getNodeValue()); //$NON-NLS-1$
				String transactionDescription = attributes.getNamedItem("Description").getNodeValue(); //$NON-NLS-1$
				//String transactionAmount = attributes.getNamedItem("Amount").getNodeValue();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
				Date transactionDate = dateFormat.parse(attributes.getNamedItem("Date").getNodeValue()); //$NON-NLS-1$

				//Create transaction instance
				FinanceTransaction transaction = null;
				if(transactionType.equals(ExpenseTransaction.class.getSimpleName()))
					transaction = new ExpenseTransaction(transactionDescription, null, transactionDate);
				else if(transactionType.equals(TransferTransaction.class.getSimpleName()))
					transaction = new TransferTransaction(transactionDescription, null, transactionDate);
				transactionsMap.put(transactionId, transaction);
				entityManager.persist(transaction);

				//Extract transaction tags and components from XML
				List<String> tagsList = new LinkedList<>();
				Node childNode = null;
				for(childNode=currentNode.getFirstChild();childNode!=null;childNode=childNode.getNextSibling()){
					if(childNode.getNodeType()!=Node.ELEMENT_NODE)
						continue;
					if(childNode.getNodeName().equals("Tag")){ //$NON-NLS-1$
						String tag = childNode.getTextContent();
						tagsList.add(tag);
					}else if(childNode.getNodeName().equals("Component")){ //$NON-NLS-1$
						//long componentId = Long.parseLong(childNode.getAttributes().getNamedItem("Id").getNodeValue());
						long componentAccountId =  Long.parseLong(childNode.getAttributes().getNamedItem("Account").getNodeValue()); //$NON-NLS-1$
						long componentAmount = Long.parseLong(childNode.getAttributes().getNamedItem("Amount").getNodeValue()); //$NON-NLS-1$
						long componentTransactionId = Long.parseLong(childNode.getAttributes().getNamedItem("Transaction").getNodeValue()); //$NON-NLS-1$
						TransactionComponent component = new TransactionComponent(accountsMap.get(componentAccountId),transactionsMap.get(componentTransactionId),componentAmount);
						entityManager.persist(component);
						transaction.addComponent(component);
					}
				}

				String[] tagsArray = new String[tagsList.size()];
				tagsArray = tagsList.toArray(tagsArray);
				transaction.setTags(tagsArray);

			}
			entityManager.getTransaction().commit();
			entityManager.close();
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
			throw new VogonImportLogicalException(Messages.XmlImporter_Error_Missing_data_from_XML, e);
		} catch (DOMException e) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportException(e);
		} catch (ParseException e) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportLogicalException(Messages.XmlImporter_Error_Invalid_data_format, e);
		}

	}
}
