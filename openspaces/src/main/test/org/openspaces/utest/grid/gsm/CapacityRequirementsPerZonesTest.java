/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.utest.grid.gsm;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsConfigurer;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsPerZonesConfig;
import org.openspaces.admin.zone.config.AnyZonesConfig;
import org.openspaces.admin.zone.config.AtLeastOneZoneConfig;
import org.openspaces.admin.zone.config.AtLeastOneZoneConfigurer;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.admin.zone.config.ExactZonesConfigurer;
import org.openspaces.admin.zone.config.ZonesConfig;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerZones;

/**
 * @author Itai Frenkel
 * @since 9.1.0
 * Tests {@link CapacityRequirementsPerZonesConfig} and {@link CapacityRequirementsPerZones} 
 */
public class CapacityRequirementsPerZonesTest extends TestCase {

    private static final ExactZonesConfig NO_ZONE = new ExactZonesConfig();
    private static final ExactZonesConfig EXACT_ZONE1 = new ExactZonesConfigurer().addZone("zone1").create();
    private static final ExactZonesConfig EXACT_ZONE1_ZONE2 = new ExactZonesConfigurer().addZones("zone1","zone2").create();
    private static final AnyZonesConfig ANY_ZONE = new AnyZonesConfig();
    private static final AtLeastOneZoneConfig AT_LEAST_ZONE1 = new AtLeastOneZoneConfigurer().addZone("zone1").create();
    private static final AtLeastOneZoneConfig AT_LEAST_ZONE1_OR_ZONE2 = new AtLeastOneZoneConfigurer().addZones("zone1","zone2").create();
    private static final CapacityRequirements capacity = new CapacityRequirementsConfigurer().memoryCapacity("1g").create().toCapacityRequirements();
    
    @Test
    public void testNoZones() {
        runTestForZones(NO_ZONE);
    }
/*
    @Test
    public void testExactZones() {
        runTestForZones(EXACT_ZONE1);
        runTestForZones(EXACT_ZONE1_ZONE2);
    }
    
    @Test
    public void testAnyZone() {
        runTestForZones(ANY_ZONE);
    }

    @Test
    public void testAtLeastZone() {
        runTestForZones(AT_LEAST_ZONE1);
        runTestForZones(AT_LEAST_ZONE1_OR_ZONE2);
    }
  */  
    private void runTestForZones(ZonesConfig zones) {
        CapacityRequirementsPerZones capacityPerZones = new CapacityRequirementsPerZones();
        capacityPerZones = capacityPerZones.add(zones, capacity);
        validateToConfig(capacityPerZones);
    }
    
    private void validateToConfig(CapacityRequirementsPerZones capacityPerZones) {
        Assert.assertEquals(
                capacityPerZones,
                new CapacityRequirementsPerZonesConfig(capacityPerZones).toCapacityRequirementsPerZones());
    }
}
