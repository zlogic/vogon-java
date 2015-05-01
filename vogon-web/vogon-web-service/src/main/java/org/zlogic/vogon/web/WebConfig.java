/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.web.bind.support.AuthenticationPrincipalArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * Spring boot annotations configuration
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

	/**
	 * The customized MappingJackson2HttpMessageConverter instance
	 */
	@Autowired
	private MappingJackson2HttpMessageConverter jacksonMessageConverter;

	/**
	 * Adds view controllers to the registry
	 *
	 * @param registry the registry
	 */
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		super.addViewControllers(registry);
		registry.addViewController("/").setViewName("index"); //NOI18N
	}

	/**
	 * Returns the view resolver
	 *
	 * @return the view resolver
	 */
	@Bean
	public ViewResolver getViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/"); //NOI18N
		resolver.setSuffix(".jsp"); //NOI18N
		return resolver;
	}

	/**
	 * Configures message converters and adds the customized JSON converter
	 *
	 * @param converters the converters list to use
	 */
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(jacksonMessageConverter);
		converters.add(new ByteArrayHttpMessageConverter());
		super.configureMessageConverters(converters);
	}

	/**
	 * Adds argument resolvers and adds the
	 * AuthenticationPrincipalArgumentResolver
	 *
	 * @param argumentResolvers list of argument resolvers to add
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new AuthenticationPrincipalArgumentResolver());
		super.addArgumentResolvers(argumentResolvers);
	}

	/**
	 * Enable default servlet handler
	 *
	 * @param configurer DefaultServletHandlerConfigurer instance
	 */
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}
}
