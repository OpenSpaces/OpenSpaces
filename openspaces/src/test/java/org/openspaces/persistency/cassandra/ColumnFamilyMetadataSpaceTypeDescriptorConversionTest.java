package org.openspaces.persistency.cassandra;

import junit.framework.Assert;

import org.junit.Test;
import org.openspaces.persistency.cassandra.data.MyCassandraDocumentWrapper;
import org.openspaces.persistency.cassandra.data.MyCassandraPojo1;
import org.openspaces.persistency.cassandra.data.MyCassandraPojo2;
import org.openspaces.persistency.cassandra.data.MyCassandraPojo3;
import org.openspaces.persistency.cassandra.data.MyCassandraPojo4;
import org.openspaces.persistency.cassandra.data.MyCassandraPojoWithPrimitives;
import org.openspaces.persistency.cassandra.meta.mapping.TestSpaceTypeDescriptorUtils;
import org.openspaces.persistency.cassandra.mock.MockIntroduceTypeData;

import com.gigaspaces.annotation.pojo.FifoSupport;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.metadata.SpaceDocumentSupport;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.metadata.StorageType;
import com.gigaspaces.metadata.index.SpaceIndexType;

public class ColumnFamilyMetadataSpaceTypeDescriptorConversionTest 
    extends AbstractCassandraTest
{

    @Test
    public void test()
    {
        SpaceTypeDescriptor typeDescriptor = createTypeDescriptor("TypeName");
        testTypeDescriptor(typeDescriptor);
    }

    private SpaceTypeDescriptor createTypeDescriptor(String name)
    {
        return createTypeDescriptor(new SpaceTypeDescriptorBuilder(name));
    }

    private SpaceTypeDescriptor createTypeDescriptor(SpaceTypeDescriptorBuilder builder)
    {
        return builder
            .addFifoGroupingIndex("fifoGroupingIndexPath.1")
            .addFifoGroupingIndex("fifoGroupingIndexPath.2")
            .addFixedProperty("namedProperty", "namePropertyType")
            .addFixedProperty("typedProperty", MyCassandraPojoWithPrimitives.class)
            .addFixedProperty("propertyDocumentSupportConvert", MyCassandraPojo1.class, SpaceDocumentSupport.CONVERT)
            .addFixedProperty("propertyDocumentSupportCopy", MyCassandraPojo2.class, SpaceDocumentSupport.COPY)
            .addFixedProperty("propertyDocumentSupportDefault", MyCassandraPojo2.class, SpaceDocumentSupport.DEFAULT)
            .addFixedProperty("propertyStorageTypeDefault", MyCassandraPojo3.class, StorageType.DEFAULT)
            .addFixedProperty("propertyStorageTypeObject", MyCassandraPojo3.class, StorageType.OBJECT)
            .addFixedProperty("propertyStorageTypeBinary", MyCassandraPojo4.class, StorageType.BINARY)
            .addFixedProperty("propertyStorageTypeCompressed", MyCassandraPojo4.class, StorageType.COMPRESSED)
            .addPathIndex("path.index.basic", SpaceIndexType.BASIC)
            .addPathIndex("path.index.extended", SpaceIndexType.EXTENDED)
            .addPathIndex("path.index.none", SpaceIndexType.NONE)
            .documentWrapperClass(MyCassandraDocumentWrapper.class)
            .fifoGroupingProperty("fifo.grouping.path")
            .fifoSupport(FifoSupport.ALL)
            .idProperty("idPropertyName", true)
            .replicable(true)
            .routingProperty("routing.property.name")
            .storageType(StorageType.OBJECT)
            .supportsDynamicProperties(true)
            .supportsOptimisticLocking(true)
            .create();
    }

    private void testTypeDescriptor(SpaceTypeDescriptor typeDescriptor)
    {
        _syncInterceptor.onIntroduceType(new MockIntroduceTypeData(typeDescriptor));
        DataIterator<SpaceTypeDescriptor> dataIterator = _dataSource.initialMetadataLoad();
        while (dataIterator.hasNext())
        {
            SpaceTypeDescriptor readTypeDescriptor = dataIterator.next();
            if (!readTypeDescriptor.getTypeName().equals(typeDescriptor.getTypeName()))
                continue;
            
            TestSpaceTypeDescriptorUtils.assertTypeDescriptorsEquals(typeDescriptor, readTypeDescriptor);
            return;
        }
        Assert.fail("Could not find metadata for " + typeDescriptor.getTypeName());
    }


    
}
