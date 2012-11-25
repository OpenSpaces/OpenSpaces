package org.openspaces.persistency.cassandra;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.cassandra.cql.jdbc.CassandraDataSource;
import org.junit.After;
import org.junit.Before;
import org.openspaces.persistency.cassandra.helper.EmbeddedCassandraController;
import org.openspaces.persistency.cassandra.helper.TestLoggingHelper;
import org.openspaces.persistency.cassandra.meta.mapping.filter.FlattenedPropertiesFilter;
import org.openspaces.persistency.cassandra.mock.MockIntroduceTypeData;
import org.openspaces.persistency.cassandra.suite.CassandraTestSuite;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.metadata.index.SpaceIndexType;

abstract public class AbstractCassandraTest
{
    protected static final Random                         random        = new Random();

    private static final String                           CQL_VERSION   = "2.0.0";
    private static final String                           LOCALHOST     = "127.0.0.1";
    private static final String                           DEFAULT_AUTH  = "default";
    
    protected String                                      _keySpaceName = "space";
    protected int                                         _rpcPort;
    
    protected final EmbeddedCassandraController           _cassandraController = new EmbeddedCassandraController();
    protected CassandraSynchronizationEndpointInterceptor _syncInterceptor;
    protected CassandraSpaceDataSource                    _dataSource;
    
    @Before
    public void initialSetup()
    {
        if (CassandraTestSuite.isSuiteMode())
        {
            _keySpaceName = CassandraTestSuite.createKeySpaceAndReturnItsName();
            _rpcPort = CassandraTestSuite.getRpcPort();
        }
        else
        {
            TestLoggingHelper.init();
            _cassandraController.initCassandra(isEmbedded());
            _cassandraController.createKeySpace(_keySpaceName);
            _rpcPort = _cassandraController.getRpcPort();
        }
        
        _syncInterceptor = createCassandraSyncEndpointInterceptor(createCassandraHectorClient("cluster-sync"));
        _dataSource = createCassandraSpaceDataSource(createCassandraHectorClient("cluster-datasource"));
    }
    
    @After
    public void finalTeardown()
    {
        _syncInterceptor.close();
        _dataSource.close();
        
        if (!CassandraTestSuite.isSuiteMode())
            _cassandraController.stopCassandra();
    }

    protected boolean isEmbedded()
    {
        return false;
    }
    
    protected HectorCassandraClient createCassandraHectorClient(String clusterName)
    {
        HectorCassandraClient hectorClient = new HectorCassandraClient(LOCALHOST,
                                                                       _rpcPort,
                                                                       _keySpaceName,
                                                                       clusterName);
        return hectorClient;
    }
    
    protected CassandraSynchronizationEndpointInterceptor createCassandraSyncEndpointInterceptor(
            HectorCassandraClient hectorClient)
    {
        CassandraSynchronizationEndpointInterceptor syncInterceptor = 
                new CassandraSynchronizationEndpointInterceptorConfigurer()
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
                                       _rpcPort,
                                       _keySpaceName,
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
