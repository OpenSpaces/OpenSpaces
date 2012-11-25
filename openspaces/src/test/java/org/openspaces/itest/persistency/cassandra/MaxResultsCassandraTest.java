package org.openspaces.itest.persistency.cassandra;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.apache.cassandra.cql.jdbc.CassandraDataSource;
import org.junit.Before;
import org.junit.Test;
import org.openspaces.itest.persistency.cassandra.mock.MockDataSourceQuery;
import org.openspaces.itest.persistency.cassandra.mock.MockOperationsBatchDataBuilder;
import org.openspaces.persistency.cassandra.CassandraSpaceDataSource;
import org.openspaces.persistency.cassandra.HectorCassandraClient;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.sync.IntroduceTypeData;

public class MaxResultsCassandraTest
        extends AbstractCassandraTest
{
    private final String keyName = "key";
    private final String typeName = "TypeName";
    private final Set<Object> written = new HashSet<Object>();
    private IntroduceTypeData introduceDataType;
    
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
                                                                           3 /* batch limit */);
        return dataSource;
    }
    
    @Before
    public void before()
    {
        introduceDataType = createIntroduceTypeDataFromSpaceDocument(createSpaceDocument(false),
                                                                                  keyName);
        _syncInterceptor.onIntroduceType(introduceDataType);
        _dataSource.initialMetadataLoad();
    }
    
    @Test
    public void test()
    {
        MockOperationsBatchDataBuilder builder = new MockOperationsBatchDataBuilder();
        builder.write(createSpaceDocument(true), keyName)
               .write(createSpaceDocument(true), keyName);
        _syncInterceptor.onOperationsBatchSynchronization(builder.build());

        // query of maxResults 1 batchLimit 3 and 2 actual results written
        assertIteratorEntriesCount(readWithMaxResults(1), 1);
        
        // query of maxResults 2 batchLimit 3 and 2 actual results written
        assertIteratorEntriesCount(readWithMaxResults(2), 2);
        
        // query of maxResults 3 batchLimit 3 and 2 actual results written
        assertIteratorEntriesCount(readWithMaxResults(3), 2);

        // query of maxResults 4 batchLimit 3 and 2 actual results written
        assertIteratorEntriesCount(readWithMaxResults(4), 2);
        
        builder.clear().write(createSpaceDocument(true), keyName);
        _syncInterceptor.onOperationsBatchSynchronization(builder.build());
        
        // query of maxResults 1 batchLimit 3 and 3 actual results written
        assertIteratorEntriesCount(readWithMaxResults(1), 1);
        
        // query of maxResults 2 batchLimit 3 and 3 actual results written
        assertIteratorEntriesCount(readWithMaxResults(2), 2);
        
        // query of maxResults 3 batchLimit 3 and 3 actual results written
        assertIteratorEntriesCount(readWithMaxResults(3), 3);

        // query of maxResults 4 batchLimit 3 and 3 actual results written
        assertIteratorEntriesCount(readWithMaxResults(4), 3);
        
        builder.clear().write(createSpaceDocument(true), keyName);
        _syncInterceptor.onOperationsBatchSynchronization(builder.build());
        
        // query of maxResults 1 batchLimit 3 and 4 actual results written
        assertIteratorEntriesCount(readWithMaxResults(1), 1);
        
        // query of maxResults 2 batchLimit 3 and 4 actual results written
        assertIteratorEntriesCount(readWithMaxResults(2), 2);
        
        // query of maxResults 3 batchLimit 3 and 4 actual results written
        assertIteratorEntriesCount(readWithMaxResults(3), 3);

        // query of maxResults 4 batchLimit 3 and 4 actual results written
        assertIteratorEntriesCount(readWithMaxResults(4), 4);

        // query of maxResults 5 batchLimit 3 and 4 actual results written
        assertIteratorEntriesCount(readWithMaxResults(5), 4);
    }
    
    private DataIterator<Object> readWithMaxResults(int maxResults)
    {
        return _dataSource.getDataIterator(new MockDataSourceQuery(introduceDataType.getTypeDescriptor(),
                                           new SpaceDocument(typeName),
                                           maxResults));
    }
    
    private void assertIteratorEntriesCount(DataIterator<Object> iterator, int count)
    {
        Set<Object> result = new HashSet<Object>();
        while (iterator.hasNext()) 
        {
            result.add(iterator.next());
        }
        
        iterator.close();
        
        Assert.assertEquals("Written documents", count, result.size());
        if (count == written.size())
            Assert.assertEquals("Written documents", written, result);
            
    }
    
    private SpaceDocument createSpaceDocument(boolean addToWritten)
    {
        SpaceDocument result = new SpaceDocument(typeName)
            .setProperty(keyName, random.nextLong())
            .setProperty("some_prop", random.nextInt());
        if (addToWritten)
            written.add(result);
        
        return result;
    }
    
}
