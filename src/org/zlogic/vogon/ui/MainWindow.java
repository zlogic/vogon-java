/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */

package org.zlogic.vogon.ui;

import java.io.File;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.zlogic.vogon.data.CsvImporter;
import org.zlogic.vogon.data.DatabaseManager;
import org.zlogic.vogon.data.ExpenseTransaction;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.data.TransferTransaction;

import com.ibm.icu.text.NumberFormat;

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
	private class TransactionsTableLabelProvider extends LabelProvider implements ITableLabelProvider,ITableColorProvider  {
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
					return MessageFormat.format("{0,date,medium}", new Object[]{transaction.getDate()}); //$NON-NLS-1$
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
							builder.append(i != 0 ? "," : "").append(accounts[i]!=null?accounts[i].getName():Messages.MainWindow_Bad_Account_Substitute); //$NON-NLS-1$ //$NON-NLS-2$
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
			}else if(element instanceof TransactionComponent){
				TransactionComponent component = (TransactionComponent)element;
				switch (columnIndex) {
				case 3:
					return MessageFormat.format("{0,number,0.00}", new Object[]{component.getAmount()}); //$NON-NLS-1$
				case 4:
					return component.getAccount()!=null?component.getAccount().getName():Messages.MainWindow_Bad_Account_Substitute;
				}

			}
			return ""; //$NON-NLS-1$
		}
		@Override
		public Color getBackground(Object element, int column) {
			if(element instanceof FinanceTransaction){
				FinanceTransaction transaction = (FinanceTransaction)element;
				if(column==0 && transaction.getComponentsCount()==0)
					return new Color(Display.getCurrent(), 255, 0, 0);
				if(column==3 && transaction instanceof TransferTransaction && !((TransferTransaction)transaction).isAmountOk())
					return new Color(Display.getCurrent(), 255, 0, 0);
			}
			return null;
		}
		@Override
		public Color getForeground(Object element, int column) {
			// TODO Auto-generated method stub
			return null;
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
	private static class TransactionsContentProvider implements ITreeContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		}

		@Override
		public Object[] getChildren(Object arg0) {
			if(arg0 instanceof FinanceTransaction){
				List<TransactionComponent> componentsList = ((FinanceTransaction)arg0).getComponents();
				Object[] components = new Object[componentsList.size()];
				components = componentsList.toArray(components);
				return components;
			}
			return null;
		}

		@Override
		public Object[] getElements(Object arg0) {
			if(arg0 instanceof FinanceData){
				FinanceData financeData = (FinanceData)arg0;
				Object[] elements = new Object[financeData.getNumTransactions()];
				for(int i=0;i<financeData.getNumTransactions();i++)
					elements[i] = financeData.getTransaction(i);
				return elements;
			}else return new Object[]{};
		}

		@Override
		public Object getParent(Object arg0) {
			if(arg0 instanceof TransactionComponent)
				return ((TransactionComponent)arg0).getTransaction();
			return null;
		}

		@Override
		public boolean hasChildren(Object arg0) {
			if(arg0 instanceof FinanceTransaction){
				return ((FinanceTransaction)arg0).getComponentsCount()>0;
			}
			return false;
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
	 * The accounts table
	 */
	private Table accountsTable;
	/**
	 * The accounts table viewer
	 */
	private TableViewer accountsTableViewer;
	private Button btnDeleteAccount;
	private TreeViewer transactionsTreeViewer;
	private Tree transactionsTree;
	private Button btnDeleteTransaction;
	private Button btnAddComponent;

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
		FinanceData financeData = FinanceData.restoreFromDatabase();
		transactionsTreeViewer.setInput(financeData);
		accountsTableViewer.setInput(financeData);
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(550, 440);
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

		Composite compositeTransactions = new Composite(tabFolder, SWT.NONE);
		tbtmTransactions.setControl(compositeTransactions);
		compositeTransactions.setLayout(new FormLayout());

		Composite compositeTransactionsTree = new Composite(compositeTransactions, SWT.NONE);
		FormData fd_compositeTransactionsTree = new FormData();
		fd_compositeTransactionsTree.bottom = new FormAttachment(100);
		fd_compositeTransactionsTree.right = new FormAttachment(100);
		fd_compositeTransactionsTree.top = new FormAttachment(0, 35);
		fd_compositeTransactionsTree.left = new FormAttachment(0);
		compositeTransactionsTree.setLayoutData(fd_compositeTransactionsTree);

		TreeColumnLayout tcl_compositeTransactionsTree = new TreeColumnLayout();
		compositeTransactionsTree.setLayout(tcl_compositeTransactionsTree);

		transactionsTreeViewer = new TreeViewer(compositeTransactionsTree, SWT.BORDER | SWT.FULL_SELECTION);
		transactionsTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent arg0) {
				btnDeleteTransaction.setEnabled(!arg0.getSelection().isEmpty());
				btnAddComponent.setEnabled(!arg0.getSelection().isEmpty());
			}
		});
		transactionsTree = transactionsTreeViewer.getTree();
		transactionsTree.setHeaderVisible(true);

		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(transactionsTreeViewer, SWT.NONE);
		treeViewerColumn.setEditingSupport(new EditingSupport(transactionsTreeViewer) {
			protected boolean canEdit(Object element) {
				return element instanceof FinanceTransaction;
			}
			protected CellEditor getCellEditor(Object element) {
				if(element instanceof FinanceTransaction){
					return (CellEditor)new TextCellEditor(transactionsTree);
				}else
					return null;
			}
			protected Object getValue(Object element) {
				if(element instanceof FinanceTransaction)
					return ((FinanceTransaction)element).getDescription();
				return null;
			}
			protected void setValue(Object element, Object value) {
				try {
					FinanceData financeData = (FinanceData)transactionsTreeViewer.getInput();
					if(element instanceof FinanceTransaction && value instanceof String && financeData!=null)
						financeData.setTransactionDescription((FinanceTransaction)element, (String)value);
					transactionsTreeViewer.update(element, null);
				} catch (Exception ex) {
					//TODO: warn user?
					Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE,null, ex);
				}
			}
		});
		TreeColumn trclmnDescription = treeViewerColumn.getColumn();
		tcl_compositeTransactionsTree.setColumnData(trclmnDescription, new ColumnWeightData(50));
		trclmnDescription.setText(Messages.MainWindow_trclmnDescription_text);

		TreeViewerColumn treeViewerColumn_1 = new TreeViewerColumn(transactionsTreeViewer, SWT.NONE);
		treeViewerColumn_1.setEditingSupport(new EditingSupport(transactionsTreeViewer) {
			protected boolean canEdit(Object element) {
				return element instanceof FinanceTransaction;
			}
			protected CellEditor getCellEditor(Object element) {
				if(element instanceof FinanceTransaction){
					return (CellEditor)new TextCellEditor(transactionsTree);
				}else
					return null;
			}
			protected Object getValue(Object element) {
				if(element instanceof FinanceTransaction)
					return MessageFormat.format("{0,date,yyyy-MM-dd}", new Object[]{((FinanceTransaction)element).getDate()}); //$NON-NLS-1$
				return null;
			}
			protected void setValue(Object element, Object value) {
				try {
					FinanceData financeData = (FinanceData)transactionsTreeViewer.getInput();
					if(element instanceof FinanceTransaction && value instanceof String && financeData!=null){
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
						try {
							financeData.setTransactionDate((FinanceTransaction)element, dateFormat.parse((String)value)); //$NON-NLS-1$;
						} catch (ParseException ex) {
							//TODO: warn user?
							Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE,null, ex);
						}
					}
					transactionsTreeViewer.update(element, null);
				} catch (Exception ex) {
					//TODO: warn user?
					Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE,null, ex);
				}
			}
		});
		TreeColumn trclmnDate = treeViewerColumn_1.getColumn();
		tcl_compositeTransactionsTree.setColumnData(trclmnDate,  new ColumnWeightData(15));
		trclmnDate.setText(Messages.MainWindow_trclmnDate_text);

		TreeViewerColumn treeViewerColumn_2 = new TreeViewerColumn(transactionsTreeViewer, SWT.NONE);
		treeViewerColumn_2.setEditingSupport(new EditingSupport(transactionsTreeViewer) {
			protected boolean canEdit(Object element) {
				return element instanceof FinanceTransaction;
			}
			protected CellEditor getCellEditor(Object element) {
				if(element instanceof FinanceTransaction){
					return (CellEditor)new TextCellEditor(transactionsTree);
				}else
					return null;
			}
			protected Object getValue(Object element) {
				if(element instanceof FinanceTransaction)
					return org.zlogic.vogon.data.Utils.join(((FinanceTransaction)element).getTags(), ","); //$NON-NLS-1$;
				return null;
			}
			protected void setValue(Object element, Object value) {
				try {
					FinanceData financeData = (FinanceData)transactionsTreeViewer.getInput();
					if(element instanceof FinanceTransaction && value instanceof String && financeData!=null){
						financeData.setTransactionTags((FinanceTransaction)element, ((String)value).split("\\s*,\\s*")); //$NON-NLS-1$;
					}
					transactionsTreeViewer.update(element, null);
				} catch (Exception ex) {
					//TODO: warn user?
					Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE,null, ex);
				}
			}
		});
		TreeColumn trclmnTags = treeViewerColumn_2.getColumn();
		tcl_compositeTransactionsTree.setColumnData(trclmnTags,  new ColumnWeightData(15));
		trclmnTags.setText(Messages.MainWindow_trclmnTags_text);

		TreeViewerColumn treeViewerColumn_3 = new TreeViewerColumn(transactionsTreeViewer, SWT.NONE);
		treeViewerColumn_3.setEditingSupport(new EditingSupport(transactionsTreeViewer) {
			protected boolean canEdit(Object element) {
				return (element instanceof ExpenseTransaction && ((ExpenseTransaction)element).getComponentsCount()<=1)
						|| (element instanceof TransactionComponent);
			}
			protected CellEditor getCellEditor(Object element) {
				if(element instanceof FinanceTransaction || element instanceof TransactionComponent){
					return (CellEditor)new TextCellEditor(transactionsTree);
				}else
					return null;
			}
			protected Object getValue(Object element) {
				if(element instanceof FinanceTransaction)
					return MessageFormat.format("{0,number,0.00}", new Object[]{((FinanceTransaction)element).getAmount()}); //$NON-NLS-1$
				if(element instanceof TransactionComponent)
					return MessageFormat.format("{0,number,0.00}", new Object[]{((TransactionComponent)element).getAmount()}); //$NON-NLS-1$
				return null;
			}
			protected void setValue(Object element, Object value) {
				try {
					FinanceData financeData = (FinanceData)transactionsTreeViewer.getInput();
					if(value instanceof String && element instanceof ExpenseTransaction && ((ExpenseTransaction)element).getComponentsCount()<=1){
						try {
							double amount = NumberFormat.getInstance().parse((String)value).doubleValue();
							financeData.setTransactionAmount((ExpenseTransaction)element, amount);
						} catch (ParseException ex) {
							//TODO: warn user?
							Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE,null, ex);
						}
						transactionsTreeViewer.update(element, null);
						for(TransactionComponent component : ((ExpenseTransaction)element).getComponents())
							transactionsTreeViewer.update(component, null);
						updateAccounts();
					}
					if(value instanceof String && element instanceof TransactionComponent){
						try {
							double amount = NumberFormat.getInstance().parse((String)value).doubleValue();
							financeData.setTransactionComponentAmount((TransactionComponent)element, amount);
						} catch (ParseException ex) {
							//TODO: warn user?
							Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE,null, ex);
						}
						transactionsTreeViewer.update(element, null);
						transactionsTreeViewer.update(((TransactionComponent)element).getTransaction(), null);
						updateAccounts();
					}
				} catch (Exception ex) {
					//TODO: warn user?
					Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE,null, ex);
				}
			}
		});
		TreeColumn trclmnAmount = treeViewerColumn_3.getColumn();
		trclmnAmount.setAlignment(SWT.RIGHT);
		tcl_compositeTransactionsTree.setColumnData(trclmnAmount,  new ColumnWeightData(15));
		trclmnAmount.setText(Messages.MainWindow_trclmnAmount_text);

		TreeViewerColumn treeViewerColumn_4 = new TreeViewerColumn(transactionsTreeViewer, SWT.NONE);
		treeViewerColumn_4.setEditingSupport(new EditingSupport(transactionsTreeViewer) {
			protected boolean canEdit(Object element) {
				return (element instanceof ExpenseTransaction && ((ExpenseTransaction)element).getComponentsCount()<=1)
						|| (element instanceof TransactionComponent);
			}
			protected CellEditor getCellEditor(Object element) {
				if(element instanceof ExpenseTransaction || element instanceof TransactionComponent){
					FinanceData financeData = (FinanceData)transactionsTreeViewer.getInput();
					String[] accounts = new String[financeData.getNumAccounts()];
					for(int i=0;i<accounts.length;i++)
						accounts[i] = financeData.getAccount(i).getName();
					return (CellEditor)new ComboBoxCellEditor(transactionsTree,accounts);
				}else
					return null;
			}
			protected Object getValue(Object element) {
				FinanceData financeData = (FinanceData)transactionsTreeViewer.getInput();
				FinanceAccount accountToFind = null;
				if(element instanceof ExpenseTransaction && ((ExpenseTransaction)element).getComponentsCount()<=1){
					FinanceAccount[] accounts = ((ExpenseTransaction)element).getAccounts();
					accountToFind = accounts.length==1?accounts[0]:null;
				}
				if(element instanceof TransactionComponent){
					accountToFind = ((TransactionComponent)element).getAccount();
				}
				if(accountToFind!=null)
					for(int i=0;i<financeData.getNumAccounts();i++)
						if(financeData.getAccount(i)==accountToFind)
							return i;
				return -1;
			}
			protected void setValue(Object element, Object value) {
				try {
					FinanceData financeData = (FinanceData)transactionsTreeViewer.getInput();
					if(value instanceof Integer && element instanceof ExpenseTransaction && ((ExpenseTransaction)element).getComponentsCount()<=1){
						financeData.setTransactionAccount((ExpenseTransaction)element, financeData.getAccount((Integer)value));
						transactionsTreeViewer.update(element, null);
						for(TransactionComponent component : ((ExpenseTransaction)element).getComponents())
							transactionsTreeViewer.update(component, null);
						updateAccounts();
					}
					if(value instanceof Integer && element instanceof TransactionComponent){
						financeData.setTransactionComponentAccount((TransactionComponent)element, financeData.getAccount((Integer)value));
						transactionsTreeViewer.update(element, null);
						transactionsTreeViewer.update(((TransactionComponent)element).getTransaction(), null);
						updateAccounts();
					}
				} catch (Exception ex) {
					//TODO: warn user?
					Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE,null, ex);
				}
			}
		});
		TreeColumn trclmnAccounts = treeViewerColumn_4.getColumn();
		tcl_compositeTransactionsTree.setColumnData(trclmnAccounts,  new ColumnWeightData(15));
		trclmnAccounts.setText(Messages.MainWindow_trclmnAccounts_text);

		Button btnAddExpenseTransaction = new Button(compositeTransactions, SWT.NONE);
		btnAddExpenseTransaction.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FinanceTransaction newTransaction = new ExpenseTransaction(Messages.MainWindow_New_Expense_Transaction_Name, new String[]{}, new Date());
				TransactionComponent component = new TransactionComponent(null, newTransaction, 0);
				newTransaction.addComponent(component);
				transactionsTreeViewer.add(transactionsTreeViewer.getInput(), newTransaction);
			}
		});
		FormData fd_btnAddExpenseTransaction = new FormData();
		fd_btnAddExpenseTransaction.top = new FormAttachment(0);
		btnAddExpenseTransaction.setLayoutData(fd_btnAddExpenseTransaction);
		btnAddExpenseTransaction.setText(Messages.MainWindow_btnAddTransaction_text);

		Button btnAddTransferTransaction = new Button(compositeTransactions, SWT.NONE);
		btnAddTransferTransaction.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FinanceTransaction newTransaction = new TransferTransaction(Messages.MainWindow_New_Transfer_Transaction_Name, new String[]{}, new Date());
				TransactionComponent componentFrom = new TransactionComponent(null, newTransaction, 0);
				TransactionComponent componentTo = new TransactionComponent(null, newTransaction, 0);
				newTransaction.addComponent(componentFrom);
				newTransaction.addComponent(componentTo);
				transactionsTreeViewer.add(transactionsTreeViewer.getInput(), newTransaction);
			}
		});
		fd_btnAddExpenseTransaction.right = new FormAttachment(btnAddTransferTransaction, -6);
		FormData fd_btnAddTransferTransaction = new FormData();
		fd_btnAddTransferTransaction.top = new FormAttachment(0);
		fd_btnAddTransferTransaction.right = new FormAttachment(compositeTransactionsTree, 0, SWT.RIGHT);
		btnAddTransferTransaction.setLayoutData(fd_btnAddTransferTransaction);
		btnAddTransferTransaction.setText(Messages.MainWindow_btnAddTransferTransaction_text);

		btnAddComponent = new Button(compositeTransactions, SWT.NONE);
		btnAddComponent.setEnabled(false);
		btnAddComponent.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = transactionsTreeViewer.getSelection();

				if(selection instanceof IStructuredSelection){
					Object selectedObject = ((IStructuredSelection)selection).getFirstElement();
					if(selectedObject instanceof FinanceTransaction){
						TransactionComponent component = new TransactionComponent(null,(FinanceTransaction)selectedObject,0);
						transactionsTreeViewer.add(selectedObject,component);
						transactionsTreeViewer.setSelection(new StructuredSelection(component), true);
					}
					if(selectedObject instanceof TransactionComponent){
						FinanceTransaction updateTransaction = ((TransactionComponent)selectedObject).getTransaction();
						TransactionComponent component = new TransactionComponent(null,updateTransaction,0);
						transactionsTreeViewer.add(updateTransaction,component);
						transactionsTreeViewer.setSelection(new StructuredSelection(component), true);
					}
				}
			}
		});
		FormData fd_btnAddComponent = new FormData();
		fd_btnAddComponent.top = new FormAttachment(btnAddExpenseTransaction, 0, SWT.TOP);
		fd_btnAddComponent.right = new FormAttachment(btnAddExpenseTransaction, -6);
		btnAddComponent.setLayoutData(fd_btnAddComponent);
		btnAddComponent.setText(Messages.MainWindow_btnAddComponent_text);

		btnDeleteTransaction = new Button(compositeTransactions, SWT.NONE);
		btnDeleteTransaction.setEnabled(false);
		btnDeleteTransaction.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = transactionsTreeViewer.getSelection();

				if(selection instanceof IStructuredSelection){
					Object selectedObject = ((IStructuredSelection)selection).getFirstElement();
					FinanceData financeData = (FinanceData)transactionsTreeViewer.getInput();
					if(selectedObject instanceof FinanceTransaction){
						financeData.deleteTransaction((FinanceTransaction)selectedObject);
						transactionsTreeViewer.remove(selectedObject);
						updateAccounts();
					}
					if(selectedObject instanceof TransactionComponent){
						FinanceTransaction updateTransaction = ((TransactionComponent)selectedObject).getTransaction();
						financeData.deleteTransactionComponent((TransactionComponent)selectedObject);
						transactionsTreeViewer.remove(selectedObject);
						transactionsTreeViewer.update(updateTransaction, null);
						updateAccounts();
					}
				}
			}
		});
		FormData fd_btnDeleteTransaction = new FormData();
		fd_btnDeleteTransaction.top = new FormAttachment(btnAddExpenseTransaction, 0, SWT.TOP);
		fd_btnDeleteTransaction.right = new FormAttachment(btnAddComponent, -6);
		btnDeleteTransaction.setLayoutData(fd_btnDeleteTransaction);
		btnDeleteTransaction.setText(Messages.MainWindow_btnDeleteSelection_text);
		transactionsTreeViewer.setLabelProvider(new TransactionsTableLabelProvider());
		transactionsTreeViewer.setContentProvider(new TransactionsContentProvider());

		TabItem tbtmAccounts = new TabItem(tabFolder, SWT.NONE);
		tbtmAccounts.setText(Messages.MainWindow_tbtmAccounts_text);

		Composite compositeAccounts = new Composite(tabFolder, SWT.NONE);
		tbtmAccounts.setControl(compositeAccounts);
		compositeAccounts.setLayout(new FormLayout());

		Button btnAddAccount = new Button(compositeAccounts, SWT.NONE);
		btnAddAccount.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FinanceAccount newAccount = new FinanceAccount(Messages.MainWindow_New_Account_Name);
				accountsTableViewer.add(newAccount);
				accountsTableViewer.setSelection(new StructuredSelection(newAccount), true);
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
				ISelection selection = accountsTableViewer.getSelection();
				if(selection instanceof IStructuredSelection){
					Object selectedObject = ((IStructuredSelection)selection).getFirstElement();
					if(selectedObject instanceof FinanceAccount){
						FinanceAccount account = (FinanceAccount)selectedObject;
						FinanceData financeData = (FinanceData)accountsTableViewer.getInput();
						if(account!=null && financeData!=null){
							accountsTableViewer.remove(account);
							financeData.deleteAccount(account);
							updateTransactions();
						}
					}
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
				try {
					FinanceData financeData = (FinanceData)accountsTableViewer.getInput();
					if(element instanceof FinanceAccount && value instanceof String && financeData!=null){
						financeData.setAccountName((FinanceAccount)element, (String)value);
					}
					accountsTableViewer.update(element, null);
					updateTransactions();
				} catch (Exception ex) {
					//TODO: warn user?
					Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE,null, ex);
				}
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
				transactionsTreeViewer.setInput(financeData);
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
		transactionsTreeViewer.refresh(true);
	}

	/**
	 * Updates the accounts table when data changes
	 */
	protected void updateAccounts() {
		accountsTableViewer.refresh(true);
	}
}
