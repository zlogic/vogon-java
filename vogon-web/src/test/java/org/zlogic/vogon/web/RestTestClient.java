/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;
import javax.net.ssl.SSLContext;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * REST Client user for tests with configuration required for Vogon
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Service
public class RestTestClient implements InitializingBean {

	private RestTemplate restTemplate;

	@Autowired
	private ServerTypeDetector serverTypeDetector;

	/**
	 * Creates the RestTempate instance
	 */
	@Override
	public void afterPropertiesSet() {
		try {
			SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(new File(serverTypeDetector.getKeystoreFile()), serverTypeDetector.getKeystorePassword().toCharArray()).build();
			HttpClient client = HttpClientBuilder.create().setSSLContext(sslContext).setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

			restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(client));
		} catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Returns the default HttpHeaders
	 *
	 * @return the default HttpHeaders
	 */
	public HttpHeaders getDefaultHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		return headers;
	}

	/**
	 * Performs authentication and returns the default HttpHeaders, including
	 * the OAuth2 token
	 */
	public HttpHeaders authenticate() {
		return authenticate("user01", "mypassword");
	}

	/**
	 * Performs authentication and returns the default HttpHeaders, including
	 * the OAuth2 token
	 *
	 * @param username the authentication username
	 * @param password the authentication password
	 */
	public HttpHeaders authenticate(String username, String password) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("username", username);
		map.add("password", password);
		map.add("grant_type", "password");
		map.add("client_id", "vogonweb");
		String token;
		try {
			JSONObject result = new JSONObject(restTemplate.postForObject("https://localhost:8443/oauth/token", map, String.class));
			token = result.getString("access_token");
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		}

		HttpHeaders headers = getDefaultHeaders();
		headers.set("Authorization", "Bearer " + token);
		return headers;
	}

	/**
	 * Returns the default HttpHeaders, including a bad OAuth2 token
	 */
	public HttpHeaders badAuthenticate() {
		HttpHeaders headers = getDefaultHeaders();
		headers.set("Authorization", "Bearer bad_token");
		return headers;
	}

	/**
	 * Returns the RestTemplate instance
	 *
	 * @return the RestTemplate instance
	 */
	public RestTemplate getRestTemplate() {
		return restTemplate;
	}
}
