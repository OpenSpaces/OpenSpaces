package org.openspaces.admin;

import org.openspaces.admin.machine.MachineAware;
import org.openspaces.admin.os.OperatingSystemAware;
import org.openspaces.admin.transport.TransportAware;

/**
 * @author kimchy
 */
public interface GridComponent extends MachineAware, TransportAware, OperatingSystemAware {

    String getUID();
}
