package org.openspaces.events.polling.receive;

import org.openspaces.core.GigaSpace;
import org.springframework.dao.DataAccessException;

import com.j_spaces.core.client.ReadModifiers;

/**
 * First tries and perform a {@link org.openspaces.core.GigaSpace#readMultiple(Object,int,int)}
 * using the provided template and configured maxEntries (defaults to <code>50</code>). 
 * <p>If no values are returned, will perform a blocking read operation-
 * {@link org.openspaces.core.GigaSpace#read(Object,long,int)}.
 * 
 * <p>Read operations are performed with {@link ReadModifiers#FIFO_GROUPS_POLL} and under an exclusive read lock which mimics the similar behavior 
 * as take without actually taking the entry from the space.
 * <p>This handler uses the Fifo Groups capability and therefore should be used with a template that uses Fifo Groups 
 * <p>Note, this receive operation handler must be performed under a transaction. 
 * 
 * @author yael
 *  @since 9.0
 */
public class FifoGroupsMultiExclusiveReadReceiveOperationHandler extends AbstractNonBlockingReceiveOperationHandler {
    private static final int DEFAULT_MAX_ENTRIES = 50;

    private int maxEntries = DEFAULT_MAX_ENTRIES;

    /**
     * Sets the max entries the initial take multiple operation will perform.
     */
    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }
    
    /**
     * First tries and perform a {@link org.openspaces.core.GigaSpace#readMultiple(Object,int,int)}
     * using the provided template and configured maxEntries (defaults to <code>50</code>). 
     * <p>If no values are returned, will perform a blocking read operation-
     * {@link org.openspaces.core.GigaSpace#read(Object,long,int)}.
     * 
     * <p>Read operations are performed with {@link ReadModifiers#FIFO_GROUPS_POLL} and under an exclusive read lock which mimics the similar behavior 
     * as take without actually taking the entry from the space.
     */
    @Override
    protected Object doReceiveBlocking(Object template, GigaSpace gigaSpace, long receiveTimeout)
            throws DataAccessException {
        Object[] results = gigaSpace.readMultiple(template, maxEntries, gigaSpace.getModifiersForIsolationLevel() | ReadModifiers.EXCLUSIVE_READ_LOCK | ReadModifiers.FIFO_GROUPS_POLL);
        if (results != null && results.length > 0) {
            return results;
        }
        return gigaSpace.read(template, receiveTimeout, gigaSpace.getModifiersForIsolationLevel() | ReadModifiers.EXCLUSIVE_READ_LOCK | ReadModifiers.FIFO_GROUPS_POLL);
    }

    /**
     * Perform a {@link org.openspaces.core.GigaSpace#readMultiple(Object,int,int)}
     * using the provided template and configured maxEntries (defaults to <code>50</code>).
     *
     * <p>Read operations are performed with {@link ReadModifiers#FIFO_GROUPS_POLL} and under an exclusive read lock which mimics the similar behavior 
     * as take without actually taking the entry from the space.
     */
    @Override
    protected Object doReceiveNonBlocking(Object template, GigaSpace gigaSpace) throws DataAccessException {
        Object[] results = gigaSpace.readMultiple(template, maxEntries, gigaSpace.getModifiersForIsolationLevel() | ReadModifiers.EXCLUSIVE_READ_LOCK | ReadModifiers.FIFO_GROUPS_POLL);
        if (results != null && results.length > 0) {
            return results;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Fifo Groups Multi Exclusive Read, maxEntries[" + maxEntries + "], nonBlocking[" + nonBlocking + "], nonBlockingFactor[" + nonBlockingFactor + "]";
    }
}
