/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import au.com.bytecode.opencsv.CSVReader;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * Implementation for importing data from CSV files
 *
 * @author Dmitry Zolotukhin
 */
public class CsvImporter implements FileImporter {

    /**
     * Parses and imports a CSV file
     *
     * @param file The file to be imported
     * @return A new FinanceData object, initialized from the CSV file
     * @throws VogonImportException In case of import errors (I/O, format,
     * indexing etc.)
     * @throws VogonImportLogicalException In case of logical errors (without
     * meaningful stack trace, just to show an error message)
     */
    @Override
    public FinanceData importFile(java.io.File file) throws VogonImportException, VogonImportLogicalException {
	EntityManagerFactory entityManagerFactory = new DatabaseManager().getPersistenceUnit();
	EntityManager entityManager = entityManagerFactory.createEntityManager();
	entityManager.getTransaction().begin();
	try {
	    ArrayList<FinanceTransaction> transactions = new ArrayList<>();
	    ArrayList<FinanceAccount> accounts = new ArrayList<>();
	    CSVReader reader = new CSVReader(new java.io.FileReader(file));
	    String[] columns;
	    String[] columnsHeader = null;
	    while ((columns = reader.readNext()) != null) {
		if (columns.length < 5)
		    throw new VogonImportLogicalException((new MessageFormat(i18nBundle.getString("CSV_FORMAT_EXCEPTION"))).format(new Object[]{Utils.join(columns, ",")}));
		if (columnsHeader == null) {
		    columnsHeader = columns;
		    //Create accounts
		    for (int i = 3; i < columns.length; i++) {
			List<FinanceAccount> foundAccounts = entityManager.createQuery("SELECT a FROM FinanceAccount a WHERE a.name=:accountName").setParameter("accountName", columns[i]).getResultList();
			if (!foundAccounts.isEmpty() && foundAccounts.get(0).getName().equals(columns[i])) {
			    accounts.add(foundAccounts.get(0));
			} else {
			    FinanceAccount account = new FinanceAccount(columns[i]);
			    entityManager.persist(account);
			    accounts.add(account);
			}
		    }
		} else {
		    //Count the transaction's total sum
		    double sum = 0;
		    int sumNum = 0;
		    boolean hasPositiveAmounts = false;
		    boolean hasNegativeAmounts = false;
		    List<TransactionComponent> accountAmounts = new LinkedList<>();
		    for (int i = 3; i < columns.length; i++)
			if (!columns[i].isEmpty()) {
			    double amount = Double.parseDouble(columns[i].replaceAll("[^ \\t]\\s+", "").replaceAll("[^0-9.-]", ""));
			    if (amount == 0)
				continue;
			    sum += amount;
			    TransactionComponent newComponent = new TransactionComponent(accounts.get(i - 3), amount);
			    entityManager.persist(newComponent);
			    accountAmounts.add(newComponent);
			    if (amount > 0)
				hasPositiveAmounts = true;
			    if (amount < 0)
				hasNegativeAmounts = true;
			    sumNum++;
			}
		    //Split tags
		    String[] tags = columns[2].split(",");
		    FinanceTransaction transaction = null;
		    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(columns[1]);
		    if ((hasPositiveAmounts || hasNegativeAmounts) && !(hasPositiveAmounts && hasNegativeAmounts)) {
			//Expense transaction
			transaction = new ExpenseTransaction(columns[0], tags, date, accountAmounts);
			entityManager.persist(transaction);
		    } else if (hasPositiveAmounts && hasNegativeAmounts) {
			//Transfer/split transaction
			transaction = new TransferTransaction(columns[0], tags, date, accountAmounts);
			entityManager.persist(transaction);
		    } else {
			throw new VogonImportLogicalException((new MessageFormat(i18nBundle.getString("CSV_TRANSFER_TRANSACTION_EXCEPTION"))).format(new Object[]{Utils.join(columns, ",")}));
		    }
		    transactions.add(transaction);
		}
	    }
	    FinanceData result = new FinanceData(transactions, accounts);

	    entityManager.getTransaction().commit();
	    entityManager.close();
	    entityManagerFactory.close();

	    return result;
	} catch (java.io.FileNotFoundException e) {
	    Logger.getLogger(CsvImporter.class.getName()).log(Level.SEVERE, null, e);
	    throw new VogonImportException(e);
	} catch (java.io.IOException | java.text.ParseException e) {
	    Logger.getLogger(CsvImporter.class.getName()).log(Level.SEVERE, null, e);
	    throw new VogonImportException(e);
	} catch (VogonImportLogicalException e) {
	    throw new VogonImportLogicalException(e);
	}

    }
    private java.util.ResourceBundle i18nBundle = java.util.ResourceBundle.getBundle("org/zlogic/vogon/data/Bundle");
}
