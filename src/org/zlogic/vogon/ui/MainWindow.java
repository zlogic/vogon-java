/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */

package org.zlogic.vogon.ui;

import java.io.File;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.zlogic.vogon.data.CsvImporter;
import org.zlogic.vogon.data.DatabaseManager;
import org.zlogic.vogon.data.ExpenseTransaction;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransferTransaction;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * The main window class
 * 
 * @author Dmitry Zolotukhin
 */
public class MainWindow {

	/**
	 * The transactions table label provider implementation
	 * 
	 * @author Dmitry Zolotukhin
	 */
	private class TransactionsTableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			if(element instanceof FinanceTransaction){
				FinanceTransaction transaction = (FinanceTransaction)element;
				switch (columnIndex) {
				case 0:
					return transaction.getDescription();
				case 1:
					return transaction.getDate().toString();
				case 2:
					return org.zlogic.vogon.data.Utils.join(transaction.getTags(), ","); //$NON-NLS-1$
				case 3:
					if (transaction.getClass() == ExpenseTransaction.class)
						return MessageFormat.format("{0,number,0.00}", new Object[]{((ExpenseTransaction) transaction).getAmount()}); //$NON-NLS-1$
					else if (transaction.getClass() == TransferTransaction.class)
						return MessageFormat.format("{0,number,0.00}", new Object[]{((TransferTransaction) transaction).getAmount()}); //$NON-NLS-1$
				case 4:
					if (transaction.getClass() == ExpenseTransaction.class) {
						FinanceAccount[] accounts = ((ExpenseTransaction) transaction).getAccounts();
						StringBuilder builder = new StringBuilder();
						for (int i = 0; i < accounts.length; i++)
							builder.append(i != 0 ? "," : "").append(accounts[i].getName()); //$NON-NLS-1$ //$NON-NLS-2$
						if(accounts.length==0)
							builder.append(Messages.MainWindow_Bad_Account_Substitute);
						return builder.toString();
					} else if (transaction.getClass() == TransferTransaction.class) {
						FinanceAccount[] toAccounts = ((TransferTransaction) transaction).getToAccounts();
						FinanceAccount[] fromAccounts = ((TransferTransaction) transaction).getFromAccounts();
						StringBuilder builder = new StringBuilder();
						if (fromAccounts.length > 1) {
							builder.append("("); //$NON-NLS-1$
							for (int i = 0; i < fromAccounts.length; i++)
								builder.append(i != 0 ? "," : "").append(fromAccounts[i].getName()); //$NON-NLS-1$ //$NON-NLS-2$
							builder.append(")"); //$NON-NLS-1$
						} else if (fromAccounts.length == 1)
							builder.append(fromAccounts[0].getName());
						else
							builder.append(Messages.MainWindow_Bad_Account_Substitute);
						builder.append("->"); //$NON-NLS-1$
						if (toAccounts.length > 1) {
							builder.append("("); //$NON-NLS-1$
							for (int i = 0; i < toAccounts.length; i++)
								builder.append(i != 0 ? "," : "").append(toAccounts[i].getName()); //$NON-NLS-1$ //$NON-NLS-2$
							builder.append(")"); //$NON-NLS-1$
						} else if (toAccounts.length == 1)
							builder.append(toAccounts[0].getName());
						else
							builder.append(Messages.MainWindow_Bad_Account_Substitute);
						return builder.toString();
					} else
						return ""; //$NON-NLS-1$
				}
			}
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * The accounts table label provider implementation
	 * 
	 * @author Dmitry Zolotukhin
	 */
	private class AccountsTableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			if(element instanceof FinanceAccount){
				FinanceAccount account = (FinanceAccount)element;
				switch (columnIndex) {
				case 0:
					return account.getName();
				case 1:
					return MessageFormat.format("{0,number,0.00}", new Object[]{account.getBalance()}); //$NON-NLS-1$
				}
			}
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * The transactions content provider implementation
	 * 
	 * @author Dmitry Zolotukhin
	 */
	private static class TransactionsContentProvider implements IStructuredContentProvider {		
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof FinanceData){
				FinanceData financeData = (FinanceData)inputElement;
				Object[] elements = new Object[financeData.getNumTransactions()];
				for(int i=0;i<financeData.getNumTransactions();i++)
					elements[i] = financeData.getTransaction(i);
				return elements;
			}else return new Object[]{};
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	/**
	 * The accounts content provider implementation
	 * 
	 * @author Dmitry Zolotukhin
	 */
	private static class AccountsContentProvider implements IStructuredContentProvider {		
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof FinanceData){
				FinanceData financeData = (FinanceData)inputElement;
				Object[] elements = new Object[financeData.getNumAccounts()];
				for(int i=0;i<financeData.getNumAccounts();i++)
					elements[i] = financeData.getAccount(i);
				return elements;
			}else return new Object[]{};
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * The main window shell
	 */
	protected Shell shell;

	/**
	 * The last opened directory
	 */
	protected File lastDirectory = null;

	/**
	 * Java preference storage for this class
	 */
	protected java.util.prefs.Preferences preferenceStorage = java.util.prefs.Preferences.userNodeForPackage(MainWindow.class);

	/**
	 * An instance of the finance data class
	 */
	protected FinanceData financeData;
	
	/**
	 * The transactions table
	 */
	private Table tableTransactions;
	/**
	 * The transactions table viewer
	 */
	private TableViewer transactionsTableViewer;

	/**
	 * The accounts table
	 */
	private Table accountsTable;
	/**
	 * The accounts table viewer
	 */
	private TableViewer accountsTableViewer;
	private Button btnDeleteAccount;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MainWindow window = new MainWindow();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();

		loadData();

		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		DatabaseManager.getInstance().shutdown();
	}

	/**
	 * Restores data from database and updates the tables
	 */
	protected void loadData(){
		FinanceData financeData = new FinanceData();
		transactionsTableViewer.setInput(financeData);
		accountsTableViewer.setInput(financeData);
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(450, 300);
		shell.setText(Messages.MainWindow_shell_text);
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));

		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		MenuItem mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu.setText(Messages.MainWindow_mntmNewSubmenu_text);

		Menu menu_1 = new Menu(mntmNewSubmenu);
		mntmNewSubmenu.setMenu(menu_1);

		MenuItem mntmImport = new MenuItem(menu_1, SWT.NONE);
		mntmImport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showImportDialog();
			}
		});
		mntmImport.setText(Messages.MainWindow_mntmImport_text);

		MenuItem mntmPreferences = new MenuItem(menu_1, SWT.NONE);
		mntmPreferences.setText(Messages.MainWindow_mntmPreferences_text);

		TabFolder tabFolder = new TabFolder(shell, SWT.NONE);

		TabItem tbtmTransactions = new TabItem(tabFolder, SWT.NONE);
		tbtmTransactions.setText(Messages.MainWindow_tbtmTransactions_text);

		transactionsTableViewer = new TableViewer(tabFolder, SWT.BORDER | SWT.FULL_SELECTION);
		tableTransactions = transactionsTableViewer.getTable();
		tableTransactions.setHeaderVisible(true);
		tbtmTransactions.setControl(tableTransactions);
		transactionsTableViewer.setLabelProvider(new TransactionsTableLabelProvider());
		transactionsTableViewer.setContentProvider(new TransactionsContentProvider());

		TableColumn tblclmnDescription = new TableColumn(tableTransactions, SWT.NONE);
		tblclmnDescription.setWidth(100);
		tblclmnDescription.setText(Messages.MainWindow_tblclmnDescription_text);

		TableColumn tblclmnDate = new TableColumn(tableTransactions, SWT.NONE);
		tblclmnDate.setWidth(100);
		tblclmnDate.setText(Messages.MainWindow_tblclmnDate_text);

		TableColumn tblclmnTags = new TableColumn(tableTransactions, SWT.NONE);
		tblclmnTags.setWidth(100);
		tblclmnTags.setText(Messages.MainWindow_tblclmnTags_text);

		TableColumn tblclmnAmount = new TableColumn(tableTransactions, SWT.NONE);
		tblclmnAmount.setWidth(100);
		tblclmnAmount.setText(Messages.MainWindow_tblclmnAmount_text);

		TableColumn tblclmnAccounts = new TableColumn(tableTransactions, SWT.NONE);
		tblclmnAccounts.setWidth(100);
		tblclmnAccounts.setText(Messages.MainWindow_tblclmnAccounts_text);

		TabItem tbtmAccounts = new TabItem(tabFolder, SWT.NONE);
		tbtmAccounts.setText(Messages.MainWindow_tbtmAccounts_text);

		Composite compositeAccounts = new Composite(tabFolder, SWT.NONE);
		tbtmAccounts.setControl(compositeAccounts);
		compositeAccounts.setLayout(new FormLayout());

		Button btnAddAccount = new Button(compositeAccounts, SWT.NONE);
		btnAddAccount.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				accountsTableViewer.add(new FinanceAccount(Messages.MainWindow_New_Account_Name));
			}
		});
		FormData fd_btnAddAccount = new FormData();
		fd_btnAddAccount.right = new FormAttachment(100);
		fd_btnAddAccount.top = new FormAttachment(0);
		btnAddAccount.setLayoutData(fd_btnAddAccount);
		btnAddAccount.setText(Messages.MainWindow_btnAddAccount_text);
		
		btnDeleteAccount = new Button(compositeAccounts, SWT.NONE);
		btnDeleteAccount.setEnabled(false);
		btnDeleteAccount.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FinanceAccount account = (FinanceAccount)accountsTableViewer.getElementAt(accountsTable.getSelectionIndex());
				FinanceData financeData = (FinanceData)accountsTableViewer.getInput();
				if(account!=null && financeData!=null){
					accountsTableViewer.remove(account);
					financeData.deleteAccount(account);
					updateTransactions();
				}
			}
		});
		FormData fd_btnDeleteAccount = new FormData();
		fd_btnDeleteAccount.top = new FormAttachment(btnAddAccount, 0, SWT.TOP);
		fd_btnDeleteAccount.right = new FormAttachment(btnAddAccount, -6);
		btnDeleteAccount.setLayoutData(fd_btnDeleteAccount);
		btnDeleteAccount.setText(Messages.MainWindow_btnDeleteAccount_text);

		Composite compositeAccountsTable = new Composite(compositeAccounts, SWT.EMBEDDED);
		FormData fd_compositeAccountsTable = new FormData();
		fd_compositeAccountsTable.top = new FormAttachment(btnAddAccount, 6);
		fd_compositeAccountsTable.left = new FormAttachment(0);
		fd_compositeAccountsTable.bottom = new FormAttachment(100);
		fd_compositeAccountsTable.right = new FormAttachment(100);
		compositeAccountsTable.setLayoutData(fd_compositeAccountsTable);
		TableColumnLayout tcl_compositeAccountsTable = new TableColumnLayout();
		compositeAccountsTable.setLayout(tcl_compositeAccountsTable);

		accountsTableViewer = new TableViewer(compositeAccountsTable, SWT.BORDER | SWT.FULL_SELECTION);
		accountsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent arg0) {
				btnDeleteAccount.setEnabled(!arg0.getSelection().isEmpty());
			}
		});
		accountsTable = accountsTableViewer.getTable();
		accountsTable.setHeaderVisible(true);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(accountsTableViewer, SWT.NONE);
		tableViewerColumn.setEditingSupport(new EditingSupport(accountsTableViewer) {
			protected boolean canEdit(Object element) {
				return element instanceof FinanceAccount;
			}
			protected CellEditor getCellEditor(Object element) {
				if(element instanceof FinanceAccount)
					return (CellEditor)new TextCellEditor(accountsTable);
				else
					return null;
			}
			protected Object getValue(Object element) {
				if(element instanceof FinanceAccount){
					return ((FinanceAccount)element).getName();
				}else
					return null;
			}
			protected void setValue(Object element, Object value) {
				FinanceData financeData = (FinanceData)accountsTableViewer.getInput();
				if(element instanceof FinanceAccount && value instanceof String && financeData!=null){
					financeData.setAccountName((FinanceAccount)element, (String)value);
				}
				accountsTableViewer.update(element, null);
				updateTransactions();
			}
		});
		TableColumn tblclmnAccount = tableViewerColumn.getColumn();
		tcl_compositeAccountsTable.setColumnData(tblclmnAccount, new ColumnWeightData(60));
		tblclmnAccount.setText(Messages.MainWindow_tblclmnAccount_text);

		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(accountsTableViewer, SWT.NONE);
		TableColumn tblclmnBalance = tableViewerColumn_1.getColumn();
		tblclmnBalance.setAlignment(SWT.RIGHT);
		tcl_compositeAccountsTable.setColumnData(tblclmnBalance, new ColumnWeightData(30));
		tblclmnBalance.setText(Messages.MainWindow_tblclmnBalance_text);
		accountsTableViewer.setLabelProvider(new AccountsTableLabelProvider());
		accountsTableViewer.setContentProvider(new AccountsContentProvider());
	}

	/**
	 * Displays the import dialog and performs import of files
	 */
	protected void showImportDialog() {
		if (lastDirectory == null)
			lastDirectory = preferenceStorage.get("lastDirectory", null) == null ? null	: new java.io.File(preferenceStorage.get("lastDirectory",null)); //$NON-NLS-1$ //$NON-NLS-2$

		FileDialog importCsvFileDialog = new FileDialog(shell, SWT.OPEN);
		importCsvFileDialog.setText(Messages.MainWindow_File_Import_Dialog_Header);
		importCsvFileDialog.setFilterExtensions(new String[] { "*.csv" }); //$NON-NLS-1$
		importCsvFileDialog.setFilterNames(new String[] { Messages.MainWindow_File_Import_Dialog_CsvFilter });
		if (lastDirectory != null)
			importCsvFileDialog.setFilterPath(lastDirectory.getAbsolutePath());

		String filename = null;
		if ((filename = importCsvFileDialog.open()) != null) {
			java.io.File selectedFile = new java.io.File(filename);
			lastDirectory = selectedFile.isDirectory() ? selectedFile : selectedFile.getParentFile();
			preferenceStorage.put("lastDirectory", lastDirectory.toString()); //$NON-NLS-1$

			// Test code for printing data
			CsvImporter importer = new CsvImporter();
			try {
				financeData = importer.importFile(selectedFile);
				transactionsTableViewer.setInput(financeData);
				accountsTableViewer.setInput(financeData);
			} catch (org.zlogic.vogon.data.VogonImportLogicalException ex) {
				Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE,null, ex);
				MessageDialog dialog = new MessageDialog(shell,Messages.MainWindow_Error_Importing_File,null,new MessageFormat(Messages.MainWindow_Error_Importing_File_Description).format(new Object[] { ex.getMessage() }),MessageDialog.ERROR, new String[] { Messages.MainWindow_OK }, 0);
				dialog.open();
			} catch (Exception ex) {
				Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE,null, ex);
				MessageDialog dialog = new MessageDialog(shell,Messages.MainWindow_Error_Importing_File,null,new MessageFormat(Messages.MainWindow_Error_Importing_File_Description).format(new Object[] { ex.getMessage() }),MessageDialog.ERROR, new String[] { Messages.MainWindow_OK }, 0);
				dialog.open();
			}
		}
	}

	/**
	 * Updates the transactions table when data changes
	 */
	protected void updateTransactions() {
		transactionsTableViewer.refresh(true);
	}
}
