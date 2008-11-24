package org.openspaces.admin;

import org.openspaces.admin.machine.MachineAware;
import org.openspaces.admin.transport.TransportAware;

/**
 * @author kimchy
 */
public interface GridComponent extends MachineAware, TransportAware {

    String getUID();
}
