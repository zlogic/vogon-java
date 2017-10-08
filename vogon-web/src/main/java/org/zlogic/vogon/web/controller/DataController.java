/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.web.controller.serialization.JSONMapper;
import org.zlogic.vogon.web.data.AccountRepository;
import org.zlogic.vogon.web.data.TransactionRepository;
import org.zlogic.vogon.web.data.UserRepository;
import org.zlogic.vogon.web.data.model.importexport.ImportExportData;
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
	 * JSONMapper instance
	 */
	@Autowired
	private JSONMapper jsonMapper;

	/**
	 * Imports uploaded XML data
	 *
	 * @param data the file to import
	 * @param userPrincipal the authenticated user
	 * @return true of import succeeded
	 */
	@RequestMapping(value = "/import", method = RequestMethod.POST, produces = "application/json", consumes = "multipart/form-data")
	public @ResponseBody
	Boolean importData(@RequestParam("file") MultipartFile data, @AuthenticationPrincipal VogonSecurityUser userPrincipal) throws RuntimeException {
		VogonUser user = userRepository.findByUsernameIgnoreCase(userPrincipal.getUsername());
		ImportExportData importData = null;
		try {
			importData = jsonMapper.readValue(data.getInputStream(), ImportExportData.class);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		
		importData.persist(user, em);

		return true;
	}

	/**
	 * Returns all data
	 *
	 * @param userPrincipal the authenticated user
	 * @return the HTTPEntity for the file download
	 */
	@RequestMapping(value = "/export", method = {RequestMethod.GET, RequestMethod.POST})
	public HttpEntity<byte[]> exportDataJSON(@AuthenticationPrincipal VogonSecurityUser userPrincipal) throws RuntimeException {
		VogonUser user = userRepository.findByUsernameIgnoreCase(userPrincipal.getUsername());

		Sort accountSort = new Sort(Sort.Direction.ASC, "id");//NOI18N
		Sort transactionSort = new Sort(Sort.Direction.ASC, "id");//NOI18N

		ImportExportData data = new ImportExportData(accountRepository.findByOwner(user, accountSort), transactionRepository.findByOwner(user, transactionSort));
		byte[] output = null;
		try {
			//Convert to JSON
			output = jsonMapper.writer().withDefaultPrettyPrinter().writeValueAsString(data).getBytes("utf-8"); //NOI18N
		} catch (JsonProcessingException | UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}

		String date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()); //NOI18N

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.setContentLength(output.length);
		headers.setContentDispositionFormData("attachment", MessageFormat.format("vogon-{0}.json", date)); //NOI18N //NOI18N

		return new HttpEntity<>(output, headers);
	}
}
