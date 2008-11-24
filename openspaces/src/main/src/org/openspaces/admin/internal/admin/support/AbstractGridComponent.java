package org.openspaces.admin.internal.admin.support;

import org.openspaces.admin.Machine;
import org.openspaces.admin.Transport;
import org.openspaces.admin.internal.admin.machine.InternalMachineAware;
import org.openspaces.admin.internal.admin.transport.InternalTransportAware;

/**
 * @author kimchy
 */
public abstract class AbstractGridComponent implements InternalMachineAware, InternalTransportAware {

    private volatile Machine machine;

    private volatile Transport transport;

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public Machine getMachine() {
        return this.machine;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public Transport getTransport() {
        return this.transport;
    }
}
