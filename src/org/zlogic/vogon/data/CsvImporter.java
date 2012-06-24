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

/**
 * Implementation for importing data from CSV files
 *
 * @author Dmitry Zolotukhin
 */
public class CsvImporter implements FileImporter {

    /**
     * Parses and imports a CSV file
     * @param file The file to be imported
     * @return A new FinanceData object, initialized from the CSV file 
     * @throws VogonImportException In case of any import errors (I/O, format etc.)
     */
    @Override
    public FinanceData importFile(java.io.File file) throws VogonImportException {
	try {
	    ArrayList<FinanceTransaction> transactions = new ArrayList<>();
	    CSVReader reader = new CSVReader(new java.io.FileReader(file));
	    String[] columns; 
	    String[] columnsHeader = null;
	    while ((columns = reader.readNext()) != null) {
		if(columns.length<5)
		    throw new VogonImportException((new MessageFormat(i18nBundle.getString("CSV_FORMAT_EXCEPTION"))).format(new Object[] {columns.toString()}));
		if(columnsHeader==null)
		    columnsHeader = columns;
		else{
		    //Count the transaction's total sum
		    double sum=0;
		    for(int i=3;i<columns.length;i++)
			if(!columns[i].isEmpty())
			    sum+= Double.parseDouble(columns[i].replaceAll("[^ \\t]\\s+","").replaceAll("[^0-9.-]",""));
		    transactions.add(new ExpenseTransaction(columns[0],columns[2],new SimpleDateFormat("yyyy-MM-dd").parse(columns[1]),sum));
		}
	    }
	    FinanceData result = new FinanceData(transactions);
	    return result;
	} catch (java.io.FileNotFoundException e) {
	    throw new VogonImportException(e);
	} catch (java.io.IOException e) {
	    throw new VogonImportException(e);
	} catch (java.text.ParseException e) {
	    throw new VogonImportException(e);
	}
    }

    private java.util.ResourceBundle i18nBundle= java.util.ResourceBundle.getBundle("org/zlogic/vogon/data/Bundle");
}
