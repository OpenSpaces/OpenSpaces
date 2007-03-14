package org.openspaces.events.polling.receive;

import org.openspaces.core.GigaSpace;
import org.springframework.dao.DataAccessException;

/**
 * Performs single take operation using {@link org.openspaces.core.GigaSpace#take(Object,long)}.
 * 
 * @author kimchy
 */
public class SingleTakeReceiveOperationHandler implements ReceiveOperationHandler {

    /**
     * Performs single take operation using {@link org.openspaces.core.GigaSpace#take(Object,long)}.
     */
    public Object receive(Object template, GigaSpace gigaSpace, long receiveTimeout) throws DataAccessException {
        return gigaSpace.take(template, receiveTimeout);
    }
}
