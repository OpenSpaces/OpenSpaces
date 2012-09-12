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

package org.openspaces.admin.pu.events;

/**
 * An event manager allowing to add and remove {@link org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener}s.
 *
 * @author kimchy
 * @see org.openspaces.admin.pu.ProcessingUnits#getProcessingUnitRemoved()
 */
public interface ProcessingUnitRemovedEventManager {

    /**
     * Adds an event listener.
     */
    void add(ProcessingUnitRemovedEventListener eventListener);

    /**
     * Removes an event listener.
     */
    void remove(ProcessingUnitRemovedEventListener eventListener);
}