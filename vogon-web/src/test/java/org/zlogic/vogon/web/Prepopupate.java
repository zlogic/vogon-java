/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.FinanceTransaction;
import org.zlogic.vogon.data.TransactionComponent;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.web.data.AccountRepository;
import org.zlogic.vogon.web.data.TransactionRepository;
import org.zlogic.vogon.web.data.UserRepository;

/**
 * Class for pre-populating the database with some simple test data
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Service
@Transactional
public class Prepopupate {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	/**
	 * Parses a date in JSON format
	 *
	 * @param date the date string to parse
	 * @return the parsed date
	 */
	public static Date parseJSONDate(String date) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(date); //NOI18N
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Prepopulate the database with default test data
	 */
	public void prepopulate() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		VogonUser user01 = new VogonUser("user01", passwordEncoder.encode("mypassword"));
		VogonUser user02 = new VogonUser("user02", passwordEncoder.encode("mypassword2"));
		userRepository.save(Arrays.asList(user01, user02));

		FinanceAccount account1 = new FinanceAccount(user01, "test account 1", Currency.getInstance("RUB"));
		account1.setIncludeInTotal(true);
		account1.setShowInList(true);
		FinanceAccount account2 = new FinanceAccount(user01, "test account 2", Currency.getInstance("EUR"));
		account2.setIncludeInTotal(true);
		account2.setShowInList(true);
		FinanceAccount account3 = new FinanceAccount(user02, "test account 3", Currency.getInstance("RUB"));
		account3.setIncludeInTotal(true);
		account3.setShowInList(true);
		accountRepository.save(Arrays.asList(account1, account2, account3));

		FinanceTransaction transaction1 = new FinanceTransaction(user01, "test transaction 1", new String[]{"hello", "world"}, parseJSONDate("2014-02-17"), FinanceTransaction.Type.EXPENSEINCOME);
		FinanceTransaction transaction3 = new FinanceTransaction(user01, "test transaction 3", new String[]{}, parseJSONDate("2014-02-17"), FinanceTransaction.Type.TRANSFER);
		FinanceTransaction transaction2 = new FinanceTransaction(user01, "test transaction 2", new String[]{"magic", "hello"}, parseJSONDate("2015-01-07"), FinanceTransaction.Type.EXPENSEINCOME);
		FinanceTransaction transaction4 = new FinanceTransaction(user02, "test transaction 3", new String[]{}, parseJSONDate("2014-05-17"), FinanceTransaction.Type.EXPENSEINCOME);
		TransactionComponent component1 = new TransactionComponent(account1, transaction1, 42 * 100);
		TransactionComponent component2 = new TransactionComponent(account2, transaction1, 160 * 100);
		TransactionComponent component3 = new TransactionComponent(account2, transaction2, -314);
		TransactionComponent component4 = new TransactionComponent(account1, transaction2, 272);
		TransactionComponent component5 = new TransactionComponent(account3, transaction4, 100 * 100);
		transaction1.addComponent(component1);
		transaction1.addComponent(component2);
		transaction2.addComponent(component3);
		transaction2.addComponent(component4);
		transaction4.addComponent(component5);
		transactionRepository.save(Arrays.asList(transaction1, transaction2, transaction3, transaction4));
		accountRepository.save(Arrays.asList(account1, account2, account3));
	}

	/**
	 * Clear everything from the database
	 */
	public void clear() {
		transactionRepository.deleteAll();
		accountRepository.deleteAll();
		userRepository.deleteAll();
	}
}
