package org.openspaces.esb.mule.queue;

import org.mule.providers.AbstractMessageDispatcherFactory;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.provider.UMOMessageDispatcher;

/**
 * @author kimchy
 */
public class OpenSpacesQueueMessageDispatcherFactory extends AbstractMessageDispatcherFactory {

    public UMOMessageDispatcher create(UMOImmutableEndpoint endpoint) throws UMOException {
        return new OpenSpacesQueueMessageDispatcher(endpoint);
    }
}
