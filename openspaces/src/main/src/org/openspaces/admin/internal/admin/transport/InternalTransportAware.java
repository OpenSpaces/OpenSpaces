package org.openspaces.admin.internal.admin.transport;

import org.openspaces.admin.Transport;
import org.openspaces.admin.TransportAware;

/**
 * @author kimchy
 */
public interface InternalTransportAware extends TransportAware {

    void setTransport(Transport transport);
}
