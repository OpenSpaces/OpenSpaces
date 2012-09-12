/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.admin.space.events;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import org.openspaces.admin.space.SpaceInstance;

/**
 * An event indicating that a {@link org.openspaces.admin.space.SpaceInstance} mode was changed (for example,
 * form primary to backup).
 *
 * @author kimchy
 * @see org.openspaces.admin.space.SpaceInstance#getSpaceModeChanged()
 * @see org.openspaces.admin.space.Space#getSpaceModeChanged() 
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

    /**
     * Returns the Space Instance that space mode changed for.
     */
    public SpaceInstance getSpaceInstance() {
        return spaceInstance;
    }

    /**
     * Returns the previous space mode.
     */
    public SpaceMode getPreviousMode() {
        return previousSpaceMode;
    }

    /**
     * Returns the new space mode.
     */
    public SpaceMode getNewMode() {
        return newSpaceMode;
    }
}
