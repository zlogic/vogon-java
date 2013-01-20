/*
 * Awesome Time Tracker project.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.att.data.converters;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.zlogic.att.data.CustomField;
import org.zlogic.att.data.PersistenceHelper;
import org.zlogic.att.data.Task;
import org.zlogic.att.data.TimeSegment;

/**
 * Imported or Grindstone 3 data exported with SQL Compact Command Line Tool
 * <http://sqlcecmd.codeplex.com/>
 *
 * @author Dmitry Zolotukhin <zlogic@gmail.com>
 */
public class GrindstoneImporter implements Importer {

	/**
	 * File to be imported
	 */
	private File importFile;
	/**
	 * The logger
	 */
	private final static Logger log = Logger.getLogger(GrindstoneImporter.class.getName());
	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/att/data/messages");

	/**
	 * Constructor for the importer
	 *
	 * @param importFile the file to be imported
	 */
	public GrindstoneImporter(File importFile) {
		this.importFile = importFile;
	}

	/**
	 * Converts a node's attributes and element-attributes into a Map.
	 * Attributes have a preference above element-attributes. Later elements
	 * have a preference over earlier elements.
	 *
	 * @param node the node to be converted
	 * @return the node's attributes and element-attributes as a Map
	 */
	protected Map<String, String> convertNodeToMap(Node node) {
		Map<String, String> nodeMap = new TreeMap<>();
		//Convert element-attributes
		for (Node currentNode = node.getFirstChild(); currentNode != null; currentNode = currentNode.getNextSibling())
			if ((currentNode.getNodeType() == Node.ELEMENT_NODE) && (!currentNode.hasChildNodes() || (currentNode.getChildNodes().getLength() == 1 && currentNode.getFirstChild().getNodeType() == Node.TEXT_NODE))) {
				log.log(Level.FINER, messages.getString("CONVERTING_ELEMENT-ATTRIBUTE"), new Object[]{currentNode.getNodeName(), currentNode.getTextContent()});
				nodeMap.put(currentNode.getNodeName(), currentNode.getTextContent());
			} else {
				log.log(Level.FINER, messages.getString("SKIPPING_ELEMENT-ATTRIBUTE_BECAUSE_IT_HAS_CHILD_NODES"), currentNode.getNodeName());
			}
		//Convert attributes (override element-attributes)
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attributeNode = attributes.item(i);
			log.log(Level.FINER, messages.getString("CONVERTING_ATTRIBUTE"), new Object[]{attributeNode.getNodeName(), attributeNode.getTextContent()});
			nodeMap.put(attributeNode.getNodeName(), attributeNode.getNodeValue());
		}
		return nodeMap;
	}

	@Override
	public void importData(EntityManager entityManager) {
		log.log(Level.FINER, messages.getString("IMPORTING_FILE"), importFile.toString());
		PersistenceHelper persistenceHelper = new PersistenceHelper();
		try {
			//Read XML
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(importFile);
			doc.getDocumentElement().normalize();

			//Get root node
			Node rootNode = doc.getFirstChild();
			if (rootNode == null || !rootNode.getNodeName().equals("Grindstone"))
				throw new ImportException(messages.getString("CANNOT_FIND_ROOT_XML_ELEMENT"));

			Map<String, List<Map<String, String>>> tables = new TreeMap<>();

			//Iterate through exported tables
			for (Node currentNode = rootNode.getFirstChild(); currentNode != null; currentNode = currentNode.getNextSibling()) {
				log.log(Level.FINER, messages.getString("CONVERTING_NODE"), currentNode.getNodeName());
				if (currentNode.getNodeName().equals("NewDataSet")) {
					//Iterate through values from a single table
					for (Node entryNode = currentNode.getFirstChild(); entryNode != null; entryNode = entryNode.getNextSibling()) {
						if (entryNode.getNodeName().equals("xs:schema"))
							log.log(Level.FINER, messages.getString("SKIPPING_SCHEMA_NODE"), currentNode.getNodeName());
						else if (entryNode.getNodeType() == Node.ELEMENT_NODE) {
							String nodeType = entryNode.getNodeName();
							log.log(Level.FINER, messages.getString("PROCESSING_NODE"), nodeType);
							if (!tables.containsKey(nodeType))
								tables.put(nodeType, new LinkedList<Map<String, String>>());
							List<Map<String, String>> nodesList = tables.get(nodeType);
							Map<String, String> nodeAttributes = convertNodeToMap(entryNode);
							nodesList.add(nodeAttributes);
						} else
							log.log(Level.FINER, messages.getString("SKIPPING_NODE_TYPE"), entryNode.getNodeType());
					}
				}
			}

			//Create entities from parsed tables
			Map<String, CustomField> customFields = new TreeMap<>();
			Map<String, Task> tasks = new TreeMap<>();

			//Parse custom fields
			for (Map<String, String> entry : tables.get("CustomFields")) {
				CustomField customField = persistenceHelper.createCustomField(entityManager);
				customField.setName(entry.get("Name"));
				customFields.put(entry.get("Id"), customField);
			}

			//Parse tasks
			for (Map<String, String> entry : tables.get("Tasks")) {
				Task task = persistenceHelper.createTask(entityManager);
				task.setName(entry.get("Name"));
				task.setCompleted(entry.containsKey("Complete"));
				task.setDescription(entry.get("Notes"));
				tasks.put(entry.get("Id"), task);
			}

			//Parse custom fields values
			for (Map<String, String> entry : tables.get("CustomValues")) {
				Task task = tasks.get(entry.get("TaskId"));
				CustomField customField = customFields.get(entry.get("CustomFieldId"));
				if (task == null || customField == null)
					throw new ImportException(messages.getString("CANNOT_MATCH_CUSTOM_FIELD_WITH_TASK_FOR_CUSTOM_VALUE"));
				task.setCustomField(customField, entry.get("Value"));
			}

			//Parse timeslots
			for (Map<String, String> entry : tables.get("Times")) {
				Task task = tasks.get(entry.get("TaskId"));
				if (task == null)
					throw new ImportException(messages.getString("CANNOT_MATCH_TIME_WITH_TASK"));
				TimeSegment timeSegment = persistenceHelper.createTimeSegment(entityManager, task);
				timeSegment.setStartTime(DatatypeConverter.parseDateTime(entry.get("Start")).getTime());
				timeSegment.setEndTime(DatatypeConverter.parseDateTime(entry.get("End")).getTime());
				timeSegment.setDescription(entry.get("Notes"));
			}
		} catch (SAXException | IOException | ParserConfigurationException ex) {
			Logger.getLogger(GrindstoneImporter.class.getName()).log(Level.SEVERE, null, ex);
			throw new ImportException(ex);
		}
	}
}
