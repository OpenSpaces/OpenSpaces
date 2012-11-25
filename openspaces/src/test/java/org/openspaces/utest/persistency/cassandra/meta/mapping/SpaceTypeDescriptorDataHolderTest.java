package org.openspaces.utest.persistency.cassandra.meta.mapping;

import org.junit.Test;
import org.openspaces.itest.persistency.cassandra.data.MyCassandraDocumentWrapper;
import org.openspaces.itest.persistency.cassandra.data.MyCassandraDocumentWrapper2;
import org.openspaces.persistency.cassandra.meta.mapping.SpaceTypeDescriptorHolder;

import com.gigaspaces.annotation.pojo.FifoSupport;
import com.gigaspaces.metadata.SpaceDocumentSupport;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.metadata.StorageType;
import com.gigaspaces.metadata.index.SpaceIndexType;

public class SpaceTypeDescriptorDataHolderTest
{

    @Test
    public void test()
    {
        testFifoGroupingIndexingAndPropertyPath();
        testDocumentWrapperClass();
        testFifoSupport();
        testReplicable();
        testFixedProperties();
        testSupportsDynamicProperties();
        testSupportsOptimisticLocking();
        testIdProperty();
        testRoutingProperty();
        testStorageType();
        testIndexes();
    }

    private void testIndexes()
    {
        testHandler(new TestTypeDescriptorHandler()
        {
            public void doWithSuper(SpaceTypeDescriptorBuilder builder)
            {
                builder.addPathIndex("a.b.c", SpaceIndexType.BASIC);
                builder.addPathIndex("a.b.d", SpaceIndexType.EXTENDED);
            }
            public void doWithSub(SpaceTypeDescriptorBuilder builder)
            {
                builder.addPathIndex("a.b.e", SpaceIndexType.BASIC);
                builder.addPathIndex("a.b.f", SpaceIndexType.EXTENDED);
            }
        });        
    }

    private void testStorageType()
    {
        testHandler(new TestTypeDescriptorHandler()
        {
            public void doWithSuper(SpaceTypeDescriptorBuilder builder)
            {
                builder.storageType(StorageType.COMPRESSED);
            }
            public void doWithSub(SpaceTypeDescriptorBuilder builder)
            {

            }
        });
    }

    private void testRoutingProperty()
    {
        testHandler(new TestTypeDescriptorHandler()
        {
            public void doWithSuper(SpaceTypeDescriptorBuilder builder)
            {
                builder.routingProperty("prop1", SpaceIndexType.BASIC);
            }
            public void doWithSub(SpaceTypeDescriptorBuilder builder)
            {
                builder.routingProperty("prop2", SpaceIndexType.EXTENDED);
            }
        });
    }

    private void testIdProperty()
    {
        testHandler(new TestTypeDescriptorHandler()
        {
            public void doWithSuper(SpaceTypeDescriptorBuilder builder)
            {
                builder.idProperty("prop1", false, SpaceIndexType.BASIC);
            }
            public void doWithSub(SpaceTypeDescriptorBuilder builder)
            {
                builder.idProperty("prop2", true, SpaceIndexType.EXTENDED);
            }
        });
    }

    private void testSupportsOptimisticLocking()
    {
        testHandler(new TestTypeDescriptorHandler()
        {
            public void doWithSuper(SpaceTypeDescriptorBuilder builder)
            {
                builder.supportsOptimisticLocking(false);
            }
            public void doWithSub(SpaceTypeDescriptorBuilder builder)
            {
                builder.supportsOptimisticLocking(true);
            }
        });
    }

    private void testSupportsDynamicProperties()
    {
        testHandler(new TestTypeDescriptorHandler()
        {
            public void doWithSuper(SpaceTypeDescriptorBuilder builder)
            {
                builder.supportsDynamicProperties(false);
            }
            public void doWithSub(SpaceTypeDescriptorBuilder builder)
            {
                builder.supportsDynamicProperties(true);
            }
        });
    }

    private void testFixedProperties()
    {
        testHandler(new TestTypeDescriptorHandler()
        {
            public void doWithSuper(SpaceTypeDescriptorBuilder builder)
            {
                builder.addFixedProperty("prop1", Object.class, SpaceDocumentSupport.CONVERT, StorageType.COMPRESSED);
                builder.addFixedProperty("prop2", Object.class, SpaceDocumentSupport.COPY, StorageType.BINARY);
            }
            public void doWithSub(SpaceTypeDescriptorBuilder builder)
            {
                builder.addFixedProperty("prop3", String.class, SpaceDocumentSupport.DEFAULT, StorageType.OBJECT);
                builder.addFixedProperty("prop4", Integer.class, SpaceDocumentSupport.CONVERT, StorageType.DEFAULT);
            }
        });
    }

    private void testReplicable()
    {
        testHandler(new TestTypeDescriptorHandler()
        {
            public void doWithSuper(SpaceTypeDescriptorBuilder builder)
            {
                builder.replicable(false);
            }
            public void doWithSub(SpaceTypeDescriptorBuilder builder)
            {
                builder.replicable(true);
            }
        });
    }

    private void testFifoSupport()
    {
        testHandler(new TestTypeDescriptorHandler()
        {
            public void doWithSuper(SpaceTypeDescriptorBuilder builder)
            {
                builder.fifoSupport(FifoSupport.OPERATION);
            }
            public void doWithSub(SpaceTypeDescriptorBuilder builder)
            {
                builder.fifoSupport(FifoSupport.ALL);
            }
        });
    }

    private void testDocumentWrapperClass()
    {
        testHandler(new TestTypeDescriptorHandler()
        {
            public void doWithSuper(SpaceTypeDescriptorBuilder builder)
            {
                builder.documentWrapperClass(MyCassandraDocumentWrapper.class);
            }
            public void doWithSub(SpaceTypeDescriptorBuilder builder)
            {
                builder.documentWrapperClass(MyCassandraDocumentWrapper2.class);
            }
        });
    }

    private void testFifoGroupingIndexingAndPropertyPath()
    {
        testHandler(new TestTypeDescriptorHandler()
        {
            public void doWithSuper(SpaceTypeDescriptorBuilder builder)
            {
                builder.fifoGroupingProperty("fifo.grouping.property");
                builder.addFifoGroupingIndex("fifo.grouping.indexing.path");
            }
            public void doWithSub(SpaceTypeDescriptorBuilder builder)
            {
                builder.addFifoGroupingIndex("fifo.grouping.indexing.path");
                builder.addFifoGroupingIndex("fifo.grouping.indexing.path2");
            }
        });
    }
    
    private void testHandler(TestTypeDescriptorHandler handler)
    {
        SpaceTypeDescriptorBuilder superBuilder = new SpaceTypeDescriptorBuilder("super");
        handler.doWithSuper(superBuilder);
        SpaceTypeDescriptor superDesc = superBuilder.create();
        SpaceTypeDescriptorHolder superHolder = new SpaceTypeDescriptorHolder(superDesc);
        TestSpaceTypeDescriptorUtils.assertTypeDescriptorsEquals(superDesc, superHolder.getTypeDescriptor());
        
        SpaceTypeDescriptorBuilder subBuilder = new SpaceTypeDescriptorBuilder("sub", superDesc);
        handler.doWithSub(subBuilder);
        SpaceTypeDescriptor subDesc = subBuilder.create();
        SpaceTypeDescriptorHolder supHolder = new SpaceTypeDescriptorHolder(subDesc);
        TestSpaceTypeDescriptorUtils.assertTypeDescriptorsEquals(subDesc, supHolder.getTypeDescriptor());
    }
    
    private static interface TestTypeDescriptorHandler
    {
        void doWithSuper(SpaceTypeDescriptorBuilder builder);
        void doWithSub(SpaceTypeDescriptorBuilder builder);
    }
    
}
