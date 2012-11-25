package org.openspaces.utest.persistency.cassandra.meta.mapping;

import junit.framework.Assert;

import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.index.SpaceIndex;

public class TestSpaceTypeDescriptorUtils
{
    public static void assertTypeDescriptorsEquals(SpaceTypeDescriptor expected, SpaceTypeDescriptor actual)
    {
        Assert.assertEquals(expected.getTypeName(), actual.getTypeName());
        Assert.assertEquals(expected.getSuperTypeName(), actual.getSuperTypeName());
        Assert.assertEquals(expected.isAutoGenerateId(), actual.isAutoGenerateId());
        Assert.assertEquals(expected.getTypeSimpleName(), actual.getTypeSimpleName());
        Assert.assertEquals(expected.getFifoGroupingPropertyPath(), actual.getFifoGroupingPropertyPath());
        Assert.assertEquals(expected.getIdPropertyName(), actual.getIdPropertyName());
        Assert.assertEquals(expected.getNumOfFixedProperties(), actual.getNumOfFixedProperties());
        Assert.assertEquals(expected.getRoutingPropertyName(), actual.getRoutingPropertyName());
        Assert.assertEquals(expected.isConcreteType(), actual.isConcreteType());
        Assert.assertEquals(expected.isReplicable(), actual.isReplicable());
        Assert.assertEquals(expected.getDocumentWrapperClass(), actual.getDocumentWrapperClass());
        Assert.assertEquals(expected.getFifoGroupingIndexesPaths(), actual.getFifoGroupingIndexesPaths());
        Assert.assertEquals(expected.getFifoSupport(), actual.getFifoSupport());
        Assert.assertEquals(expected.supportsDynamicProperties(), actual.supportsDynamicProperties());
        Assert.assertEquals(expected.supportsOptimisticLocking(), actual.supportsOptimisticLocking());
        Assert.assertEquals(expected.getObjectClass(), actual.getObjectClass());
        Assert.assertEquals(expected.getStorageType(), actual.getStorageType());
        for (int i = 0; i < expected.getNumOfFixedProperties(); i++)
        {
            Assert.assertEquals(expected.getFixedProperty(i).getName(), actual.getFixedProperty(i).getName());
            Assert.assertEquals(expected.getFixedProperty(i).getTypeName(), actual.getFixedProperty(i).getTypeName());
            Assert.assertEquals(expected.getFixedProperty(i).getTypeDisplayName(), actual.getFixedProperty(i).getTypeDisplayName());
            Assert.assertEquals(expected.getFixedProperty(i).getDocumentSupport(), actual.getFixedProperty(i).getDocumentSupport());
            Assert.assertEquals(expected.getFixedProperty(i).getType(), actual.getFixedProperty(i).getType());
            Assert.assertEquals(expected.getFixedProperty(i).getStorageType(), actual.getFixedProperty(i).getStorageType());
        }

        for (SpaceIndex expectedSpaceIndex : expected.getIndexes().values())
        {
            SpaceIndex actualSpaceIndex = actual.getIndexes().get(expectedSpaceIndex.getName());
            Assert.assertEquals(expectedSpaceIndex.getName(), actualSpaceIndex.getName());
            Assert.assertEquals(expectedSpaceIndex.getIndexType(), actualSpaceIndex.getIndexType());
        }
        
    }
}
