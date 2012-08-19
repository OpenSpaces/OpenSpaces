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
package org.openspaces.utest.grid.gsm;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerAgent;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.capacity.DriveCapacityRequirement;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;

public class CapacityRequirementsPerAgentTest extends TestCase {

    private static final CapacityRequirements DRIVE = new CapacityRequirements(new DriveCapacityRequirement("d:",1L));
    private static final CapacityRequirements CPU = new CapacityRequirements(new CpuCapacityRequirement(Fraction.ONE));
    private static final CapacityRequirements MEMORY = new CapacityRequirements(new MemoryCapacityRequirement(1L));

    @Test
    public void testEqualsZero() {
        CapacityRequirementsPerAgent ac1 = new CapacityRequirementsPerAgent();
        Assert.assertTrue(ac1.equalsZero());
        
        CapacityRequirementsPerAgent ac2 = ac1.add("UUID1", MEMORY);
        CapacityRequirementsPerAgent ac3 = ac1.add("UUID1", CPU);
        CapacityRequirementsPerAgent ac4 = ac1.add("UUID1", DRIVE);
        Assert.assertFalse(ac2.equalsZero());
        Assert.assertFalse(ac3.equalsZero());
        Assert.assertFalse(ac4.equalsZero());
        
    }
    
    @Test
    public void testAdd() {
        CapacityRequirementsPerAgent ac1 = new CapacityRequirementsPerAgent();
        CapacityRequirementsPerAgent ac2 = ac1.add("UUID1", MEMORY.add(DRIVE));
        CapacityRequirementsPerAgent ac3 = ac2.add("UUID1", CPU);
        CapacityRequirementsPerAgent expectedAc3 = ac1.add("UUID1",MEMORY.add(DRIVE).add(CPU));
        Assert.assertEquals(expectedAc3,ac3);
        
        CapacityRequirementsPerAgent ac4 = ac3.add(ac3);
        CapacityRequirementsPerAgent expectedAc4 = ac1.add("UUID1",MEMORY.add(DRIVE).add(CPU).multiply(2));
        Assert.assertEquals(expectedAc4,ac4);
        
        CapacityRequirementsPerAgent ac5 = ac3.add( "UUID2", MEMORY.add(DRIVE).add(CPU).multiply(2));
        Assert.assertEquals(MEMORY.add(DRIVE).add(CPU),ac5.getAgentCapacity("UUID1"));
        Assert.assertEquals(MEMORY.add(DRIVE).add(CPU).multiply(2),ac5.getAgentCapacity("UUID2"));
        Assert.assertEquals(MEMORY.add(DRIVE).add(CPU),ac5.getAgentCapacityOrZero("UUID1"));
        Assert.assertEquals(MEMORY.add(DRIVE).add(CPU).multiply(2),ac5.getAgentCapacityOrZero("UUID2"));
        Assert.assertTrue(ac5.getAgentCapacityOrZero("UUID3").equalsZero());
        
        Assert.assertEquals(MEMORY.add(DRIVE).add(CPU).multiply(3),ac5.getTotalAllocatedCapacity());
    }
    
    @Test
    public void testSubtract() {
        CapacityRequirementsPerAgent ac = 
            new CapacityRequirementsPerAgent().add( 
                "UUID1", MEMORY.add(DRIVE).add(CPU));

        Assert.assertTrue(
                ac.subtract("UUID1", MEMORY.add(DRIVE).add(CPU))
                .equalsZero());
        
        Assert.assertTrue(ac.subtract(ac).equalsZero());
    }

    @Test
    public void testSubtractWrongUuid() {
        try {
            CapacityRequirementsPerAgent ac = 
                new CapacityRequirementsPerAgent().add("UUID1", MEMORY.add(DRIVE).add(CPU));
    
            ac.subtract("UUID2", MEMORY.add(DRIVE).add(CPU));
            Assert.fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            
        }
    }
}
