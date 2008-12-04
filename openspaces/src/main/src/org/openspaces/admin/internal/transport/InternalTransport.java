package org.openspaces.admin.internal.transport;

import org.openspaces.admin.transport.Transport;
import org.openspaces.admin.vm.VirtualMachine;

/**
 * @author kimchy
 */
public interface InternalTransport extends Transport {

    void addTransportInfoProvider(InternalTransportInfoProvider transportInfoProvider);

    void removeTransportInfoProvider(InternalTransportInfoProvider transportInfoProvider);

    boolean hasTransportInfoProviders();

    void setVirtualMachine(VirtualMachine virtualMachine);
}
