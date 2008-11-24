package org.openspaces.admin.internal.admin.transport;

import org.openspaces.admin.Transport;

/**
 * @author kimchy
 */
public interface InternalTransport extends Transport {

    void addTransportInfoProvider(InternalTransportInfoProvider transportInfoProvider);

    void removeTransportInfoProvider(InternalTransportInfoProvider transportInfoProvider);

    boolean hasTransportInfoProviders();
}
