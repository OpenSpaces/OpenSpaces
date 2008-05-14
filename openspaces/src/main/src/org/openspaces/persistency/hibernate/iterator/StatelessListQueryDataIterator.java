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

package org.openspaces.persistency.hibernate.iterator;

import com.gigaspaces.datasource.DataIterator;
import com.j_spaces.core.client.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

import java.util.Iterator;

/**
 * A simple iterator that iterates over a {@link com.j_spaces.core.client.SQLQuery} by creating
 * an Hiberante query using Hibernate {@link org.hibernate.StatelessSession} and listing it.
 *
 * @author kimchy
 */
public class StatelessListQueryDataIterator implements DataIterator {

    protected final String entityName;

    protected final SQLQuery sqlQuery;

    protected final SessionFactory sessionFactory;

    protected Transaction transaction;

    protected StatelessSession session;

    private Iterator iterator;

    public StatelessListQueryDataIterator(SQLQuery sqlQuery, SessionFactory sessionFactory) {
        this.sqlQuery = sqlQuery;
        this.entityName = null;
        this.sessionFactory = sessionFactory;
    }

    public StatelessListQueryDataIterator(String entityName, SessionFactory sessionFactory) {
        this.sqlQuery = null;
        this.entityName = entityName;
        this.sessionFactory = sessionFactory;
    }

    public boolean hasNext() {
        if (iterator == null) {
            iterator = createIterator();
        }
        return iterator.hasNext();
    }

    public Object next() {
        return iterator.next();
    }

    public void remove() {
        throw new UnsupportedOperationException("remove not supported");
    }

    public void close() {
        if (transaction == null) {
            return;
        }
        try {
            transaction.commit();
        } finally {
            transaction = null;
            session.close();
        }
    }

    protected Iterator createIterator() {
        session = sessionFactory.openStatelessSession();
        if (entityName != null) {
            return session.createCriteria(entityName).list().iterator();
        } else if (sqlQuery != null) {
            return HibernateIteratorUtils.createQueryFromSQLQuery(sqlQuery, session).list().iterator();
        } else {
            throw new IllegalStateException("Either SQLQuery or entity must be provided");
        }
    }
}