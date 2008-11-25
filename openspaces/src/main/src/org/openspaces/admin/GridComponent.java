package org.openspaces.admin;

import org.openspaces.admin.machine.MachineAware;
import org.openspaces.admin.os.OperatingSystemAware;
import org.openspaces.admin.transport.TransportAware;
import org.openspaces.admin.vm.VirtualMachineAware;

/**
 * @author kimchy
 */
public interface GridComponent extends MachineAware, TransportAware, OperatingSystemAware, VirtualMachineAware {

    String getUID();
}
