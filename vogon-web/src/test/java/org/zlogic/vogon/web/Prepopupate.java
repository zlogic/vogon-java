/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.web.data.UserRepository;

/**
 * Class for pre-populating the database with some simple test data
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Service
public class Prepopupate {

	@Autowired
	private UserRepository userRepository;

	/**
	 * Prepopulate the database with default test data
	 */
	public void prepopulate() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		VogonUser user01 = new VogonUser("user01", passwordEncoder.encode("mypassword"));
		VogonUser user02 = new VogonUser("user02", passwordEncoder.encode("mypassword2"));
		userRepository.save(Arrays.asList(user01, user02));
	}

	/**
	 * Clear everything from the database
	 */
	public void clear() {
		userRepository.deleteAll();
	}
}
