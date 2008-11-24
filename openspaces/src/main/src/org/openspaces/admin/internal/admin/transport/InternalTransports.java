package org.openspaces.admin.internal.admin.transport;

import org.openspaces.admin.Transport;
import org.openspaces.admin.Transports;

/**
 * @author kimchy
 */
public interface InternalTransports extends Transports {

    void addTransport(Transport transport);

    void removeTransport(String uid);
}
