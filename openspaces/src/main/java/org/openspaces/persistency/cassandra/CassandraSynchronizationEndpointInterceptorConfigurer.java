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

import com.gigaspaces.document.SpaceDocument;

/**
 * 
 *  A configurer for creating {@link CassandraSynchronizationEndpointInterceptor} instances.
 * 
 * @since 9.5
 * @author Dan Kilman
 */
public class CassandraSynchronizationEndpointInterceptorConfigurer {

    private int                                   maxNestingLevel = 10;
    private PropertyValueSerializer               fixedPropertyValueSerializer;
    private PropertyValueSerializer               dynamicPropertyValueSerializer;
    private FlattenedPropertiesFilter             flattenedPropertiesFilter;
    private ColumnFamilyNameConverter             columnFamilyNameConverter;
    private HectorCassandraClient                 hectorClient;
    
    /**
     * @param maxNestingLevel This property defines are deep does POJO and {@link SpaceDocument} introspection
     * should go before deciding to stop digging deeper in this path. 
     * i.e. If a POJO property is found at the max nesting level than it will be serialized as a blob.
     * (default: 10)
     * @return {@code this} instance.
     */
    public CassandraSynchronizationEndpointInterceptorConfigurer maxNestingLevel(int maxNestingLevel) {
        this.maxNestingLevel = maxNestingLevel;
        return this;
    }
    
    /**
     * Optional. If set, all fixed properties with a type that is not primitive nor a common 
     * java type will be serialized using {@link PropertyValueSerializer#toByteBuffer(Object)}
     * Note: This property must correspond to the property set on {@link CassandraSpaceDataSource}.
     * (default: Java object serialization)
     * @param fixedPropertyValueSerializer the {@link PropertyValueSerializer} to use.
     * @return {@code this} instance.
     */
    public CassandraSynchronizationEndpointInterceptorConfigurer fixedPropertyValueSerializer(
            PropertyValueSerializer fixedPropertyValueSerializer) {
        this.fixedPropertyValueSerializer = fixedPropertyValueSerializer;
        return this;
    }

    /**
     * Optional. If set, all dynamic properties will be serialized using 
     * {@link PropertyValueSerializer#fromByteBuffer(java.nio.ByteBuffer)}.
     * Note: This property must correspond to the property set on 
     * {@link CassandraSynchronizationEndpointInterceptor}.
     * (default {@link DynamicPropertyValueSerializer})
     * @param dynamicPropertyValueSerializer the {@link PropertyValueSerializer} to use.
     * @return {@code this} instance.
     */
    public CassandraSynchronizationEndpointInterceptorConfigurer dynamicPropertyValueSerializer(
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
    public CassandraSynchronizationEndpointInterceptorConfigurer flattenedPropertiesFilter(
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
    public CassandraSynchronizationEndpointInterceptorConfigurer columnFamilyNameConverter(
            ColumnFamilyNameConverter columnFamilyNameConverter) {
        this.columnFamilyNameConverter = columnFamilyNameConverter;
        return this;
    }
    
    /**
     * @param hectorClient an instance of {@link HectorCassandraClient}.
     * @return {@code this} instance.
     */
    public CassandraSynchronizationEndpointInterceptorConfigurer hectorClient(
            HectorCassandraClient hectorClient) {
        this.hectorClient = hectorClient;
        return this;
    }
    
    /**
     * @return An instance of {@link CassandraSynchronizationEndpointInterceptor} 
     * matching this configurer configuration.
     */
    public CassandraSynchronizationEndpointInterceptor create() {
        return new CassandraSynchronizationEndpointInterceptor(maxNestingLevel,
                                                               fixedPropertyValueSerializer,
                                                               dynamicPropertyValueSerializer,
                                                               flattenedPropertiesFilter,
                                                               columnFamilyNameConverter,
                                                               hectorClient);
    }
    
}
