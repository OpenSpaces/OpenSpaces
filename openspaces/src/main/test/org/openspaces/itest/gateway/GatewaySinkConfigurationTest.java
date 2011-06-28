package org.openspaces.itest.gateway;

import org.openspaces.core.gateway.GatewaySinkFactoryBean;
import org.openspaces.core.gateway.SinkErrorHandlingFactoryBean;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Test Sink component spring configuration
 * 
 * @author idan
 * @since 8.0.3
 *
 */
@SuppressWarnings("deprecation")
public class GatewaySinkConfigurationTest extends AbstractDependencyInjectionSpringContextTests {

    public GatewaySinkConfigurationTest() {
        setPopulateProtectedVariables(true);
    }
    
    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/gateway/sink.xml"};
    }
    
    protected GatewaySinkFactoryBean sink;
    
    public void testClusterConfiguration() throws SecurityException, NoSuchFieldException {
        SinkErrorHandlingFactoryBean error = sink.getErrorHandlingConfiguration();
        assertEquals(Integer.valueOf(5), error.getMaximumRetriesOnTransactionLock());
        assertEquals(Integer.valueOf(1000), error.getTransactionLockRetryInterval());
        assertTrue(error.getConflictResolver() instanceof MyConflictResolver);
        assertEquals(Long.valueOf(7500), sink.getTransactionTimeout());
        assertEquals(Long.valueOf(10), sink.getLocalSpaceLookupTimeout());
    }



    
}
