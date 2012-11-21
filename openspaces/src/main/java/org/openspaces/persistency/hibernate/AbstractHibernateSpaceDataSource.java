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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.openspaces.persistency.hibernate.iterator.HibernateProxyRemoverIterator;
import org.openspaces.persistency.support.ConcurrentMultiDataIterator;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.SpaceDataSource;

/**
 * A base class for Hibernate based {@link SpaceDataSource} implementations.
 * @author eitany
 * @since 9.5
 */
public abstract class AbstractHibernateSpaceDataSource extends SpaceDataSource {

    protected static final Log logger = LogFactory.getLog(AbstractHibernateSpaceDataSource.class);
    
    private final int fetchSize;
    private final boolean performOrderById;
    private final String[] initialLoadEntries;
    private final int initialLoadThreadPoolSize;
    private final int initialLoadChunkSize;
    private final boolean useScrollableResultSet;
    private final ManagedEntitiesContainer sessionManager;
    private final SessionFactory sessionFactory;


    public AbstractHibernateSpaceDataSource(SessionFactory sessionFactory, Set<String> managedEntries, int fetchSize, boolean performOrderById, String[] initialLoadEntries, int initialLoadThreadPoolSize, int initialLoadChunkSize, boolean useScrollableResultSet) {
        this.sessionFactory = sessionFactory;
        this.fetchSize = fetchSize;
        this.performOrderById = performOrderById;
        this.initialLoadEntries = createInitialLoadEntries(initialLoadEntries, sessionFactory);
        this.initialLoadThreadPoolSize = initialLoadThreadPoolSize;
        this.initialLoadChunkSize = initialLoadChunkSize;
        this.useScrollableResultSet = useScrollableResultSet;
        this.sessionManager = new ManagedEntitiesContainer(sessionFactory, managedEntries);
    }

    
    private static String[] createInitialLoadEntries(String[] initialLoadEntries, SessionFactory sessionFactory) {
        if (initialLoadEntries == null) {
            Set<String> initialLoadEntriesSet = new HashSet<String>();
            // try and derive the managedEntries
            Map<String, ClassMetadata> allClassMetaData = sessionFactory.getAllClassMetadata();
            for (Map.Entry<String, ClassMetadata> entry : allClassMetaData.entrySet()) {
                String entityname = entry.getKey();
                ClassMetadata classMetadata = entry.getValue();
                if (classMetadata.isInherited()) {
                    String superClassEntityName = ((AbstractEntityPersister) classMetadata).getMappedSuperclass();
                    ClassMetadata superClassMetadata = allClassMetaData.get(superClassEntityName);
                    Class superClass = superClassMetadata.getMappedClass(EntityMode.POJO);
                    // only filter out classes that their super class has mappings
                    if (superClass != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Entity [" + entityname + "] is inherited and has a super class ["
                                    + superClass + "] filtering it out for intial load managedEntries");
                        }
                        continue;
                    }
                }
                initialLoadEntriesSet.add(entityname);
            }
            initialLoadEntries = initialLoadEntriesSet.toArray(new String[initialLoadEntriesSet.size()]);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Using Hibernate inital load managedEntries [" + Arrays.toString(initialLoadEntries) + "]");
        }
        return initialLoadEntries;
    }
    
    protected String[] getInitialLoadEntries() {
        return initialLoadEntries;
    }

    protected int getInitialLoadChunkSize() {
        return initialLoadChunkSize;
    }

    protected boolean isUseScrollableResultSet() {
        return useScrollableResultSet;
    }

    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    protected int getFetchSize() {
        return fetchSize;
    }

    protected boolean isPerformOrderById() {
        return performOrderById;
    }

    /**
     * A helper method that creates the initial load iterator using the {@link org.openspaces.persistency.support.ConcurrentMultiDataIterator}
     * with the provided {@link #setInitialLoadThreadPoolSize(int)} thread pool size.
     */
    protected DataIterator createInitialLoadIterator(DataIterator[] iterators) {
        return new HibernateProxyRemoverIterator(new ConcurrentMultiDataIterator(iterators, initialLoadThreadPoolSize));
    }
    
    /**
     * Returns if the given entity name is part of the {@link #getManagedEntries()} list.
     */
    protected boolean isManagedEntry(String entityName) {
        return sessionManager.isManagedEntry(entityName);
    }
    
    /* (non-Javadoc)
     * @see com.gigaspaces.datasource.SpaceDataSource#supportsInheritance()
     */
    @Override
    public boolean supportsInheritance() {
        return true;
    }


}