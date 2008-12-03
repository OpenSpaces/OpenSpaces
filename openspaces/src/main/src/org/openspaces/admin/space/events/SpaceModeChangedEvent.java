package org.openspaces.admin.space.events;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import org.openspaces.admin.space.SpaceInstance;

/**
 * @author kimchy
 */
public class SpaceModeChangedEvent {

    private final SpaceInstance spaceInstance;

    private final SpaceMode previousSpaceMode;

    private final SpaceMode newSpaceMode;

    public SpaceModeChangedEvent(SpaceInstance spaceInstance, SpaceMode previousSpaceMode, SpaceMode newSpaceMode) {
        this.spaceInstance = spaceInstance;
        this.previousSpaceMode = previousSpaceMode;
        this.newSpaceMode = newSpaceMode;
    }

    public SpaceInstance getSpaceInstance() {
        return spaceInstance;
    }

    public SpaceMode getPreviousMode() {
        return previousSpaceMode;
    }

    public SpaceMode getNewMode() {
        return newSpaceMode;
    }
}
