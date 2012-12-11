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

import org.openspaces.persistency.cassandra.meta.conversion.ColumnFamilyNameConverter;
import org.openspaces.persistency.cassandra.meta.conversion.DefaultColumnFamilyNameConverter;
import org.openspaces.persistency.cassandra.meta.mapping.filter.DefaultFlattenedPropertiesFilter;
import org.openspaces.persistency.cassandra.meta.mapping.filter.FlattenedPropertiesFilter;
import org.openspaces.persistency.cassandra.meta.types.dynamic.DynamicPropertyValueSerializer;
import org.openspaces.persistency.cassandra.meta.types.dynamic.PropertyValueSerializer;

/**
 * 
 *  A configurer for creating {@link CassandraSpaceSynchronizationEndpoint} instances.
 * 
 * @since 9.1.1
 * @author Dan Kilman
 */
public class CassandraSpaceSynchronizationEndpointConfigurer {

    private PropertyValueSerializer               fixedPropertyValueSerializer;
    private PropertyValueSerializer               dynamicPropertyValueSerializer;
    private FlattenedPropertiesFilter             flattenedPropertiesFilter;
    private ColumnFamilyNameConverter             columnFamilyNameConverter;
    private HectorCassandraClient                 hectorClient;
    
    /**
     * Optional. If set, all fixed properties with a type that is not primitive nor a common 
     * java type will be serialized using {@link PropertyValueSerializer#toByteBuffer(Object)}
     * Note: This property must correspond to the property set on {@link CassandraSpaceDataSource}.
     * (default: Java object serialization)
     * @param fixedPropertyValueSerializer the {@link PropertyValueSerializer} to use.
     * @return {@code this} instance.
     */
    public CassandraSpaceSynchronizationEndpointConfigurer fixedPropertyValueSerializer(
            PropertyValueSerializer fixedPropertyValueSerializer) {
        this.fixedPropertyValueSerializer = fixedPropertyValueSerializer;
        return this;
    }

    /**
     * Optional. If set, all dynamic properties will be serialized using 
     * {@link PropertyValueSerializer#fromByteBuffer(java.nio.ByteBuffer)}.
     * Note: This property must correspond to the property set on 
     * {@link CassandraSpaceSynchronizationEndpoint}.
     * (default {@link DynamicPropertyValueSerializer})
     * @param dynamicPropertyValueSerializer the {@link PropertyValueSerializer} to use.
     * @return {@code this} instance.
     */
    public CassandraSpaceSynchronizationEndpointConfigurer dynamicPropertyValueSerializer(
            PropertyValueSerializer dynamicPropertyValueSerializer) {
        this.dynamicPropertyValueSerializer = dynamicPropertyValueSerializer;
        return this;
    }
    
    /**
     * Optional.
     * @param flattenedPropertiesFilter the {@link FlattenedPropertiesFilter} to use.
     * (default: {@link DefaultFlattenedPropertiesFilter})
     * @see FlattenedPropertiesFilter
     * @return {@code this} instance.
     */
    public CassandraSpaceSynchronizationEndpointConfigurer flattenedPropertiesFilter(
            FlattenedPropertiesFilter flattenedPropertiesFilter) {
        this.flattenedPropertiesFilter = flattenedPropertiesFilter;
        return this;
    }
    
    /**
     * Optional. 
     * @param columnFamilyNameConverter The {@link ColumnFamilyNameConverter} to use.
     * (default: {@link DefaultColumnFamilyNameConverter})
     * @see ColumnFamilyNameConverter
     * @return {@code this} instance.
     */
    public CassandraSpaceSynchronizationEndpointConfigurer columnFamilyNameConverter(
            ColumnFamilyNameConverter columnFamilyNameConverter) {
        this.columnFamilyNameConverter = columnFamilyNameConverter;
        return this;
    }
    
    /**
     * @param hectorClient an instance of {@link HectorCassandraClient}.
     * @return {@code this} instance.
     */
    public CassandraSpaceSynchronizationEndpointConfigurer hectorClient(
            HectorCassandraClient hectorClient) {
        this.hectorClient = hectorClient;
        return this;
    }
    
    /**
     * @return An instance of {@link CassandraSpaceSynchronizationEndpoint} 
     * matching this configurer configuration.
     */
    public CassandraSpaceSynchronizationEndpoint create() {
        return new CassandraSpaceSynchronizationEndpoint(fixedPropertyValueSerializer,
                                                         dynamicPropertyValueSerializer,
                                                         flattenedPropertiesFilter,
                                                         columnFamilyNameConverter,
                                                         hectorClient);
    }
    
}
