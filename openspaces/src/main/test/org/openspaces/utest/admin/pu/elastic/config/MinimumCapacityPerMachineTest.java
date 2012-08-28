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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsConfigurer;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.admin.zone.config.ExactZonesConfigurer;
import org.openspaces.admin.zone.config.ZonesConfig;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaUtils;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerZones;

/**
 * @author Itai Frenkel
 * @since 9.1.0
 */
public class MinimumCapacityPerMachineTest extends TestCase {

    private static final ExactZonesConfig EXACT_ZONE1 = new ExactZonesConfigurer().addZone("zone1").create();
    private static final ExactZonesConfig EXACT_ZONE2 = new ExactZonesConfigurer().addZone("zone2").create();
    private static final Set<ZonesConfig> ZONES = new HashSet<ZonesConfig>(Arrays.asList(new ZonesConfig[] {EXACT_ZONE1,EXACT_ZONE2}));

    private static final CapacityRequirements ZERO_CAPACITY = new CapacityRequirements();
    private static final CapacityRequirements ONE_CAPACITY = new CapacityRequirementsConfigurer().memoryCapacity("100m").create().toCapacityRequirements();
    private static final CapacityRequirements TWO_CAPACITY = ONE_CAPACITY.multiply(2);
    private static final CapacityRequirements THREE_CAPACITY = ONE_CAPACITY.multiply(3);
    private static final CapacityRequirements FOUR_CAPACITY = ONE_CAPACITY.multiply(4);
    private static final CapacityRequirements SIX_CAPACITY = ONE_CAPACITY.multiply(6);
    
    /**
     * minPerZone=0
     * totalMin=4
     * last= {Zone1:1 , Zone2:2} 
     * expected result zone1:2
     */
    @Test
    public void testBelowMinimumWithoutNewCapacity() {
        
        CapacityRequirements totalMin = FOUR_CAPACITY;
        CapacityRequirements minPerZone = ZERO_CAPACITY;
        
        CapacityRequirementsPerZones lastEnforcedPlannedCapacity = 
                new CapacityRequirementsPerZones()
                .add(EXACT_ZONE1, ONE_CAPACITY)
                .add(EXACT_ZONE2, TWO_CAPACITY);
        
        CapacityRequirementsPerZones newPlannedCapacityPerZones = new CapacityRequirementsPerZones();
        
        CapacityRequirements newZone1Capacity = AutoScalingSlaUtils.getMinimumCapacity(totalMin, minPerZone, lastEnforcedPlannedCapacity, newPlannedCapacityPerZones, EXACT_ZONE1, ZONES);
        Assert.assertEquals(TWO_CAPACITY,newZone1Capacity);
    }

    
    /**
     * minPerZone=0
     * totalMin=6
     * last= {Zone1:4 , Zone2:2} 
     * new = {Zone1:3}
     * expected result zone2:3
     */
    @Test
    public void testBelowMinimumWithNewCapacity() {
        CapacityRequirements totalMin = SIX_CAPACITY;
        CapacityRequirements minPerZone = ZERO_CAPACITY;
        
        CapacityRequirementsPerZones lastEnforcedPlannedCapacity = 
                new CapacityRequirementsPerZones()
                .add(EXACT_ZONE1, FOUR_CAPACITY)
                .add(EXACT_ZONE2, TWO_CAPACITY);
        
        CapacityRequirementsPerZones newPlannedCapacityPerZones = 
                new CapacityRequirementsPerZones()
                .add(EXACT_ZONE1, THREE_CAPACITY);
        
        CapacityRequirements newZone2Capacity = AutoScalingSlaUtils.getMinimumCapacity(totalMin, minPerZone, lastEnforcedPlannedCapacity, newPlannedCapacityPerZones, EXACT_ZONE2, ZONES);
        Assert.assertEquals(THREE_CAPACITY,newZone2Capacity);
    }
    
    /**
     * minPerZone=3
     * totalMin=0
     * last= {Zone1:3 , Zone2:2} 
     * new = {Zone1:4}
     * expected result zone2:3
     */
    @Test
    public void testAboveMinimumAndLast() {
        CapacityRequirements totalMin = ZERO_CAPACITY;
        CapacityRequirements minPerZone = THREE_CAPACITY;
        
        CapacityRequirementsPerZones lastEnforcedPlannedCapacity = 
                new CapacityRequirementsPerZones()
                .add(EXACT_ZONE1, THREE_CAPACITY)
                .add(EXACT_ZONE2, TWO_CAPACITY);
        
        CapacityRequirementsPerZones newPlannedCapacityPerZones = 
                new CapacityRequirementsPerZones()
                .add(EXACT_ZONE1, FOUR_CAPACITY);
        
        CapacityRequirements newZone2Capacity = AutoScalingSlaUtils.getMinimumCapacity(totalMin, minPerZone, lastEnforcedPlannedCapacity, newPlannedCapacityPerZones, EXACT_ZONE2, ZONES);
        Assert.assertEquals(THREE_CAPACITY,newZone2Capacity);
    }
    
    
    /**
     * minPerZone=6
     * totalMin=0
     * last= {Zone1:3 , Zone2:2} 
     * new = {Zone1:4}
     * expected result zone2:6
     */
    @Test
    public void testMinimumPerZone() {
        CapacityRequirements totalMin = ZERO_CAPACITY;
        CapacityRequirements minPerZone = SIX_CAPACITY;
        
        CapacityRequirementsPerZones lastEnforcedPlannedCapacity = 
                new CapacityRequirementsPerZones()
                .add(EXACT_ZONE1, THREE_CAPACITY)
                .add(EXACT_ZONE2, TWO_CAPACITY);
        
        CapacityRequirementsPerZones newPlannedCapacityPerZones = 
                new CapacityRequirementsPerZones()
                .add(EXACT_ZONE1, FOUR_CAPACITY);
        
        CapacityRequirements newZone2Capacity = AutoScalingSlaUtils.getMinimumCapacity(totalMin, minPerZone, lastEnforcedPlannedCapacity, newPlannedCapacityPerZones, EXACT_ZONE2, ZONES);
        Assert.assertEquals(SIX_CAPACITY,newZone2Capacity);
    }
}
