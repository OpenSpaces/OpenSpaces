package org.openspaces.events.polling.receive;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceException;

/**
 * Performs single take operation using {@link org.openspaces.core.GigaSpace#read(Object,long)}.
 *
 * @author kimchy
 */
public class SingleReadReceiveOperationHandler implements ReceiveOperationHandler {

    /**
     * Performs single take operation using {@link org.openspaces.core.GigaSpace#read(Object,long)}.
     */
    public Object receive(Object template, GigaSpace gigaSpace, long receiveTimeout) throws GigaSpaceException {
        return gigaSpace.read(template, receiveTimeout);
    }
}
