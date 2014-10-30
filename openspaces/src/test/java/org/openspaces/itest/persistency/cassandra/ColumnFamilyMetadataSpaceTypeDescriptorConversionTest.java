package org.openspaces.itest.persistency.cassandra;

import com.gigaspaces.annotation.pojo.FifoSupport;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.metadata.SpaceDocumentSupport;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.metadata.StorageType;
import com.gigaspaces.metadata.index.SpaceIndexType;
import junit.framework.Assert;
import org.junit.Test;
import org.openspaces.itest.persistency.common.data.*;
import org.openspaces.itest.persistency.common.mock.MockIntroduceTypeData;
import org.openspaces.utest.persistency.common.TestSpaceTypeDescriptorUtils;

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
            .addFixedProperty("typedProperty", TestPojoWithPrimitives.class)
            .addFixedProperty("propertyDocumentSupportConvert", TestPojo1.class, SpaceDocumentSupport.CONVERT)
            .addFixedProperty("propertyDocumentSupportCopy", TestPojo2.class, SpaceDocumentSupport.COPY)
            .addFixedProperty("propertyDocumentSupportDefault", TestPojo2.class, SpaceDocumentSupport.DEFAULT)
            .addFixedProperty("propertyStorageTypeDefault", TestPojo3.class, StorageType.DEFAULT)
            .addFixedProperty("propertyStorageTypeObject", TestPojo3.class, StorageType.OBJECT)
            .addFixedProperty("propertyStorageTypeBinary", TestPojo4.class, StorageType.BINARY)
            .addFixedProperty("propertyStorageTypeCompressed", TestPojo4.class, StorageType.COMPRESSED)
            .addPathIndex("path.index.basic", SpaceIndexType.BASIC)
            .addPathIndex("path.index.extended", SpaceIndexType.EXTENDED)
            .addPathIndex("path.index.none", SpaceIndexType.NONE)
            .documentWrapperClass(TestDocumentWrapper.class)
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
