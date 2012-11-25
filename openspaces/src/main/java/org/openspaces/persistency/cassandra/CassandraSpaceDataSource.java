/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.persistency.cassandra;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.concurrent.GuardedBy;

import org.apache.cassandra.cql.jdbc.CassandraDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.persistency.cassandra.datasource.CQLQueryContext;
import org.openspaces.persistency.cassandra.datasource.CassandraTokenRangeAwareDataIterator;
import org.openspaces.persistency.cassandra.datasource.CassandraTokenRangeAwareInitialLoadDataIterator;
import org.openspaces.persistency.cassandra.datasource.DataIteratorAdapter;
import org.openspaces.persistency.cassandra.datasource.SingleEntryDataIterator;
import org.openspaces.persistency.cassandra.error.CassandraErrorHandler;
import org.openspaces.persistency.cassandra.error.DefaultCassandraErrorHandler;
import org.openspaces.persistency.cassandra.error.GetDataIteratorErrorReason;
import org.openspaces.persistency.cassandra.meta.ColumnFamilyMetadata;
import org.openspaces.persistency.cassandra.meta.mapping.DefaultSpaceDocumentColumnFamilyMapper;
import org.openspaces.persistency.cassandra.meta.mapping.SpaceDocumentColumnFamilyMapper;
import org.openspaces.persistency.cassandra.meta.mapping.SpaceTypeDescriptorHolder;
import org.openspaces.persistency.cassandra.meta.mapping.TypeHierarcyTopologySorter;
import org.openspaces.persistency.cassandra.meta.types.dynamic.PropertyValueSerializer;
import org.openspaces.persistency.cassandra.pool.ConnectionResource;
import org.openspaces.persistency.cassandra.pool.ConnectionResourceFactory;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataSourceIdQuery;
import com.gigaspaces.datasource.DataSourceQuery;
import com.gigaspaces.datasource.DataSourceSQLQuery;
import com.gigaspaces.datasource.SpaceDataSource;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.j_spaces.kernel.pool.IResourceFactory;
import com.j_spaces.kernel.pool.IResourcePool;
import com.j_spaces.kernel.pool.IResourceProcedure;
import com.j_spaces.kernel.pool.ResourcePool;

/**
 * 
 * A Cassandra implementation of {@link com.gigaspaces.datasource.SpaceDataSource}.
 * 
 * @since 9.5
 * @author Dan Kilman
 */
public class CassandraSpaceDataSource
        extends SpaceDataSource {
    
    private static final String                     CQL_VERSION  = "2.0.0";
    
    private static final Log                        logger       = LogFactory.getLog(CassandraSpaceDataSource.class);

    private final CassandraErrorHandler             errorHandler = new DefaultCassandraErrorHandler();

    private final SpaceDocumentColumnFamilyMapper   mapper;

    private final IResourcePool<ConnectionResource> connectionPool;
    private final HectorCassandraClient             hectorClient;

    private final int                               batchLimit;

    private final Object                            lock         = new Object();
    
    @GuardedBy("lock")
    private boolean                                 closed       = false;
    
    public CassandraSpaceDataSource(
            PropertyValueSerializer fixedPropertyValueSerializer,
            PropertyValueSerializer dynamicPropertyValueSerializer,
            CassandraDataSource cassandraDataSource,
            HectorCassandraClient hectorClient,
            int minimumNumberOfConnections, int maximumNumberOfConnections,
            int batchLimit) {
        
        if (hectorClient == null) {
            throw new IllegalArgumentException("hectorClient must be set and initiated");
        }
        
        if (cassandraDataSource == null) {
            throw new IllegalArgumentException("dataSource must be set");
        }
        
        if (!CQL_VERSION.equals(cassandraDataSource.getVersion())) {
            throw new IllegalArgumentException("dataSource version must be set to " + CQL_VERSION);
        }
        
        if (minimumNumberOfConnections <= 0) {
            throw new IllegalArgumentException("mininumNumberOfConnections must be positive number");
        }
        
        if (maximumNumberOfConnections < minimumNumberOfConnections) {
            throw new IllegalArgumentException("maxmimumNumberOfConnections must not be smaller than" +
                                               "mininummNumberOfConnections");
        }
        
        if (batchLimit <= 0) {
            throw new IllegalArgumentException("batchSize must be a positive number");
        }
        
        this.batchLimit = batchLimit;
        this.hectorClient = hectorClient;
        
        IResourceFactory<ConnectionResource> resourceFactory = new ConnectionResourceFactory(cassandraDataSource);
        connectionPool = new ResourcePool<ConnectionResource>(resourceFactory,
                                                               minimumNumberOfConnections,
                                                               maximumNumberOfConnections);
        
        mapper = new DefaultSpaceDocumentColumnFamilyMapper(fixedPropertyValueSerializer,
                                                             dynamicPropertyValueSerializer);
        
    }

    /**
     * Closes open jdbc connections and the hector client connection pool.
     */
    public void close() {
        synchronized (lock) {
            if (closed) {
                return;
            }
            
            connectionPool.forAllResources(new IResourceProcedure<ConnectionResource>() {
                public void invoke(ConnectionResource resource) {
                    try {
                        resource.getConnection().close();
                    } catch (SQLException e) {
                        logger.debug("Failed closing data source connection", e);
                    }
                }
            });
            hectorClient.close();
            closed = true;
        }
    }
    
    @Override
    public DataIterator<Object> getDataIterator(DataSourceQuery query) {
        
        String typeName = query.getTypeDescriptor().getTypeName();
        ColumnFamilyMetadata metadata = hectorClient.getColumnFamilyMetadata(typeName);
        if (metadata == null) {
            metadata = hectorClient.fetchKnownColumnFamily(typeName, mapper);
            if (metadata == null) {
                errorHandler.onGetDataIteraotrError(query, 
                                                     GetDataIteratorErrorReason.MissingColumnFamily, 
                                                     null);
                return new SingleEntryDataIterator(null);
            }
        }
        
        CQLQueryContext queryContext = null;
        if (query.supportsTemplateAsDocument()) {
            SpaceDocument templateDocument = query.getTemplateAsDocument();
            
            Map<String, Object> properties = templateDocument.getProperties();
            queryContext = new CQLQueryContext(properties, null, null);
        } else if (query.supportsAsSQLQuery()) {
            DataSourceSQLQuery<Object> sqlQuery = query.getAsSQLQuery();
            Object[] params = sqlQuery.getQueryParameters();
            queryContext = new CQLQueryContext(null, sqlQuery.getQuery(), params);
        } else {
            errorHandler.onGetDataIteraotrError(query, 
                                                 GetDataIteratorErrorReason.UnsupportedDataSourceQuery, 
                                                 null);
            
            return new SingleEntryDataIterator(null);
        }
         
        try {
            Object keyValue = getKeyValue(queryContext, metadata);
            boolean performIdQuery = keyValue != null && !templateHasPropertyOtherThanKey(queryContext, metadata);
            
            if (performIdQuery) {
                return new SingleEntryDataIterator(getByIdImpl(metadata.getTypeName(), keyValue));
            } else {
                int maxResults = keyValue != null ? 1 : query.getMaxResults();
                return new CassandraTokenRangeAwareDataIterator(mapper,
                                                                metadata, 
                                                                connectionPool.getResource(), 
                                                                queryContext, 
                                                                maxResults,
                                                                batchLimit);
            }
        } catch (Exception e) {
            errorHandler.onGetDataIteraotrError(query, 
                                                 GetDataIteratorErrorReason.QueryExecutionException, 
                                                 e);
            
            return new SingleEntryDataIterator(null);
        }
    }

    private Object getKeyValue(CQLQueryContext queryContext, ColumnFamilyMetadata metadata) {
        if (!queryContext.hasProperties()) {
            return null;
        }
        
        return queryContext.getProperties().get(metadata.getKeyName());
    }
    
    private boolean templateHasPropertyOtherThanKey(
            CQLQueryContext queryContext,
            ColumnFamilyMetadata metadata) {
        // This test is not really needed as it is only called after getKeyValue returned a
        // value differet than null, and this same test is performed there
        if (!queryContext.hasProperties()) {
            return true;
        }
        
        for (Entry<String, Object> entry : queryContext.getProperties().entrySet()) {
            if (!metadata.getKeyName().equals(entry.getKey()) &&
                entry.getValue() != null) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public Object getById(DataSourceIdQuery idQuery) {
        String typeName = idQuery.getTypeDescriptor().getTypeName();
        Object id = idQuery.getId();
        return getByIdImpl(typeName, id);
    }
    
    private Object getByIdImpl(String typeName, Object id) {
        return hectorClient.readDocmentByKey(mapper, typeName, id);
    }
    
    @Override
    public DataIterator<SpaceTypeDescriptor> initialMetadataLoad() {
        
        Map<String, ColumnFamilyMetadata> columnFamilies = hectorClient.populateKnownColumnFamilies(mapper);
        Map<String, SpaceTypeDescriptorHolder> typeDescriptors = new HashMap<String, SpaceTypeDescriptorHolder>();
        
        for (ColumnFamilyMetadata metadata : columnFamilies.values()) {
            typeDescriptors.put(metadata.getTypeName(), metadata.getTypeDescriptorData());
        }
        
        List<SpaceTypeDescriptor> result = TypeHierarcyTopologySorter.getSortedList(typeDescriptors);
        
        return new DataIteratorAdapter<SpaceTypeDescriptor>(result.iterator());
    }

    @Override
    public DataIterator<Object> initialDataLoad() {
        
        Collection<ColumnFamilyMetadata> columnFamilies = hectorClient.getKnownColumnFamilies().values();
        return new CassandraTokenRangeAwareInitialLoadDataIterator(mapper,
                                                                   columnFamilies,
                                                                   connectionPool.getResource(),
                                                                   batchLimit);
    }
    
    /**
     * Returns <code>false</code>, inheritance is not supported.
     * @return <code>false</code>.
     */
    @Override
    public boolean supportsInheritance() {
        return false;
    }

}
