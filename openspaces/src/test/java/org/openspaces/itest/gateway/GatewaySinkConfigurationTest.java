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
package org.openspaces.itest.gateway;

import org.openspaces.core.gateway.GatewaySinkFactoryBean;
import org.openspaces.core.gateway.GatewaySinkServiceDetails;
import org.openspaces.core.gateway.SinkErrorHandlingFactoryBean;
import org.openspaces.pu.service.ServiceDetails;
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
        
        ServiceDetails[] serviceDetails = sink.getServicesDetails();
        assertEquals(1, serviceDetails.length);
        
        GatewaySinkServiceDetails sinkDetails = (GatewaySinkServiceDetails) serviceDetails[0];
        assertEquals(2, sinkDetails.getGatewaySourceNames().length);
        assertEquals("localGateway", sinkDetails.getGatewaySourceNames()[0]);        
        assertEquals("targetGateway", sinkDetails.getGatewaySourceNames()[1]);
    }



    
}
