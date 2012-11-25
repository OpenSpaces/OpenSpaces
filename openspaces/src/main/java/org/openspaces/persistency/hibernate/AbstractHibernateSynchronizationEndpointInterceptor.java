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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.metadata.ClassMetadata;
import org.openspaces.persistency.patterns.ManagedEntriesSynchronizationEndpointInterceptor;

import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.sync.DataSyncOperation;

/**
 * A base class for Hibernate based synchronization endpoint interceptor implementations.
 * @author eitany
 * @since 9.5
 */
public abstract class AbstractHibernateSynchronizationEndpointInterceptor extends ManagedEntriesSynchronizationEndpointInterceptor {

    protected static final Log logger = LogFactory.getLog(AbstractHibernateSynchronizationEndpointInterceptor.class);
    private final ManagedEntitiesContainer sessionManager;
    private final SessionFactory sessionFactory;

    public AbstractHibernateSynchronizationEndpointInterceptor(SessionFactory sessionFactory, Set<String> managedEntries) {
        this.sessionFactory = sessionFactory;
        this.sessionManager = new ManagedEntitiesContainer(sessionFactory, managedEntries);
    }

    /**
     * @param dataSyncOperation
     * @return
     */
    protected boolean isManaged(DataSyncOperation dataSyncOperation) {
        if (!dataSyncOperation.supportsGetTypeDescriptor())
            return false;
        
        String typeName = dataSyncOperation.getTypeDescriptor().getTypeName();
        if (!sessionManager.isManagedEntry(typeName)){
            if (logger.isTraceEnabled()) {
                logger.trace("Entry [" + typeName + ":" + dataSyncOperation+ "] is not managed, filtering it out");
            }
            return false;
        }
        return true;
    
    }

    /**
     * Filter from the input map the unmapped field of this entity
     * 
     * @param entityName 
     * @param itemValues map of properties to filter
     * 
     */
    protected Map<String, Object> filterItemValue(String entityName, Map<String, Object> itemValues) {
        ClassMetadata classMetadata = getSessionFactory().getClassMetadata(entityName);
        String[] propertyNames = classMetadata.getPropertyNames();
        List<String> names = Arrays.asList(propertyNames);
        Iterator<String> iterator = itemValues.keySet().iterator();
        while(iterator.hasNext()){
            if(!names.contains(iterator.next()))
                iterator.remove();
        }
        return itemValues;
    }

    protected String getPartialUpdateHQL(DataSyncOperation dataSyncOperation, Map<String, Object> updatedValues) {
    
        final StringBuilder updateQueryBuilder = new StringBuilder("update ");
        SpaceTypeDescriptor typeDescriptor = dataSyncOperation.getTypeDescriptor();
        updateQueryBuilder.append(typeDescriptor.getTypeName()).append(" set ");
    
        int i  = 0;
        for (Map.Entry<String, Object> updateEntry : updatedValues.entrySet()) {
            updateQueryBuilder.append(updateEntry.getKey()).append("=:").append(updateEntry.getKey());
            if(i < updatedValues.size()-1)
                updateQueryBuilder.append(',');
            i++;
        }
    
        updateQueryBuilder.append( " where ").append(typeDescriptor.getIdPropertyName())
        .append("=:id_"). append(typeDescriptor.getIdPropertyName());
        String hql = updateQueryBuilder.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("Partial Update HQL [" + hql + ']');
        }
        return hql;
    }

    protected void rollbackTx(Transaction tr) {
        try {
            tr.rollback();
        } catch (Exception e) {
            // ignore this exception
        }
    }
    
    /**
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    /* (non-Javadoc)
     * @see org.openspaces.persistency.patterns.ManagedEntriesSynchronizationEndpointInterceptor#getManagedEntries()
     */
    @Override
    public Iterable<String> getManagedEntries() {
        return sessionManager.getManagedEntries();
    }


}