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
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.openspaces.persistency.support.ConcurrentMultiDataIterator;
import org.openspaces.persistency.support.MultiDataIterator;

import java.util.ArrayList;

/**
 * A base class that accespts a batch size and will create several iterators on the given
 * entity by chunking it into batch size chuncks, each iterator will iterate only on the given
 * chunk.
 *
 * @author kimchy
 */
public abstract class AbstractChunkScrollableDataIterator implements MultiDataIterator {

    protected final String entityName;

    protected final SessionFactory sessionFactory;

    protected final boolean perfromOrderById;

    protected final int fetchSize;

    protected final int batchSize;

    private DataIterator[] iterators;

    private MultiDataIterator multiDataIterator;

    public AbstractChunkScrollableDataIterator(String entityName, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int batchSize) {
        this.entityName = entityName;
        this.sessionFactory = sessionFactory;
        this.fetchSize = fetchSize;
        this.perfromOrderById = performOrderById;
        this.batchSize = batchSize;
    }

    public DataIterator[] iterators() {
        initIterators();
        return this.iterators;
    }

    public boolean hasNext() {
        initIterators();
        if (multiDataIterator == null) {
            multiDataIterator = new ConcurrentMultiDataIterator(iterators, 10);
        }
        return multiDataIterator.hasNext();
    }

    public Object next() {
        return multiDataIterator.next();
    }

    public void remove() {
        multiDataIterator.remove();
    }

    public void close() {
        if (multiDataIterator != null) {
            try {
                multiDataIterator.close();
            } finally {
                multiDataIterator = null;
            }
        }
    }

    private void initIterators() {
        if (iterators == null) {
            ArrayList<DataIterator> itList = new ArrayList<DataIterator>();
            Session session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();
            try {
                Criteria criteria = session.createCriteria(entityName);
                criteria.setProjection(Projections.rowCount());
                int count = ((Number) criteria.uniqueResult()).intValue();
                int from = 0;
                while (from < count) {
                    itList.add(createIteartor(entityName, sessionFactory, fetchSize, perfromOrderById, from, batchSize));
                    from += batchSize;
                }
                iterators = itList.toArray(new DataIterator[itList.size()]);
            } finally {
                transaction.commit();
                session.close();
            }
        }
    }

    protected abstract DataIterator createIteartor(String entityName, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int from, int size);
}
