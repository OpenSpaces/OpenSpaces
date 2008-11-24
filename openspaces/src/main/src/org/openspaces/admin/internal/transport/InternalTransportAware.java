package org.openspaces.admin.internal.transport;

import org.openspaces.admin.transport.Transport;
import org.openspaces.admin.transport.TransportAware;

/**
 * @author kimchy
 */
public interface InternalTransportAware extends TransportAware {

    void setTransport(Transport transport);
}
