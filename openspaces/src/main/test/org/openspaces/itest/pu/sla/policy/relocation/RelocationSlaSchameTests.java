package org.openspaces.itest.pu.sla.policy.relocation;

import org.openspaces.pu.sla.RelocationPolicy;
import org.openspaces.pu.sla.SLA;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class RelocationSlaSchameTests extends AbstractDependencyInjectionSpringContextTests {

    public RelocationSlaSchameTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/pu/sla/policy/relocation/relocation-policy.xml"};
    }


    public void testRelocationSchema() {
        SLA sla = (SLA) getApplicationContext().getBean("SLA");
        assertNotNull(sla);
        assertNotNull(sla.getPolicy());
        assertTrue(sla.getPolicy() instanceof RelocationPolicy);
        RelocationPolicy relocationPolicy = (RelocationPolicy) sla.getPolicy();
        assertEquals("test", relocationPolicy.getMonitor());
        assertEquals(0.2, relocationPolicy.getLow());
        assertEquals(0.8, relocationPolicy.getHigh());
    }
}