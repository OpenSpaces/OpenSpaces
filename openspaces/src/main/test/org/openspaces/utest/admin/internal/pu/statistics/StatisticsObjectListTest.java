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
package org.openspaces.utest.admin.internal.pu.statistics;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.openspaces.admin.internal.pu.statistics.StatisticsObjectList;

/**
 * Tests {@link StatisticsObjectList}
 * @author itaif
 * @since 9.0.0
 */
public class StatisticsObjectListTest extends TestCase {

    public void testSimpleList2() {
        StatisticsObjectList list = new StatisticsObjectList();
        list.add(0);
        list.add(1);
        Assert.assertEquals(0, list.getMinimum());
        Assert.assertEquals(1, list.getMaximum());
        Assert.assertEquals(0.5, list.getAverage());
        Assert.assertEquals(0, list.getPercentile(0));
        Assert.assertEquals(0, list.getPercentile(1));
        Assert.assertEquals(0, list.getPercentile(49));
        Assert.assertEquals(1, list.getPercentile(50));
        Assert.assertEquals(1, list.getPercentile(51));
        Assert.assertEquals(1, list.getPercentile(99));
        Assert.assertEquals(1, list.getPercentile(100));
    }
    
    public void testSimpleList3() {
        StatisticsObjectList list = new StatisticsObjectList();
        list.add(2);
        list.add(0);
        list.add(1);
        Assert.assertEquals(0, list.getMinimum());
        Assert.assertEquals(2, list.getMaximum());
        Assert.assertEquals(1.0, list.getAverage());
        Assert.assertEquals(0, list.getPercentile(0));
        Assert.assertEquals(0, list.getPercentile(1));
        Assert.assertEquals(1, list.getPercentile(49));
        Assert.assertEquals(1, list.getPercentile(50));
        Assert.assertEquals(1, list.getPercentile(51));
        Assert.assertEquals(2, list.getPercentile(99));
        Assert.assertEquals(2, list.getPercentile(100));
    }
    
    public void testComparable() {
        StatisticsObjectList list = new StatisticsObjectList();
        list.add("c");
        list.add("a");
        list.add("b");
        Assert.assertEquals("a", list.getMinimum());
        Assert.assertEquals("c", list.getMaximum());
        try {
            list.getAverage();
            Assert.fail("Expected ClassCastException");
            
        }
        catch (ClassCastException e) {
        }
      
    }
    
    
    public void testAverageNotComparable() {
        StatisticsObjectList list = new StatisticsObjectList();
        list.add(new Object());
        try {
            list.getAverage();
            Assert.fail("Expected ClassCastException");
        }
        catch (ClassCastException e) {
        }
        
        try {
            list.getMinimum();
            Assert.fail("Expected ClassCastException");
            
        }
        catch (ClassCastException e) {
        }
    }
}
