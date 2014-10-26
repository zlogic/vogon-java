/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.wildfly;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * Proxy class to make sure the Spring MultipartAutoConfiguration finds the
 * WildFly-compatible MultipartResolver
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
public class MultipartResolver extends StandardServletMultipartResolver {

	/**
	 * The target resolver class
	 */
	private CommonsMultipartResolver targetResolver = new CommonsMultipartResolver();

	/**
	 * Proxy call to CommonsMultipartResolver.setResolveLazily
	 *
	 * @param resolveLazily true if CommonsMultipartResolver should resolve
	 * elements lazily
	 */
	@Override
	public void setResolveLazily(boolean resolveLazily) {
		targetResolver.setResolveLazily(resolveLazily);
	}

	/**
	 * Proxy call to CommonsMultipartResolver.setResolveLazily
	 *
	 * @param request HttpServletRequest to check
	 * @return true if HttpServletRequest is multipart
	 */
	@Override
	public boolean isMultipart(HttpServletRequest request) {
		return targetResolver.isMultipart(request);
	}

	/**
	 * Proxy call to CommonsMultipartResolver.resolveMultipart
	 *
	 * @param request the HttpServletRequest to resolve
	 * @return the HttpServletRequest wrapped inside a
	 * MultipartHttpServletRequest
	 * @throws MultipartException if CommonsMultipartResolver throws the
	 * exception
	 */
	@Override
	public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
		return targetResolver.resolveMultipart(request);
	}

	/**
	 * Proxy call to CommonsMultipartResolver.cleanupMultipart
	 *
	 * @param request MultipartHttpServletRequest for cleanup
	 */
	@Override
	public void cleanupMultipart(MultipartHttpServletRequest request) {
		targetResolver.cleanupMultipart(request);
	}
}
