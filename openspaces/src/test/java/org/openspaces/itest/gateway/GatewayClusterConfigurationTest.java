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

import junit.framework.Assert;

import org.openspaces.core.gateway.GatewayTargetsFactoryBean;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.gigaspaces.internal.cluster.node.impl.gateway.GatewaysPolicy;
import com.j_spaces.core.cluster.RedoLogCapacityExceededPolicy;

/**
 * Test "os-gateway:gateway-targets" element parsing & conversion to {@link GatewaysPolicy} object.
 * Each "gateway-target" element should inherit its attributes values from the parent element if not
 * explicitly overridden.
 * 
 * 
 * @author idan
 * @since 8.0.3
 *
 */
public class GatewayClusterConfigurationTest extends AbstractDependencyInjectionSpringContextTests {

    public GatewayClusterConfigurationTest() {
        setPopulateProtectedVariables(true);
    }
    
    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/gateway/cluster.xml"};
    }
    
    protected GatewayTargetsFactoryBean gatewayTargets;
    
    public void testClusterConfiguration() {
        Assert.assertNotNull(gatewayTargets);
        assertGatewaysPolicy(gatewayTargets.asGatewaysPolicy());
    }

    private void assertGatewaysPolicy(GatewaysPolicy policy) {
        Assert.assertEquals(4, policy.getGatewayPolicies().length);
        //
        Assert.assertEquals(1, policy.getGatewayPolicies()[0].getBulkSize());
        Assert.assertEquals(2L, policy.getGatewayPolicies()[0].getIdleTimeThreshold());
        Assert.assertEquals(3, policy.getGatewayPolicies()[0].getPendingOperationThreshold());
        Assert.assertEquals(4L, policy.getGatewayPolicies()[0].getMaxRedoLogCapacity());
        Assert.assertEquals(RedoLogCapacityExceededPolicy.DROP_OLDEST, policy.getGatewayPolicies()[0].getOnRedoLogCapacityExceeded());
        //
        Assert.assertEquals(10, policy.getGatewayPolicies()[1].getBulkSize());
        Assert.assertEquals(20L, policy.getGatewayPolicies()[1].getIdleTimeThreshold());
        Assert.assertEquals(30, policy.getGatewayPolicies()[1].getPendingOperationThreshold());
        Assert.assertEquals(40L, policy.getGatewayPolicies()[1].getMaxRedoLogCapacity());
        Assert.assertEquals(RedoLogCapacityExceededPolicy.DROP_OLDEST, policy.getGatewayPolicies()[1].getOnRedoLogCapacityExceeded());
        //
        Assert.assertEquals(9991, policy.getGatewayPolicies()[2].getBulkSize());
        Assert.assertEquals(200L, policy.getGatewayPolicies()[2].getIdleTimeThreshold());
        Assert.assertEquals(GatewaysPolicy.PENDING_OPERATION_THRESHOLD_DEFAULT, policy.getGatewayPolicies()[2].getPendingOperationThreshold());
        Assert.assertEquals(400L, policy.getGatewayPolicies()[2].getMaxRedoLogCapacity());
        Assert.assertEquals(RedoLogCapacityExceededPolicy.DROP_OLDEST, policy.getGatewayPolicies()[2].getOnRedoLogCapacityExceeded());
        //
        Assert.assertEquals(9991, policy.getGatewayPolicies()[3].getBulkSize());
        Assert.assertEquals(9992L, policy.getGatewayPolicies()[3].getIdleTimeThreshold());
        Assert.assertEquals(GatewaysPolicy.PENDING_OPERATION_THRESHOLD_DEFAULT, policy.getGatewayPolicies()[3].getPendingOperationThreshold());
        Assert.assertEquals(-1L, policy.getGatewayPolicies()[3].getMaxRedoLogCapacity());
        Assert.assertEquals(RedoLogCapacityExceededPolicy.BLOCK_OPERATIONS, policy.getGatewayPolicies()[3].getOnRedoLogCapacityExceeded());
    }


    
}
