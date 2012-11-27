package org.openspaces.itest.persistency.cassandra;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.cassandra.cql.jdbc.CassandraDataSource;
import org.junit.After;
import org.junit.Before;
import org.openspaces.itest.persistency.cassandra.mock.MockIntroduceTypeData;
import org.openspaces.persistency.cassandra.CassandraSpaceDataSource;
import org.openspaces.persistency.cassandra.CassandraSpaceSynchronizationEndpoint;
import org.openspaces.persistency.cassandra.CassandraSpaceSynchronizationEndpointConfigurer;
import org.openspaces.persistency.cassandra.HectorCassandraClient;
import org.openspaces.persistency.cassandra.meta.mapping.filter.FlattenedPropertiesFilter;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.metadata.index.SpaceIndexType;

abstract public class AbstractCassandraTest
{
    protected static final Random                         random        = new Random();

    private static final String                           CQL_VERSION   = "2.0.0";
    private static final String                           LOCALHOST     = "127.0.0.1";
    private static final String                           DEFAULT_AUTH  = "default";
    
    protected CassandraSpaceSynchronizationEndpoint _syncInterceptor;
    protected CassandraSpaceDataSource                    _dataSource;
    
    private CassandraTestServer server = new CassandraTestServer();
    
    @Before
    public void initialSetup()
    {
       
    	server.initialize(isEmbedded());
    	
        _syncInterceptor = createCassandraSyncEndpointInterceptor(createCassandraHectorClient("cluster-sync"));
        _dataSource = createCassandraSpaceDataSource(createCassandraHectorClient("cluster-datasource"));
    }
    
    @After
    public void finalTeardown()
    {
    	if (_syncInterceptor != null) {
    		_syncInterceptor.close();
    	}
    	
    	if (_dataSource != null) {
    		_dataSource.close();
    	}
    	
    	server.destroy();
    }

    protected boolean isEmbedded()
    {
        return false;
    }
    
    protected HectorCassandraClient createCassandraHectorClient(String clusterName)
    {
        HectorCassandraClient hectorClient = new HectorCassandraClient(LOCALHOST,
                                                                       server.getPort(),
                                                                       server.getKeySpaceName(),
                                                                       clusterName);
        return hectorClient;
    }
    
    protected CassandraSpaceSynchronizationEndpoint createCassandraSyncEndpointInterceptor(
            HectorCassandraClient hectorClient)
    {
        CassandraSpaceSynchronizationEndpoint syncInterceptor = 
                new CassandraSpaceSynchronizationEndpointConfigurer()
                    .flattenedPropertiesFilter(new FlattenedPropertiesFilter() {
                        public boolean shouldFlatten(String pathToProperty, String propertyName,
                                Class<?> propertyType, boolean isDynamicProperty) {
                            return true;
                        }
                    })
                    .hectorClient(hectorClient).create();
        return syncInterceptor;
    }

    protected CassandraSpaceDataSource createCassandraSpaceDataSource(HectorCassandraClient hectorClient)
    {
        CassandraDataSource ds = createCassandraDataSource();
        CassandraSpaceDataSource dataSource = new CassandraSpaceDataSource(null,
                                                                           null, 
                                                                           ds,
                                                                           hectorClient,
                                                                           5,
                                                                           30,
                                                                           10000);
        return dataSource;
    }

    protected CassandraDataSource createCassandraDataSource()
    {
        return new CassandraDataSource(LOCALHOST,
                                       server.getPort(),
                                       server.getKeySpaceName(),
                                       DEFAULT_AUTH,
                                       DEFAULT_AUTH,
                                       CQL_VERSION);
    }
    
    protected MockIntroduceTypeData createIntroduceTypeDataFromSpaceDocument(
            SpaceDocument document, String key)
    {
        return createIntroduceTypeDataFromSpaceDocument(document, key, new HashSet<String>());
    }
    
    protected MockIntroduceTypeData createIntroduceTypeDataFromSpaceDocument(
            SpaceDocument document, String key, Set<String> indexes)
    {
        SpaceTypeDescriptorBuilder builder = new SpaceTypeDescriptorBuilder(document.getTypeName());
        for (Entry<String, Object> entry : document.getProperties().entrySet())
            builder.addFixedProperty(entry.getKey(), entry.getValue().getClass());
        for (String index : indexes)
            builder.addPathIndex(index, SpaceIndexType.BASIC);
        builder.idProperty(key);
        return new MockIntroduceTypeData(builder.create());
    }
    
}
