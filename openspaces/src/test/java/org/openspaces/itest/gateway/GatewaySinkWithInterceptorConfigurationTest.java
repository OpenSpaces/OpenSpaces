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

import com.gigaspaces.sync.SynchronizationEndpointInterceptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.gateway.GatewaySinkFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test Sink component spring configuration
 * 
 * @author idan
 * @since 8.0.3
 *
 */
@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/gateway/sinkinterceptor.xml")
public class GatewaySinkWithInterceptorConfigurationTest   { 

    public GatewaySinkWithInterceptorConfigurationTest() {
 
    }
    
    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/gateway/sinkinterceptor.xml"};
    }
    
     @Autowired protected GatewaySinkFactoryBean sink;
    
     @Test public void testClusterConfiguration() throws SecurityException, NoSuchFieldException {
        SynchronizationEndpointInterceptor interceptor = sink.getSyncEndpointInterceptorConfiguration().getInterceptor();
        assertNotNull(interceptor);
        assertTrue(interceptor instanceof MySyncEndPointInterceptor);
    }



    
}

