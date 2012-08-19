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
package org.openspaces.utest.admin.zones.config;

import junit.framework.TestCase;

import org.junit.Test;
import org.openspaces.admin.zone.config.AnyZonesConfig;
import org.openspaces.admin.zone.config.AtLeastOneZoneConfig;
import org.openspaces.admin.zone.config.AtLeastOneZoneConfigurer;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.admin.zone.config.ExactZonesConfigurer;

/**
 * Tests {@link CapacityRequirementsPerZoneConfig}
 * @author Itai Frenkel
 * @since 9.1.0
 */
public class ZonesConfigTest extends TestCase {
    
    private static final ExactZonesConfig NO_ZONE = new ExactZonesConfig();
    private static final ExactZonesConfig EXACT_ZONE1 = new ExactZonesConfigurer().addZone("zone1").create();
    private static final ExactZonesConfig EXACT_ZONE2 = new ExactZonesConfigurer().addZone("zone2").create();
    private static final ExactZonesConfig EXACT_ZONE1_ZONE2 = new ExactZonesConfigurer().addZones("zone1","zone2").create();
    private static final AnyZonesConfig ANY_ZONE = new AnyZonesConfig();
    private static final AtLeastOneZoneConfig AT_LEAST_ZONE1 = new AtLeastOneZoneConfigurer().addZone("zone1").create();
    private static final AtLeastOneZoneConfig AT_LEAST_ZONE3 = new AtLeastOneZoneConfigurer().addZone("zone3").create();
    private static final AtLeastOneZoneConfig AT_LEAST_ZONE1_OR_ZONE3 = new AtLeastOneZoneConfigurer().addZones("zone1","zone3").create();
    
    @Test
    public void testAnyZoneConfig() {
        assertTrue(ANY_ZONE.isSatisfiedBy(EXACT_ZONE1));
        assertTrue(ANY_ZONE.isSatisfiedBy(EXACT_ZONE2));
        assertTrue(ANY_ZONE.isSatisfiedBy(EXACT_ZONE1_ZONE2));
        assertTrue(ANY_ZONE.isSatisfiedBy(NO_ZONE));
    }
    
    @Test
    public void testAtLeastOneZoneConfig() {
        assertTrue(AT_LEAST_ZONE1.isSatisfiedBy(EXACT_ZONE1));
        assertFalse(AT_LEAST_ZONE1.isSatisfiedBy(EXACT_ZONE2));
        assertTrue(AT_LEAST_ZONE1.isSatisfiedBy(EXACT_ZONE1_ZONE2));
        assertFalse(AT_LEAST_ZONE1.isSatisfiedBy(NO_ZONE));
        
        assertFalse(AT_LEAST_ZONE3.isSatisfiedBy(EXACT_ZONE1));
        assertFalse(AT_LEAST_ZONE3.isSatisfiedBy(EXACT_ZONE2));
        assertFalse(AT_LEAST_ZONE3.isSatisfiedBy(EXACT_ZONE1_ZONE2));
        assertFalse(AT_LEAST_ZONE3.isSatisfiedBy(NO_ZONE));
        
        assertTrue(AT_LEAST_ZONE1_OR_ZONE3.isSatisfiedBy(EXACT_ZONE1));
        assertFalse(AT_LEAST_ZONE1_OR_ZONE3.isSatisfiedBy(EXACT_ZONE2));
        assertTrue(AT_LEAST_ZONE1_OR_ZONE3.isSatisfiedBy(EXACT_ZONE1_ZONE2));
        assertFalse(AT_LEAST_ZONE1_OR_ZONE3.isSatisfiedBy(NO_ZONE));
    }
}
