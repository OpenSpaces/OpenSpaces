package org.openspaces.utest.grid.gsm;

import junit.framework.Assert;

import org.junit.Test;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.grid.gsm.capacity.AggregatedAllocatedCapacity;
import org.openspaces.grid.gsm.capacity.AllocatedCapacity;

public class AggregatedAllocatedCapacityTest {

    @Test
    public void testEqualsZero() {
        AggregatedAllocatedCapacity ac1 = new AggregatedAllocatedCapacity();
        Assert.assertTrue(ac1.equalsZero());
        
        AggregatedAllocatedCapacity ac2 = AggregatedAllocatedCapacity.add(ac1, "UUID1", new AllocatedCapacity(Fraction.ZERO,1));
        AggregatedAllocatedCapacity ac3 = AggregatedAllocatedCapacity.add(ac1, "UUID1", new AllocatedCapacity(Fraction.ONE,0));
        Assert.assertFalse(ac2.equalsZero());
        Assert.assertFalse(ac3.equalsZero());
    }
    
    @Test
    public void testAdd() {
        AggregatedAllocatedCapacity ac1 = new AggregatedAllocatedCapacity();
        AggregatedAllocatedCapacity ac2 = AggregatedAllocatedCapacity.add(ac1, "UUID1", new AllocatedCapacity(Fraction.ZERO,1));
        AggregatedAllocatedCapacity ac3 = AggregatedAllocatedCapacity.add(ac2, "UUID1", new AllocatedCapacity(Fraction.ONE,0));
        AggregatedAllocatedCapacity expectedAc3 = AggregatedAllocatedCapacity.add(ac1,"UUID1",new AllocatedCapacity(Fraction.ONE,1));
        Assert.assertEquals(expectedAc3,ac3);
        
        AggregatedAllocatedCapacity ac4 = AggregatedAllocatedCapacity.add(ac3, ac3);
        AggregatedAllocatedCapacity expectedAc4 = AggregatedAllocatedCapacity.add(ac1,"UUID1",new AllocatedCapacity(Fraction.TWO,2));
        Assert.assertEquals(expectedAc4,ac4);
        
        AggregatedAllocatedCapacity ac5 = AggregatedAllocatedCapacity.add(ac3, "UUID2", new AllocatedCapacity(Fraction.TWO,2));
        Assert.assertEquals(new AllocatedCapacity(Fraction.ONE,1),ac5.getAgentCapacity("UUID1"));
        Assert.assertEquals(new AllocatedCapacity(Fraction.TWO,2),ac5.getAgentCapacity("UUID2"));
    }
    
    @Test
    public void testSubtract() {
        AggregatedAllocatedCapacity ac = 
            AggregatedAllocatedCapacity.add(new AggregatedAllocatedCapacity(), 
                "UUID1", new AllocatedCapacity(Fraction.ONE,1));

        Assert.assertTrue(
                AggregatedAllocatedCapacity.subtract(ac, "UUID1", new AllocatedCapacity(Fraction.ONE,1))
                .equalsZero());
        
        Assert.assertTrue(AggregatedAllocatedCapacity.subtract(ac,ac).equalsZero());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSubtractWrongUuid() {
        AggregatedAllocatedCapacity ac = 
            AggregatedAllocatedCapacity.add(new AggregatedAllocatedCapacity(), 
                "UUID1", new AllocatedCapacity(Fraction.ONE,1));

        AggregatedAllocatedCapacity.subtract(ac, "UUID2", new AllocatedCapacity(Fraction.ONE,1));
    }
}
