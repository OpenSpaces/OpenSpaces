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
import org.openspaces.persistency.cassandra.meta.mapping.filter.FlattenedPropertiesFilter;
import org.openspaces.persistency.cassandra.meta.types.dynamic.PropertyValueSerializer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;


/**
 * 
 * A {@link FactoryBean} for creating a singleton instance of
 * {@link CassandraSpaceSynchronizationEndpoint}.
 * 
 * @since 9.1.1
 * @author Dan Kilman
 */
public class CassandraSpaceSynchronizationEndpointFactoryBean implements 
    FactoryBean<CassandraSpaceSynchronizationEndpoint>, InitializingBean, DisposableBean {

    private final CassandraSpaceSynchronizationEndpointConfigurer configurer =
            new CassandraSpaceSynchronizationEndpointConfigurer();
    
    private CassandraSpaceSynchronizationEndpoint cassandraSynchronizationEndpointInterceptor;
    
    /**
     * @see CassandraSpaceSynchronizationEndpointConfigurer#maxNestingLevel(int)
     */
    public void setMaxNestingLevel(int maxNestingLevel) {
        configurer.maxNestingLevel(maxNestingLevel);
    }

    /**
     * @see CassandraSpaceSynchronizationEndpointConfigurer#fixedPropertyValueSerializer(PropertyValueSerializer)
     */
    public void setFixedPropertyValueSerializer(
            PropertyValueSerializer fixedPropertyValueSerializer) {
        configurer.fixedPropertyValueSerializer(fixedPropertyValueSerializer);
    }

    /**
     * @see CassandraSpaceSynchronizationEndpointConfigurer#dynamicPropertyValueSerializer(PropertyValueSerializer)
     */
    public void setDynamicPropertyValueSerializer(
            PropertyValueSerializer dynamicPropertyValueSerializer) {
        configurer.dynamicPropertyValueSerializer(dynamicPropertyValueSerializer);
    }

    /**
     * @see CassandraSpaceSynchronizationEndpointConfigurer#flattenedPropertiesFilter(FlattenedPropertiesFilter)
     */
    public void setFlattenedPropertiesFilter(
            FlattenedPropertiesFilter flattenedPropertiesFilter) {
        configurer.flattenedPropertiesFilter(flattenedPropertiesFilter);
    }

    /**
     * @see CassandraSpaceSynchronizationEndpointConfigurer#columnFamilyNameConverter(ColumnFamilyNameConverter)
     */
    public void setColumnFamilyNameConverter(
            ColumnFamilyNameConverter columnFamilyNameConverter) {
        configurer.columnFamilyNameConverter(columnFamilyNameConverter);
    }

    /**
     * @see CassandraSpaceSynchronizationEndpointConfigurer#hectorClient(HectorCassandraClient)
     */
    public void setHectorClient(HectorCassandraClient hectorClient) {
        configurer.hectorClient(hectorClient);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        cassandraSynchronizationEndpointInterceptor = configurer.create();
    }
    
    @Override
    public CassandraSpaceSynchronizationEndpoint getObject() throws Exception {
        return cassandraSynchronizationEndpointInterceptor;
    }

    @Override
    public Class<?> getObjectType() {
        return CassandraSpaceSynchronizationEndpoint.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() throws Exception {
        cassandraSynchronizationEndpointInterceptor.close();
    }

}
