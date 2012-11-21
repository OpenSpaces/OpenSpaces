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

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openspaces.persistency.hibernate.iterator.DefaultChunkListDataIterator;
import org.openspaces.persistency.hibernate.iterator.DefaultChunkScrollableDataIterator;
import org.openspaces.persistency.hibernate.iterator.DefaultListQueryDataIterator;
import org.openspaces.persistency.hibernate.iterator.DefaultScrollableDataIterator;
import org.openspaces.persistency.hibernate.iterator.HibernateProxyRemoverIterator;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataSourceQuery;
import com.gigaspaces.datasource.DataSourceSQLQuery;

/**
 * The default Hibernate space data source implementation. Based on Hibernate {@link Session}.
 * @author eitany
 * @since 9.5
 */
public class DefaultHibernateSpaceDataSource extends AbstractHibernateSpaceDataSource {

    public DefaultHibernateSpaceDataSource(SessionFactory sessionFactory, Set<String> managedEntries, int fetchSize,
            boolean performOrderById, String[] initialLoadEntries, int initialLoadThreadPoolSize,
            int initialLoadChunkSize, boolean useScrollableResultSet) {
        super(sessionFactory, managedEntries, fetchSize, performOrderById, initialLoadEntries, initialLoadThreadPoolSize, initialLoadChunkSize, useScrollableResultSet);
        
    }
    
    /**
     * Performs the initial load operation. Iterates over the {@link #setInitialLoadEntries(String[])} initial load
     * entries. If {@link #getInitialLoadChunkSize()} is set to <code>-1</code>, will use
     * {@link org.openspaces.persistency.hibernate.iterator.DefaultScrollableDataIterator} for each entity. If
     * {@link #getInitialLoadChunkSize()} is set to a non <code>-1</code> value, will use the
     * {@link org.openspaces.persistency.hibernate.iterator.DefaultChunkScrollableDataIterator}.
     */
    @Override
    public DataIterator<Object> initialDataLoad() {
        DataIterator[] iterators = new DataIterator[getInitialLoadEntries().length];
        int iteratorCounter = 0;
        for (String entityName : getInitialLoadEntries()) {
            if (getInitialLoadChunkSize() == -1) {
                if (isUseScrollableResultSet()) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Creating initial load scrollable iterator for entry [" + entityName + "]");
                    }
                    iterators[iteratorCounter++] = new DefaultScrollableDataIterator(entityName, getSessionFactory(), getFetchSize(), isPerformOrderById());
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Creating initial load list iterator for entry [" + entityName + "]");
                    }
                    iterators[iteratorCounter++] = new DefaultListQueryDataIterator(entityName, getSessionFactory());
                }
            } else {
                if (isUseScrollableResultSet()) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Creating initial load chunk scrollable iterator for entry [" + entityName + "]");
                    }
                    iterators[iteratorCounter++] = new DefaultChunkScrollableDataIterator(entityName, getSessionFactory(), getFetchSize(), isPerformOrderById(), getInitialLoadChunkSize());
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Creating initial load chunk list iterator for entry [" + entityName + "]");
                    }
                    iterators[iteratorCounter++] = new DefaultChunkListDataIterator(entityName, getSessionFactory(), getFetchSize(), isPerformOrderById(), getInitialLoadChunkSize());
                }
            }
        }
        return createInitialLoadIterator(iterators);
    }
    
    /**
     * Returns a {@link org.openspaces.persistency.hibernate.iterator.DefaultListQueryDataIterator} for the
     * given sql query.
     */
    @Override
    public DataIterator<Object> getDataIterator(DataSourceQuery query) {
        if (!query.supportsAsSQLQuery())
            return null;
        
        DataSourceSQLQuery<Object> sqlQuery = query.getAsSQLQuery();
        
        if (!isManagedEntry(query.getTypeDescriptor().getTypeName())) {
            if (logger.isTraceEnabled()) {
                logger.trace("Ignoring query (no mapping in hibernate) [" + sqlQuery + ']');
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Iterator over query [" + sqlQuery + ']');
        }
        return new HibernateProxyRemoverIterator(new DefaultListQueryDataIterator(sqlQuery, getSessionFactory()));
    }        

}
