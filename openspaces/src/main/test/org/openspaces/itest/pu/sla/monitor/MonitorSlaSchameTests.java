package org.openspaces.itest.pu.sla.monitor;

import org.openspaces.pu.sla.SLA;
import org.openspaces.pu.sla.monitor.BeanPropertyMonitor;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class MonitorSlaSchameTests extends AbstractDependencyInjectionSpringContextTests {

    public MonitorSlaSchameTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/pu/sla/monitor/monitor.xml"};
    }

    public void testMonitor() {
        SLA sla = (SLA) getApplicationContext().getBean("SLA");
        assertNotNull(sla);
        assertNotNull(sla.getMonitors());
        assertEquals(1, sla.getMonitors().size());
        BeanPropertyMonitor monitor = (BeanPropertyMonitor) sla.getMonitors().get(0);
        assertEquals("test", monitor.getName());
        assertEquals("bean", monitor.getRef());
        assertEquals("prop", monitor.getPropertyName());
    }
}