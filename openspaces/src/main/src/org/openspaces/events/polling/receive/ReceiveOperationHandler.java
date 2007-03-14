package org.openspaces.events.polling.receive;

import org.openspaces.core.GigaSpace;
import org.springframework.dao.DataAccessException;

/**
 * Perform the actual receive operations for
 * {@link org.openspaces.events.polling.AbstractPollingEventListenerContainer}. Can return either a
 * single object or an array of objects.
 * 
 * @author kimchy
 * @see org.openspaces.events.polling.AbstractPollingEventListenerContainer
 */
public interface ReceiveOperationHandler {

    /**
     * Performs the actual receive operation. Return values allowed are single object or an array of
     * objects.
     * 
     * @param template
     *            The template to use for the receive operation.
     * @param gigaSpace
     *            The GigaSpace interface to perform the receive operations with
     * @param receiveTimeout
     *            Receive timeout value
     * @return The receive result. <code>null</code> indicating no receive occured. Single object
     *         or an array of objects indicating the receive operation result.
     * @throws DataAccessException
     */
    Object receive(Object template, GigaSpace gigaSpace, long receiveTimeout) throws DataAccessException;
}
