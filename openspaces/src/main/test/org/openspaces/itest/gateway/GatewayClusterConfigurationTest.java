package org.openspaces.itest.gateway;

import junit.framework.Assert;

import org.openspaces.core.gateway.GatewayTargetsFactoryBean;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.gigaspaces.internal.cluster.node.impl.gateway.GatewaysPolicy;

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
        Assert.assertEquals(2L, policy.getGatewayPolicies()[0].getIntervalMilis());
        Assert.assertEquals(3, policy.getGatewayPolicies()[0].getIntervalOperations());
        Assert.assertEquals(4L, policy.getGatewayPolicies()[0].getMaxRedoLogCapacity());
        //
        Assert.assertEquals(10, policy.getGatewayPolicies()[1].getBulkSize());
        Assert.assertEquals(20L, policy.getGatewayPolicies()[1].getIntervalMilis());
        Assert.assertEquals(30, policy.getGatewayPolicies()[1].getIntervalOperations());
        Assert.assertEquals(40L, policy.getGatewayPolicies()[1].getMaxRedoLogCapacity());
        //
        Assert.assertEquals(9991, policy.getGatewayPolicies()[2].getBulkSize());
        Assert.assertEquals(200L, policy.getGatewayPolicies()[2].getIntervalMilis());
        Assert.assertEquals(GatewaysPolicy.INTERVAL_OPERS_DEFAULT, policy.getGatewayPolicies()[2].getIntervalOperations());
        Assert.assertEquals(400L, policy.getGatewayPolicies()[2].getMaxRedoLogCapacity());
        //
        Assert.assertEquals(9991, policy.getGatewayPolicies()[3].getBulkSize());
        Assert.assertEquals(9992L, policy.getGatewayPolicies()[3].getIntervalMilis());
        Assert.assertEquals(GatewaysPolicy.INTERVAL_OPERS_DEFAULT, policy.getGatewayPolicies()[3].getIntervalOperations());
        Assert.assertEquals(9994L, policy.getGatewayPolicies()[3].getMaxRedoLogCapacity());
    }


    
}
