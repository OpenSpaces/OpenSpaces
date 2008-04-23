package org.openspaces.esb.mule.queue;

import org.mule.api.MuleException;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.transport.MessageDispatcher;
import org.mule.transport.AbstractMessageDispatcherFactory;

/**
 * @author kimchy
 */
public class OpenSpacesQueueMessageDispatcherFactory extends AbstractMessageDispatcherFactory {

    public MessageDispatcher create(OutboundEndpoint endpoint) throws MuleException {
        return new OpenSpacesQueueMessageDispatcher(endpoint);
    }
}
