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

package org.openspaces.admin.vm.events;

/**
 * An event manager allowing to add and remove {@link VirtualMachineStatisticsChangedEventListener}.
 *
 * @author kimchy
 */
public interface VirtualMachineStatisticsChangedEventManager {

    /**
     * Adds an event listener.
     */
    void add(VirtualMachineStatisticsChangedEventListener eventListener);

    /**
     * Adds an event listener, will also fire events for history statistics.
     */
    void add(VirtualMachineStatisticsChangedEventListener eventListener, boolean withHistory);

    /**
     * Removes an event listener.
     */
    void remove(VirtualMachineStatisticsChangedEventListener eventListener);
}