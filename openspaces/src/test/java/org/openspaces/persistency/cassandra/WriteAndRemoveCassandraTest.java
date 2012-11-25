package org.openspaces.persistency.cassandra;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openspaces.persistency.cassandra.mock.MockOperationsBatchDataBuilder;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.document.SpaceDocument;

public class WriteAndRemoveCassandraTest extends AbstractCassandraTest
{

    private final String typeName = "MyType";
    private final String keyName = "keyName";
    
    @Before
    public void before()
    {
        _syncInterceptor.onIntroduceType(createIntroduceTypeDataFromSpaceDocument(createSpaceDocument(0),
                                                                                  keyName));
        _dataSource.initialMetadataLoad();
    }
    
    @Test
    public void test()
    {
        MockOperationsBatchDataBuilder builder = new MockOperationsBatchDataBuilder();
        builder.write(createSpaceDocument(1), keyName);
        builder.write(createSpaceDocument(2), keyName);
        builder.write(createSpaceDocument(3), keyName);
        _syncInterceptor.onOperationsBatchSynchronization(builder.build());
        
        assertValidData(1,2,3);
        
        builder.clear();
        builder.remove(createSpaceDocument(1), keyName);
        _syncInterceptor.onOperationsBatchSynchronization(builder.build());
        
        assertValidData(2,3);
        
        builder.clear();
        builder.write(createSpaceDocument(1), keyName);
        builder.write(createSpaceDocument(2), keyName);
        builder.write(createSpaceDocument(3), keyName);
        builder.write(createSpaceDocument(4), keyName);
        _syncInterceptor.onOperationsBatchSynchronization(builder.build());
        
        assertValidData(1,2,3,4);
        
        builder.clear();
        builder.write(createSpaceDocument(1), keyName);
        builder.remove(createSpaceDocument(2), keyName);
        builder.write(createSpaceDocument(3), keyName);
        builder.write(createSpaceDocument(5), keyName);
        builder.remove(createSpaceDocument(6), keyName);
        _syncInterceptor.onOperationsBatchSynchronization(builder.build());
        
        assertValidData(1,3,4,5);
        
        builder.clear();
    }
    
    private void assertValidData(long ... expectedKeys)
    {
        Set<Long> expected = new HashSet<Long>();
        for (long num : expectedKeys)
            expected.add(num);
        
        DataIterator<Object> iterator = _dataSource.initialDataLoad();
        Set<Long> read = new HashSet<Long>();
        while (iterator.hasNext())
        {
            SpaceDocument doc = (SpaceDocument) iterator.next();
            Long key = doc.getProperty(keyName);
            read.add(key);
            Assert.assertEquals("unexpected property", key, doc.getProperty("some_prop"));
        }
        
        iterator.close();
        
        Assert.assertEquals("Unexpected set", expected, read);
    }
    
    private SpaceDocument createSpaceDocument(long key)
    {
        return new SpaceDocument(typeName)
            .setProperty(keyName, key)
            .setProperty("some_prop", key);
    }
    
}
