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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
     * @throws VogonImportLogicalException In case of logical erors (without
     * meaningful stack trace, just to show an error message)
     */
    @Override
    public FinanceData importFile(java.io.File file) throws VogonImportException, VogonImportLogicalException {
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
		    for (int i = 3; i < columns.length; i++)
			accounts.add(new FinanceAccount(columns[i]));
		} else {
		    //Count the transaction's total sum
		    double sum = 0;
		    int sumNum = 0;
		    boolean hasPositiveAmounts = false;
		    boolean hasNegativeAmounts = false;
		    HashMap<FinanceAccount, Double> accountAmounts = new HashMap<>();
		    for (int i = 3; i < columns.length; i++)
			if (!columns[i].isEmpty()) {
			    double amount = Double.parseDouble(columns[i].replaceAll("[^ \\t]\\s+", "").replaceAll("[^0-9.-]", ""));
			    if (amount == 0)
				continue;
			    sum += amount;
			    accountAmounts.put(accounts.get(i - 3), amount);
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
			transaction = new ExpenseTransaction(columns[0], tags, date, sum, accountAmounts);
		    } else if (hasPositiveAmounts && hasNegativeAmounts) {
			//Transfer/split transaction
			if (sum != 0)
			    throw new VogonImportLogicalException((new MessageFormat(i18nBundle.getString("CSV_TRANSFER_TRANSACTION_SUM_EXCEPTION"))).format(new Object[]{Utils.join(columns, ",")}));
			FinanceAccount accountFrom = null;
			double amountFrom = 0;
			for (Map.Entry<FinanceAccount, Double> amount : accountAmounts.entrySet()) {
			    if (amount.getValue() < 0) {
				if (accountFrom == null) {
				    accountFrom = amount.getKey();
				    amountFrom = -amount.getValue();
				} else
				    throw new VogonImportLogicalException((new MessageFormat(i18nBundle.getString("CSV_TRANSFER_TRANSACTION_EXCEPTION"))).format(new Object[]{Utils.join(columns, ",")}));
			    }
			}
			accountAmounts.remove(accountFrom);
			transaction = new TransferTransaction(columns[0], tags, date, amountFrom, accountFrom, accountAmounts);
		    } else {
			throw new VogonImportLogicalException((new MessageFormat(i18nBundle.getString("CSV_TRANSFER_TRANSACTION_EXCEPTION"))).format(new Object[]{Utils.join(columns, ",")}));
		    }
		    transactions.add(transaction);
		}
	    }
	    FinanceData result = new FinanceData(transactions, accounts);
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
