/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.data.interop.VogonExportException;
import org.zlogic.vogon.data.interop.VogonImportException;
import org.zlogic.vogon.data.interop.VogonImportLogicalException;
import org.zlogic.vogon.data.interop.XmlExporter;
import org.zlogic.vogon.data.interop.XmlImporter;
import org.zlogic.vogon.web.data.AccountRepository;
import org.zlogic.vogon.web.data.TransactionRepository;
import org.zlogic.vogon.web.data.UserRepository;
import org.zlogic.vogon.web.security.VogonSecurityUser;

/**
 * Spring MVC controller for importing/exporting data
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Controller
@Transactional
@RequestMapping(value = "/service")
public class DataController {

	/**
	 * The EntityManager instance
	 */
	@PersistenceContext
	private EntityManager em;
	/**
	 * The users repository
	 */
	@Autowired
	private UserRepository userRepository;
	/**
	 * The transactions repository
	 */
	@Autowired
	private TransactionRepository transactionRepository;
	/**
	 * The accounts repository
	 */
	@Autowired
	private AccountRepository accountRepository;

	/**
	 * Imports uploaded XML data
	 *
	 * @param data the file to import
	 * @param userPrincipal the authenticated user
	 * @return true of import succeeded
	 */
	@RequestMapping(value = "/import", method = RequestMethod.POST, produces = "application/json", consumes = "multipart/form-data")
	public @ResponseBody
	Boolean importData(@RequestPart("file") MultipartFile data, @AuthenticationPrincipal VogonSecurityUser userPrincipal) throws RuntimeException {
		VogonUser user = userRepository.findByUsername(userPrincipal.getUsername());
		try {
			XmlImporter importer = new XmlImporter(data.getInputStream());
			importer.importData(user, em);
		} catch (IOException | VogonImportException | VogonImportLogicalException ex) {
			throw new RuntimeException(ex);
		}
		return true;
	}

	/**
	 * Returns all currencies
	 *
	 * @param data the file to import
	 * @param userPrincipal the authenticated user
	 * @return true of import succeeded
	 */
	@RequestMapping(value = "/export", method = RequestMethod.GET, produces = "application/xml")
	public void exportData(OutputStream outputStream, @AuthenticationPrincipal VogonSecurityUser userPrincipal) throws RuntimeException {
		VogonUser user = userRepository.findByUsername(userPrincipal.getUsername());
		XmlExporter exporter = new XmlExporter(outputStream);
		try {
			exporter.exportData(user, accountRepository.findByOwner(user), transactionRepository.findByOwner(user), null);
		} catch (VogonExportException ex) {
			throw new RuntimeException(ex);
		}
	}
}
