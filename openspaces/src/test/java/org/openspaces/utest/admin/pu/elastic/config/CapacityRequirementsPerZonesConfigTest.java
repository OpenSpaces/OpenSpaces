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
package org.openspaces.utest.admin.pu.elastic.config;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsConfig;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsConfigurer;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsPerZonesConfig;
import org.openspaces.admin.zone.config.AtLeastOneZoneConfig;
import org.openspaces.admin.zone.config.AtLeastOneZoneConfigurer;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.admin.zone.config.ExactZonesConfigurer;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerZones;

/**
 * Tests {@link CapacityRequirementsPerZoneConfig}
 * @author Itai Frenkel
 * @since 9.1.0
 */
public class CapacityRequirementsPerZonesConfigTest extends TestCase {
    
    private static final ExactZonesConfig ZONES1 = new ExactZonesConfigurer().addZone("zone1").create();
    private static final ExactZonesConfig ZONES2 = new ExactZonesConfigurer().addZone("zone2").create();
    private static final ExactZonesConfig ZONES3 = new ExactZonesConfigurer().addZones("zone1","zone2").create();
    private static final CapacityRequirementsConfig ZONES1_CAPACITY = new CapacityRequirementsConfigurer().memoryCapacity("100m").create();
    private static final CapacityRequirementsConfig ZONES2_CAPACITY = new CapacityRequirementsConfigurer().memoryCapacity("100m").create();
    private static final CapacityRequirementsConfig ZONES3_CAPACITY = new CapacityRequirementsConfigurer().memoryCapacity("300m").create();

    private static final AtLeastOneZoneConfig CLOUDIFY_SERVICE_ZONES=new AtLeastOneZoneConfigurer().addZone("petclinic.tomcat").create();
    private static final CapacityRequirementsConfig CLOUDIFY_SERVICE_CAPACITY = new CapacityRequirementsConfigurer().memoryCapacity("476m").create();
    
    @Test
    public void testAdd() {
        Map<String, String> properties = new HashMap<String,String>();
        properties.put("hello","world");
        CapacityRequirementsPerZonesConfig config = new CapacityRequirementsPerZonesConfig("zones.", properties);
                config.addCapacity(ZONES1, ZONES1_CAPACITY);
        config.addCapacity(ZONES2, ZONES2_CAPACITY);
        config.addCapacity(ZONES2, ZONES2_CAPACITY);
        config.addCapacity(ZONES3, ZONES3_CAPACITY);
        CapacityRequirementsPerZones expected = 
                new CapacityRequirementsPerZones()
                .add(ZONES1, ZONES1_CAPACITY.toCapacityRequirements())
                .add(ZONES2, ZONES2_CAPACITY.toCapacityRequirements().multiply(2))
                .add(ZONES3, ZONES3_CAPACITY.toCapacityRequirements());
        Assert.assertEquals(expected, config.toCapacityRequirementsPerZones());
        Assert.assertEquals("world", properties.get("hello"));
    }
    
    @Test
    public void testCloudifyZoneWithDot() {
        CapacityRequirementsPerZonesConfig config = new CapacityRequirementsPerZonesConfig("zones.", new HashMap<String,String>());
        config.addCapacity(CLOUDIFY_SERVICE_ZONES, CLOUDIFY_SERVICE_CAPACITY);
        CapacityRequirementsPerZones expected = 
                new CapacityRequirementsPerZones()
                .add(CLOUDIFY_SERVICE_ZONES, CLOUDIFY_SERVICE_CAPACITY.toCapacityRequirements());
        Assert.assertEquals(expected, config.toCapacityRequirementsPerZones());
    }
    
}
