package org.openspaces.persistency.hibernate.iterator;

import com.gigaspaces.datasource.DataIterator;
import com.j_spaces.core.client.SQLQuery;
import org.hibernate.SessionFactory;

/**
 * A stateless batch iterator that is based on {@link StatelessScrollableDataIterator}
 * for each chunk.
 *
 * @author kimchy
 */
public class StatelessChunkScrollableDataIterator extends AbstractChunkScrollableDataIterator {

    public StatelessChunkScrollableDataIterator(String entityName, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int batchSize) {
        super(entityName, sessionFactory, fetchSize, performOrderById, batchSize);
    }

    public StatelessChunkScrollableDataIterator(SQLQuery sqlQuery, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int batchSize) {
        super(sqlQuery, sessionFactory, fetchSize, performOrderById, batchSize);
    }

    protected DataIterator createScrollableIteartor(String entityName, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int from, int size) {
        return new StatelessScrollableDataIterator(entityName, sessionFactory, fetchSize, performOrderById, from, size);
    }

    protected DataIterator createScrollableIteartor(SQLQuery sqlQuery, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int from, int size) {
        return new StatelessScrollableDataIterator(sqlQuery, sessionFactory, fetchSize, performOrderById, from, size);
    }

    protected DataIterator createListIteartor(String entityName, SessionFactory sessionFactory) {
        return new StatelessListQueryDataIterator(entityName, sessionFactory);
    }

    protected DataIterator createListIteartor(SQLQuery sqlQuery, SessionFactory sessionFactory) {
        return new StatelessListQueryDataIterator(sqlQuery, sessionFactory);
    }
}