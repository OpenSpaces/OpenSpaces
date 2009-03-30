package org.openspaces.admin.internal.zone;

import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.zone.Zone;

/**
 * @author kimchy
 */
public interface InternalZone extends Zone {

    void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance);

    void removeProcessingUnitInstance(String uid);

    void addSpaceInstance(SpaceInstance spaceInstance);

    void removeSpaceInstance(String uid);
}
