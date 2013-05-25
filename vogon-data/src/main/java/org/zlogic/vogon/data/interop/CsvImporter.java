/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic42@outlook.com>
 */
package org.zlogic.vogon.data.interop;

import au.com.bytecode.opencsv.CSVReader;
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
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.zlogic.vogon.data.Constants;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceAccount_;
import org.zlogic.vogon.data.FinanceData;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.data.Utils;

/**
 * Implementation for importing data from CSV files
 *
 * @author Dmitry Zolotukhin <a
 * href="mailto:zlogic42@outlook.com">zlogic42@outlook.com</a>
 */
public class CsvImporter implements FileImporter {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/data/messages");
	/**
	 * The input CSV file
	 */
	protected File inputFile;

	/**
	 * Creates an instance of the CSV Importer
	 *
	 * @param inputFile the input file to read
	 */
	public CsvImporter(File inputFile) {
		this.inputFile = inputFile;
	}

	@Override
	public void importFile(FinanceData financeData, EntityManager entityManager) throws VogonImportException, VogonImportLogicalException {
		try (CSVReader reader = new CSVReader(new java.io.InputStreamReader(new java.io.FileInputStream(inputFile), "UTF8"))) {//NOI18N
			entityManager.getTransaction().begin();

			List<FinanceAccount> accounts = new ArrayList<>();
			String[] columns;
			String[] columnsHeader = null;
			while ((columns = reader.readNext()) != null) {
				if (columns.length < 5) {
					reader.close();
					throw new VogonImportLogicalException((new MessageFormat(messages.getString("CSV_FORMAT_EXCEPTION"))).format(new Object[]{Utils.join(columns, ",")}));  //NOI18N
				}
				if (columnsHeader == null) {
					columnsHeader = columns;
					//Create accounts
					for (int i = 3; i < columns.length; i++) {
						// Try searching existing account in database
						CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
						CriteriaQuery<FinanceAccount> accountsCriteriaQuery = criteriaBuilder.createQuery(FinanceAccount.class);
						Root<FinanceAccount> acc = accountsCriteriaQuery.from(FinanceAccount.class);
						Predicate condition = criteriaBuilder.equal(acc.get(FinanceAccount_.name), columns[i]);
						accountsCriteriaQuery.where(condition);
						FinanceAccount foundAccount = null;
						try {
							foundAccount = entityManager.createQuery(accountsCriteriaQuery).getSingleResult();
						} catch (javax.persistence.NoResultException ex) {
						}

						if (foundAccount != null && foundAccount.getName().equals(columns[i])) {
							accounts.add(foundAccount);
						} else {
							FinanceAccount account = new FinanceAccount(columns[i], null);
							entityManager.persist(account);
							accounts.add(account);
						}
					}
				} else {
					boolean hasPositiveAmounts = false;
					boolean hasNegativeAmounts = false;
					Map<FinanceAccount, Long> accountAmounts = new HashMap<>();
					for (int i = 3; i < columns.length; i++)
						if (!columns[i].isEmpty()) {
							double amount = Double.parseDouble(columns[i].replaceAll("[^ \\t]\\s+", "").replaceAll("[^0-9.-]", "")); //NOI18N
							if (amount == 0)
								continue;
							amount = Math.round(amount * Constants.rawAmountMultiplier);
							accountAmounts.put(accounts.get(i - 3), (long) (amount));
							if (amount > 0)
								hasPositiveAmounts = true;
							if (amount < 0)
								hasNegativeAmounts = true;
						}
					//Split tags
					String[] tags = columns[2].split(","); //NOI18N
					FinanceTransaction transaction = null;
					Date date = new SimpleDateFormat("yyyy-MM-dd").parse(columns[1]); //NOI18N
					FinanceTransaction.Type transactionType = null;
					if ((hasPositiveAmounts || hasNegativeAmounts) && !(hasPositiveAmounts && hasNegativeAmounts))
						transactionType = FinanceTransaction.Type.EXPENSEINCOME;
					else if (hasPositiveAmounts && hasNegativeAmounts)
						transactionType = FinanceTransaction.Type.TRANSFER;
					if (transactionType != null) {
						//Expense transaction
						transaction = new FinanceTransaction(columns[0], tags, date, transactionType);
					} else {
						reader.close();
						throw new VogonImportLogicalException((new MessageFormat(messages.getString("CSV_TRANSACTION_TOO_COMPLEX"))).format(new Object[]{Utils.join(columns, ",")})); //NOI18N
					}

					//Add components
					List<TransactionComponent> components = new LinkedList<>();
					for (Entry<FinanceAccount, Long> amount : accountAmounts.entrySet()) {
						TransactionComponent newComponent = new TransactionComponent(amount.getKey(), transaction, amount.getValue());
						entityManager.persist(newComponent);
						components.add(newComponent);
					}
					transaction.addComponents(components);
					entityManager.persist(transaction);
				}
			}
			reader.close();

			entityManager.getTransaction().commit();
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
