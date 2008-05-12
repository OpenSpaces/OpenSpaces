package org.openspaces.persistency.hibernate.iterator;

import com.gigaspaces.datasource.DataIterator;
import org.hibernate.SessionFactory;

/**
 * A default batch iterator that is based on {@link org.openspaces.persistency.hibernate.iterator.DefaultScrollableDataIterator}
 * for each chunk.
 *
 * @author kimchy
 */
public class DefaultChunkScrollableDataIterator extends AbstractChunkScrollableDataIterator {

    public DefaultChunkScrollableDataIterator(String entityName, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int batchSize) {
        super(entityName, sessionFactory, fetchSize, performOrderById, batchSize);
    }

    protected DataIterator createIteartor(String entityName, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int from, int size) {
        return new DefaultScrollableDataIterator(entityName, sessionFactory, fetchSize, performOrderById, from, size);
    }
}
