package org.openspaces.persistency.cassandra;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openspaces.persistency.cassandra.data.MyCassandraPojo1;
import org.openspaces.persistency.cassandra.mock.MockDataSourceQuery;
import org.openspaces.persistency.cassandra.mock.MockOperationsBatchDataBuilder;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.sync.IntroduceTypeData;

public class DataIteratorWithPropertyAddedLaterCassandraTest extends AbstractCassandraTest
{
    private final String keyName = "key";
    private final String typeName = "TypeName";
    private final MyCassandraPojo1 newType = new MyCassandraPojo1("123");
    private IntroduceTypeData introduceDataType;
    
    @Before
    public void before()
    {
        introduceDataType = createIntroduceTypeDataFromSpaceDocument(createSpaceDocument(),
                                                                                  keyName);
        _syncInterceptor.onIntroduceType(introduceDataType);
        _dataSource.initialMetadataLoad();
    }
    
    @Test
    public void test()
    {
        MockOperationsBatchDataBuilder builder = new MockOperationsBatchDataBuilder();
        builder.write(createSpaceDocument(), keyName);
        _syncInterceptor.onOperationsBatchSynchronization(builder.build());

        DataIterator<Object> iterator;
        SpaceDocument doc;
        
        iterator = _dataSource.getDataIterator(new MockDataSourceQuery(introduceDataType.getTypeDescriptor(),
                                                                       new SpaceDocument(typeName),
                                                                       Integer.MAX_VALUE));
        Assert.assertTrue("No object found", iterator.hasNext());
        doc = (SpaceDocument) iterator.next();
        Assert.assertEquals("Wrong type name", typeName, doc.getTypeName());
        Assert.assertEquals("Wrong value", 1, doc.getProperty(keyName));
        Assert.assertEquals("Wrong value", true, doc.getProperty("some_prop"));
        
        builder = new MockOperationsBatchDataBuilder();
        builder.write(createSpaceDocument()
                      .setProperty("new_prop", newType)
                      .setProperty("new_prop2", 2), keyName);
        _syncInterceptor.onOperationsBatchSynchronization(builder.build());
        
        iterator.close();
        iterator = _dataSource.getDataIterator(new MockDataSourceQuery(introduceDataType.getTypeDescriptor(),
                                                                       new SpaceDocument(typeName),
                                                                       Integer.MAX_VALUE));
        Assert.assertTrue("No object found", iterator.hasNext());
        doc = (SpaceDocument) iterator.next();
        Assert.assertEquals("Wrong type name", typeName, doc.getTypeName());
        Assert.assertEquals("Wrong value", 1, doc.getProperty(keyName));
        Assert.assertEquals("Wrong value", true, doc.getProperty("some_prop"));
        
        // uncomment if we decide pojo will be restored as documents no matter what
        Assert.assertEquals("Wrong value", newType, doc.getProperty("new_prop"));
//        Assert.assertEquals("Wrong value", newType.getStr(), ((SpaceDocument)doc.getProperty("new_prop")).getProperty("str"));

        Assert.assertEquals("Wrong value", 2, doc.getProperty("new_prop2"));
        
        iterator.close();
    }
    
    private SpaceDocument createSpaceDocument()
    {
        return new SpaceDocument(typeName)
            .setProperty(keyName, 1)
            .setProperty("some_prop", true);
    }
    
}
