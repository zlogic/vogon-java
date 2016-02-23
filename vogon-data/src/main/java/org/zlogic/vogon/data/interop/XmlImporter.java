/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data.interop;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.text.ParseException;
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
import org.zlogic.vogon.data.CurrencyRate;
import org.zlogic.vogon.data.CurrencyRate_;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceAccount_;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.data.TransactionComponent;

/**
 * Implementation for importing data from XML files
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class XmlImporter implements Importer {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/data/messages");
	/**
	 * The input XML stream
	 */
	protected InputStream inputStream;

	/**
	 * Creates an instance of the CSV Importer
	 *
	 * @param inputStream the input stream to read
	 */
	public XmlImporter(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public void importData(VogonUser owner, EntityManager entityManager) throws VogonImportException, VogonImportLogicalException {
		try {
			Map<Long, FinanceAccount> accountsMap = new HashMap<>();

			//Read XML
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputStream);
			doc.getDocumentElement().normalize();

			//TODO: do not throw exceptions on missing elements
			//Get root node
			Node rootNode = doc.getFirstChild();
			if (rootNode == null || !rootNode.getNodeName().equals(XmlFields.ROOT_NODE))
				throw new VogonImportLogicalException(messages.getString("MISSING_VOGONFINANCEDATA_NODE_IN_XML"));

			//Iterate through root children
			Node accountsNode = null, currenciesNode = null, transactionsNode = null;
			for (Node currentNode = rootNode.getFirstChild(); currentNode != null; currentNode = currentNode.getNextSibling()) {
				if (currentNode.getNodeName().equals(XmlFields.ACCOUNTS_NODE))
					accountsNode = currentNode;
				else if (currentNode.getNodeName().equals(XmlFields.CURRENCIES_NODE))
					currenciesNode = currentNode;
				else if (currentNode.getNodeName().equals(XmlFields.TRANSACTIONS_NODE))
					transactionsNode = currentNode;
				else if (currentNode.getNodeType() != Node.TEXT_NODE)
					Logger.getLogger(XmlImporter.class.getName()).log(Level.WARNING, MessageFormat.format(messages.getString("UNRECOGNIZED_NODE"), currentNode.getNodeName()));
			}

			//Process default properties
			{
				Node defaultCurrencyAttrubute = rootNode.getAttributes().getNamedItem(XmlFields.DEFAULT_CURRENCY_ATTRIBUTE);
				if (defaultCurrencyAttrubute != null) {
					String defaultCurrency = defaultCurrencyAttrubute.getNodeValue();

					owner.setDefaultCurrency(Currency.getInstance(defaultCurrency));
				}
			}

			//Process accounts
			for (Node currentNode = accountsNode.getFirstChild(); currentNode != null; currentNode = currentNode.getNextSibling()) {
				if (currentNode.getNodeType() != Node.ELEMENT_NODE)
					continue;

				//Extract attributes from XML
				NamedNodeMap attributes = currentNode.getAttributes();
				String accountName = attributes.getNamedItem(XmlFields.NAME_ATTRIBUTE).getNodeValue(); //NOI18N
				long accountId = Long.parseLong(attributes.getNamedItem(XmlFields.ID_ATTRIBUTE).getNodeValue());
				boolean includeInTotal = attributes.getNamedItem(XmlFields.INCLUDE_IN_TOTAL_ATTRIBUTE) != null ? Boolean.parseBoolean(attributes.getNamedItem(XmlFields.INCLUDE_IN_TOTAL_ATTRIBUTE).getNodeValue()) : true;
				boolean showInList = attributes.getNamedItem(XmlFields.SHOW_IN_LIST_ATTRIBUTE) != null ? Boolean.parseBoolean(attributes.getNamedItem(XmlFields.SHOW_IN_LIST_ATTRIBUTE).getNodeValue()) : true;
				String currency = attributes.getNamedItem(XmlFields.CURRENCY_ATTRIBUTE) != null ? attributes.getNamedItem(XmlFields.CURRENCY_ATTRIBUTE).getNodeValue() : null;
				//long accountBalance = Long.parseLong(attributes.getNamedItem(XmlFields.BALANCE_ATTRIBUTE).getNodeValue());

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
					FinanceAccount account = new FinanceAccount(owner, accountName, currency != null ? Currency.getInstance(currency) : null);
					account.setIncludeInTotal(includeInTotal);
					account.setShowInList(showInList);
					accountsMap.put(accountId, account);
					entityManager.persist(account);
				}
			}

			//Process currencies
			for (Node currentNode = currenciesNode.getFirstChild(); currentNode != null; currentNode = currentNode.getNextSibling()) {
				if (currentNode.getNodeType() != Node.ELEMENT_NODE)
					continue;

				//Extract attributes from XML
				NamedNodeMap attributes = currentNode.getAttributes();
				String sourceCurrencyName = attributes.getNamedItem(XmlFields.SOURCE_ATTRIBUTE).getNodeValue();
				String destinationCurrencyName = attributes.getNamedItem(XmlFields.DESTINATION_ATTRIBUTE).getNodeValue();
				double exchangeRate = Double.parseDouble(attributes.getNamedItem(XmlFields.RATE_ATTRIBUTE).getNodeValue());

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
			for (Node currentNode = transactionsNode.getFirstChild(); currentNode != null; currentNode = currentNode.getNextSibling()) {
				if (currentNode.getNodeType() != Node.ELEMENT_NODE)
					continue;

				//Extract attributes from XML
				NamedNodeMap attributes = currentNode.getAttributes();
				String transactionType = attributes.getNamedItem(XmlFields.TYPE_ATTRIBUTE).getNodeValue();
				//long transactionId = Long.parseLong(attributes.getNamedItem(XmlFields.ID_ATTRIBUTE).getNodeValue());
				String transactionDescription = attributes.getNamedItem(XmlFields.DESCRIPTION_ATTRIBUTE).getNodeValue();
				//String transactionAmount = attributes.getNamedItem(XmlFields.AMOUNT_ATTRIBUTE).getNodeValue();
				Date transactionDate = XmlFields.DATE_FORMAT.parse(attributes.getNamedItem(XmlFields.DATE_ATTRIBUTE).getNodeValue());

				//Create transaction instance
				FinanceTransaction.Type transactionTypeEnum;
				switch (transactionType) {
					case XmlFields.TRANSACTION_TYPE_TRANSFER_VALUE:
						transactionTypeEnum = FinanceTransaction.Type.TRANSFER;
						break;
					case XmlFields.TRANSACTION_TYPE_EXPENSEINCOME_VALUE:
						transactionTypeEnum = FinanceTransaction.Type.EXPENSEINCOME;
						break;
					default:
						transactionTypeEnum = FinanceTransaction.Type.UNDEFINED;
						break;
				}
				if (transactionTypeEnum == FinanceTransaction.Type.UNDEFINED)
					throw new VogonImportLogicalException(MessageFormat.format(messages.getString("UNKNOWN_TRANSACTION_TYPE"), transactionType));
				FinanceTransaction transaction = new FinanceTransaction(owner, transactionDescription, null, transactionDate, transactionTypeEnum);
				entityManager.persist(transaction);

				//Extract transaction tags and components from XML
				List<String> tagsList = new LinkedList<>();
				for (Node childNode = currentNode.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
					if (childNode.getNodeType() != Node.ELEMENT_NODE)
						continue;
					switch (childNode.getNodeName()) {
						case XmlFields.TAG_NODE:
							String tag = childNode.getTextContent();
							tagsList.add(tag);
							break;
						case XmlFields.TRANSACTION_COMPONENT_NODE:
							//long componentId = Long.parseLong(childNode.getAttributes().getNamedItem(XmlFields.ID_ATTRIBUTE).getNodeValue());
							long componentAccountId = Long.parseLong(childNode.getAttributes().getNamedItem(XmlFields.ACCOUNT_ATTRIBUTE).getNodeValue());
							long componentAmount = Long.parseLong(childNode.getAttributes().getNamedItem(XmlFields.AMOUNT_ATTRIBUTE).getNodeValue());
							TransactionComponent component = new TransactionComponent(accountsMap.get(componentAccountId), transaction, componentAmount);
							entityManager.persist(component);
							transaction.addComponent(component);
							break;
					}
				}

				transaction.setTags(tagsList.toArray(new String[0]));
			}
		} catch (VogonImportLogicalException e) {
			throw new VogonImportLogicalException(e);
		} catch (FileNotFoundException e) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportException(e);
		} catch (IOException e) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportException(e);
		} catch (ParserConfigurationException | SAXException | DOMException e) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportException(e);
		} catch (NullPointerException e) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportLogicalException(messages.getString("MISSING_DATA_FROM_XML"), e);
		} catch (ParseException e) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, e);
			throw new VogonImportLogicalException(messages.getString("INVALID_DATA_FORMAT"), e);
		}
	}
}
