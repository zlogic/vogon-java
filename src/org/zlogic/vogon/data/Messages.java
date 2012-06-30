package org.zlogic.vogon.data;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.zlogic.vogon.data.messages"; //$NON-NLS-1$
	public static String CsvImporter_CSV_Format_Exception;
	public static String CsvImporter_Transaction_Too_Complex;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
