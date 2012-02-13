package org.openspaces.events.polling.receive;

import org.openspaces.core.GigaSpace;
import org.springframework.dao.DataAccessException;

import com.j_spaces.core.client.TakeModifiers;

/**
 * Performs single take operation using {@link org.openspaces.core.GigaSpace#take(Object, long, int)} 
 * with {@link TakeModifiers#FIFO_GROUPS_POLL} modifier.
 * <p>This handler uses the Fifo Groups capability and therefore should be used with a template that uses Fifo Groups 
 * <p>Note, this receive operation handler must be performed under a transaction. 
 * 
 *  @author yael
 *  @since 9.0
 */
public class FifoGroupsSingleTakeReceiveOperationHandler extends AbstractNonBlockingReceiveOperationHandler {


    /**
     * Performs a single take operation using {@link org.openspaces.core.GigaSpace#take(Object, long)} 
     * with {@link TakeModifiers#FIFO_GROUPS_POLL} modifier and the given timeout
     */
    @Override
    protected Object doReceiveBlocking(Object template, GigaSpace gigaSpace, long receiveTimeout)
            throws DataAccessException {
        return gigaSpace.take(template,receiveTimeout,TakeModifiers.FIFO_GROUPS_POLL);
    }

    /**
     * Performs a single take operation using {@link org.openspaces.core.GigaSpace#take(Object, long)} with
     * {@link TakeModifiers#FIFO_GROUPS_POLL} modifier and no timeout.
     */
    @Override
    protected Object doReceiveNonBlocking(Object template, GigaSpace gigaSpace) throws DataAccessException {
        return gigaSpace.take(template,0,TakeModifiers.FIFO_GROUPS_POLL);
    }
    
    @Override
    public String toString() {
        return "Fifo Groups Single Take, nonBlocking[" + nonBlocking + "], nonBlockingFactor[" + nonBlockingFactor + "]";
    }

}
