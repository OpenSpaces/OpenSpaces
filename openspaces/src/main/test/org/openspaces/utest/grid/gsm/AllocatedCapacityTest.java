package org.openspaces.utest.grid.gsm;

import junit.framework.Assert;

import org.junit.Test;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.grid.gsm.capacity.AllocatedCapacity;

public class AllocatedCapacityTest {

    @Test
    public void testAdd() {
        AllocatedCapacity c1 = new AllocatedCapacity(Fraction.ZERO,1);
        AllocatedCapacity c2 = new AllocatedCapacity(Fraction.ONE,0);
        Assert.assertEquals(
                new AllocatedCapacity(Fraction.ONE,1), 
                c1.add(c2));
    }
    
    @Test
    public void testEqualsZero() {
        Assert.assertTrue(new AllocatedCapacity(Fraction.ZERO,0).equalsZero());
        Assert.assertTrue(new AllocatedCapacity(Fraction.ZERO,0).isCpuCoresEqualsZero());
        Assert.assertTrue(new AllocatedCapacity(Fraction.ZERO,0).isMemoryEqualsZero());
        Assert.assertFalse(new AllocatedCapacity(Fraction.ONE,0).equalsZero());
        Assert.assertFalse(new AllocatedCapacity(Fraction.ONE,0).isCpuCoresEqualsZero());
        Assert.assertTrue(new AllocatedCapacity(Fraction.ONE,0).isMemoryEqualsZero());
        Assert.assertFalse(new AllocatedCapacity(Fraction.ZERO,1).equalsZero());
        Assert.assertTrue(new AllocatedCapacity(Fraction.ZERO,1).isCpuCoresEqualsZero());
        Assert.assertFalse(new AllocatedCapacity(Fraction.ZERO,1).isMemoryEqualsZero());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNegativeCpuException() {
        AllocatedCapacity c1 = new AllocatedCapacity(Fraction.ZERO,1);
        AllocatedCapacity c2 = new AllocatedCapacity(Fraction.ONE,0);
        c1.subtract(c2);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNegativeMemoryException() {
        AllocatedCapacity c1 = new AllocatedCapacity(Fraction.ZERO,1);
        AllocatedCapacity c2 = new AllocatedCapacity(Fraction.ONE,0);
        c2.subtract( c1);
    }
    
    @Test
    public void testSubtractCpu() {
        AllocatedCapacity c1 = new AllocatedCapacity(Fraction.ONE,1);
        AllocatedCapacity c2 = new AllocatedCapacity(Fraction.ONE,0);
        Assert.assertEquals(new AllocatedCapacity(Fraction.ZERO,1), c1.subtract(c2));
    }
    
    @Test
    public void testSubtractMemory() {
        AllocatedCapacity c1 = new AllocatedCapacity(Fraction.ONE,1);
        AllocatedCapacity c2 = new AllocatedCapacity(Fraction.ZERO,1);
        Assert.assertEquals(new AllocatedCapacity(Fraction.ONE,0), c1.subtract(c2));
    }
    
    @Test
    public void testSubtractOrZero() {
        AllocatedCapacity c1 = new AllocatedCapacity(Fraction.ONE,1);
        AllocatedCapacity c2 = new AllocatedCapacity(Fraction.ONE,1);
        Assert.assertTrue(c1.subtractOrZero(c2).equalsZero());
        
        AllocatedCapacity c3 = new AllocatedCapacity(Fraction.ZERO,0);
        AllocatedCapacity c4 = new AllocatedCapacity(Fraction.ONE,1);
        Assert.assertTrue(c3.subtractOrZero(c4).equalsZero());
    }
    
    @Test
    public void testSatisfies() {
        AllocatedCapacity c1 = new AllocatedCapacity(Fraction.ONE,1);
        AllocatedCapacity c2 = new AllocatedCapacity(Fraction.ONE,1);
        Assert.assertTrue(c1.satisfies(c2));
        Assert.assertTrue(c2.satisfies(c1));
        
        AllocatedCapacity c3 = new AllocatedCapacity(Fraction.TWO,1);
        AllocatedCapacity c4 = new AllocatedCapacity(Fraction.ONE,2);
        Assert.assertTrue(c3.satisfies(c1));
        Assert.assertTrue(c4.satisfies(c1));
        Assert.assertFalse(c1.satisfies(c3));
        Assert.assertFalse(c1.satisfies(c4));
        
        AllocatedCapacity c5 = new AllocatedCapacity(Fraction.TWO,2);
        Assert.assertTrue(c5.satisfies(c1));
        Assert.assertFalse(c1.satisfies(c5));
    }
    
    @Test
    public void testMoreThanSatisfies() {
        AllocatedCapacity c1 = new AllocatedCapacity(Fraction.ONE,1);
        AllocatedCapacity c2 = new AllocatedCapacity(Fraction.ONE,1);
        Assert.assertFalse(c1.moreThanSatisfies(c2));
        Assert.assertFalse(c2.moreThanSatisfies(c1));
        
        AllocatedCapacity c3 = new AllocatedCapacity(Fraction.TWO,1);
        AllocatedCapacity c4 = new AllocatedCapacity(Fraction.ONE,2);
        Assert.assertFalse(c3.moreThanSatisfies(c1));
        Assert.assertFalse(c4.moreThanSatisfies(c1));
        Assert.assertFalse(c1.moreThanSatisfies(c3));
        Assert.assertFalse(c1.moreThanSatisfies(c4));
        
        AllocatedCapacity c5 = new AllocatedCapacity(Fraction.TWO,2);
        Assert.assertTrue(c5.moreThanSatisfies(c1));
        Assert.assertFalse(c1.moreThanSatisfies(c5));
    }
}
