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
import java.util.ResourceBundle;
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

	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/data/messages");
	/**
	 * The input CSV file
	 */
	protected File inputFile;

	/**
	 * Creates an instance of the CSV Importer
	 *
	 * @param inputFile the input file to read
	 */
	public XmlImporter(File inputFile) {
		this.inputFile = inputFile;
	}

	/**
	 * Parses and imports a CSV file
	 *
	 * @throws VogonImportException in case of import errors (I/O, format,
	 * indexing etc.)
	 * @throws VogonImportLogicalException in case of logical errors (without
	 * meaningful stack trace, just to show an error message)
	 */
	@Override
	public void importFile() throws VogonImportException, VogonImportLogicalException {
		try {
			EntityManager entityManager = DatabaseManager.getInstance().createEntityManager();
			entityManager.getTransaction().begin();

			Map<Long, FinanceTransaction> transactionsMap = new HashMap<>();
			Map<Long, FinanceAccount> accountsMap = new HashMap<>();

			//Read XML
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();

			//Get root node
			Node rootNode = doc.getFirstChild();
			if (rootNode == null || !rootNode.getNodeName().equals("VogonFinanceData")) //NOI18N
				throw new VogonImportLogicalException(messages.getString("MISSING_VOGONFINANCEDATA_NODE_IN_XML"));

			Node currentNode = null;

			//Iterate through root children
			Node accountsNode = null, currenciesNode = null, transactionsNode = null;
			for (currentNode = rootNode.getFirstChild(); currentNode != null; currentNode = currentNode.getNextSibling()) {
				if (currentNode.getNodeName().equals("Accounts")) //NOI18N
					accountsNode = currentNode;
				if (currentNode.getNodeName().equals("Currencies")) //NOI18N
					currenciesNode = currentNode;
				if (currentNode.getNodeName().equals("Transactions")) //NOI18N
					transactionsNode = currentNode;
			}

			//Process default properties
			{
				String defaultCurrency = rootNode.getAttributes().getNamedItem("DefaultCurrency").getNodeValue(); //NOI18N

				CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
				CriteriaQuery<Preferences> preferencesCriteriaQuery = criteriaBuilder.createQuery(Preferences.class);
				Root<Preferences> prf = preferencesCriteriaQuery.from(Preferences.class);
				Preferences foundPreferences = null;
				try {
					foundPreferences = entityManager.createQuery(preferencesCriteriaQuery).getSingleResult();
				} catch (javax.persistence.NoResultException ex) {
				}

				if (foundPreferences == null) {
					Preferences preferences = new Preferences();
					entityManager.persist(preferences);
					preferences.setDefaultCurrency(Currency.getInstance(defaultCurrency));
				} else if (foundPreferences.getDefaultCurrency() == null) {
					foundPreferences.setDefaultCurrency(Currency.getInstance(defaultCurrency));
				}
			}

			//Process accounts
			for (currentNode = accountsNode.getFirstChild(); currentNode != null; currentNode = currentNode.getNextSibling()) {
				if (currentNode.getNodeType() != Node.ELEMENT_NODE)
					continue;

				//Extract attributes from XML
				NamedNodeMap attributes = currentNode.getAttributes();
				String accountName = attributes.getNamedItem("Name").getNodeValue(); //NOI18N
				long accountId = Long.parseLong(attributes.getNamedItem("Id").getNodeValue()); //NOI18N
				boolean includeInTotal = Boolean.parseBoolean(attributes.getNamedItem("IncludeInTotal").getNodeValue()); //NOI18N
				String currency = attributes.getNamedItem("Currency") != null ? attributes.getNamedItem("Currency").getNodeValue() : null; //NOI18N
				//long accountBalance = Long.parseLong(attributes.getNamedItem("Balance").getNodeValue());

				//Search existing accounts in DB
				CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
				CriteriaQuery<FinanceAccount> accountsCriteriaQuery = criteriaBuilder.createQuery(FinanceAccount.class);
				Root<FinanceAccount> acc = accountsCriteriaQuery.from(FinanceAccount.class);
				Predicate condition = criteriaBuilder.equal(acc.get(FinanceAccount_.name), accountName);
				accountsCriteriaQuery.where(condition);
				FinanceAccount foundAccount = null;
				try {
					foundAccount = entityManager.createQuery(accountsCriteriaQuery).getSingleResult();
				} catch (javax.persistence.NoResultException ex) {
				}

				//Match by account name
				if (foundAccount != null && foundAccount.getName().equals(accountName)) {
					accountsMap.put(accountId, foundAccount);
				} else {
					FinanceAccount account = new FinanceAccount(accountName, currency != null ? Currency.getInstance(currency) : null);
					account.setIncludeInTotal(includeInTotal);
					accountsMap.put(accountId, account);
					entityManager.persist(account);
				}
			}

			//Process currencies
			for (currentNode = currenciesNode.getFirstChild(); currentNode != null; currentNode = currentNode.getNextSibling()) {
				if (currentNode.getNodeType() != Node.ELEMENT_NODE)
					continue;

				//Extract attributes from XML
				NamedNodeMap attributes = currentNode.getAttributes();
				String sourceCurrencyName = attributes.getNamedItem("Source").getNodeValue(); //NOI18N
				String destinationCurrencyName = attributes.getNamedItem("Destination").getNodeValue(); //NOI18N
				double exchangeRate = Double.parseDouble(attributes.getNamedItem("Rate").getNodeValue()); //NOI18N

				//Search existing currency rates in DB
				CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
				CriteriaQuery<CurrencyRate> currencyCriteriaQuery = criteriaBuilder.createQuery(CurrencyRate.class);
				Root<CurrencyRate> rate = currencyCriteriaQuery.from(CurrencyRate.class);
				Predicate sourceCondition = criteriaBuilder.equal(rate.get(CurrencyRate_.source), sourceCurrencyName);
				Predicate destinationCondition = criteriaBuilder.equal(rate.get(CurrencyRate_.destination), destinationCurrencyName);
				currencyCriteriaQuery.where(criteriaBuilder.and(sourceCondition, destinationCondition));
				CurrencyRate foundCurrencyRate = null;
				try {
					foundCurrencyRate = entityManager.createQuery(currencyCriteriaQuery).getSingleResult();
				} catch (javax.persistence.NoResultException ex) {
				}

				//Match by currency source and destination
				if (foundCurrencyRate == null || !(foundCurrencyRate.getSource().getCurrencyCode().equals(sourceCurrencyName) && foundCurrencyRate.getDestination().getCurrencyCode().equals(destinationCurrencyName))) {
					CurrencyRate currencyRate = new CurrencyRate(Currency.getInstance(sourceCurrencyName), Currency.getInstance(destinationCurrencyName), exchangeRate);
					entityManager.persist(currencyRate);
				}
			}

			//Process transactions
			for (currentNode = transactionsNode.getFirstChild(); currentNode != null; currentNode = currentNode.getNextSibling()) {
				if (currentNode.getNodeType() != Node.ELEMENT_NODE)
					continue;

				//Extract attributes from XML
				NamedNodeMap attributes = currentNode.getAttributes();
				String transactionType = attributes.getNamedItem("Type").getNodeValue(); //NOI18N
				long transactionId = Long.parseLong(attributes.getNamedItem("Id").getNodeValue()); //NOI18N
				String transactionDescription = attributes.getNamedItem("Description").getNodeValue(); //NOI18N
				//String transactionAmount = attributes.getNamedItem("Amount").getNodeValue();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); //NOI18N
				Date transactionDate = dateFormat.parse(attributes.getNamedItem("Date").getNodeValue()); //NOI18N

				//Create transaction instance
				FinanceTransaction.Type transactionTypeEnum = null;
				if (transactionType.equals("Transfer"))
					transactionTypeEnum = FinanceTransaction.Type.TRANSFER;
				else if (transactionType.equals("ExpenseIncome"))
					transactionTypeEnum = FinanceTransaction.Type.EXPENSEINCOME;
				else
					transactionTypeEnum = FinanceTransaction.Type.UNDEFINED;
				FinanceTransaction transaction = new FinanceTransaction(transactionDescription, null, transactionDate, transactionTypeEnum);
				transactionsMap.put(transactionId, transaction);
				entityManager.persist(transaction);

				//Extract transaction tags and components from XML
				List<String> tagsList = new LinkedList<>();
				Node childNode = null;
				for (childNode = currentNode.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
					if (childNode.getNodeType() != Node.ELEMENT_NODE)
						continue;
					if (childNode.getNodeName().equals("Tag")) { //NOI18N
						String tag = childNode.getTextContent();
						tagsList.add(tag);
					} else if (childNode.getNodeName().equals("Component")) { //NOI18N
						//long componentId = Long.parseLong(childNode.getAttributes().getNamedItem("Id").getNodeValue());
						long componentAccountId = Long.parseLong(childNode.getAttributes().getNamedItem("Account").getNodeValue()); //NOI18N
						long componentAmount = Long.parseLong(childNode.getAttributes().getNamedItem("Amount").getNodeValue()); //NOI18N
						long componentTransactionId = Long.parseLong(childNode.getAttributes().getNamedItem("Transaction").getNodeValue()); //NOI18N
						TransactionComponent component = new TransactionComponent(accountsMap.get(componentAccountId), transactionsMap.get(componentTransactionId), componentAmount);
						entityManager.persist(component);
						transaction.addComponent(component);
					}
				}

				transaction.setTags(tagsList.toArray(new String[0]));
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
		} catch (NullPointerException e) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportLogicalException(messages.getString("MISSING_DATA_FROM_XML"), e);
		} catch (DOMException e) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportException(e);
		} catch (ParseException e) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportLogicalException(messages.getString("INVALID_DATA_FORMAT"), e);
		}

	}
}
