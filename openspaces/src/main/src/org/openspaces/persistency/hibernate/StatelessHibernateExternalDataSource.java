/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.persistency.hibernate;

import com.gigaspaces.datasource.BulkDataPersister;
import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.datasource.SQLDataProvider;
import com.j_spaces.core.client.SQLQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.openspaces.persistency.hibernate.iterator.StatelessChunkScrollableDataIterator;
import org.openspaces.persistency.hibernate.iterator.StatelessListQueryDataIterator;
import org.openspaces.persistency.hibernate.iterator.StatelessScrollableDataIterator;

import java.util.List;

/**
 * An external data source implemenation based on Hiberante {@link org.hibernate.StatelessSession}.
 *
 * <p>Note, stateless session is much faster than regular Hibernate session, but at the expense of not having
 * a first level cache, as well as not performing any cascading operations (both in read operations as well as
 * dirty operations).
 *
 * @author kimchy
 */
public class StatelessHibernateExternalDataSource extends AbstractHibernateExternalDataSource implements BulkDataPersister, SQLDataProvider {

    private Log batchingLogger = LogFactory.getLog("org.hibernate.jdbc.BatchingBatcher");

    /**
     * Perform the given bulk changes using Hibernate {@link org.hibernate.StatelessSession}. First, tries to perform
     * "optimistic" operations without checking in advance for existance of certain entity. If this fails, will
     * try and perform the same operations again, simply with checking if the entry exists or not.
     */
    public void executeBulk(List<BulkItem> bulkItems) throws DataSourceException {
        StatelessSession session = getSessionFactory().openStatelessSession();
        Transaction tr = session.beginTransaction();
        Exception batchModeException = null;
        try {
            for (BulkItem bulkItem : bulkItems) {
                Object entry = bulkItem.getItem();
                if (!isManagedEntry(entry.getClass().getName())) {
                    continue;
                }
                switch (bulkItem.getOperation()) {
                    case BulkItem.REMOVE:
                        session.delete(entry);
                        break;
                    case BulkItem.WRITE:
                        session.insert(entry);
                        break;
                    case BulkItem.UPDATE:
                        session.update(entry);
                        break;
                    default:
                        break;
                }
            }
            tr.commit();
        } catch (Exception e) {
            rollbackTx(tr);
            batchModeException = new DataSourceException("Failed to execute bulk operation in batch mode", e);
        } finally {
            closeSession(session);
        }
        if (batchModeException == null) {
            // all is well, return
            return;
        } else {
            batchingLogger.error("Ignoring Hibernate StaleStateException, trying with exists batching");
        }

        // if something went wrong, do it with exists checks

        Object latest = null;
        session = getSessionFactory().openStatelessSession();
        tr = session.beginTransaction();
        try {
            for (BulkItem bulkItem : bulkItems) {
                Object entry = bulkItem.getItem();
                if (!isManagedEntry(entry.getClass().getName())) {
                    continue;
                }
                latest = entry;
                switch (bulkItem.getOperation()) {
                    case BulkItem.REMOVE:
                        if (exists(entry, session)) {
                            session.delete(entry);
                        }
                        break;
                    case BulkItem.WRITE:
                    case BulkItem.UPDATE:
                        if (exists(entry, session)) {
                            session.update(entry);
                        } else {
                            session.insert(entry);
                        }
                        break;
                    default:
                        break;
                }
            }
            tr.commit();
        } catch (Exception e) {
            rollbackTx(tr);
            throw new DataSourceException("Failed to execute bulk operation, latest object [" + latest + "]", e);
        } finally {
            closeSession(session);
        }
    }

    /**
     * Returns a {@link org.openspaces.persistency.hibernate.iterator.StatelessListQueryDataIterator} for the given
     * query.
     */
    public DataIterator iterator(SQLQuery sqlQuery) throws DataSourceException {
        return new StatelessListQueryDataIterator(sqlQuery, getSessionFactory());
    }

    /**
     * Performs the inital load operation. Iterates over the {@link #setInitialLoadEntries(String[])} inital load
     * entries. If {@link #getInitalLoadChunkSize()} is set to <code>-1</code>, will use
     * {@link org.openspaces.persistency.hibernate.iterator.StatelessScrollableDataIterator} for each entity. If
     * {@link # getInitalLoadChunkSize ()} is set to a non <code>-1</code> value, will use the
     * {@link org.openspaces.persistency.hibernate.iterator.StatelessChunkScrollableDataIterator}.
     */
    public DataIterator initialLoad() throws DataSourceException {
        DataIterator[] iterators = new DataIterator[getInitialLoadEntries().length];
        int iteratorCounter = 0;
        for (String entityName : getInitialLoadEntries()) {
            if (getInitalLoadChunkSize() == -1) {
                iterators[iteratorCounter++] = new StatelessScrollableDataIterator(entityName, getSessionFactory(), getFetchSize(), isPerformOrderById());
            } else {
                iterators[iteratorCounter++] = new StatelessChunkScrollableDataIterator(entityName, getSessionFactory(), getFetchSize(), isPerformOrderById(), getInitalLoadChunkSize());
            }
        }
        return createInitialLoadIterator(iterators);
    }

    protected boolean exists(Object entry, StatelessSession session) {
        Criteria criteria = session.createCriteria(entry.getClass().getName());
        ClassMetadata classMetaData = getSessionFactory().getClassMetadata(entry.getClass());
        criteria.add(Restrictions.idEq(classMetaData.getIdentifier(entry, EntityMode.POJO)));
        criteria.setProjection(Projections.rowCount());
        return ((Number) criteria.uniqueResult()).intValue() > 0;
    }

    private void rollbackTx(Transaction tr) {
        try {
            tr.rollback();
        } catch (Exception e) {
            // ignore this exception
        }
    }

    private void closeSession(StatelessSession session) {
        session.close();
    }
}