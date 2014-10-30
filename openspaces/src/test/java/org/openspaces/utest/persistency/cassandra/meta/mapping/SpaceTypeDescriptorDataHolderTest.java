package org.openspaces.utest.persistency.cassandra.meta.mapping;

import com.gigaspaces.annotation.pojo.FifoSupport;
import com.gigaspaces.metadata.SpaceDocumentSupport;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.metadata.StorageType;
import com.gigaspaces.metadata.index.SpaceIndexType;
import org.junit.Test;
import org.openspaces.itest.persistency.common.data.TestDocumentWrapper;
import org.openspaces.itest.persistency.common.data.TestDocumentWrapper2;
import org.openspaces.persistency.support.SpaceTypeDescriptorContainer;
import org.openspaces.utest.persistency.common.TestSpaceTypeDescriptorUtils;

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
                builder.documentWrapperClass(TestDocumentWrapper.class);
            }
            public void doWithSub(SpaceTypeDescriptorBuilder builder)
            {
                builder.documentWrapperClass(TestDocumentWrapper2.class);
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
        SpaceTypeDescriptorContainer superHolder = new SpaceTypeDescriptorContainer(superDesc);
        TestSpaceTypeDescriptorUtils.assertTypeDescriptorsEquals(superDesc, superHolder.getTypeDescriptor());
        
        SpaceTypeDescriptorBuilder subBuilder = new SpaceTypeDescriptorBuilder("sub", superDesc);
        handler.doWithSub(subBuilder);
        SpaceTypeDescriptor subDesc = subBuilder.create();
        SpaceTypeDescriptorContainer supHolder = new SpaceTypeDescriptorContainer(subDesc);
        TestSpaceTypeDescriptorUtils.assertTypeDescriptorsEquals(subDesc, supHolder.getTypeDescriptor());
    }
    
    private static interface TestTypeDescriptorHandler
    {
        void doWithSuper(SpaceTypeDescriptorBuilder builder);
        void doWithSub(SpaceTypeDescriptorBuilder builder);
    }
    
}
