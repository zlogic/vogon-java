/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.zlogic.vogon.web.security.UserService;
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
		 * The PasswordEncoder instance
		 */
		@Autowired
		private PasswordEncoder passwordEncoder;

		/**
		 * Adds the UserService to the AuthenticationManager
		 *
		 * @param auth the AuthenticationManagerBuilder instance
		 * @throws Exception if
		 * AuthenticationManagerBuilder.userDetailsService() throws an exception
		 */
		@Autowired
		protected void registerAuthentication(AuthenticationManagerBuilder auth) throws Exception {
			auth
					.userDetailsService(userService)
					.passwordEncoder(passwordEncoder);
		}

		/**
		 * Returns the authenticationManagerBean used by other configurators
		 *
		 * @return the authenticationManagerBean used by other configurators
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

		/**
		 * Performs WebSecurity configuration
		 *
		 * @param web the WebSecurity instance to configure
		 * @throws Exception if WebSecurity throws an exception
		 */
		@Override
		public void configure(WebSecurity web) throws Exception {
			web
					.ignoring().antMatchers("/webjars/**");//Fix IE SSL font download bug //NOI18N
		}

		/**
		 * Returns the PasswordEncoder bean
		 *
		 * @return the PasswordEncoder bean
		 */
		@Bean
		public PasswordEncoder passwordEncoder() {
			return new BCryptPasswordEncoder();
		}
	}

	/**
	 * Spring ResourceServer configuration
	 */
	@Configuration
	@EnableResourceServer
	protected static class ResourceServer extends ResourceServerConfigurerAdapter {

		/**
		 * The ServerTypeDetector instance
		 */
		@Autowired
		private ServerTypeDetector serverTypeDetector;

		/**
		 * The TokenStore instance
		 */
		@Autowired
		private TokenStore tokenStore;

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
			//The logout handler to delete tokens
			LogoutHandler logoutHandler = new LogoutHandler() {
				/**
				 * The TokenExtractor instance
				 */
				private TokenExtractor tokenExtractor = new BearerTokenExtractor();

				@Override
				public void logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
					auth = tokenExtractor.extract(request);

					if (auth.getPrincipal().getClass() != String.class)
						return;
					OAuth2Authentication oauth2 = tokenStore.readAuthentication((String) auth.getPrincipal());

					OAuth2AccessToken token = tokenStore.getAccessToken(oauth2);
					if (token == null)
						return;
					tokenStore.removeAccessToken(token);//TODO: Remove refresh token as well?
				}
			};
			// The logout success handler
			LogoutSuccessHandler logoutSuccessHandler = new LogoutSuccessHandler() {

				@Override
				public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth) throws IOException, ServletException {

				}
			};
			(serverTypeDetector.isSslSupported() ? http.requiresChannel().anyRequest().requiresSecure().and() : http)
					//.authorizeRequests().antMatchers("/oauth/token").fullyAuthenticated().and()
					.authorizeRequests().antMatchers("/oauth/token").anonymous().and() //NOI18N
					.authorizeRequests().antMatchers("/service/**").hasAuthority(VogonSecurityUser.AUTHORITY_USER).and() //NOI18N
					.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
					.logout().addLogoutHandler(logoutHandler).logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler); //NOI18N
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
					.accessTokenValiditySeconds(60 * 60 * 24 * 30);
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
		return new InMemoryTokenStore();
	}
}
