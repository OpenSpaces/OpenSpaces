package org.openspaces.admin.internal.machine;

import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.os.OperatingSystem;

/**
 * @author kimchy
 */
public interface InternalMachine extends Machine {

    void setOperatingSystem(OperatingSystem operatingSystem);
}
