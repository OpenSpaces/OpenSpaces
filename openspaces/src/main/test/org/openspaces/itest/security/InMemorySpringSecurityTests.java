package org.openspaces.itest.security;

import java.util.Properties;

import org.openspaces.security.spring.SpringSecurityManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.gigaspaces.security.Authentication;
import com.gigaspaces.security.AuthenticationException;
import com.gigaspaces.security.authorities.SpaceAuthority.SpacePrivilege;
import com.gigaspaces.security.directory.User;

/**
 * Tests that the Spring-Security bridge can load a Spring security configuration file (in-memory)
 * and extract the credentials and authorities correctly.
 * 
 * @author Moran Avigdor
 */
public class InMemorySpringSecurityTests extends AbstractDependencyInjectionSpringContextTests {
    
    private SpringSecurityManager securityManager;
 
    @Override
    protected void onSetUp() throws Exception {
        Properties props = new Properties();
        props.setProperty(SpringSecurityManager.SPRING_SECURITY_CONFIG_LOCATION, "classpath:/org/openspaces/itest/security/in-memory-security-config.xml");
        securityManager = new SpringSecurityManager();
        securityManager.init(props);
        super.onSetUp();
    }
    
    public void testSuccessfulAuthentication() {
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
    
    public void testUnknownUserAuthentication() {
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
