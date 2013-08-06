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
 * An event manager allowing to add and remove {@link org.openspaces.admin.space.events.SpaceAddedEventListener}s.
 *
 * <p>A {@link org.openspaces.admin.space.Space} is added when its first {@link org.openspaces.admin.space.SpaceInstance} is
 * discovered. It is removed when there are no {@link org.openspaces.admin.space.SpaceInstance}s.
 *
 * @author kimchy
 * @see org.openspaces.admin.space.Spaces#getSpaceAdded()
 * @see org.openspaces.admin.space.Spaces#addLifecycleListener(SpaceLifecycleEventListener)
 */
public interface SpaceAddedEventManager {

    /**
     * Adds an event listener. Note, the event will also be called for all currently discovered
     * {@link org.openspaces.admin.space.Space}s.
     */
    void add(SpaceAddedEventListener eventListener);
    
    /**
     * Adds an event listener allowing to control using the <code>includeExisting</code> if events
     * will be fired for existing spaces as well.
     */
    void add(SpaceAddedEventListener eventListener, boolean includeExisting);    

    /**
     * Removes an event listener.
     */
    void remove(SpaceAddedEventListener eventListener);
}