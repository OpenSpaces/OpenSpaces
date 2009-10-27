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

/**
 * An event manager allowing to add and remove {@link SpaceInstanceAddedEventListener}s.
 *
 * @author kimchy
 * @see org.openspaces.admin.space.Space#getSpaceInstanceAdded()
 * @see org.openspaces.admin.space.Spaces#getSpaceInstanceAdded()
 */
public interface SpaceInstanceAddedEventManager {

    /**
     * Adds an event listener. Note, the event will be called for currently discovered
     * {@link org.openspaces.admin.space.SpaceInstance}s.
     */
    void add(SpaceInstanceAddedEventListener eventListener);

    /**
     * Adds an event listener allowing to control using the <code>includeExisting</code> if events
     * will be fired for existing space instances as well.
     */
    void add(SpaceInstanceAddedEventListener eventListener, boolean includeExisting);

    /**
     * Removes the event listener.
     */
    void remove(SpaceInstanceAddedEventListener eventListener);
}