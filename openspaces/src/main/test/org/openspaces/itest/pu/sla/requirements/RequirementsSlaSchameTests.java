package org.openspaces.itest.pu.sla.requirements;

import org.openspaces.pu.sla.SLA;
import org.openspaces.pu.sla.requirement.HostRequirement;
import org.openspaces.pu.sla.requirement.SystemRequirement;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class RequirementsSlaSchameTests extends AbstractDependencyInjectionSpringContextTests {

    public RequirementsSlaSchameTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/pu/sla/requirements/requirements.xml"};
    }

    public void testRequirements() {
        SLA sla = (SLA) getApplicationContext().getBean("SLA");
        assertNotNull(sla);
        assertNotNull(sla.getRequirements());
        assertEquals(2, sla.getRequirements().size());
        HostRequirement host = (HostRequirement) sla.getRequirements().get(0);
        assertEquals("test", host.getHost());
        SystemRequirement system = (SystemRequirement) sla.getRequirements().get(1);
        assertEquals("test2", system.getName());
        assertNotNull(system.getAttributes());
    }
}