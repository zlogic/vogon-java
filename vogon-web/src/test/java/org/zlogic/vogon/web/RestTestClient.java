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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
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
	 * Returns the RestTemplate instance
	 *
	 * @return the RestTemplate instance
	 */
	public RestTemplate getRestTemplate() {
		return restTemplate;
	}
}
