package org.openspaces.admin.internal.support;

import org.openspaces.admin.internal.machine.InternalMachineAware;
import org.openspaces.admin.internal.os.InternalOperatingSystemAware;
import org.openspaces.admin.internal.transport.InternalTransportAware;
import org.openspaces.admin.internal.vm.InternalVirtualMachineAware;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.transport.Transport;
import org.openspaces.admin.vm.VirtualMachine;

/**
 * @author kimchy
 */
public abstract class AbstractGridComponent implements InternalMachineAware, InternalTransportAware, InternalOperatingSystemAware, InternalVirtualMachineAware {

    private volatile Machine machine;

    private volatile Transport transport;

    private volatile OperatingSystem operatingSystem;

    private volatile VirtualMachine virtualMachine;

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

    public void setOperatingSystem(OperatingSystem operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public OperatingSystem getOperatingSystem() {
        return this.operatingSystem;
    }

    public void setVirtualMachine(VirtualMachine virtualMachine) {
        this.virtualMachine = virtualMachine;
    }

    public VirtualMachine getVirtualMachine() {
        return this.virtualMachine;
    }
}
