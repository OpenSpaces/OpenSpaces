package org.openspaces.persistency.hibernate.iterator;

import com.gigaspaces.datasource.DataIterator;
import com.j_spaces.core.client.SQLQuery;
import org.hibernate.SessionFactory;

/**
 * A default batch iterator that is based on {@link org.openspaces.persistency.hibernate.iterator.DefaultScrollableDataIterator}
 * for each chunk.
 *
 * @author kimchy
 */
public class DefaultChunkScrollableDataIterator extends AbstractChunkDataIterator {

    /**
     * Constructs a scrollable iterator over the given entity name.
     *
     * @param entityName       The entity name to scroll over
     * @param sessionFactory   The session factory to use to construct the session
     * @param fetchSize        The fetch size of the scrollabale result set
     * @param performOrderById Should the query perform order by id or not
     * @param chunkSize        The size of the chunks the entity table will be broken to
     */
    public DefaultChunkScrollableDataIterator(String entityName, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int chunkSize) {
        super(entityName, sessionFactory, fetchSize, performOrderById, chunkSize);
    }

    /**
     * Constructs a scrollable iterator over the given GigaSpaces <code>SQLQuery</code>.
     *
     * @param sqlQuery         The <code>SQLQuery</code> to scroll over
     * @param sessionFactory   The session factory to use to construct the session
     * @param fetchSize        The fetch size of the scrollabale result set
     * @param performOrderById Should the query perform order by id or not
     * @param chunkSize        The size of the chunks the entity table will be broken to
     */
    public DefaultChunkScrollableDataIterator(SQLQuery sqlQuery, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int chunkSize) {
        super(sqlQuery, sessionFactory, fetchSize, performOrderById, chunkSize);
    }

    /**
     * Constructs a scrollable iterator over the given hibernate query string.
     *
     * @param hQuery         The hiberante query string to scroll over
     * @param sessionFactory The session factory to use to construct the session
     * @param fetchSize      The fetch size of the scrollabale result set
     * @param chunkSize      The size of the chunks the entity table will be broken to
     */
    public DefaultChunkScrollableDataIterator(String hQuery, SessionFactory sessionFactory, int fetchSize, int chunkSize) {
        super(hQuery, sessionFactory, fetchSize, chunkSize);
    }

    protected DataIterator createIteratorByEntityName(String entityName, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int from, int size) {
        return new DefaultScrollableDataIterator(entityName, sessionFactory, fetchSize, performOrderById, from, size);
    }

    protected DataIterator createIteratorBySQLQuery(SQLQuery sqlQuery, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int from, int size) {
        return new DefaultScrollableDataIterator(sqlQuery, sessionFactory, fetchSize, performOrderById, from, size);
    }

    protected DataIterator createIteratorByHibernateQuery(String hQuery, SessionFactory sessionFactory, int fetchSize, int from, int size) {
        return new DefaultScrollableDataIterator(hQuery, sessionFactory, fetchSize, from, size);
    }
}
