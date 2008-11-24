package org.openspaces.admin.internal.support;

import org.openspaces.admin.internal.machine.InternalMachineAware;
import org.openspaces.admin.internal.transport.InternalTransportAware;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.transport.Transport;

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
