package org.openspaces.admin.internal.transport;

import org.openspaces.admin.transport.Transport;
import org.openspaces.admin.transport.Transports;

/**
 * @author kimchy
 */
public interface InternalTransports extends Transports {

    void addTransport(Transport transport);

    void removeTransport(String uid);
}
