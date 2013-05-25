/*
 * Awesome Time Tracker project.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
 */
package org.zlogic.att.data.converters;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
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
import org.zlogic.att.data.CustomField;
import org.zlogic.att.data.PersistenceHelper;
import org.zlogic.att.data.Task;
import org.zlogic.att.data.TimeSegment;

/**
 * Exporter of data to XML files
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
 */
public class XmlExporter implements Exporter {

	/**
	 * The output XML file
	 */
	protected File outputFile;

	/**
	 * Creates an instance of the XML Exporter
	 *
	 * @param outputFile the output file to write
	 */
	public XmlExporter(File outputFile) {
		this.outputFile = outputFile;
	}

	/**
	 * Exports data into an XML file
	 *
	 * @param persistenceHelper the PersistenceHelper to be used for obtaining
	 * data
	 * @throws ExportException exception which happens during exporting
	 */
	@Override
	public void exportData(PersistenceHelper persistenceHelper) throws ExportException {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();

			// Top element (FinanceData)
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("AwesomeTimeTracker"); //NOI18N
			doc.appendChild(rootElement);

			//CustomFields node
			Element customFieldsElement = doc.createElement("CustomFields"); //NOI18N
			rootElement.appendChild(customFieldsElement);

			//Tasks node
			Element tasksElement = doc.createElement("Tasks"); //NOI18N
			rootElement.appendChild(tasksElement);

			//Custom fields list
			List<CustomField> customFields = persistenceHelper.getCustomFields();
			for (CustomField customField : customFields) {
				Element customFieldElement = doc.createElement("CustomField"); //NOI18N
				customFieldElement.setAttribute("Id", Long.toString(customField.getId())); //NOI18N
				customFieldElement.setAttribute("Name", customField.getName()); //NOI18N
				customFieldsElement.appendChild(customFieldElement);
			}

			//Tasks list
			DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
			for (Task task : persistenceHelper.getAllTasks(false)) {
				Element taskElement = doc.createElement("Task"); //NOI18N
				taskElement.setAttribute("Id", Long.toString(task.getId())); //NOI18N
				if (task.getName() != null)
					taskElement.setAttribute("Name", task.getName()); //NOI18N
				if (task.getDescription() != null)
					taskElement.setAttribute("Description", task.getDescription()); //NOI18N
				taskElement.setAttribute("Completed", Boolean.toString(task.getCompleted())); //NOI18N

				//Tags list
				for (CustomField customField : customFields) {
					String customFieldValue = task.getCustomField(customField);
					if (customFieldValue == null)
						continue;
					Element customFieldElement = doc.createElement("CustomField"); //NOI18N
					customFieldElement.setAttribute("Id", Long.toString(customField.getId())); //NOI18N
					customFieldElement.setAttribute("Value", customFieldValue); //NOI18N
					taskElement.appendChild(customFieldElement);
				}
				//Transaction components list
				for (TimeSegment timeSegment : task.getTimeSegments()) {
					Element timeSegmentElement = doc.createElement("TimeSegment"); //NOI18N
					timeSegmentElement.setAttribute("Id", Long.toString(timeSegment.getId())); //NOI18N
					if (timeSegment.getStartTime() != null) {
						GregorianCalendar startTimeCalendar = new GregorianCalendar();
						startTimeCalendar.setTime(timeSegment.getStartTime());
						timeSegmentElement.setAttribute("StartTime", datatypeFactory.newXMLGregorianCalendar(startTimeCalendar).toXMLFormat()); //NOI18N
					}
					if (timeSegment.getEndTime() != null) {
						GregorianCalendar endTimeCalendar = new GregorianCalendar();
						endTimeCalendar.setTime(timeSegment.getEndTime());
						timeSegmentElement.setAttribute("EndTime", datatypeFactory.newXMLGregorianCalendar(endTimeCalendar).toXMLFormat()); //NOI18N
					}
					if (timeSegment.getDescription() != null)
						timeSegmentElement.setAttribute("Description", timeSegment.getDescription()); //NOI18N
					taskElement.appendChild(timeSegmentElement);
				}
				tasksElement.appendChild(taskElement);
			}

			// Write the content into XML file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //NOI18N
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(outputFile);

			transformer.transform(source, result);
		} catch (DatatypeConfigurationException | ParserConfigurationException | TransformerException e) {
			Logger.getLogger(XmlExporter.class.getName()).log(Level.SEVERE, null, e);
			throw new ExportException(e);
		}
	}
}
