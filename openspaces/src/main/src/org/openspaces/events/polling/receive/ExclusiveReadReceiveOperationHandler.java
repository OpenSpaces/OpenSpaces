package org.openspaces.events.polling.receive;

import com.j_spaces.core.client.ReadModifiers;
import org.openspaces.core.GigaSpace;
import org.springframework.dao.DataAccessException;

/**
 * Performs single read operation using {@link org.openspaces.core.GigaSpace#read(Object,long)}
 * under an exclusive read lock. This receive operation handler allows to lock entries so other
 * receive operations won't be able to obtain it (mimics the take operation) but without actually
 * perfroming a take from the Space.
 *
 * Note, this receive operation handler must be perfomed under a transaction.
 *
 * @author kimchy
 */
public class ExclusiveReadReceiveOperationHandler implements ReceiveOperationHandler {

    /**
     * Performs single read operation using {@link org.openspaces.core.GigaSpace#read(Object,long)}
     * under an exclusive read lock. This receive operation handler allows to lock entries so other
     * receive operations won't be able to obtain it (mimics the take operation) but without actually
     * perfroming a take from the Space.
     *
     * Note, this receive operation handler must be perfomed under a transaction.
     */
    public Object receive(Object template, GigaSpace gigaSpace, long receiveTimeout) throws DataAccessException {
        return gigaSpace.read(template, receiveTimeout, gigaSpace.getModifiersForIsolationLevel() | ReadModifiers.EXCLUSIVE_READ_LOCK);
    }
}