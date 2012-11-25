package org.openspaces.persistency.cassandra;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.cassandra.cql.jdbc.CassandraDataSource;
import org.junit.Before;
import org.junit.Test;
import org.openspaces.persistency.cassandra.mock.MockOperationsBatchDataBuilder;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.document.SpaceDocument;

public class MultiTypeCassandraTest extends AbstractCassandraTest
{
    private final Set<Object> writtenKeys      = new HashSet<Object>();
    private final Set<Object> readKeys         = new HashSet<Object>();

    private final int         batchCount       = 20;
    private final int         thirdBatchSize   = 20;

    private final String      document1KeyName = "key1";
    private final String      document2KeyName = "key2";
    private final String      document3KeyName = "key3";
    
    @Override
    protected CassandraSpaceDataSource createCassandraSpaceDataSource(
            HectorCassandraClient hectorClient)
    {
        CassandraDataSource ds = createCassandraDataSource();
        CassandraSpaceDataSource dataSource = new CassandraSpaceDataSource(null,
                                                                           null, 
                                                                           ds,
                                                                           hectorClient,
                                                                           5,
                                                                           30,
                                                                           10 /* batchLimit */);
        return dataSource;
    }
    
    @Before
    public void before()
    {
        _syncInterceptor.onIntroduceType(createIntroduceTypeDataFromSpaceDocument(createSpaceDocument1(false),
                                                                                  document1KeyName));
        _syncInterceptor.onIntroduceType(createIntroduceTypeDataFromSpaceDocument(createSpaceDocument2(false),
                                                                                  document2KeyName));
        _syncInterceptor.onIntroduceType(createIntroduceTypeDataFromSpaceDocument(createSpaceDocument3(false),
                                                                                  document3KeyName));
        _dataSource.initialMetadataLoad();
    }

    @Test
    public void test()
    {
        for (int i = 0; i < batchCount; i++)
        {
            MockOperationsBatchDataBuilder builder = new MockOperationsBatchDataBuilder();
            for (int j = 0; j < thirdBatchSize; j++)
            {
                builder.write(createSpaceDocument1(true), document1KeyName);
                builder.write(createSpaceDocument2(true), document2KeyName);
                builder.write(createSpaceDocument3(true), document3KeyName);
            }
            _syncInterceptor.onOperationsBatchSynchronization(builder.build());
        }
        
        DataIterator<Object> iterator = _dataSource.initialDataLoad();
        int count = 0;
        while (iterator.hasNext())
        {
            count++;
            SpaceDocument spaceDoc = (SpaceDocument) iterator.next();
            Object key;
            if (spaceDoc.containsProperty(document1KeyName)) 
                key = spaceDoc.getProperty(document1KeyName);
            else if (spaceDoc.containsProperty(document2KeyName))
                key = spaceDoc.getProperty(document2KeyName);
            else
                key = spaceDoc.getProperty(document3KeyName);
            
            readKeys.add(key);
        }
        
        iterator.close();
        
        Assert.assertEquals("count differs", batchCount * thirdBatchSize * 3, count);
        Assert.assertEquals("keys differ", writtenKeys, readKeys);
    }
    
    private SpaceDocument createSpaceDocument1(boolean addToWrittenKeys)
    {
        Long key = random.nextLong();
        if (addToWrittenKeys)
            writtenKeys.add(key);
        byte[] bytes = new byte[10];
        random.nextBytes(bytes);
        return new SpaceDocument("TypeName")
            .setProperty(document1KeyName, key)
            .setProperty("payload", bytes);
    }

    private SpaceDocument createSpaceDocument2(boolean addToWrittenKeys)
    {
        UUID key = UUID.randomUUID();
        if (addToWrittenKeys)
            writtenKeys.add(key);
        byte[] bytes = new byte[10];
        random.nextBytes(bytes);
        return new SpaceDocument("TypeName2")
            .setProperty(document2KeyName, key)
            .setProperty("payload", bytes)
            .setProperty("payload2", bytes);
    }

    private SpaceDocument createSpaceDocument3(boolean addToWrittenKeys)
    {
        String key = random.nextLong() + "#";
        if (addToWrittenKeys)
            writtenKeys.add(key);
        byte[] bytes = new byte[10];
        random.nextBytes(bytes);
        return new SpaceDocument("TypeName3")
            .setProperty(document3KeyName, key)
            .setProperty("payload2", bytes)
            .setProperty("some_other_prop", random.nextFloat());
    }
    
}
