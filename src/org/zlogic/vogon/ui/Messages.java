package org.zlogic.vogon.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.zlogic.vogon.ui.messages"; //$NON-NLS-1$
	public static String MainWindow_Bad_Account_Substitute;
	public static String MainWindow_File_Import_Dialog_CsvFilter;
	public static String MainWindow_File_Import_Dialog_Header;
	public static String MainWindow_shell_text;
	public static String MainWindow_mntmNewSubmenu_text;
	public static String MainWindow_mntmImport_text;
	public static String MainWindow_mntmPreferences_text;
	public static String MainWindow_tbtmTransactions_text;
	public static String MainWindow_tbtmAccounts_text;
	public static String MainWindow_tblclmnAccount_text;
	public static String MainWindow_tblclmnBalance_text;
	public static String MainWindow_btnAddAccount_text;
	public static String MainWindow_btnDeleteAccount_text;
	public static String MainWindow_Error_Importing_File;
	public static String MainWindow_Error_Importing_File_Description;
	public static String MainWindow_New_Account_Name;
	public static String MainWindow_New_Expense_Transaction_Name;
	public static String MainWindow_New_Transfer_Transaction_Name;
	public static String MainWindow_OK;
	public static String MainWindow_trclmnDescription_text;
	public static String MainWindow_trclmnDate_text;
	public static String MainWindow_trclmnTags_text;
	public static String MainWindow_trclmnAmount_text;
	public static String MainWindow_trclmnAccounts_text;
	public static String MainWindow_btnAddTransaction_text;
	public static String MainWindow_btnAddComponent_text;
	public static String MainWindow_btnDeleteSelection_text;
	public static String MainWindow_btnAddTransferTransaction_text;
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
