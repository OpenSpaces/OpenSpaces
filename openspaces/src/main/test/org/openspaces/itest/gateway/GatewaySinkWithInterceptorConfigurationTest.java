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
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.gigaspaces.sync.SyncEndPointInterceptor;

/**
 * Test Sink component spring configuration
 * 
 * @author idan
 * @since 8.0.3
 *
 */
@SuppressWarnings("deprecation")
public class GatewaySinkWithInterceptorConfigurationTest extends AbstractDependencyInjectionSpringContextTests {

    public GatewaySinkWithInterceptorConfigurationTest() {
        setPopulateProtectedVariables(true);
    }
    
    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/gateway/sinkinterceptor.xml"};
    }
    
    protected GatewaySinkFactoryBean sink;
    
    public void testClusterConfiguration() throws SecurityException, NoSuchFieldException {
        SyncEndPointInterceptor interceptor = sink.getSyncEndpointInterceptorConfiguration().getInterceptor();
        assertNotNull(interceptor);
        assertTrue(interceptor instanceof MySyncEndPointInterceptor);
    }



    
}
