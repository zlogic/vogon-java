package org.zlogic.vogon.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.zlogic.vogon.ui.messages"; //$NON-NLS-1$
	public static String MainWindow_File_Import_Dialog_CsvFilter;
	public static String MainWindow_File_Import_Dialog_Header;
	public static String MainWindow_shell_text;
	public static String MainWindow_mntmNewSubmenu_text;
	public static String MainWindow_mntmImport_text;
	public static String MainWindow_mntmPreferences_text;
	public static String MainWindow_tbtmTransactions_text;
	public static String MainWindow_tbtmAccounts_text;
	public static String MainWindow_tblclmnDescription_text;
	public static String MainWindow_tblclmnDate_text;
	public static String MainWindow_tblclmnTags_text;
	public static String MainWindow_tblclmnAccounts_text;
	public static String MainWindow_tblclmnAccount_text;
	public static String MainWindow_tblclmnBalance_text;
	public static String MainWindow_tblclmnAmount_text;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private Messages() {
		// do not instantiate
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Class initialization
	//
	////////////////////////////////////////////////////////////////////////////
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
