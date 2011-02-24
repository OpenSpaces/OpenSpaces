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
        
        AggregatedAllocatedCapacity ac2 = ac1.add("UUID1", new AllocatedCapacity(Fraction.ZERO,1));
        AggregatedAllocatedCapacity ac3 = ac1.add("UUID1", new AllocatedCapacity(Fraction.ONE,0));
        Assert.assertFalse(ac2.equalsZero());
        Assert.assertFalse(ac3.equalsZero());
    }
    
    @Test
    public void testAdd() {
        AggregatedAllocatedCapacity ac1 = new AggregatedAllocatedCapacity();
        AggregatedAllocatedCapacity ac2 = ac1.add("UUID1", new AllocatedCapacity(Fraction.ZERO,1));
        AggregatedAllocatedCapacity ac3 = ac2.add("UUID1", new AllocatedCapacity(Fraction.ONE,0));
        AggregatedAllocatedCapacity expectedAc3 = ac1.add("UUID1",new AllocatedCapacity(Fraction.ONE,1));
        Assert.assertEquals(expectedAc3,ac3);
        
        AggregatedAllocatedCapacity ac4 = ac3.add(ac3);
        AggregatedAllocatedCapacity expectedAc4 = ac1.add("UUID1",new AllocatedCapacity(Fraction.TWO,2));
        Assert.assertEquals(expectedAc4,ac4);
        
        AggregatedAllocatedCapacity ac5 = ac3.add( "UUID2", new AllocatedCapacity(Fraction.TWO,2));
        Assert.assertEquals(new AllocatedCapacity(Fraction.ONE,1),ac5.getAgentCapacity("UUID1"));
        Assert.assertEquals(new AllocatedCapacity(Fraction.TWO,2),ac5.getAgentCapacity("UUID2"));
        Assert.assertEquals(new AllocatedCapacity(Fraction.ONE,1),ac5.getAgentCapacityOrZero("UUID1"));
        Assert.assertEquals(new AllocatedCapacity(Fraction.TWO,2),ac5.getAgentCapacityOrZero("UUID2"));
        Assert.assertEquals(new AllocatedCapacity(Fraction.ZERO,0),ac5.getAgentCapacityOrZero("UUID3"));
        
        Assert.assertEquals(new AllocatedCapacity(new Fraction(3),3),ac5.getTotalAllocatedCapacity());
    }
    
    @Test
    public void testSubtract() {
        AggregatedAllocatedCapacity ac = 
            new AggregatedAllocatedCapacity().add( 
                "UUID1", new AllocatedCapacity(Fraction.ONE,1));

        Assert.assertTrue(
                ac.subtract("UUID1", new AllocatedCapacity(Fraction.ONE,1))
                .equalsZero());
        
        Assert.assertTrue(ac.subtract(ac).equalsZero());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSubtractWrongUuid() {
        AggregatedAllocatedCapacity ac = 
            new AggregatedAllocatedCapacity().add("UUID1", new AllocatedCapacity(Fraction.ONE,1));

        ac.subtract("UUID2", new AllocatedCapacity(Fraction.ONE,1));
    }
}
