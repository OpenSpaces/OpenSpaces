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

import com.gigaspaces.datasource.DataIterator;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.proxy.HibernateProxy;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implements the {@link com.gigaspaces.datasource.DataIterator} interface by using
 * Hibernate constructs (Query or criteria).
 * Results are fetched in bulks, and bulk size can be determined by the method {@link #setLoadBatchSize(int)} .
 *
 * @author Uri Cohen
 */
class HibernateDataIterator<T> implements DataIterator<T> {
    /**
     * The default fetch size of every batch of objects from the database
     */
    static final int DEFAULT_LOAD_BATCH_SIZE = 10000;

    protected final Session session;
    protected final Query query;
    protected final Criteria criteria;
    protected final Transaction tx;
    private int currentPosition = 0;
    private Iterator<T> currentIterator = null;
    private T next = null;
    private boolean isExhausted = false;
    int loadBatchSize = DEFAULT_LOAD_BATCH_SIZE;

    public HibernateDataIterator(Query query, Session session, Transaction tx) {
        this.session = session;
        this.tx = tx;
        this.query = query;
        this.criteria = null;
        assignNextElement();
    }

    public HibernateDataIterator(Criteria criteria, Session session, Transaction tx) {
        this.session = session;
        this.tx = tx;
        this.criteria = criteria;
        this.query = null;
        assignNextElement();
    }

    /**
     * Determines the value of the load batch size. Defaults to {@link #DEFAULT_LOAD_BATCH_SIZE}
     *
     * @param batchSize the batch size
     * @see #DEFAULT_LOAD_BATCH_SIZE
     */
    public void setLoadBatchSize(int batchSize) {
        this.loadBatchSize = batchSize;
    }

    public boolean hasNext() {
        return (next != null);
    }

    private void assignNextElement() {
        if (isExhausted) {
            return;
        }
        if (currentIterator == null || !currentIterator.hasNext()) {
            if (query != null) {
                query.setFirstResult(currentPosition);
                query.setMaxResults(loadBatchSize);
                currentIterator = query.list().iterator();

            } else { // criteria != null
                criteria.setFirstResult(currentPosition);
                criteria.setMaxResults(loadBatchSize);
                currentIterator = criteria.list().iterator();
            }
            if (!currentIterator.hasNext()) {
                next = null;
                isExhausted = true;
                return;
            }
        }
        next = currentIterator.next();
        currentPosition++;
    }

    /*
      * @see java.util.Iterator#next()
      */
    public T next() {
        if (next == null) {
            throw new NoSuchElementException("Iterator is exhausted");
        }
        if (next instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy) next;
            next = (T) proxy.getHibernateLazyInitializer().getImplementation();
        }
        assignNextElement();
        return next;
    }

    /*
      * @see java.util.Iterator#remove()
      */
    public void remove() {
        Transaction tx = session.beginTransaction();
        try {
            tx.begin();
            currentIterator.remove();
            tx.commit();
        }
        catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException("Can't remove entity from iterator", e);
        }

    }

    /*
      * @see com.j_spaces.javax.cache.CacheIterator#close()
      */
    public void close() {
        if (session.isOpen()) {
            if (tx != null) {
                tx.commit();
            }
            session.close();
        }
    }
}
