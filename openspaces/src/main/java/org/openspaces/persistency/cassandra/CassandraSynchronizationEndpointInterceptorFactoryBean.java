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
 * {@link CassandraSynchronizationEndpointInterceptor}.
 * 
 * @since 9.5
 * @author Dan Kilman
 */
public class CassandraSynchronizationEndpointInterceptorFactoryBean implements 
    FactoryBean<CassandraSynchronizationEndpointInterceptor>, InitializingBean, DisposableBean {

    private final CassandraSynchronizationEndpointInterceptorConfigurer configurer =
            new CassandraSynchronizationEndpointInterceptorConfigurer();
    
    private CassandraSynchronizationEndpointInterceptor cassandraSynchronizationEndpointInterceptor;
    
    /**
     * @see CassandraSynchronizationEndpointInterceptorConfigurer#maxNestingLevel(int)
     */
    public void setMaxNestingLevel(int maxNestingLevel) {
        configurer.maxNestingLevel(maxNestingLevel);
    }

    /**
     * @see CassandraSynchronizationEndpointInterceptorConfigurer#fixedPropertyValueSerializer(PropertyValueSerializer)
     */
    public void setFixedPropertyValueSerializer(
            PropertyValueSerializer fixedPropertyValueSerializer) {
        configurer.fixedPropertyValueSerializer(fixedPropertyValueSerializer);
    }

    /**
     * @see CassandraSynchronizationEndpointInterceptorConfigurer#dynamicPropertyValueSerializer(PropertyValueSerializer)
     */
    public void setDynamicPropertyValueSerializer(
            PropertyValueSerializer dynamicPropertyValueSerializer) {
        configurer.dynamicPropertyValueSerializer(dynamicPropertyValueSerializer);
    }

    /**
     * @see CassandraSynchronizationEndpointInterceptorConfigurer#flattenedPropertiesFilter(FlattenedPropertiesFilter)
     */
    public void setFlattenedPropertiesFilter(
            FlattenedPropertiesFilter flattenedPropertiesFilter) {
        configurer.flattenedPropertiesFilter(flattenedPropertiesFilter);
    }

    /**
     * @see CassandraSynchronizationEndpointInterceptorConfigurer#columnFamilyNameConverter(ColumnFamilyNameConverter)
     */
    public void setColumnFamilyNameConverter(
            ColumnFamilyNameConverter columnFamilyNameConverter) {
        configurer.columnFamilyNameConverter(columnFamilyNameConverter);
    }

    /**
     * @see CassandraSynchronizationEndpointInterceptorConfigurer#hectorClient(HectorCassandraClient)
     */
    public void setHectorClient(HectorCassandraClient hectorClient) {
        configurer.hectorClient(hectorClient);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        cassandraSynchronizationEndpointInterceptor = configurer.create();
    }
    
    @Override
    public CassandraSynchronizationEndpointInterceptor getObject() throws Exception {
        return cassandraSynchronizationEndpointInterceptor;
    }

    @Override
    public Class<?> getObjectType() {
        return CassandraSynchronizationEndpointInterceptor.class;
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
