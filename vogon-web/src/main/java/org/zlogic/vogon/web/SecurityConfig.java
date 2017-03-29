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
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.zlogic.vogon.web.configuration.VogonConfiguration;
import org.zlogic.vogon.web.security.JpaTokenStore;
import org.zlogic.vogon.web.security.VogonSecurityUser;

/**
 * Configures security
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig {

	/**
	 * The accessed resource ID
	 */
	private static final String resourceId = "springsec"; //NOI18N
	
	/**
	 * Spring ResourceServer configuration
	 */
	@Configuration
	@EnableResourceServer
	@EnableWebSecurity
	protected static class ResourceServer extends ResourceServerConfigurerAdapter {

		/**
		 * The ServerTypeDetector instance
		 */
		@Autowired
		private ServerTypeDetector serverTypeDetector;

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
			http.requiresChannel().anyRequest().requiresSecure().and()
				//.authorizeRequests().antMatchers("/oauth/token").fullyAuthenticated().and()
				.authorizeRequests()
					.antMatchers("/oauth/token").anonymous() //NOI18N
					.antMatchers("/service/**", "/oauth/logout").hasAuthority(VogonSecurityUser.AUTHORITY_USER).and() //NOI18N
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
		 * The TokenStore instance
		 */
		@Autowired
		private TokenStore tokenStore;
		
		/**
		 * The configuration handler
		 */
		@Autowired
		private VogonConfiguration configuration;

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
			clients.inMemory().withClient("vogonweb") //NOI18N
					.authorizedGrantTypes("password", "authorization_code") //NOI18N
					.authorities(VogonSecurityUser.AUTHORITY_USER)
					.scopes("read", "write", "trust") //NOI18N
					.resourceIds(resourceId)
					.accessTokenValiditySeconds(configuration.getTokenExpiresSeconds());
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
			endpoints.authenticationManager(authenticationManager).tokenStore(tokenStore);
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

	/**
	 * Returns the TokenStore instance
	 *
	 * @return the TokenStore instance
	 */
	@Bean
	public TokenStore tokenStore() {
		return new JpaTokenStore();
	}
}
