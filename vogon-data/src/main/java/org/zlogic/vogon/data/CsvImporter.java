/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.File;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Implementation for importing data from CSV files
 *
 * @author Dmitry Zolotukhin
 */
public class CsvImporter implements FileImporter {

	/**
	 * The input CSV file
	 */
	protected File inputFile;

	/**
	 * Creates an instance of the CSV Importer
	 * 
	 * @param inputFile The input file to read
	 */
	public CsvImporter(File inputFile){
		this.inputFile = inputFile;
	}

	/**
	 * Parses and imports a CSV file
	 *
	 * @return A new FinanceData object, initialized from the CSV file
	 * @throws VogonImportException In case of import errors (I/O, format, indexing etc.)
	 * @throws VogonImportLogicalException In case of logical errors (without meaningful stack trace, just to show an error message)
	 */
	@Override
	public FinanceData importFile() throws VogonImportException, VogonImportLogicalException {

		try {
			List<FinanceTransaction> transactions = new ArrayList<>();
			List<FinanceAccount> accounts = new ArrayList<>();
			CSVReader reader = new CSVReader(new java.io.InputStreamReader(new java.io.FileInputStream(inputFile),"UTF8")); //$NON-NLS-1$
			String[] columns;
			String[] columnsHeader = null;
			while ((columns = reader.readNext()) != null) {
				if (columns.length < 5){
					reader.close();
					throw new VogonImportLogicalException((new MessageFormat(Messages.CsvImporter_CSV_Format_Exception)).format(new Object[]{Utils.join(columns, ",")})); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-1$
				}
				if (columnsHeader == null) {
					columnsHeader = columns;
					//Create accounts
					EntityManager entityManager = DatabaseManager.getInstance().getEntityManager();

					for (int i = 3; i < columns.length; i++) {
						// Try searching existing account in database
						CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
						CriteriaQuery<FinanceAccount> accountsCriteriaQuery = criteriaBuilder.createQuery(FinanceAccount.class);
						Root<FinanceAccount> acc = accountsCriteriaQuery.from(FinanceAccount.class);
						Predicate condition = criteriaBuilder.equal(acc.get(FinanceAccount_.name), columns[i]);
						accountsCriteriaQuery.where(condition);
						List<FinanceAccount> foundAccounts = entityManager.createQuery(accountsCriteriaQuery).getResultList();

						if (!foundAccounts.isEmpty() && foundAccounts.get(0).getName().equals(columns[i])) {
							accounts.add(foundAccounts.get(0));
						} else {
							FinanceAccount account = new FinanceAccount(columns[i]);
							accounts.add(account);
						}
					}
				} else {
					boolean hasPositiveAmounts = false;
					boolean hasNegativeAmounts = false;
					Map<FinanceAccount,Long> accountAmounts = new HashMap<>();
					for (int i = 3; i < columns.length; i++)
						if (!columns[i].isEmpty()) {
							double amount = Double.parseDouble(columns[i].replaceAll("[^ \\t]\\s+", "").replaceAll("[^0-9.-]", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							if (amount == 0)
								continue;
							amount = Math.round(amount*100);
							accountAmounts.put(accounts.get(i - 3), (long)(amount));
							if (amount > 0)
								hasPositiveAmounts = true;
							if (amount < 0)
								hasNegativeAmounts = true;
						}
					//Split tags
					String[] tags = columns[2].split(","); //$NON-NLS-1$
					FinanceTransaction transaction = null;
					Date date = new SimpleDateFormat("yyyy-MM-dd").parse(columns[1]); //$NON-NLS-1$
					if ((hasPositiveAmounts || hasNegativeAmounts) && !(hasPositiveAmounts && hasNegativeAmounts)) {
						//Expense transaction
						transaction = new ExpenseTransaction(columns[0], tags, date);
					} else if (hasPositiveAmounts && hasNegativeAmounts) {
						//Transfer/split transaction
						transaction = new TransferTransaction(columns[0], tags, date);
					} else {
						reader.close();
						throw new VogonImportLogicalException((new MessageFormat(Messages.CsvImporter_Transaction_Too_Complex)).format(new Object[]{Utils.join(columns, ",")})); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-1$
					}

					//Add components
					List<TransactionComponent> components = new LinkedList<>();
					for(Entry<FinanceAccount, Long> amount : accountAmounts.entrySet()){
						TransactionComponent newComponent = new TransactionComponent(amount.getKey(),transaction,amount.getValue());
						components.add(newComponent);
					}
					transaction.addComponents(components);
					transactions.add(transaction);
				}
			}
			reader.close();
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
}
