package org.openspaces.utest.grid.gsm;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.ClusterCapacityRequirements;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.capacity.DriveCapacityRequirement;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;

public class ClusterCapacityRequirementsTest extends TestCase {

    private static final CapacityRequirements DRIVE = new CapacityRequirements(new DriveCapacityRequirement("d:",1L));
    private static final CapacityRequirements CPU = new CapacityRequirements(new CpuCapacityRequirement(Fraction.ONE));
    private static final CapacityRequirements MEMORY = new CapacityRequirements(new MemoryCapacityRequirement(1L));

    @Test
    public void testEqualsZero() {
        ClusterCapacityRequirements ac1 = new ClusterCapacityRequirements();
        Assert.assertTrue(ac1.equalsZero());
        
        ClusterCapacityRequirements ac2 = ac1.add("UUID1", MEMORY);
        ClusterCapacityRequirements ac3 = ac1.add("UUID1", CPU);
        ClusterCapacityRequirements ac4 = ac1.add("UUID1", DRIVE);
        Assert.assertFalse(ac2.equalsZero());
        Assert.assertFalse(ac3.equalsZero());
        Assert.assertFalse(ac4.equalsZero());
        
    }
    
    @Test
    public void testAdd() {
        ClusterCapacityRequirements ac1 = new ClusterCapacityRequirements();
        ClusterCapacityRequirements ac2 = ac1.add("UUID1", MEMORY.add(DRIVE));
        ClusterCapacityRequirements ac3 = ac2.add("UUID1", CPU);
        ClusterCapacityRequirements expectedAc3 = ac1.add("UUID1",MEMORY.add(DRIVE).add(CPU));
        Assert.assertEquals(expectedAc3,ac3);
        
        ClusterCapacityRequirements ac4 = ac3.add(ac3);
        ClusterCapacityRequirements expectedAc4 = ac1.add("UUID1",MEMORY.add(DRIVE).add(CPU).multiply(2));
        Assert.assertEquals(expectedAc4,ac4);
        
        ClusterCapacityRequirements ac5 = ac3.add( "UUID2", MEMORY.add(DRIVE).add(CPU).multiply(2));
        Assert.assertEquals(MEMORY.add(DRIVE).add(CPU),ac5.getAgentCapacity("UUID1"));
        Assert.assertEquals(MEMORY.add(DRIVE).add(CPU).multiply(2),ac5.getAgentCapacity("UUID2"));
        Assert.assertEquals(MEMORY.add(DRIVE).add(CPU),ac5.getAgentCapacityOrZero("UUID1"));
        Assert.assertEquals(MEMORY.add(DRIVE).add(CPU).multiply(2),ac5.getAgentCapacityOrZero("UUID2"));
        Assert.assertTrue(ac5.getAgentCapacityOrZero("UUID3").equalsZero());
        
        Assert.assertEquals(MEMORY.add(DRIVE).add(CPU).multiply(3),ac5.getTotalAllocatedCapacity());
    }
    
    @Test
    public void testSubtract() {
        ClusterCapacityRequirements ac = 
            new ClusterCapacityRequirements().add( 
                "UUID1", MEMORY.add(DRIVE).add(CPU));

        Assert.assertTrue(
                ac.subtract("UUID1", MEMORY.add(DRIVE).add(CPU))
                .equalsZero());
        
        Assert.assertTrue(ac.subtract(ac).equalsZero());
    }

    @Test
    public void testSubtractWrongUuid() {
        try {
            ClusterCapacityRequirements ac = 
                new ClusterCapacityRequirements().add("UUID1", MEMORY.add(DRIVE).add(CPU));
    
            ac.subtract("UUID2", MEMORY.add(DRIVE).add(CPU));
            Assert.fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            
        }
    }
}
