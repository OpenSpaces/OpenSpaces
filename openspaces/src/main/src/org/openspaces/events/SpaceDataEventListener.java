package org.openspaces.events;

import org.openspaces.core.GigaSpace;
import org.springframework.transaction.TransactionStatus;

/**
 * A Space data event listener interface allowing for reception of events triggered by different
 * container types. Note, changing the container typs should be just a matter of configuration, with
 * the event handling code remaining the same. For simplified, Pojo like, event listeners see the
 * adapter package.
 * 
 * @author kimchy
 * @see org.openspaces.events.adapter.MethodEventListenerAdapter
 * @see org.openspaces.events.adapter.AnnotationEventListenerAdapter
 */
public interface SpaceDataEventListener {

    /**
     * An event callback with the actual data object of the event.
     * 
     * @param data
     *            The actual data object of the event
     * @param gigaSpace
     *            A GigaSpace instance that can be used to perofrm additional operations against the
     *            space
     * @param txStatus
     *            An optional transaction status allowing to rollback a transaction programmatically
     * @param source
     *            Optional additional data or the actual source event data object (where relevant)
     */
    void onEvent(Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source);
}
