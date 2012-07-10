package org.zlogic.vogon.data;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.zlogic.vogon.data.messages"; //$NON-NLS-1$
	public static String CsvImporter_CSV_Format_Exception;
	public static String CsvImporter_Transaction_Too_Complex;
	public static String XmlImporter_Error_Invalid_data_format;
	public static String XmlImporter_Error_Missing_data_from_XML;
	public static String XmlImporter_Error_Missing_VogonFinanceData_node_in_XML;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
