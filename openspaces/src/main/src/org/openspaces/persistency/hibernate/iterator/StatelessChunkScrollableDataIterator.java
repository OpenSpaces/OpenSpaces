package org.openspaces.persistency.hibernate.iterator;

import com.gigaspaces.datasource.DataIterator;
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

    protected DataIterator createIteartor(String entityName, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int from, int size) {
        return new StatelessScrollableDataIterator(entityName, sessionFactory, fetchSize, performOrderById, from, size);
    }
}