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
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
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

public class MainWindow {
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
					return org.zlogic.vogon.data.Utils.join(transaction.getTags(), ",");
				case 3:
					if (transaction.getClass() == ExpenseTransaction.class)
						return Double.toString(((ExpenseTransaction) transaction).getAmount());
					else if (transaction.getClass() == TransferTransaction.class)
						return Double.toString(((TransferTransaction) transaction).getAmount());
				case 4:
					if (transaction.getClass() == ExpenseTransaction.class) {
						FinanceAccount[] accounts = ((ExpenseTransaction) transaction).getAccounts();
						StringBuilder builder = new StringBuilder();
						for (int i = 0; i < accounts.length; i++)
							builder.append(i != 0 ? "," : "").append(accounts[i].getName());
						return builder.toString();
					} else if (transaction.getClass() == TransferTransaction.class) {
						FinanceAccount[] toAccounts = ((TransferTransaction) transaction).getToAccounts();
						FinanceAccount[] fromAccounts = ((TransferTransaction) transaction).getFromAccounts();
						StringBuilder builder = new StringBuilder();
						if (fromAccounts.length > 1) {
							builder.append("(");
							for (int i = 0; i < fromAccounts.length; i++)
								builder.append(i != 0 ? "," : "").append(fromAccounts[i].getName());
							builder.append(")");
						} else if (fromAccounts.length == 1)
							builder.append(fromAccounts[0].getName());
						builder.append("->");
						if (toAccounts.length > 1) {
							builder.append("(");
							for (int i = 0; i < toAccounts.length; i++)
								builder.append(i != 0 ? "," : "").append(toAccounts[i].getName());
							builder.append(")");
						} else if (toAccounts.length == 1)
							builder.append(toAccounts[0].getName());
						return builder.toString();
					} else
						return "";
				}
			}
			return "";
		}
	}


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
					return Double.toString(account.getActualBalance());
				}
			}
			return "";
		}
	}

	private static class TransactionsContentProvider implements IStructuredContentProvider {		
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof FinanceTransaction){
				FinanceTransaction transaction = (FinanceTransaction) inputElement;
				return new Object[]{transaction.getDescription(),transaction.getDate()};

			}else return new Object[]{};
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private static class AccountsContentProvider implements IStructuredContentProvider {		
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof FinanceAccount){
				FinanceAccount account = (FinanceAccount) inputElement;
				return new Object[]{account.getName(),account.getActualBalance()};

			}else return new Object[]{};
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	protected Shell shell;

	protected File lastDirectory = null;

	protected java.util.prefs.Preferences preferenceStorage = java.util.prefs.Preferences.userNodeForPackage(MainWindow.class);
	private Table tableTransactions;

	protected TransactionsContentProvider transactionsContentProvider = new TransactionsContentProvider();
	private TableViewer transactionsTableViewer;

	protected FinanceData financeData = new FinanceData();
	private Table tableAccounts;
	private TableViewer accountsTableViewer;

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
		financeData = null;
		new DatabaseManager().shutdown();
	}

	protected void loadData(){
		FinanceData financeData = new FinanceData();
		for(int i=0;i<financeData.getNumTransactions();i++)
			transactionsTableViewer.add(financeData.getTransaction(i));
		for(int i=0;i<financeData.getNumAccounts();i++)
			accountsTableViewer.add(financeData.getAccount(i));
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
		tableTransactions.setLinesVisible(true);
		tableTransactions.setHeaderVisible(true);
		tbtmTransactions.setControl(tableTransactions);
		transactionsTableViewer.setLabelProvider(new TransactionsTableLabelProvider());
		transactionsTableViewer.setContentProvider(transactionsContentProvider);

		TabItem tbtmAccounts = new TabItem(tabFolder, SWT.NONE);
		tbtmAccounts.setText(Messages.MainWindow_tbtmAccounts_text);

		accountsTableViewer = new TableViewer(tabFolder, SWT.BORDER | SWT.FULL_SELECTION);
		tableAccounts = accountsTableViewer.getTable();
		tableAccounts.setLinesVisible(true);
		tableAccounts.setHeaderVisible(true);
		tbtmAccounts.setControl(tableAccounts);

		TableColumn tblclmnAccount = new TableColumn(tableAccounts, SWT.NONE);
		tblclmnAccount.setWidth(100);
		tblclmnAccount.setText(Messages.MainWindow_tblclmnAccount_text);

		TableColumn tblclmnBalance = new TableColumn(tableAccounts, SWT.NONE);
		tblclmnBalance.setWidth(100);
		tblclmnBalance.setText(Messages.MainWindow_tblclmnBalance_text);
		accountsTableViewer.setLabelProvider(new AccountsTableLabelProvider());
		accountsTableViewer.setContentProvider(new AccountsContentProvider());

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


	}

	protected void showImportDialog() {
		if (lastDirectory == null)
			lastDirectory = preferenceStorage.get("lastDirectory", null) == null ? null	: new java.io.File(preferenceStorage.get("lastDirectory",null));

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
			preferenceStorage.put("lastDirectory", lastDirectory.toString());

			// Test code for printing data
			CsvImporter importer = new CsvImporter();
			try {
				FinanceData financeData = importer.importFile(selectedFile);
				for (int i = 0; i < financeData.getNumTransactions(); i++) 
					transactionsTableViewer.add(financeData.getTransaction(i));
				for(int i=0;i<financeData.getNumAccounts();i++)
					accountsTableViewer.add(financeData.getAccount(i));
			} catch (org.zlogic.vogon.data.VogonImportLogicalException ex) {
				Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE,null, ex);
				MessageDialog dialog = new MessageDialog(shell,"Error importing file",null,new MessageFormat("An error has occurred while importing the file: {0}").format(new Object[] { ex.getMessage() }),MessageDialog.ERROR, new String[] { "OK" }, 0);
				dialog.open();
			} catch (Exception ex) {
				Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE,null, ex);
				MessageDialog dialog = new MessageDialog(shell,"Error importing file",null,new MessageFormat("An error has occurred while importing the file: {0}").format(new Object[] { ex.getMessage() }),MessageDialog.ERROR, new String[] { "OK" }, 0);
				dialog.open();
			}
		}
	}
}
