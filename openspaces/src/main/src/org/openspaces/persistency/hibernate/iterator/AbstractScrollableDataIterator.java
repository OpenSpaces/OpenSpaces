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
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;

/**
 * A base class for scrollable result set ({@link ScrollableResults} created based on either an
 * entity name or a <code>SQLQuery</code>. Also allows for a "from" and "size" to be provided, in this case, it will iterate
 * from the given index till "size" results.
 *
 * @author kimchy
 */
public abstract class AbstractScrollableDataIterator implements DataIterator {

    protected final String entityName;

    protected final SQLQuery sqlQuery;

    protected final SessionFactory sessionFactory;

    protected final boolean perfromOrderById;

    protected final int fetchSize;

    protected final int from;

    protected final int size;

    private ScrollableResults cursor;

    private int clearCounter;

    private int globalCounter;

    public AbstractScrollableDataIterator(String entityName, SessionFactory sessionFactory, int fetchSize, boolean performOrderById) {
        this(entityName, sessionFactory, fetchSize, performOrderById, -1, -1);
    }

    public AbstractScrollableDataIterator(String entityName, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int from, int size) {
        this.entityName = entityName;
        this.sqlQuery = null;
        this.sessionFactory = sessionFactory;
        this.fetchSize = fetchSize;
        this.perfromOrderById = performOrderById;
        this.from = from;
        this.size = size;
    }

    public AbstractScrollableDataIterator(SQLQuery sqlQuery, SessionFactory sessionFactory, int fetchSize, boolean performOrderById) {
        this(sqlQuery, sessionFactory, fetchSize, performOrderById, -1, -1);
    }

    public AbstractScrollableDataIterator(SQLQuery sqlQuery, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int from, int size) {
        this.sqlQuery = sqlQuery;
        this.entityName = null;
        this.sessionFactory = sessionFactory;
        this.fetchSize = fetchSize;
        this.perfromOrderById = performOrderById;
        this.from = from;
        this.size = size;
    }

    public boolean hasNext() {
        if (cursor == null) {
            cursor = createCursor();
        }
        boolean hasNext = cursor.next();
        if (hasNext) {
            if (size != -1 && ++globalCounter > size) {
                return false;
            }
        }
        return hasNext;
    }

    public Object next() {
        if (clearCounter++ > fetchSize) {
            clearCounter = 0;
            clear();
        }
        return cursor.get(0);
    }

    public void remove() {
        throw new UnsupportedOperationException("remove is not supported");
    }

    public void close() {
        if (cursor == null) {
            return;
        }
        try {
            cursor.close();
        } finally {
            doClose();
        }
    }

    protected abstract void doClose();

    protected abstract void clear();

    protected abstract ScrollableResults createCursor();
}
