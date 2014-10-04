/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.zlogic.vogon.web.security.UserService;
import org.zlogic.vogon.web.security.VogonSecurityUser;

/**
 * Configures security
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Configuration
public class SecurityConfig {

	/**
	 * The accessed resource ID
	 */
	private static final String resourceId = "springsec";

	/**
	 * Spring web security configuration
	 */
	@Configuration
	@EnableWebSecurity
	protected static class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

		/**
		 * The UserService instance
		 */
		@Autowired
		private UserService userService;

		/**
		 * Adds the UserService to the AuthenticationManager
		 *
		 * @param auth the AuthenticationManagerBuilder instance
		 * @throws Exception if
		 * AuthenticationManagerBuilder.userDetailsService() throws an exception
		 */
		@Autowired
		protected void registerAuthentication(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(userService);
		}

		/**
		 * Returns the authenticationManagerBean used by other configurators
		 *
		 * @return
		 * @throws Exception if
		 * WebSecurityConfigurerAdapter.authenticationManagerBean() throws an
		 * exception
		 */
		@Override
		@Bean
		public AuthenticationManager authenticationManagerBean() throws Exception {
			return super.authenticationManagerBean();
		}

		/**
		 * Performs HttpSecurity configuration
		 *
		 * @param http the HttpSecurity instance to configure
		 * @throws Exception if HttpSecurity throws an exception
		 */
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
					.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		}
	}

	/**
	 * Spring ResourceServer configuration
	 */
	@Configuration
	@EnableResourceServer
	protected static class ResourceServer extends ResourceServerConfigurerAdapter {

		/**
		 * Configures ResourceServerSecurity
		 *
		 * @param resources the ResourceServerSecurityConfigurer instance to
		 * configure
		 */
		@Override
		public void configure(ResourceServerSecurityConfigurer resources) {
			resources.resourceId(resourceId);
		}

		/**
		 * Performs HttpSecurity configuration
		 *
		 * @param http the HttpSecurity instance to configure
		 * @throws Exception if HttpSecurity throws an exception
		 */
		@Override
		public void configure(HttpSecurity http) throws Exception {
			http
					.requiresChannel().anyRequest().requiresSecure().and()
					//.authorizeRequests().antMatchers("/oauth/token").fullyAuthenticated().and()
					.authorizeRequests().antMatchers("/oauth/token").anonymous().and()
					.authorizeRequests().antMatchers("/service/**").hasAuthority(VogonSecurityUser.AUTHORITY).and()
					.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		}
	}

	/**
	 * Spring OAuth2 configuration
	 */
	@Configuration
	@EnableAuthorizationServer
	protected static class OAuth2SecurityConfig extends AuthorizationServerConfigurerAdapter {

		/**
		 * The AuthenticationManager instance
		 */
		@Autowired
		private AuthenticationManager authenticationManager;

		/**
		 * Configures the ClientDetailsService
		 *
		 * @param clients the ClientDetailsServiceConfigurer instance to
		 * configure
		 * @throws Exception if ClientDetailsServiceConfigurer throws an
		 * exception
		 */
		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
			clients.inMemory().withClient("vogonweb")
					.authorizedGrantTypes("password", "authorization_code")
					.authorities(VogonSecurityUser.AUTHORITY)
					.scopes("read", "write", "trust")
					.resourceIds(resourceId)
					.accessTokenValiditySeconds(60 * 24);
		}

		/**
		 * Configures the AuthorizationServerEndpoints
		 *
		 * @param endpoints the AuthorizationServerEndpointsConfigurer instance
		 * to configure
		 * @throws Exception if AuthorizationServerEndpointsConfigurer throws an
		 * exception
		 */
		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
			endpoints.authenticationManager(authenticationManager);
		}

		/**
		 * Configures the AuthorizationServerSecurity
		 *
		 * @param oauthServer the AuthorizationServerSecurityConfigurer instance
		 * to configure
		 * @throws Exception if AuthorizationServerSecurityConfigurer throws an
		 * exception
		 */
		@Override
		public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
			oauthServer.allowFormAuthenticationForClients();
		}
	}
}
