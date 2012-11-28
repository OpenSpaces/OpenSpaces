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

import org.apache.cassandra.cql.jdbc.CassandraDataSource;
import org.openspaces.persistency.cassandra.meta.types.dynamic.DynamicPropertyValueSerializer;
import org.openspaces.persistency.cassandra.meta.types.dynamic.PropertyValueSerializer;


/**
 * 
 * A configurer for creating {@link CassandraSpaceDataSource} instances.
 * 
 * @since 9.1.1
 * @author Dan Kilman
 */
public class CassandraSpaceDataSourceConfigurer {

    private PropertyValueSerializer fixedPropertyValueSerializer;
    private PropertyValueSerializer dynamicPropertyValueSerializer;
    private CassandraDataSource     cassandraDataSource;
    private HectorCassandraClient   hectorClient;
    private int                     minimumNumberOfConnections = 5;
    private int                     maximumNumberOfConnections = 30;
    private int                     batchLimit                 = 10000;
    
    /**
     * Optional. If set, all fixed properties with a type that is not primitive nor a common 
     * java type will be deserialized using {@link PropertyValueSerializer#fromByteBuffer(java.nio.ByteBuffer)}
     * Note: This property must correspond to the property set on 
     * {@link CassandraSpaceSynchronizationEndpoint}.
     * (default: Java object deserialization)
     * @param fixedPropertyValueSerializer The {@link PropertyValueSerializer} to use.
     * @return {@code this} instance.
     */
    public CassandraSpaceDataSourceConfigurer fixedPropertyValueSerializer(
            PropertyValueSerializer fixedPropertyValueSerializer) {
        this.fixedPropertyValueSerializer = fixedPropertyValueSerializer;
        return this;
    }

    /**
     * Optional. If set, all dynamic properties will be deserialized using 
     * {@link PropertyValueSerializer#fromByteBuffer(java.nio.ByteBuffer)}.
     * Note: This property must correspond to the property set on 
     * {@link CassandraSpaceSynchronizationEndpoint}.
     * (default {@link DynamicPropertyValueSerializer})
     * @param dynamicPropertyValueSerializer the {@link PropertyValueSerializer} to use.
     * @return {@code this} instance.
     */
    public CassandraSpaceDataSourceConfigurer dynamicPropertyValueSerializer(
            PropertyValueSerializer dynamicPropertyValueSerializer) {
        this.dynamicPropertyValueSerializer = dynamicPropertyValueSerializer;
        return this;
    }

    /**
     * @param cassandraDataSource An instance of {@link CassandraDataSource} configured
     * to use CQL version 2.0.0.
     * @return {@code this} instance.
     */
    public CassandraSpaceDataSourceConfigurer cassandraDataSource(
            CassandraDataSource cassandraDataSource) {
        this.cassandraDataSource = cassandraDataSource;
        return this;
    }
    
    /**
     * @param hectorClient an instance of {@link HectorCassandraClient}.
     * @return {@code this} instance.
     */
    public CassandraSpaceDataSourceConfigurer hectorClient(
            HectorCassandraClient hectorClient) {
        this.hectorClient = hectorClient;
        return this;
    }
    
    /**
     * Optional.
     * @param minimumNumberOfConnections Minimum number of cassandra-jdbc connections to maintain in the 
     * connection pool. (default: 5)
     * @return {@code this} instance.
     */
    public CassandraSpaceDataSourceConfigurer minimumNumberOfConnections(
            int minimumNumberOfConnections) {
        this.minimumNumberOfConnections = minimumNumberOfConnections;
        return this;
    }

    /**
     * Optional.
     * @param maximumNumberOfConnections Maximum number of cassandra-jdbc connections to maintain in the 
     * connection pool. (default: 30)
     * @return {@code this} instance.
     */
    public CassandraSpaceDataSourceConfigurer maximumNumberOfConnections(
            int maximumNumberOfConnections) {
        this.maximumNumberOfConnections = maximumNumberOfConnections;
        return this;
    }

    /**
     * Optional.
     * @param batchLimit Maximum number of rows that will be transferred in batches.
     * (default: 10000).
     * e.g. If batchLimit is set to 10000 and a certain query result set size is 22000,
     * then the query will be translated to 3 queries each with the CQL LIMIT argument set
     * to 10000.
     * @return {@code this} instance.
     */
    public CassandraSpaceDataSourceConfigurer batchLimit(
            int batchLimit) {
        this.batchLimit = batchLimit;
        return this;
    }
    
    /**
     * @return An instance of {@link CassandraSpaceDataSource} matching this configurer
     * configuration.
     */
    public CassandraSpaceDataSource create() {
        return new CassandraSpaceDataSource(fixedPropertyValueSerializer,
                                            dynamicPropertyValueSerializer,
                                            cassandraDataSource,
                                            hectorClient,
                                            minimumNumberOfConnections,
                                            maximumNumberOfConnections,
                                            batchLimit);
    }
    
}
