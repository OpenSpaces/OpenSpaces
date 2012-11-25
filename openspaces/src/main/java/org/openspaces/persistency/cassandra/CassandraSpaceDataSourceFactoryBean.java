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
import org.openspaces.persistency.cassandra.meta.types.dynamic.PropertyValueSerializer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;


/**
 * 
 * A {@link FactoryBean} for creating a singleton instance of
 * {@link CassandraSpaceDataSource}.
 * 
 * @since 9.5
 * @author Dan Kilman
 */
public class CassandraSpaceDataSourceFactoryBean implements 
    FactoryBean<CassandraSpaceDataSource>, InitializingBean, DisposableBean {

    private final CassandraSpaceDataSourceConfigurer configurer = new CassandraSpaceDataSourceConfigurer();
    
    private CassandraSpaceDataSource cassandraSpaceDataSource;

    /**
     * @see CassandraSpaceDataSourceConfigurer#fixedPropertyValueSerializer(PropertyValueSerializer)
     */
    public void setFixedPropertyValueSerializer(
            PropertyValueSerializer fixedPropertyValueSerializer) {
        configurer.fixedPropertyValueSerializer(fixedPropertyValueSerializer);
    }

    /**
     * @see CassandraSpaceDataSourceConfigurer#dynamicPropertyValueSerializer(PropertyValueSerializer)
     */
    public void setDynamicPropertyValueSerializer(
            PropertyValueSerializer dynamicPropertyValueSerializer) {
        configurer.dynamicPropertyValueSerializer(dynamicPropertyValueSerializer);
    }

    /**
     * @see CassandraSpaceDataSourceConfigurer#cassandraDataSource(CassandraDataSource)
     */
    public void setCassandraDataSource(CassandraDataSource cassandraDataSource) {
        configurer.cassandraDataSource(cassandraDataSource);
    }

    /**
     * @see CassandraSpaceDataSourceConfigurer#cassandraDataSource(CassandraDataSource)
     */
    public void setHectorClient(HectorCassandraClient hectorClient) {
        configurer.hectorClient(hectorClient);
    }

    /**
     * @see CassandraSpaceDataSourceConfigurer#minimumNumberOfConnections(int)
     */
    public void setMinimumNumberOfConnections(int minimumNumberOfConnections) {
        configurer.minimumNumberOfConnections(minimumNumberOfConnections);
    }

    /**
     * @see CassandraSpaceDataSourceConfigurer#maximumNumberOfConnections(int)
     */
    public void setMaximumNumberOfConnections(int maximumNumberOfConnections) {
        configurer.maximumNumberOfConnections(maximumNumberOfConnections);
    }

    /**
     * @see CassandraSpaceDataSourceConfigurer#batchLimit(int)
     */
    public void setBatchLimit(int batchLimit) {
        configurer.batchLimit(batchLimit);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        cassandraSpaceDataSource = configurer.create();
    }

    @Override
    public CassandraSpaceDataSource getObject() throws Exception {
        return cassandraSpaceDataSource;
    }

    @Override
    public Class<?> getObjectType() {
        return CassandraSpaceDataSource.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() throws Exception {
        cassandraSpaceDataSource.close();
    }

}
