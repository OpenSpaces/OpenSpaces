package org.openspaces.events.polling.receive;

import com.j_spaces.core.client.ReadModifiers;
import org.openspaces.core.GigaSpace;
import org.springframework.dao.DataAccessException;

/**
 * First tries and perform a {@link org.openspaces.core.GigaSpace#readMultiple(Object,int)} using
 * the provided template and configured maxEntries (defaults to <code>50</code>). If no values
 * are returned, will perform a blocking read operation using
 * {@link org.openspaces.core.GigaSpace#read(Object,long)}.
 *
 * <p>Read operations are performed under an exclusive read lock which mimics the similar behaviour
 * as take without actually taking the entry from the space.
 *
 * @author kimchy
 */
public class MultiExclusiveReadReceiveOperationHandler implements ReceiveOperationHandler {

    private static final int DEFAULT_MAX_ENTRIES = 50;

    private int maxEntries = DEFAULT_MAX_ENTRIES;

    /**
     * Sets the max entries the inital take multiple operation will perform.
     */
    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    /**
     * First tries and perform a {@link org.openspaces.core.GigaSpace#readMultiple(Object,int)}
     * using the provided template and configured maxEntries (defaults to <code>50</code>). If no
     * values are returned, will perform a blocking read operation using
     * {@link org.openspaces.core.GigaSpace#read(Object,long)}.
     *
     * <p>Read operations are performed under an exclusive read lock which mimics the similar behaviour
     * as take without actually taking the entry from the space.
     */
    public Object receive(Object template, GigaSpace gigaSpace, long receiveTimeout) throws DataAccessException {
        Object[] results = gigaSpace.readMultiple(template, maxEntries, gigaSpace.getModifiersForIsolationLevel() | ReadModifiers.EXCLUSIVE_READ_LOCK);
        if (results != null && results.length > 0) {
            return results;
        }
        return gigaSpace.read(template, receiveTimeout, gigaSpace.getModifiersForIsolationLevel() | ReadModifiers.EXCLUSIVE_READ_LOCK);
    }
}