/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.ui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.zlogic.vogon.data.CsvImporter;
import org.zlogic.vogon.data.DatabaseManager;
import org.zlogic.vogon.data.FileExporter;
import org.zlogic.vogon.data.FileImporter;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.VogonExportException;
import org.zlogic.vogon.data.VogonImportLogicalException;
import org.zlogic.vogon.data.XmlExporter;
import org.zlogic.vogon.data.XmlImporter;

/**
 * Main window class
 *
 * @author Zlogic
 */
public class MainWindow extends javax.swing.JFrame {

	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/ui/messages");

	/**
	 * Creates new form MainWindow
	 */
	public MainWindow() {
		initComponents();
		initCustomComponents();
	}

	/**
	 * Completes user configuration of form
	 */
	private void initCustomComponents() {
		//Restore settings
		lastDirectory = preferenceStorage.get("lastDirectory", null) == null ? null : new java.io.File(preferenceStorage.get("lastDirectory", null)); //NOI18N

		//Load data from DB
		transactionsTableModel.setFinanceData(financeData);
		accountsTableModel.setFinanceData(financeData);
		currenciesTableModel.setFinanceData(financeData);
		transactionEditor.setFinanceData(financeData);
		analyticsViewer.setFinanceData(financeData);
		transactionEditor.updateAccountsCombo();

		financeData.addTransactionCreatedListener(externalEventHandler);
		financeData.addTransactionUpdatedListener(externalEventHandler);
		financeData.addTransactionUpdatedListener(transactionsTableModel);
		financeData.addTransactionDeletedListener(transactionsTableModel);
		financeData.addAccountCreatedListener(transactionEditor);
		financeData.addAccountUpdatedListener(transactionEditor);
		financeData.addAccountDeletedListener(transactionEditor);
		financeData.addAccountCreatedListener(accountsTableModel);
		financeData.addAccountUpdatedListener(accountsTableModel);
		financeData.addAccountDeletedListener(accountsTableModel);
		financeData.addCurrencyUpdatedListener(externalEventHandler);
		financeData.addCurrencyUpdatedListener(currenciesTableModel);

		jTableTransactions.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				if (jTableTransactions.getSelectedRow() >= 0)
					transactionEditor.editTransaction(transactionsTableModel.getTransaction(jTableTransactions.convertRowIndexToModel(jTableTransactions.getSelectedRow())));
				else
					transactionEditor.editTransaction(null);
			}
		});

		updateDefaultCurrencyCombo();
		externalEventHandler.transactionsUpdated();

		jTableAccounts.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox(accountsTableModel.getCurrenciesComboList())));
		jTableAccounts.getColumnModel().getColumn(1).setCellRenderer(SumTableCell.getRenderer());

		jTableTransactions.getColumnModel().getColumn(3).setCellRenderer(SumTableCell.getRenderer());
		jLabelProgressIndicator.setVisible(false);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelTransactions = new javax.swing.JPanel();
        jSplitPaneTransactions = new javax.swing.JSplitPane();
        transactionEditor = new org.zlogic.vogon.ui.TransactionEditor();
        jPanelTransactionsList = new javax.swing.JPanel();
        jPanelTransactionControls = new javax.swing.JPanel();
        jButtonDeleteTransaction = new javax.swing.JButton();
        javax.swing.JLabel jLabelPageNumber = new javax.swing.JLabel();
        jSpinnerPage = new javax.swing.JSpinner();
        jScrollPaneTransactions = new javax.swing.JScrollPane();
        jTableTransactions = new javax.swing.JTable();
        jPanelAnalytics = new javax.swing.JPanel();
        analyticsViewer = new org.zlogic.vogon.ui.AnalyticsViewer();
        jPanelAccounts = new javax.swing.JPanel();
        jPanelAccountsControls = new javax.swing.JPanel();
        jButtonAddAccount = new javax.swing.JButton();
        jButtonDeleteAccount = new javax.swing.JButton();
        jScrollPaneAccounts = new javax.swing.JScrollPane();
        jTableAccounts = new javax.swing.JTable();
        jPanelCurrencies = new javax.swing.JPanel();
        jPanelCurrenciesControls = new javax.swing.JPanel();
        javax.swing.JLabel jLabelDefaultCurrency = new javax.swing.JLabel();
        jComboBoxDefaultCurrency = new javax.swing.JComboBox();
        jScrollPaneCurrencies = new javax.swing.JScrollPane();
        jTableCurrencies = new javax.swing.JTable();
        jPanelStatus = new javax.swing.JPanel();
        jLabelCurrentTask = new javax.swing.JLabel();
        jLabelProgressIndicator = new javax.swing.JLabel();
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemImport = new javax.swing.JMenuItem();
        jMenuItemExport = new javax.swing.JMenuItem();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuTools = new javax.swing.JMenu();
        jMenuItemRecalculateBalance = new javax.swing.JMenuItem();
        jMenuItemCleanupDB = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(messages.getString("MAINWINDOW_TITLE")); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanelTransactions.setLayout(new java.awt.BorderLayout());

        jSplitPaneTransactions.setDividerLocation(300);
        jSplitPaneTransactions.setTopComponent(transactionEditor);

        jPanelTransactionsList.setLayout(new java.awt.BorderLayout());

        jButtonDeleteTransaction.setText(messages.getString("DELETE")); // NOI18N
        jButtonDeleteTransaction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteTransactionActionPerformed(evt);
            }
        });

        jLabelPageNumber.setText(messages.getString("PAGE")); // NOI18N

        jSpinnerPage.setModel(new javax.swing.SpinnerNumberModel(1, 1, 1, 1));
        jSpinnerPage.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerPageStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanelTransactionControlsLayout = new javax.swing.GroupLayout(jPanelTransactionControls);
        jPanelTransactionControls.setLayout(jPanelTransactionControlsLayout);
        jPanelTransactionControlsLayout.setHorizontalGroup(
            jPanelTransactionControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTransactionControlsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonDeleteTransaction)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 480, Short.MAX_VALUE)
                .addComponent(jLabelPageNumber)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSpinnerPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelTransactionControlsLayout.setVerticalGroup(
            jPanelTransactionControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTransactionControlsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelTransactionControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonDeleteTransaction)
                    .addComponent(jSpinnerPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPageNumber))
                .addContainerGap())
        );

        jPanelTransactionsList.add(jPanelTransactionControls, java.awt.BorderLayout.NORTH);

        jTableTransactions.setModel(transactionsTableModel);
        jTableTransactions.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTableTransactions.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPaneTransactions.setViewportView(jTableTransactions);

        jPanelTransactionsList.add(jScrollPaneTransactions, java.awt.BorderLayout.CENTER);

        jSplitPaneTransactions.setBottomComponent(jPanelTransactionsList);

        jPanelTransactions.add(jSplitPaneTransactions, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab(messages.getString("TRANSACTIONS"), jPanelTransactions); // NOI18N

        jPanelAnalytics.setLayout(new java.awt.BorderLayout());
        jPanelAnalytics.add(analyticsViewer, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab(messages.getString("ANALYTICS"), jPanelAnalytics); // NOI18N

        jPanelAccounts.setLayout(new java.awt.BorderLayout());

        jButtonAddAccount.setText(messages.getString("ADD_ACCOUNT")); // NOI18N
        jButtonAddAccount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddAccountActionPerformed(evt);
            }
        });

        jButtonDeleteAccount.setText(messages.getString("DELETE_ACCOUNT")); // NOI18N
        jButtonDeleteAccount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteAccountActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelAccountsControlsLayout = new javax.swing.GroupLayout(jPanelAccountsControls);
        jPanelAccountsControls.setLayout(jPanelAccountsControlsLayout);
        jPanelAccountsControlsLayout.setHorizontalGroup(
            jPanelAccountsControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAccountsControlsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonAddAccount)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDeleteAccount)
                .addGap(718, 718, 718))
        );
        jPanelAccountsControlsLayout.setVerticalGroup(
            jPanelAccountsControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAccountsControlsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelAccountsControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAddAccount)
                    .addComponent(jButtonDeleteAccount))
                .addContainerGap())
        );

        jPanelAccounts.add(jPanelAccountsControls, java.awt.BorderLayout.NORTH);

        jTableAccounts.setAutoCreateRowSorter(true);
        jTableAccounts.setModel(accountsTableModel);
        jTableAccounts.setFillsViewportHeight(true);
        jScrollPaneAccounts.setViewportView(jTableAccounts);

        jPanelAccounts.add(jScrollPaneAccounts, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab(messages.getString("ACCOUNTS"), jPanelAccounts); // NOI18N

        jPanelCurrencies.setLayout(new java.awt.BorderLayout());

        jLabelDefaultCurrency.setLabelFor(jComboBoxDefaultCurrency);
        jLabelDefaultCurrency.setText(messages.getString("DEFAULT_CURRENCY")); // NOI18N

        jComboBoxDefaultCurrency.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxDefaultCurrencyItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanelCurrenciesControlsLayout = new javax.swing.GroupLayout(jPanelCurrenciesControls);
        jPanelCurrenciesControls.setLayout(jPanelCurrenciesControlsLayout);
        jPanelCurrenciesControlsLayout.setHorizontalGroup(
            jPanelCurrenciesControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCurrenciesControlsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelDefaultCurrency)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jComboBoxDefaultCurrency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelCurrenciesControlsLayout.setVerticalGroup(
            jPanelCurrenciesControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCurrenciesControlsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelCurrenciesControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelDefaultCurrency)
                    .addComponent(jComboBoxDefaultCurrency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelCurrencies.add(jPanelCurrenciesControls, java.awt.BorderLayout.NORTH);

        jTableCurrencies.setAutoCreateRowSorter(true);
        jTableCurrencies.setModel(currenciesTableModel);
        jTableCurrencies.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPaneCurrencies.setViewportView(jTableCurrencies);

        jPanelCurrencies.add(jScrollPaneCurrencies, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab(messages.getString("CURRENCIES"), jPanelCurrencies); // NOI18N

        jLabelProgressIndicator.setIcon(new javax.swing.ImageIcon(getClass().getResource("/progress.gif"))); // NOI18N

        javax.swing.GroupLayout jPanelStatusLayout = new javax.swing.GroupLayout(jPanelStatus);
        jPanelStatus.setLayout(jPanelStatusLayout);
        jPanelStatusLayout.setHorizontalGroup(
            jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStatusLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabelCurrentTask)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelProgressIndicator))
        );
        jPanelStatusLayout.setVerticalGroup(
            jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanelStatusLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabelProgressIndicator))
            .addComponent(jLabelCurrentTask, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jMenuFile.setText(messages.getString("MAINWINDOW_MENU_FILE")); // NOI18N

        jMenuItemImport.setText(messages.getString("MAINWINDOW_MENU_IMPORT")); // NOI18N
        jMenuItemImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemImportActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemImport);

        jMenuItemExport.setText(messages.getString("MAINWINDOW_MENU_EXPORT")); // NOI18N
        jMenuItemExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExport);

        jMenuItemExit.setText(messages.getString("MAINWINDOW_MENU_EXIT")); // NOI18N
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBar.add(jMenuFile);

        jMenuTools.setText(messages.getString("MAINWINDOW_MENU_TOOLS")); // NOI18N

        jMenuItemRecalculateBalance.setText(messages.getString("MAINWINDOW_MENU_RECALCULATE_BALANCE")); // NOI18N
        jMenuItemRecalculateBalance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRecalculateBalanceActionPerformed(evt);
            }
        });
        jMenuTools.add(jMenuItemRecalculateBalance);

        jMenuItemCleanupDB.setText(messages.getString("MAINWINDOW_MENU_CLEANUP_DB")); // NOI18N
        jMenuItemCleanupDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCleanupDBActionPerformed(evt);
            }
        });
        jMenuTools.add(jMenuItemCleanupDB);

        jMenuBar.add(jMenuTools);

        setJMenuBar(jMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addComponent(jPanelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1)
                .addGap(0, 0, 0)
                .addComponent(jPanelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemImportActionPerformed
		Runnable task = new Runnable() {
			protected Component parent;

			public Runnable setParent(Component parent) {
				this.parent = parent;
				return this;
			}

			@Override
			public void run() {

				// Prepare file chooser dialog
				JFileChooser fileChooser = new JFileChooser((lastDirectory != null && lastDirectory.exists()) ? lastDirectory : null);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogTitle(messages.getString("CHOOSE_FILES_TO_IMPORT"));
				//Prepare file chooser filter
				fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(messages.getString("XML_FILES"), "xml"));//NOI18N
				fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(messages.getString("CSV_FILES_(COMMA-SEPARATED)"), "csv"));//NOI18N
				if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					lastDirectory = selectedFile.isDirectory() ? selectedFile : selectedFile.getParentFile();
					preferenceStorage.put("lastDirectory", lastDirectory.toString()); //NOI18N

					//Test code for printing data
					FileImporter importer = null;

					if (fileChooser.getFileFilter().getDescription().equals(messages.getString("CSV_FILES_(COMMA-SEPARATED)")))
						importer = new CsvImporter(selectedFile);
					else if (fileChooser.getFileFilter().getDescription().equals(messages.getString("XML_FILES")))
						importer = new XmlImporter(selectedFile);
					try {
						if (importer == null)
							throw new VogonImportLogicalException(messages.getString("UNKNOWN_FILE_TYPE"));
						financeData.importData(importer);
						transactionsTableModel.setFinanceData(financeData);
						accountsTableModel.setFinanceData(financeData);
					} catch (org.zlogic.vogon.data.VogonImportLogicalException ex) {
						Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
						JOptionPane.showMessageDialog(parent, new MessageFormat(messages.getString("IMPORT_EXCEPTION_DIALOG_TEXT")).format(new Object[]{ex.getLocalizedMessage(), org.zlogic.vogon.data.Utils.getStackTrace(ex)}), messages.getString("IMPORT_EXCEPTION_DIALOG_TITLE"), JOptionPane.ERROR_MESSAGE);
					} catch (Exception ex) {
						Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
						JOptionPane.showMessageDialog(parent, new MessageFormat(messages.getString("IMPORT_EXCEPTION_DIALOG_TEXT")).format(new Object[]{ex.getLocalizedMessage(), org.zlogic.vogon.data.Utils.getStackTrace(ex)}), messages.getString("IMPORT_EXCEPTION_DIALOG_TITLE"), JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}.setParent(this);
		try {
			backgroundTaskHandler.runTask(task, messages.getString("TASK_IMPORTING_DATA"));
		} catch (InterruptedException ex) {
			Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(this, new MessageFormat(messages.getString("BACKGROUND_TASK_EXCEPTION_DIALOG_TEXT")).format(new Object[]{ex.getLocalizedMessage(), org.zlogic.vogon.data.Utils.getStackTrace(ex)}), messages.getString("BACKGROUND_TASK_EXCEPTION_DIALOG_TITLE"), JOptionPane.ERROR_MESSAGE);
		}
    }//GEN-LAST:event_jMenuItemImportActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
		try {
			backgroundTaskHandler.completeTasks();
			transactionEditor.saveChanges();
			DatabaseManager.getInstance().shutdown();
		} catch (Exception ex) {
			Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
		}
    }//GEN-LAST:event_formWindowClosing

    private void jButtonDeleteTransactionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteTransactionActionPerformed
		int selectedRow = jTableTransactions.convertRowIndexToModel(jTableTransactions.getSelectedRow());
		if (selectedRow >= 0) {
			transactionEditor.editTransaction(null);
			transactionsTableModel.deleteTransaction(selectedRow);
		}
    }//GEN-LAST:event_jButtonDeleteTransactionActionPerformed

    private void jMenuItemExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExportActionPerformed
		Runnable task = new Runnable() {
			protected Component parent;

			public Runnable setParent(Component parent) {
				this.parent = parent;
				return this;
			}

			@Override
			public void run() {
				// Prepare file chooser dialog
				JFileChooser fileChooser = new JFileChooser((lastDirectory != null && lastDirectory.exists()) ? lastDirectory : null);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogTitle(messages.getString("CHOOSE_FILES_TO_EXPORT"));
				//Prepare file chooser filter
				fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(messages.getString("XML_FILES"), "xml"));//NOI18N
				if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					lastDirectory = selectedFile.isDirectory() ? selectedFile : selectedFile.getParentFile();
					preferenceStorage.put("lastDirectory", lastDirectory.toString()); //NOI18N

					//Test code for printing data
					FileExporter exporter = null;

					if (fileChooser.getFileFilter().getDescription().equals(messages.getString("XML_FILES")))
						exporter = new XmlExporter(selectedFile);
					try {
						financeData.exportData(exporter);
					} catch (VogonExportException ex) {
						Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
						JOptionPane.showMessageDialog(parent, new MessageFormat(messages.getString("EXPORT_EXCEPTION_DIALOG_TEXT")).format(new Object[]{ex.getLocalizedMessage(), org.zlogic.vogon.data.Utils.getStackTrace(ex)}), messages.getString("EXPORT_EXCEPTION_DIALOG_TITLE"), JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}.setParent(this);
		try {
			backgroundTaskHandler.runTask(task, messages.getString("TASK_EXPORTING_DATA"));
		} catch (InterruptedException ex) {
			Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(this, new MessageFormat(messages.getString("BACKGROUND_TASK_EXCEPTION_DIALOG_TEXT")).format(new Object[]{ex.getLocalizedMessage(), org.zlogic.vogon.data.Utils.getStackTrace(ex)}), messages.getString("BACKGROUND_TASK_EXCEPTION_DIALOG_TITLE"), JOptionPane.ERROR_MESSAGE);
		}
    }//GEN-LAST:event_jMenuItemExportActionPerformed

    private void jMenuItemRecalculateBalanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRecalculateBalanceActionPerformed
		Runnable task = new Runnable() {
			@Override
			public void run() {
				for (FinanceAccount account : financeData.getAccounts())
					financeData.refreshAccountBalance(account);
				updateAccounts();
			}
		};
		try {
			backgroundTaskHandler.runTask(task, messages.getString("TASK_RECALCULATING_BALANCE"));
		} catch (InterruptedException ex) {
			Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(this, new MessageFormat(messages.getString("BACKGROUND_TASK_EXCEPTION_DIALOG_TEXT")).format(new Object[]{ex.getLocalizedMessage(), org.zlogic.vogon.data.Utils.getStackTrace(ex)}), messages.getString("BACKGROUND_TASK_EXCEPTION_DIALOG_TITLE"), JOptionPane.ERROR_MESSAGE);
		}
    }//GEN-LAST:event_jMenuItemRecalculateBalanceActionPerformed

    private void jMenuItemCleanupDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCleanupDBActionPerformed
		Runnable task = new Runnable() {
			@Override
			public void run() {
				financeData.cleanup();
				updateAccounts();
				updateTransactions();
			}
		};
		try {
			backgroundTaskHandler.runTask(task, messages.getString("TASK_CLEANING_UP_DB"));
		} catch (InterruptedException ex) {
			Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(this, new MessageFormat(messages.getString("BACKGROUND_TASK_EXCEPTION_DIALOG_TEXT")).format(new Object[]{ex.getLocalizedMessage(), org.zlogic.vogon.data.Utils.getStackTrace(ex)}), messages.getString("BACKGROUND_TASK_EXCEPTION_DIALOG_TITLE"), JOptionPane.ERROR_MESSAGE);
		}
    }//GEN-LAST:event_jMenuItemCleanupDBActionPerformed

    private void jComboBoxDefaultCurrencyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxDefaultCurrencyItemStateChanged
		if (evt.getStateChange() == ItemEvent.SELECTED && jComboBoxDefaultCurrency.isEnabled()) {
			CurrencyComboItem selectedItem = (CurrencyComboItem) evt.getItem();
			if (selectedItem != null)
				financeData.setDefaultCurrency(selectedItem.getCurrency());
		}
    }//GEN-LAST:event_jComboBoxDefaultCurrencyItemStateChanged

    private void jButtonDeleteAccountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteAccountActionPerformed
		if (jTableAccounts.getSelectedRow() >= 0)
			accountsTableModel.deleteAccount(jTableAccounts.convertRowIndexToModel(jTableAccounts.getSelectedRow()));
    }//GEN-LAST:event_jButtonDeleteAccountActionPerformed

    private void jButtonAddAccountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddAccountActionPerformed
		int newAccountIndex = accountsTableModel.addAccount();
		jTableAccounts.setRowSelectionInterval(newAccountIndex, newAccountIndex);
    }//GEN-LAST:event_jButtonAddAccountActionPerformed

    private void jSpinnerPageStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerPageStateChanged
		transactionsTableModel.setCurrentPage((Integer) jSpinnerPage.getValue() - 1);
    }//GEN-LAST:event_jSpinnerPageStateChanged

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
		WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
    }//GEN-LAST:event_jMenuItemExitActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/*
		 * Set the Nimbus look and feel
		 */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the
		 * default look and feel. For details see
		 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) { //NOI18N
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		/*
		 * Configure logging to load config from classpath
		 */
		String loggingFile = System.getProperty("java.util.logging.config.file"); //NOI18N
		if (loggingFile == null || loggingFile.isEmpty()) {
			try {
				java.net.URL url = ClassLoader.getSystemClassLoader().getResource("logging.properties"); //NOI18N
				if (url != null)
					java.util.logging.LogManager.getLogManager().readConfiguration(url.openStream());
			} catch (IOException | SecurityException e) {
			}
		}
		/*
		 * Create and display the form
		 */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new MainWindow().setVisible(true);
			}
		});
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.zlogic.vogon.ui.AnalyticsViewer analyticsViewer;
    private javax.swing.JButton jButtonAddAccount;
    private javax.swing.JButton jButtonDeleteAccount;
    private javax.swing.JButton jButtonDeleteTransaction;
    private javax.swing.JComboBox jComboBoxDefaultCurrency;
    private javax.swing.JLabel jLabelCurrentTask;
    private javax.swing.JLabel jLabelProgressIndicator;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuItemCleanupDB;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemExport;
    private javax.swing.JMenuItem jMenuItemImport;
    private javax.swing.JMenuItem jMenuItemRecalculateBalance;
    private javax.swing.JMenu jMenuTools;
    private javax.swing.JPanel jPanelAccounts;
    private javax.swing.JPanel jPanelAccountsControls;
    private javax.swing.JPanel jPanelAnalytics;
    private javax.swing.JPanel jPanelCurrencies;
    private javax.swing.JPanel jPanelCurrenciesControls;
    private javax.swing.JPanel jPanelStatus;
    private javax.swing.JPanel jPanelTransactionControls;
    private javax.swing.JPanel jPanelTransactions;
    private javax.swing.JPanel jPanelTransactionsList;
    private javax.swing.JScrollPane jScrollPaneAccounts;
    private javax.swing.JScrollPane jScrollPaneCurrencies;
    private javax.swing.JScrollPane jScrollPaneTransactions;
    private javax.swing.JSpinner jSpinnerPage;
    private javax.swing.JSplitPane jSplitPaneTransactions;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableAccounts;
    private javax.swing.JTable jTableCurrencies;
    private javax.swing.JTable jTableTransactions;
    private org.zlogic.vogon.ui.TransactionEditor transactionEditor;
    // End of variables declaration//GEN-END:variables
	/**
	 * Last opened directory
	 */
	private File lastDirectory = null;
	/**
	 * Easy access to preference storage
	 */
	protected java.util.prefs.Preferences preferenceStorage = java.util.prefs.Preferences.userNodeForPackage(MainWindow.class);
	/**
	 * Finance Data instance
	 */
	protected FinanceData financeData = new FinanceData();
	/**
	 * Transactions table model
	 */
	private TransactionsTableModel transactionsTableModel = new TransactionsTableModel();
	/**
	 * Accounts table model
	 */
	private AccountsTableModel accountsTableModel = new AccountsTableModel();
	/**
	 * Currencies table model
	 */
	private CurrenciesTableModel currenciesTableModel = new CurrenciesTableModel();
	/**
	 * Handler of external events
	 */
	private ExternalEventHandler externalEventHandler = new ExternalEventHandler();
	/**
	 * Handler of background tasks
	 */
	private BackgroundTaskHandler backgroundTaskHandler = new BackgroundTaskHandler();

	/**
	 * Forces an update of the accounts table
	 */
	protected void updateAccounts() {
		accountsTableModel.fireTableDataChanged();
	}

	/**
	 * Forces an update of the transactions table
	 */
	protected void updateTransactions() {
		transactionEditor.editTransaction(null);
		transactionsTableModel.fireTableDataChanged();
		externalEventHandler.transactionsUpdated();
	}

	/**
	 * Updates the values displayed in the currency combo
	 */
	protected void updateDefaultCurrencyCombo() {
		jComboBoxDefaultCurrency.removeAllItems();
		jComboBoxDefaultCurrency.setEnabled(false);
		for (Object currency : currenciesTableModel.getCurrenciesComboList())
			jComboBoxDefaultCurrency.addItem(currency);
		if (financeData.getDefaultCurrency() != null)
			jComboBoxDefaultCurrency.setSelectedItem(currenciesTableModel.getDefaultCurrency());
		else
			jComboBoxDefaultCurrency.setSelectedItem(-1);
		jComboBoxDefaultCurrency.setEnabled(true);
	}

	/**
	 * Class for processing external events
	 */
	protected class ExternalEventHandler implements FinanceData.TransactionCreatedEventListener, FinanceData.TransactionUpdatedEventListener, FinanceData.TransactionDeletedEventListener, FinanceData.CurrencyUpdatedEventListener {

		@Override
		public void transactionCreated(FinanceTransaction newTransaction) {
			int newTransactionIndex = transactionsTableModel.getTransactionIndex(newTransaction);
			jTableTransactions.getSelectionModel().setSelectionInterval(newTransactionIndex, newTransactionIndex);
			jTableTransactions.scrollRectToVisible(jTableTransactions.getCellRect(newTransactionIndex, 0, true));
			((SpinnerNumberModel) jSpinnerPage.getModel()).setMaximum(transactionsTableModel.getPageCount());
		}

		@Override
		public void currenciesUpdated() {
			updateDefaultCurrencyCombo();
		}

		@Override
		public void transactionUpdated(FinanceTransaction updatedTransaction) {
			//Do nothing
		}

		@Override
		public void transactionsUpdated() {
			((SpinnerNumberModel) jSpinnerPage.getModel()).setMaximum(transactionsTableModel.getPageCount());
		}

		@Override
		public void transactionDeleted(FinanceTransaction deletedTransaction) {
			((SpinnerNumberModel) jSpinnerPage.getModel()).setMaximum(transactionsTableModel.getPageCount());
		}
	}

	/**
	 * Helper class for processing background tasks
	 */
	protected class BackgroundTaskHandler {

		/**
		 * List of components to be disabled during the background task
		 */
		protected List<JComponent> disabledComponents;
		/**
		 * The background thread
		 */
		protected Thread thread;

		/**
		 * Default constructor
		 */
		public BackgroundTaskHandler() {
		}

		private void initComponents() {
			if (disabledComponents == null) {
				disabledComponents = new LinkedList<>();
				disabledComponents.add(jMenuItemImport);
				disabledComponents.add(jMenuItemExport);
				disabledComponents.add(jMenuItemCleanupDB);
				disabledComponents.add(jMenuItemRecalculateBalance);
			}
		}

		/**
		 * Runs a runnable task in the background. Wait for the previous task to
		 * complete.
		 *
		 * @param task the task to run
		 * @param taskDescription the task description to be displayed in the
		 * status bar
		 * @throws InterruptedException if unable to join the previous task
		 */
		public void runTask(final Runnable task, String taskDescription) throws InterruptedException {
			synchronized (this) {
				initComponents();
				if (thread != null) {
					thread.join();
					thread = null;
				}
				//Prepare for for running the task
				for (JComponent component : disabledComponents)
					component.setEnabled(false);
				jLabelProgressIndicator.setVisible(true);
				jLabelCurrentTask.setText(taskDescription);

				thread = new Thread(new Runnable() {
					@Override
					public void run() {
						task.run();

						//Restore the form after running the task
						for (JComponent component : disabledComponents)
							component.setEnabled(true);
						jLabelProgressIndicator.setVisible(false);
						jLabelCurrentTask.setText(""); //NOI18N
					}
				});

				thread.start();
			}
		}

		/**
		 * Waits for all tasks to complete before shutdown
		 */
		public void completeTasks() {
			synchronized (this) {
				if (thread != null) {
					try {
						thread.join();
						thread = null;
					} catch (InterruptedException ex) {
						Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		}
	}
}
