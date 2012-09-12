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

import org.openspaces.admin.space.SpaceInstance;

/**
 * An event listener allowing to listen for {@link org.openspaces.admin.space.SpaceInstance} removals.
 *
 * @author kimchy
 * @see org.openspaces.admin.space.Space#getSpaceInstanceRemoved()
 * @see org.openspaces.admin.space.Spaces#getSpaceInstanceRemoved()
 */
public interface SpaceInstanceRemovedEventListener {

    /**
     * A callback indicating that a Space Instance was removed.
     */
    void spaceInstanceRemoved(SpaceInstance spaceInstance);
}