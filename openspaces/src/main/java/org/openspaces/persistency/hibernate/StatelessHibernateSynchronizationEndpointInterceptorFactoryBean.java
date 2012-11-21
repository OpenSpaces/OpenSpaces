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
package org.openspaces.persistency.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.gigaspaces.sync.SynchronizationEndpointInterceptor;

/**
 * A factory bean which creates {@link SynchronizationEndpointInterceptor}.
 * @author eitany
 * @since 9.5
 */
public class StatelessHibernateSynchronizationEndpointInterceptorFactoryBean implements
        FactoryBean<StatelessHibernateSynchronizationEndpointInterceptor>, InitializingBean {

    private final StatelessHibernateSynchronizationEndpointInterceptorConfigurer synchronizationEndpointInterceptorConfigurer = new StatelessHibernateSynchronizationEndpointInterceptorConfigurer();
    private StatelessHibernateSynchronizationEndpointInterceptor synchronizationEndpointInterceptor;
    /**
     * Injects the Hibernate SessionFactory to be used with this synchronization endpoint interceptor.
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        synchronizationEndpointInterceptorConfigurer.sessionFactory(sessionFactory);
    }

    /**
     * Sets all the entries this Hibernate synchronization endpoint interceptor will work with. By default, will use Hibernate meta
     * data API in order to get the list of all the given entities it handles.
     *
     * <p>This list is used to filter out entities when performing all synchronization endpoint interceptor operations.
     *
     * <p>Usually, there is no need to explicitly set this.
     */
    public void setManagedEntries(String... entries) {
        synchronizationEndpointInterceptorConfigurer.managedEntries(entries);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        synchronizationEndpointInterceptor = synchronizationEndpointInterceptorConfigurer.create();
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    @Override
    public StatelessHibernateSynchronizationEndpointInterceptor getObject() throws Exception {
        return synchronizationEndpointInterceptor;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    @Override
    public Class<?> getObjectType() {
        return StatelessHibernateSynchronizationEndpointInterceptor.class;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

}
