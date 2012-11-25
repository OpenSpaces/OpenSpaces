package org.openspaces.utest.persistency.cassandra.meta.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openspaces.persistency.cassandra.meta.mapping.SpaceTypeDescriptorHolder;
import org.openspaces.persistency.cassandra.meta.mapping.TypeHierarcyTopologySorter;

import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;

public class TypeHierarcyTopologySorterTest
{
    
    @Test
    public void test()
    {
        testHierarchy1();

        testHierarchy2();
    }

    private void testHierarchy2()
    {
        SpaceTypeDescriptor desc1 = new SpaceTypeDescriptorBuilder("d1", null).create();
        SpaceTypeDescriptor desc2 = new SpaceTypeDescriptorBuilder("d2", null).create();
        SpaceTypeDescriptor desc3 = new SpaceTypeDescriptorBuilder("d3", null).create();
        SpaceTypeDescriptor desc4 = new SpaceTypeDescriptorBuilder("d4", desc1).create();
        SpaceTypeDescriptor desc5 = new SpaceTypeDescriptorBuilder("d5", desc2).create();
        SpaceTypeDescriptor desc6 = new SpaceTypeDescriptorBuilder("d6", desc2).create();
        SpaceTypeDescriptor desc7 = new SpaceTypeDescriptorBuilder("d7", desc4).create();
        SpaceTypeDescriptor desc8 = new SpaceTypeDescriptorBuilder("d8", desc5).create();
        SpaceTypeDescriptor desc9 = new SpaceTypeDescriptorBuilder("d9", desc8).create();
        SpaceTypeDescriptor desc10 = new SpaceTypeDescriptorBuilder("d10", desc8).create();

        SpaceTypeDescriptorHolder holder_desc1 = new SpaceTypeDescriptorHolder(desc1);
        SpaceTypeDescriptorHolder holder_desc2 = new SpaceTypeDescriptorHolder(desc2);
        SpaceTypeDescriptorHolder holder_desc3 = new SpaceTypeDescriptorHolder(desc3);
        SpaceTypeDescriptorHolder holder_desc4 = new SpaceTypeDescriptorHolder(desc4);
        SpaceTypeDescriptorHolder holder_desc5 = new SpaceTypeDescriptorHolder(desc5);
        SpaceTypeDescriptorHolder holder_desc6 = new SpaceTypeDescriptorHolder(desc6);
        SpaceTypeDescriptorHolder holder_desc7 = new SpaceTypeDescriptorHolder(desc7);
        SpaceTypeDescriptorHolder holder_desc8 = new SpaceTypeDescriptorHolder(desc8);
        SpaceTypeDescriptorHolder holder_desc9 = new SpaceTypeDescriptorHolder(desc9);
        SpaceTypeDescriptorHolder holder_desc10 = new SpaceTypeDescriptorHolder(desc10);
        
        Map<String, SpaceTypeDescriptorHolder> typeDescriptorsData = new HashMap<String, SpaceTypeDescriptorHolder>();
        typeDescriptorsData.put(holder_desc1.getTypeName(), holder_desc1);
        typeDescriptorsData.put(holder_desc2.getTypeName(), holder_desc2);
        typeDescriptorsData.put(holder_desc3.getTypeName(), holder_desc3);
        typeDescriptorsData.put(holder_desc4.getTypeName(), holder_desc4);
        typeDescriptorsData.put(holder_desc5.getTypeName(), holder_desc5);
        typeDescriptorsData.put(holder_desc6.getTypeName(), holder_desc6);
        typeDescriptorsData.put(holder_desc7.getTypeName(), holder_desc7);
        typeDescriptorsData.put(holder_desc8.getTypeName(), holder_desc8);
        typeDescriptorsData.put(holder_desc9.getTypeName(), holder_desc9);
        typeDescriptorsData.put(holder_desc10.getTypeName(), holder_desc10);
        
        List<SpaceTypeDescriptor> sortedList = TypeHierarcyTopologySorter.getSortedList(typeDescriptorsData);
        
        assertOrder("d7", "d4", sortedList);
        assertOrder("d4", "d1", sortedList);
        assertOrder("d9", "d8", sortedList);
        assertOrder("d10", "d8", sortedList);
        assertOrder("d8", "d5", sortedList);
        assertOrder("d5", "d2", sortedList);
        assertOrder("d6", "d2", sortedList);
    }
    
    private void testHierarchy1()
    {
        SpaceTypeDescriptor aDesc = new SpaceTypeDescriptorBuilder(MyCassandraA.class, null).create();
        SpaceTypeDescriptor bDesc = new SpaceTypeDescriptorBuilder(MyCassandraB.class, aDesc).create();
        SpaceTypeDescriptor cDesc = new SpaceTypeDescriptorBuilder(MyCassandraC.class, null).create();
        
        SpaceTypeDescriptorHolder aDescHolder = new SpaceTypeDescriptorHolder(aDesc);
        SpaceTypeDescriptorHolder bDescHolder = new SpaceTypeDescriptorHolder(bDesc);
        SpaceTypeDescriptorHolder cDescHolder = new SpaceTypeDescriptorHolder(cDesc);
        
        Map<String, SpaceTypeDescriptorHolder> typeDescriptorsData = new HashMap<String, SpaceTypeDescriptorHolder>();
        typeDescriptorsData.put(aDescHolder.getTypeName(), aDescHolder);
        typeDescriptorsData.put(bDescHolder.getTypeName(), bDescHolder);
        typeDescriptorsData.put(cDescHolder.getTypeName(), cDescHolder);
        
        List<SpaceTypeDescriptor> sortedList = TypeHierarcyTopologySorter.getSortedList(typeDescriptorsData);

        assertOrder(MyCassandraB.class.getName(), MyCassandraA.class.getName(), sortedList);
        
    }

    private void assertOrder(String typeName, String superTypeName, List<SpaceTypeDescriptor> sortedList)
    {
        boolean seenSuper = false;
        for (SpaceTypeDescriptor spaceTypeDescriptor : sortedList)
        {
            if (superTypeName.equals(spaceTypeDescriptor.getTypeName()))
                seenSuper = true;
            else if (typeName.equals(spaceTypeDescriptor.getTypeName()) && !seenSuper)
                Assert.fail("Type " + superTypeName + " should come before " + typeName);
        }
    }
    
    public static class MyCassandraA
    {
        private String id1;

        @SpaceId
        public String getId1()
        {
            return id1;
        }

        public void setId1(String id)
        {
            this.id1 = id;
        }
    }

    public static class MyCassandraB extends MyCassandraA
    {
        private String id1;

        @SpaceId
        public String getId1()
        {
            return id1;
        }

        public void setId1(String id)
        {
            this.id1 = id;
        }
    }
    
    public static class MyCassandraC
    {
        private String id3;

        @SpaceId
        public String getId3()
        {
            return id3;
        }

        public void setId3(String id)
        {
            this.id3 = id;
        }
    }
    
}
