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

import com.j_spaces.core.client.SQLQuery;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.metadata.ClassMetadata;

/**
 * A stateless scrollable result based on Hibernate {@link StatelessSession}.
 *
 * @author kimchy
 */
public class StatelessScrollableDataIterator extends AbstractScrollableDataIterator {

    protected StatelessSession session;

    protected Transaction transaction;

    public StatelessScrollableDataIterator(String entityName, SessionFactory sessionFactory, int fetchSize, boolean performOrderById) {
        super(entityName, sessionFactory, fetchSize, performOrderById);
    }

    public StatelessScrollableDataIterator(String entityName, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int from, int size) {
        super(entityName, sessionFactory, fetchSize, performOrderById, from, size);
    }

    public StatelessScrollableDataIterator(SQLQuery sqlQuery, SessionFactory sessionFactory, int fetchSize, boolean performOrderById) {
        super(sqlQuery, sessionFactory, fetchSize, performOrderById);
    }

    public StatelessScrollableDataIterator(SQLQuery sqlQuery, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int from, int size) {
        super(sqlQuery, sessionFactory, fetchSize, performOrderById, from, size);
    }

    protected void doClose() {
        try {
            transaction.commit();
        } finally {
            session.close();
        }
    }

    protected void clear() {

    }

    protected ScrollableResults createCursor() {
        session = sessionFactory.openStatelessSession();
        transaction = session.beginTransaction();
        if (entityName != null) {
            Criteria criteria = session.createCriteria(entityName);
            criteria.setFetchSize(fetchSize);
            if (perfromOrderById) {
                ClassMetadata metadata = sessionFactory.getClassMetadata(entityName);
                String idPropName = metadata.getIdentifierPropertyName();
                if (idPropName != null) {
                    criteria.addOrder(Order.asc(idPropName));
                }
            }
            if (from > 0) {
                criteria.setFirstResult(from);
                criteria.setMaxResults(size);
            }
            return criteria.scroll(ScrollMode.FORWARD_ONLY);
        } else if (sqlQuery != null) {
            Query query = HibernateIteratorUtils.createQueryFromSQLQuery(sqlQuery, session);
            query.setFetchSize(fetchSize);
            if (from > 0) {
                query.setFirstResult(from);
                query.setMaxResults(size);
            }
            return query.scroll(ScrollMode.FORWARD_ONLY);
        } else {
            throw new IllegalStateException("Either SQLQuery or entity must be provided");
        }
    }
}