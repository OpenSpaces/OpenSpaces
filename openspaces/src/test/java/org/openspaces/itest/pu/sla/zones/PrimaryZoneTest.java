/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.itest.pu.sla.zones;

import org.openspaces.pu.sla.SLA;
import org.openspaces.pu.sla.requirement.ZoneRequirement;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author anna
 */
public class PrimaryZoneTest extends AbstractDependencyInjectionSpringContextTests {

    public PrimaryZoneTest() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/pu/sla/zones/zones.xml"};
    }

    public void testZones() {
        SLA sla = (SLA) getApplicationContext().getBean("SLA");
        assertNotNull(sla);
        assertEquals("zone2", sla.getPrimaryZone());
        assertNotNull(sla.getRequirements());
        assertEquals(2, sla.getRequirements().size());
        ZoneRequirement zone1 = (ZoneRequirement) sla.getRequirements().get(0);
        assertEquals("zone1", zone1.getZone());
        ZoneRequirement zone2 = (ZoneRequirement) sla.getRequirements().get(1);
        assertEquals("zone2", zone2.getZone());
    }
}