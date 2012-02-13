package org.openspaces.events.polling.receive;

import org.openspaces.core.GigaSpace;
import org.springframework.dao.DataAccessException;

import com.j_spaces.core.client.TakeModifiers;

/**
 * First tries and perform a {@link org.openspaces.core.GigaSpace#takeMultiple(Object,int)} using
 * the provided template and configured maxEntries (defaults to <code>50</code>). 
 * <p>If no values are returned, will perform a blocking take operation- 
 * {@link org.openspaces.core.GigaSpace#take(Object,long,int)}.
 * <p> Take operations are performed with {@link TakeModifiers#FIFO_GROUPS_POLL} 
 * <p>This handler uses the Fifo Groups capability and therefore should be used with a template that uses Fifo Groups 
 * <p>Note, this receive operation handler must be performed under a transaction. 
 *  
 * @author yael
 * @since 9.0
 */
public class FifoGroupsMultiTakeReceiveOperationHandler extends AbstractNonBlockingReceiveOperationHandler {
    private static final int DEFAULT_MAX_ENTRIES = 50;

    private int maxEntries = DEFAULT_MAX_ENTRIES;

    /**
     * Sets the max entries the initial take multiple operation will perform.
     */
    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }
    
    /**
     * First tries and perform a {@link org.openspaces.core.GigaSpace#takeMultiple(Object,int)} using
     * the provided template and configured maxEntries (defaults to <code>50</code>). 
     * <p>If no values are returned, will perform a blocking take operation- 
     * {@link org.openspaces.core.GigaSpace#take(Object,long,int)}.
     * <p> Take operations are performed with {@link TakeModifiers#FIFO_GROUPS_POLL} 
     */
    @Override
    protected Object doReceiveBlocking(Object template, GigaSpace gigaSpace, long receiveTimeout) {
        Object[] results = gigaSpace.takeMultiple(template, maxEntries, TakeModifiers.FIFO_GROUPS_POLL);
        if (results != null && results.length > 0) {
            return results;
        }
        return gigaSpace.take(template, receiveTimeout, TakeModifiers.FIFO_GROUPS_POLL);
    }

    /**
     * Performs a non blocking take operation- {@link org.openspaces.core.GigaSpace#takeMultiple(Object, int, int)} with {@link TakeModifiers#FIFO_GROUPS_POLL}.
     */
    @Override
    protected Object doReceiveNonBlocking(Object template, GigaSpace gigaSpace) throws DataAccessException {
        Object[] results = gigaSpace.takeMultiple(template, maxEntries,TakeModifiers.FIFO_GROUPS_POLL);
        if (results != null && results.length > 0) {
            return results;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Fifo Groups Multi Take, maxEntries[" + maxEntries + "], nonBlocking[" + nonBlocking + "], nonBlockingFactor[" + nonBlockingFactor + "]";
    }
}
