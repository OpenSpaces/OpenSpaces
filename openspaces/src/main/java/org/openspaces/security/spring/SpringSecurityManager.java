/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.security.spring;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import com.gigaspaces.security.AccessDeniedException;
import com.gigaspaces.security.Authentication;
import com.gigaspaces.security.AuthenticationException;
import com.gigaspaces.security.Authority;
import com.gigaspaces.security.AuthorityFactory;
import com.gigaspaces.security.SecurityException;
import com.gigaspaces.security.SecurityManager;
import com.gigaspaces.security.directory.DirectoryAccessDeniedException;
import com.gigaspaces.security.directory.DirectoryManager;
import com.gigaspaces.security.directory.User;
import com.gigaspaces.security.directory.UserDetails;

/**
 * A Spring security bridge over the GigaSpaces {@link SecurityManager} interface. The Spring
 * security configurations are loaded using the {@link FileSystemXmlApplicationContext} taking the
 * context definition files from the file system or from URLs. The location of the configuration
 * file is set using the <code>spring-security-config-location</code> property; if not set, a
 * default <code>security-config.xml</code> is considered (if present).
 * <p>
 * A common GigaSpaces security configuration: ([Gigaspaces root]/config/security/security.properties) <br>
 * <code>
 * <pre>
 * com.gs.security.security-manager.class = org.openspaces.security.spring.SpringSecurityManager
 * spring-security-config-location = ../config/security/security-config.xml
 * </pre>
 * </code>
 * 
 * @author Moran Avigdor
 * @since 7.1.1
 */
public class SpringSecurityManager implements SecurityManager {
	
    /** The security-config xml file location to create a new {@link FileSystemXmlApplicationContext} from */ 
    public static final String SPRING_SECURITY_CONFIG_LOCATION = "spring-security-config-location";

    /** default logger: org.openspaces.security.spring.level = INFO */
    private static final Logger logger = Logger.getLogger(SecurityManager.class.getPackage().getName());
            
    private ApplicationContext applicationContext;
	private AuthenticationManager authenticationManager;


	/**
	 * Initialize the security manager using the spring security configuration.
	 */
	public void init(Properties properties) throws SecurityException {
		String configLocation = properties.getProperty(SPRING_SECURITY_CONFIG_LOCATION, "security-config.xml");
		if (logger.isLoggable(Level.CONFIG)) {
		    logger.config("spring-security-config-location: " + configLocation + ", absolute path: " + new File(configLocation).getAbsolutePath());
		}
		
		/*
		 * Extract Spring AuthenticationManager definition
		 */
		applicationContext = new FileSystemXmlApplicationContext(configLocation);
		Map<String, AuthenticationManager> beansOfType = applicationContext.getBeansOfType(AuthenticationManager.class);
		if (beansOfType.isEmpty()) {
		    throw new SecurityException("No bean of type '"+AuthenticationManager.class.getName()+"' is defined in " + configLocation);
		}
		if (beansOfType.size() > 1) {
		    throw new SecurityException("More than one bean of type '"+AuthenticationManager.class.getName()+"' is defined in " + configLocation);
		}
		authenticationManager = beansOfType.values().iterator().next();
	}

    /**
     * Attempts to authenticate the passed {@link UserDetails} object, returning a fully populated
     * {@link Authentication} object (including granted authorities) if successful.
     * <p>
     * The API call is delegated to the configured
     * {@link org.springframework.security.authentication.AuthenticationManager}, using a
     * {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken} ,
     * converting the returned {@link org.springframework.security.core.Authentication} object
     * (including fully populated granted authorities) to the GigaSpaces {@link Authentication} object.
     * 
     * @param userDetails The GigaSpaces user details request object
     * @return a fully authenticated object including authorities 
     * @throws AuthenticationException if authentication fails
     */
	public Authentication authenticate(UserDetails userDetails)
			throws AuthenticationException {
		try {
			org.springframework.security.core.Authentication authenticate = authenticationManager.authenticate(createAuthenticationRequest(userDetails));
			if (!authenticate.isAuthenticated()) {
				throw new AuthenticationException("Authentication failed for user ["+userDetails.getUsername()+"]");
			}

			Collection<? extends GrantedAuthority> grantedAuthorities = authenticate.getAuthorities();
			ArrayList<Authority> authoritiesList = new ArrayList<Authority>(grantedAuthorities.size());
			for (GrantedAuthority grantedAuthority : grantedAuthorities) {
			    Authority gsAuthority = AuthorityFactory.create(grantedAuthority.getAuthority().trim());
			    authoritiesList.add(gsAuthority);
			}


			User user = new User(userDetails.getUsername(), userDetails.getPassword(), authoritiesList.toArray(new Authority[authoritiesList.size()]));
			Authentication authentication = new Authentication(user);
			return authentication;

		} catch(Exception exception) {
		    if (logger.isLoggable(Level.FINEST)) {
		        logger.log(Level.FINEST, "Caught exception upon authentication: " + exception, exception);
		    }
			throw new AuthenticationException(exception);
		}
	}

    /**
     * Creates an {@link org.springframework.security.core.Authentication} request object to be
     * passed to the
     * {@link AuthenticationManager#authenticate(org.springframework.security.core.Authentication)}
     * method on each call to {@link #authenticate(UserDetails)}.
     * <p>
     * This method can be overridden by subclasses which require authentication request other than
     * the default {@link UsernamePasswordAuthenticationToken}.
     * 
     * @param userDetails
     *            The GigaSpaces user details request object
     * @return an authentication request object
     */
    protected org.springframework.security.core.Authentication createAuthenticationRequest(UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), userDetails.getPassword());
    }

	/**
	 * Closes the Spring application context using {@link ConfigurableApplicationContext#close()}.
	 */
	public void close() {
		if (applicationContext instanceof ConfigurableApplicationContext) {
		    ((ConfigurableApplicationContext)applicationContext).close();
		}
	}

    /**
     * Throws a {@link DirectoryAccessDeniedException} on any attempt to manage the users/roles
     * using this API.
     */
	public DirectoryManager createDirectoryManager(UserDetails userDetails)
			throws AuthenticationException, AccessDeniedException {
		throw new DirectoryAccessDeniedException(
				"user/role information should be managed by a compatible external directory tools.");
	}
}
