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
package org.openspaces.itest.security;

import com.gigaspaces.security.Authentication;
import com.gigaspaces.security.AuthenticationException;
import com.gigaspaces.security.authorities.SpaceAuthority.SpacePrivilege;
import com.gigaspaces.security.directory.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.security.spring.SpringSecurityManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Tests that the Spring-Security bridge can load a Spring security configuration file (in-memory)
 * and extract the credentials and authorities correctly.
 * 
 * @author Moran Avigdor
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/security/in-memory-security-config.xml")

public class InMemorySpringSecurityTests   { 
    
    private SpringSecurityManager securityManager;
 
     @Before public  void onSetUp() throws Exception {
        Properties props = new Properties();
        props.setProperty(SpringSecurityManager.SPRING_SECURITY_CONFIG_LOCATION, "classpath:/org/openspaces/itest/security/in-memory-security-config.xml");
        securityManager = new SpringSecurityManager();
        securityManager.init(props);
    }
    
     @Test public void testSuccessfulAuthentication() {
        Authentication authenticate = securityManager.authenticate(new User("Edward", "koala"));
        assertNotNull(authenticate);
        assertTrue("Should be granted READ authority", authenticate.getGrantedAuthorities().isGranted(SpacePrivilege.READ));
        assertTrue("Should be granted READ authority", authenticate.getGrantedAuthorities().isGranted(SpacePrivilege.READ, "eg.cinema.Movie"));
        assertTrue("Should be granted READ authority", authenticate.getGrantedAuthorities().isGranted(SpacePrivilege.READ, "eg.cinema.Seat"));
        assertFalse("Should be granted WRITE authority", authenticate.getGrantedAuthorities().isGranted(SpacePrivilege.WRITE, "eg.cinema.Dummy"));
        
        assertTrue("Should be granted WRITE authority", authenticate.getGrantedAuthorities().isGranted(SpacePrivilege.WRITE));
        assertTrue("Should be granted WRITE authority", authenticate.getGrantedAuthorities().isGranted(SpacePrivilege.WRITE, "eg.cinema.Seat"));
        assertFalse("Should be granted WRITE authority", authenticate.getGrantedAuthorities().isGranted(SpacePrivilege.WRITE, "eg.cinema.Movie"));
    }
    
     @Test public void testUnknownUserAuthentication() {
        try {
            securityManager.authenticate(new User("John", "Doe"));
            fail("Should have thrown authentication exception");
        } catch(AuthenticationException ae) {
            assertTrue(
                    "Expected com.gigaspaces.security.AuthenticationException: org.springframework.security.authentication.BadCredentialsException: Bad credentials",
                    ae.getCause() instanceof BadCredentialsException);
        }
    }
}

