/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.text.MessageFormat;
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
	    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
	    java.lang.String line;
	    while ((line = reader.readLine()) != null) {
		ArrayList<String> columns = parseLine(line);
		if(columns.size()<5)
		    throw new VogonImportException((new MessageFormat(i18nBundle.getString("CSV FORMAT EXCEPTION"))).format(new Object[] {line}));
		transactions.add(new ExpenseTransaction(columns.get(0),columns.get(2),0.0/*Double.parseDouble(columns[6])*/));
	    }
	    FinanceData result = new FinanceData(transactions);
	    return result;
	} catch (java.io.FileNotFoundException e) {
	    throw new VogonImportException(e);
	} catch (java.io.IOException e) {
	    throw new VogonImportException(e);
	}
    }

    /**
     * Parses a single line from a CSV file
     * @param line A line from a *.CSV file
     * @return The parsed line as an array
     * @throws VogonImportException In case the line is not a valid CSV line (e.g. contains unmatched quotes)
     */
    protected ArrayList<String> parseLine(String line) throws VogonImportException {
	ArrayList<String> result = new ArrayList<>();
	boolean insideQuotes = false;
	int lastCommaPos = 0;
	for (int i = 0; i < line.length(); i++) {
	    char c = line.charAt(i);
	    String column = null;
	    if(lastCommaPos>i)
		continue;
	    if (insideQuotes) {
		if ((i + 1) < line.length()) {
		    if (c == '\"' && line.charAt(i + 1) != '\"'){
			column = line.substring(lastCommaPos + 1, i).replace("\"\"", "\"");
			lastCommaPos = i+2;
			insideQuotes = false;
		    }else if(c != '\"' && line.charAt(i + 1) == '\"' && i+2==line.length()){
			column = line.substring(lastCommaPos + 1, i + 1).replace("\"\"", "\"");
			lastCommaPos = i+2;
			insideQuotes = false;
		    }
		} else
		    throw new VogonImportException((new MessageFormat(i18nBundle.getString("INVALID CSV LINE EXCEPTION"))).format(new Object[] {line}));
	    } else if (c == ',' || i == (line.length() - 1)) {
		column = line.substring(lastCommaPos, (i == (line.length() - 1))? i+1:i);
		lastCommaPos = i+1;
	    } else if (c == '\"') {
		insideQuotes = true;
	    }

	    if (column != null) {
		result.add(column);
	    }
	}
	return result;
    }
    
    private java.util.ResourceBundle i18nBundle= java.util.ResourceBundle.getBundle("org/zlogic/vogon/data/Bundle");
}
