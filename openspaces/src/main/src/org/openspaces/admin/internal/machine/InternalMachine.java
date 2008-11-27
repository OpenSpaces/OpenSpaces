package org.openspaces.admin.internal.machine;

import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.space.SpaceInstance;

/**
 * @author kimchy
 */
public interface InternalMachine extends Machine {

    void setOperatingSystem(OperatingSystem operatingSystem);

    void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance);

    void removeProcessingUnitInstance(String uid);

    void addSpaceInstance(SpaceInstance spaceInstance);

    void removeSpaceInstance(String uid);
}
