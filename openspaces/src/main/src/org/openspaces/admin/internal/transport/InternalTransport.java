package org.openspaces.admin.internal.transport;

import org.openspaces.admin.transport.Transport;

/**
 * @author kimchy
 */
public interface InternalTransport extends Transport {

    void addTransportInfoProvider(InternalTransportInfoProvider transportInfoProvider);

    void removeTransportInfoProvider(InternalTransportInfoProvider transportInfoProvider);

    boolean hasTransportInfoProviders();
}
