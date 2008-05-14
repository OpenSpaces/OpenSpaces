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
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.Iterator;

/**
 * A simple iterator that iterates over a {@link com.j_spaces.core.client.SQLQuery} by creating
 * an Hiberante query using Hibernate {@link Session} and listing it.
 *
 * @author kimchy
 */
public class DefaultListQueryDataIterator implements DataIterator {

    protected final SQLQuery sqlQuery;

    protected final SessionFactory sessionFactory;

    protected Transaction transaction;

    protected Session session;

    private Iterator iterator;

    public DefaultListQueryDataIterator(SQLQuery sqlQuery, SessionFactory sessionFactory) {
        this.sqlQuery = sqlQuery;
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
        session = sessionFactory.openSession();
        Query query = HibernateIteratorUtils.createQueryFromSQLQuery(sqlQuery, session);
        return createIterator(query);
    }

    protected Iterator createIterator(Query query) {
        return query.list().iterator();
    }
}
