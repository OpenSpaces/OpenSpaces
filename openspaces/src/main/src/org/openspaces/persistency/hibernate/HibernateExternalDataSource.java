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

import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.datasource.ExternalDataSource;
import com.j_spaces.core.IGSEntry;
import com.j_spaces.core.client.SQLQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.persistency.BulkDataPersisterExceptionFilter;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * HibernateExternalDataSource is a full Hibernate implementation of {@link com.gigaspaces.datasource.ExternalDataSource}.
 * HibernateExternalDataSource is responsible for both propagating changes and delegating read operation
 * made on the space to an external database.
 * It delegates these changes into external database via Hibernate 3.0 ORM.
 *
 * @author Uri Cohen
 * @see com.gigaspaces.datasource.ExternalDataSource
 */
public class HibernateExternalDataSource implements ExternalDataSource<Object>, ClusterInfoAware {

    protected final Log log = LogFactory.getLog(getClass());

    private static final String DEFAULT_INITIAL_LOAD_QUERY = "from java.lang.Object";

    private SessionFactory sessionFactory;
    private Integer initialLoadBatchSize;
    protected String initialLoadQuery = DEFAULT_INITIAL_LOAD_QUERY;
    protected ClusterInfo clusterInfo;
    protected BulkDataPersisterExceptionFilter exceptionFilter;

    /**
     * Returns the query used for initial loading of the data (the one invoked from {@link #initialLoad})
     * By default it returns the value of {@link #DEFAULT_INITIAL_LOAD_QUERY}. Users can override it by
     * inject a new value via {@link #setInitialLoadQuery(String)}.
     * This method should be implemented by subclasses to override the default behaviour.
     * Note that when used inside the service grid, a {@link org.openspaces.core.cluster.ClusterInfo} object
     * will be injected into this instance and can be used by subclasses to determine the instance id
     * and to filter out irrelevat data using the query itself
     *
     * @return the initial load query. Defaults to {@link #DEFAULT_INITIAL_LOAD_QUERY}
     */
    protected String getInitialLoadQuery() {
        return initialLoadQuery;
    }

    public void setInitialLoadQuery(String initialLoadQuery) {
        this.initialLoadQuery = initialLoadQuery;
    }


    /**
     * @see org.openspaces.core.cluster.ClusterInfoAware#setClusterInfo(org.openspaces.core.cluster.ClusterInfo)
     */
    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    /**
     * Sets the {@link org.openspaces.persistency.BulkDataPersisterExceptionFilter} to be used
     * when executing {@link #executeBulk(java.util.List)} 
     * @param exceptionFilter the filter so set. If non set no exception will be filtered
     */
    public void setExceptionFilter(BulkDataPersisterExceptionFilter exceptionFilter) {
        this.exceptionFilter = exceptionFilter;
    }

    /**
     * Returns the cluster info if such was set. When deployed in a pu, the cluster information object
     * will be injected into this instace and provide cluster information.
     * This can be used by subclasses
     *
     * @return
     */
    protected ClusterInfo getClusterInfo() {
        return clusterInfo;
    }

    /**
     * Injects a Hibernate session factory into the data source to be used
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Sets the initial load batch size. This will be used when retreving data from the database.
     * The retreival is done in pages whose size is determined on the value of field.
     * Default value is determined by {@link org.openspaces.persistency.hibernate.HibernateDataIterator#DEFAULT_LOAD_BATCH_SIZE}
     * @param initialLoadBatchSize the batch size
     */
    public void setInitialLoadBatchSize(Integer initialLoadBatchSize) {
        this.initialLoadBatchSize = initialLoadBatchSize;
    }

    /**
     * Closes the Hibernate session factory
     *
     * @see com.gigaspaces.datasource.ManagedDataSource#shutdown()
     */
    public void shutdown() throws DataSourceException {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }

    /**
     * @see com.gigaspaces.datasource.DataProvider#read(Object)
     */
    public Object read(final Object template) throws DataSourceException {
        log.debug("method entry : read(Object template)");
        return doInHibernateTransaction(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.get(template.getClass(), getId(template));
            }
        });
    }

    private void closeSession(Session session) throws DataSourceException {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (HibernateException e) {
            log.warn("Could not close Hibernate session", e);
        }
    }

    /**
     * @see com.gigaspaces.datasource.DataPersister#remove(Object)
     */
    public void remove(final Object object) throws DataSourceException {
        log.debug("method entry : remove(Object object)");
        doInHibernateTransaction(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Object id = getId(object);
                if (id != null) {
                    session.delete(object);
                }
                return null;
            }
        });
    }

    /**
     * @see com.gigaspaces.datasource.DataPersister#update(Object)
     */
    public void update(final Object object) throws DataSourceException {
        log.debug("method entry : update(Object object)");
        doInHibernateTransaction(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Object id = getId(object);
                if (id != null) {
                    session.update(object);
                }
                return null;
            }
        });
    }

    /**
     * @see com.gigaspaces.datasource.DataPersister#write(Object)
     */
    public void write(final Object object) throws DataSourceException {
        log.debug("method entry : write(Object object)");
        doInHibernateTransaction(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Object id = getId(object);
                if (id != null) {
                    session.save(object);
                }
                return null;
            }
        });
    }

    /**
     * @see com.gigaspaces.datasource.DataPersister#writeBatch(java.util.List)
     */
    public void writeBatch(final List<Object> objects) throws DataSourceException {
        log.debug("method entry : writeBatch(List<Object> objects)");
        doInHibernateTransaction(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.setFlushMode(FlushMode.MANUAL);
                for (Object object : objects) {
                    Object id = getId(object);
                    if (id != null) {
                        // merge is used here since we are not sure if the same object will appear more than once
                        session.merge(object);
                    }
                }
                session.flush();
                return null;
            }
        });
    }

    /**
     * @see com.gigaspaces.datasource.DataPersister#removeBatch(java.util.List)
     */
    public void removeBatch(final List<Object> objects) throws DataSourceException {
        log.debug("method entry : removeBatch(List<Object> objects)");
        doInHibernateTransaction(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.setFlushMode(FlushMode.MANUAL);
                for (Object object : objects) {
                    Object id = getId(object);
                    if (id != null) {
                        session.delete(object);
                    }

                }
                session.flush();
                return null;
            }
        });
    }

    /**
     * @see com.gigaspaces.datasource.DataPersister#updateBatch(java.util.List)
     */
    public void updateBatch(final List<Object> objects) throws DataSourceException {
        log.debug("method entry : updateBatch(List<Object> objects)");
        writeBatch(objects);
    }

    /**
     * @see com.gigaspaces.datasource.BulkDataPersister#executeBulk(java.util.List)
     */
    public void executeBulk(final List<BulkItem> bulk) throws DataSourceException {
        log.debug("method entry : executeBulk(List<BulkItem> bulk)");
        try {
            doInHibernateTransaction(new HibernateCallback() {
                public Object doInHibernate(Session session) throws HibernateException, SQLException {
                    session.setFlushMode(FlushMode.MANUAL);
                    Iterator<BulkItem> iter = bulk.iterator();
                    while (iter.hasNext()) {
                        BulkItem bulkItem = iter.next();
                        Object entry = bulkItem.getItem();
                        Object id = getId(entry);
                        if (id == null) {
                            continue;
                        }
                        switch (bulkItem.getOperation()) {
                            case BulkItem.REMOVE:
                                session.delete(entry);
                                break;
                            case BulkItem.WRITE:
                            case BulkItem.UPDATE:
                                session.merge(entry);
                                break;
                            default:
                                break;
                        }
                    }
                    session.flush();
                    return null;
                }
            });
        } catch (DataSourceException e) {
            if (!shouldFilterException(e)) {
                throw e;
            }
        }
    }

    private boolean shouldFilterException(DataSourceException e) {
        if (exceptionFilter != null) {
            return exceptionFilter.shouldFilter(e);            
        }
        return false;
    }

    /**
     * @see com.gigaspaces.datasource.DataProvider#count(Object)
     */
    public int count(final Object template) throws DataSourceException {
        log.debug("method entry : count(Object template)");
        return (Integer) doInHibernateTransaction(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria;
                if (template == null) {
                    criteria = session.createCriteria(Object.class);
                } else {
                    Example example = Example.create(template);
                    criteria = session.createCriteria(template.getClass()).add(example);

                    // Handle null templates
                    Object idValue = getId(template);
                    if (idValue != null) {
                        ClassMetadata classMetaData = getMetadata(template);
                        String idPropName = classMetaData.getIdentifierPropertyName();
                        criteria.add(Restrictions.eq(idPropName, idValue));
                    }
                }
                criteria.setProjection(Projections.rowCount());
                //no caching
                criteria.setCacheMode(CacheMode.IGNORE);
                criteria.setCacheable(false);
                return criteria.uniqueResult();
            }
        });
    }

    /**
     * @see com.gigaspaces.datasource.DataProvider#iterator(java.lang.Object)
     */
    public DataIterator<Object> iterator(final Object template) throws DataSourceException {
        log.debug("method entry : iterator(Object template)");
        // Handle null templates
        if (template == null) {
            return initialLoad();
        }
        return (DataIterator<Object>) doInHibernateTransactionNoCommit(new TxAwareHibernateCallback() {
            public Object doInHibernate(Session session, Transaction transaction) throws HibernateException, SQLException {
                Example example = Example.create(template);
                Criteria query = session.createCriteria(template.getClass()).add(example);
                Object idValue = getId(template);
                if (idValue != null) {
                    ClassMetadata classMetaData = getMetadata(template);
                    String idPropName = classMetaData.getIdentifierPropertyName();
                    query.add(Restrictions.eq(idPropName, idValue));
                }
                query.setCacheMode(CacheMode.IGNORE);
                query.setCacheable(false);
                if (initialLoadBatchSize == null) {
                    initialLoadBatchSize = HibernateDataIterator.DEFAULT_LOAD_BATCH_SIZE;
                }
                HibernateDataIterator<Object> hibernateDataIterator = new HibernateDataIterator<Object>(query, session, transaction, initialLoadBatchSize);
                return hibernateDataIterator;
            }
        });
    }

    /**
     * @see com.gigaspaces.datasource.SQLDataProvider#count(com.j_spaces.core.client.SQLQuery)
     */
    public int count(final SQLQuery sqlQuery) throws DataSourceException {
        log.debug("method entry : count(SQLQuery<Object> sqlQuery)");
        return (Integer) doInHibernateTransaction(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query hQuery = session.createQuery(sqlQuery.getSelectCountQuery());
                Object[] preparedValues = sqlQuery.getParameters();
                if (preparedValues != null) {
                    for (int i = 0; i < preparedValues.length; i++) {
                        hQuery.setParameter(i, preparedValues[i]);
                    }
                }
                hQuery.setCacheMode(CacheMode.IGNORE);
                hQuery.setCacheable(false);
                return ((Number) hQuery.uniqueResult()).intValue();
            }
        });
    }

    /**
     * @see com.gigaspaces.datasource.SQLDataProvider#iterator(com.j_spaces.core.client.SQLQuery)
     */

    public DataIterator<Object> iterator(final SQLQuery<Object> query) throws DataSourceException {
        log.debug("method entry : iterator(SQLQuery<Object> query)");
        return (DataIterator<Object>) doInHibernateTransactionNoCommit(new TxAwareHibernateCallback() {
            public Object doInHibernate(Session session, Transaction transaction) throws HibernateException, SQLException {
                String select = query.getFromQuery();
                Query hQuery = session.createQuery(select);
                Object[] preparedValues = query.getParameters();
                if (preparedValues != null) {
                    for (int i = 0; i < preparedValues.length; i++) {
                        hQuery.setParameter(i, preparedValues[i]);
                    }
                }
                hQuery.setCacheMode(CacheMode.IGNORE);
                hQuery.setCacheable(false);
                hQuery.setReadOnly(true);
                if (initialLoadBatchSize == null) {
                    initialLoadBatchSize = HibernateDataIterator.DEFAULT_LOAD_BATCH_SIZE;
                }
                HibernateDataIterator<Object> hibernateDataIterator = new HibernateDataIterator<Object>(hQuery, session, transaction, initialLoadBatchSize);
                return hibernateDataIterator;
            }
        });
    }

    /**
     * @see com.gigaspaces.datasource.ManagedDataSource#initialLoad()
     */
    public DataIterator<Object> initialLoad() throws DataSourceException {
        log.debug("method entry : initialLoad()");
        return (DataIterator<Object>) doInHibernateTransactionNoCommit(new TxAwareHibernateCallback() {
            public Object doInHibernate(Session session, Transaction transaction) throws HibernateException, SQLException {
                session.setFlushMode(FlushMode.MANUAL);
                Query query = session.createQuery(getInitialLoadQuery());
                query.setCacheMode(CacheMode.IGNORE);
                query.setCacheable(false);
                if (initialLoadBatchSize == null) {
                    initialLoadBatchSize = HibernateDataIterator.DEFAULT_LOAD_BATCH_SIZE;
                }
                HibernateDataIterator<Object> hibernateDataIterator = new HibernateDataIterator<Object>(query, session, transaction, initialLoadBatchSize);
                return hibernateDataIterator;
            }
        });
    }

    /**
     * @see com.gigaspaces.datasource.ManagedDataSource#init(java.util.Properties)
     */
    public void init(Properties properties) throws DataSourceException {
        // only configure a session factory if it is not injected
        if (sessionFactory == null) {
            throw new DataSourceException("Hibernate session factory not set");
        }
    }

    //  Utility methods //
    /**
     * Returns the pojo identifier
     */

    protected Object doInHibernateTransaction(HibernateCallback hibernateCallback) throws DataSourceException {
        Session session = null;
        Transaction tx = null;

        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();
            Object result = hibernateCallback.doInHibernate(session);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new DataSourceException("Exception caught upon hibernate operation", e);
        }
        finally {
            closeSession(session);
        }
    }

    protected Object doInHibernateTransactionNoCommit(TxAwareHibernateCallback hibernateCallback) throws DataSourceException {
        Session session = null;
        Transaction tx = null;

        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();
            return hibernateCallback.doInHibernate(session, tx);
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            closeSession(session);
            throw new DataSourceException("Exception caught upon hibernate operation", e);
        }
    }

    protected Serializable getId(Object template) {
        Serializable id;
        ClassMetadata classMetaData = getMetadata(template);

        if (classMetaData == null) {//Unexpected class entity
            return null;
        }

        if (template instanceof IGSEntry) {
            id = (Serializable) ((IGSEntry) template).getFieldValue(classMetaData.getIdentifierPropertyName());
        } else {
            id = classMetaData.getIdentifier(template, EntityMode.POJO);
        }
        return id;
    }

    /**
     * Return pojo metadata
     *
     * @param pojo the pojo
     * @return returns the ClassMetadata associated with the given entity name;
     *         may return null if entity is not mapped.
     */
    protected ClassMetadata getMetadata(Object pojo) {
        String pojoName;
        if (pojo instanceof IGSEntry) {
            pojoName = ((IGSEntry) pojo).getClassName();
        } else {
            pojoName = pojo.getClass().getName();
        }
        ClassMetadata entryClassMetadata = sessionFactory.getClassMetadata(pojoName);
        if (entryClassMetadata == null) {
            log.info("Unknown entity type: " + pojoName);
        }
        return entryClassMetadata;
    }

    interface TxAwareHibernateCallback {
        Object doInHibernate(Session session, Transaction transaction) throws HibernateException, SQLException;
    }

    interface HibernateCallback {
        Object doInHibernate(Session session) throws HibernateException, SQLException;
    }

}
