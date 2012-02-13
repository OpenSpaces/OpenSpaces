package org.openspaces.events.polling.receive;

import org.openspaces.core.GigaSpace;
import org.springframework.dao.DataAccessException;

import com.j_spaces.core.client.ReadModifiers;

/**
     * Performs single read operation using {@link org.openspaces.core.GigaSpace#read(Object,long,int)}
     * under an exclusive read lock and {@link ReadModifiers#FIFO_GROUPS_POLL}. 
     * <p>This receive operation handler allows to lock entries so other
     * receive operations won't be able to obtain it (mimics the take operation) but without actually
     * performing a take from the Space.
     * <p>This handler uses the Fifo Groups capability and therefore should be used with a template that uses Fifo Groups 
     * <p>Note, this receive operation handler must be performed under a transaction.
 *
 * @author yael
 * @since 9.0
 */
public class FifoGroupsExclusiveReadReceiveOperationHandler extends AbstractNonBlockingReceiveOperationHandler {

    /**
     * Performs single read operation using {@link org.openspaces.core.GigaSpace#read(Object,long,int)}
     * with {@code receiveTimeout} , under an exclusive read lock and {@link ReadModifiers#FIFO_GROUPS_POLL}. 
     * <p>This receive operation handler allows to lock entries so other
     * receive operations won't be able to obtain it (mimics the take operation) but without actually
     * performing a take from the Space.
     * <p>Note, this receive operation handler must be performed under a transaction.
     */
    @Override
    protected Object doReceiveBlocking(Object template, GigaSpace gigaSpace, long receiveTimeout) throws DataAccessException {
        return gigaSpace.read(template, receiveTimeout, gigaSpace.getModifiersForIsolationLevel() | ReadModifiers.EXCLUSIVE_READ_LOCK | ReadModifiers.FIFO_GROUPS_POLL);
    }

    /**
     * Performs single read operation using {@link org.openspaces.core.GigaSpace#read(Object,long,int)}
     * with no timeout, under an exclusive read lock and {@link ReadModifiers#FIFO_GROUPS_POLL}. 
     * <p>This receive operation handler allows to lock entries so other
     * receive operations won't be able to obtain it (mimics the take operation) but without actually
     * performing a take from the Space.
     * <p>Note, this receive operation handler must be performed under a transaction.
     */
    @Override
    protected Object doReceiveNonBlocking(Object template, GigaSpace gigaSpace) throws DataAccessException {
        return gigaSpace.read(template, 0, gigaSpace.getModifiersForIsolationLevel() | ReadModifiers.EXCLUSIVE_READ_LOCK | ReadModifiers.FIFO_GROUPS_POLL);
    }

    @Override
    public String toString() {
        return "Fifo Groups Single Exclusive Read, nonBlocking[" + nonBlocking + "], nonBlockingFactor[" + nonBlockingFactor + "]";
    }

}
