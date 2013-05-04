/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data.converters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.zlogic.att.data.CustomField;
import org.zlogic.att.data.CustomField_;
import org.zlogic.att.data.PersistenceHelper;
import org.zlogic.att.data.Task;
import org.zlogic.att.data.TimeSegment;

/**
 * Importer of files exported by Awesome Time Tracker
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class XmlImporter implements Importer {

	/**
	 * File to be imported
	 */
	private File importFile;
	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(XmlImporter.class.getName());
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/data/messages");

	/**
	 * Constructor for the importer
	 *
	 * @param importFile the file to be imported
	 */
	public XmlImporter(File importFile) {
		this.importFile = importFile;
	}

	@Override
	public void importData(PersistenceHelper persistenceHelper, EntityManager entityManager) {
		log.log(Level.FINER, messages.getString("IMPORTING_FILE"), importFile.toString());
		try {
			//Read XML
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(importFile);
			doc.getDocumentElement().normalize();

			//Get root node
			Node rootNode = doc.getFirstChild();
			if (rootNode == null || !rootNode.getNodeName().equals("AwesomeTimeTracker")) //NOI18N
				throw new ImportException(messages.getString("CANNOT_FIND_ROOT_XML_ELEMENT"));

			//Iterate through root children
			Node customFieldsNode = null, tasksNode = null;
			for (Node currentNode = rootNode.getFirstChild(); currentNode != null; currentNode = currentNode.getNextSibling()) {
				if (currentNode.getNodeName().equals("CustomFields")) //NOI18N
					customFieldsNode = currentNode;
				else if (currentNode.getNodeName().equals("Tasks")) //NOI18N
					tasksNode = currentNode;
				else if (currentNode.getNodeType() != Node.TEXT_NODE)
					Logger.getLogger(XmlImporter.class.getName()).log(Level.WARNING, MessageFormat.format(messages.getString("UNRECOGNIZED_NODE"), currentNode.getNodeName()));
			}

			//Maps to store results
			Map<Long, CustomField> customFieldsMap = new TreeMap<>();
			Map<Long, Task> tasksMap = new TreeMap<>();

			//Process custom fields
			for (Node currentNode = customFieldsNode.getFirstChild(); currentNode != null; currentNode = currentNode.getNextSibling()) {
				if (currentNode.getNodeType() != Node.ELEMENT_NODE)
					continue;

				//Extract attributes from XML
				NamedNodeMap attributes = currentNode.getAttributes();
				String customFieldName = attributes.getNamedItem("Name").getNodeValue(); //NOI18N
				long customFieldId = Long.parseLong(attributes.getNamedItem("Id").getNodeValue()); //NOI18N

				//Search existing custom fields in DB
				CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
				CriteriaQuery<CustomField> customFieldsCriteriaQuery = criteriaBuilder.createQuery(CustomField.class);
				Root<CustomField> customFieldRoot = customFieldsCriteriaQuery.from(CustomField.class);
				Predicate condition = criteriaBuilder.equal(customFieldRoot.get(CustomField_.name), customFieldName);
				customFieldsCriteriaQuery.where(condition);
				CustomField foundCustomField = null;
				try {
					foundCustomField = entityManager.createQuery(customFieldsCriteriaQuery).getSingleResult();
				} catch (javax.persistence.NoResultException ex) {
				}

				//Match by custom field name
				if (foundCustomField != null && foundCustomField.getName().equals(customFieldName)) {
					customFieldsMap.put(customFieldId, foundCustomField);
				} else {
					CustomField customField = persistenceHelper.createCustomField(entityManager);
					customField.setName(customFieldName);
					customFieldsMap.put(customFieldId, customField);
				}
			}

			//Process tasks
			DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
			for (Node currentNode = tasksNode.getFirstChild(); currentNode != null; currentNode = currentNode.getNextSibling()) {
				if (currentNode.getNodeType() != Node.ELEMENT_NODE)
					continue;

				//Extract attributes from XML
				NamedNodeMap attributes = currentNode.getAttributes();
				long taskId = Long.parseLong(attributes.getNamedItem("Id").getNodeValue()); //NOI18N
				String taskName = (attributes.getNamedItem("Name") != null) //NOI18N
						? attributes.getNamedItem("Name").getNodeValue() //NOI18N
						: null; //NOI18N
				String taskDescription = (attributes.getNamedItem("Description") != null) //NOI18N
						? attributes.getNamedItem("Description").getNodeValue() //NOI18N
						: null; //NOI18N
				Boolean taskCompleted = (attributes.getNamedItem("Completed") != null) //NOI18N
						? Boolean.parseBoolean(attributes.getNamedItem("Completed").getNodeValue()) //NOI18N
						: null; //NOI18N

				Task task = persistenceHelper.createTask(entityManager);
				task.setName(taskName);
				task.setDescription(taskDescription);
				task.setCompleted(taskCompleted);
				tasksMap.put(taskId, task);

				//Extract custom fields and time segments from XML
				for (Node childNode = currentNode.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
					if (childNode.getNodeType() != Node.ELEMENT_NODE)
						continue;
					switch (childNode.getNodeName()) {
						case "CustomField":	//NOI18N
							long customFieldId = Long.parseLong(childNode.getAttributes().getNamedItem("Id").getNodeValue()); //NOI18N
							CustomField customField = customFieldsMap.get(customFieldId);
							String customFieldValue = (childNode.getAttributes().getNamedItem("Value") != null) //NOI18N
									? childNode.getAttributes().getNamedItem("Value").getNodeValue() //NOI18N
									: null; //NOI18N
							task.setCustomField(customField, customFieldValue);
							break;
						case "TimeSegment"://NOI18N
							//long componentId = Long.parseLong(childNode.getAttributes().getNamedItem("Id").getNodeValue()); //NOI18N
							String timeSegmentDescription = (childNode.getAttributes().getNamedItem("Description") != null) //NOI18N
									? childNode.getAttributes().getNamedItem("Description").getNodeValue() //NOI18N
									: null;
							Date startTime = (childNode.getAttributes().getNamedItem("StartTime") != null) //NOI18N
									? datatypeFactory.newXMLGregorianCalendar(childNode.getAttributes().getNamedItem("StartTime").getNodeValue()).toGregorianCalendar().getTime() //NOI18N
									: null;
							Date endTime = (childNode.getAttributes().getNamedItem("EndTime") != null) //NOI18N
									? datatypeFactory.newXMLGregorianCalendar(childNode.getAttributes().getNamedItem("EndTime").getNodeValue()).toGregorianCalendar().getTime() //NOI18N
									: null;

							TimeSegment timeSegment = persistenceHelper.createTimeSegment(entityManager, task);
							timeSegment.setStartTime(startTime);
							timeSegment.setEndTime(endTime);
							timeSegment.setDescription(timeSegmentDescription);
							break;
					}
				}
			}

		} catch (DatatypeConfigurationException ex) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, ex);
			throw new ImportException(ex);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, ex);
			throw new ImportException(ex);
		} catch (IOException ex) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, ex);
			throw new ImportException(ex);
		} catch (ParserConfigurationException | SAXException | DOMException ex) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, ex);
			throw new ImportException(ex);
		} catch (NullPointerException ex) {
			Logger.getLogger(XmlImporter.class.getName()).log(Level.SEVERE, null, ex);
			throw new ImportException(messages.getString("MISSING_DATA_FROM_XML"), ex);
		}
	}
}
